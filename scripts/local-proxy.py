#!/usr/bin/env python3
"""Local forwarding proxy that injects Basic auth for an upstream proxy.

Gradle's JVM HTTP client (Apache HttpClient) negotiates NTLM instead of
Basic auth when proxy credentials are supplied via system properties.
This script runs a local unauthenticated proxy on 127.0.0.1 that forwards
all requests to the upstream proxy with a correct Proxy-Authorization
header, bypassing the NTLM issue entirely.

Usage:
    python3 scripts/local-proxy.py [local_port]

The upstream proxy URL is read from the http_proxy / HTTP_PROXY env var.
Default local port is 18080.

Then point Gradle at the local proxy (no auth needed):
    ./gradlew build \
      -Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=18080 \
      -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=18080 \
      -Dhttp.nonProxyHosts="localhost|127.0.0.1"
"""

import base64
import os
import select
import socket
import sys
import threading
from http.server import BaseHTTPRequestHandler, HTTPServer
from urllib.parse import urlparse

BUFFER_SIZE = 65536


def parse_proxy_url(url):
    """Parse proxy URL into (host, port, user, password)."""
    parsed = urlparse(url)
    return (
        parsed.hostname,
        parsed.port or 3128,
        parsed.username or "",
        parsed.password or "",
    )


class ProxyHandler(BaseHTTPRequestHandler):
    upstream_host = None
    upstream_port = None
    proxy_auth_header = None

    def do_CONNECT(self):
        """Handle HTTPS CONNECT tunneling through the upstream proxy."""
        upstream = socket.create_connection((self.upstream_host, self.upstream_port))
        try:
            connect_req = "CONNECT {} HTTP/1.1\r\nHost: {}\r\n".format(
                self.path, self.path
            )
            if self.proxy_auth_header:
                connect_req += "Proxy-Authorization: Basic {}\r\n".format(
                    self.proxy_auth_header
                )
            connect_req += "\r\n"
            upstream.sendall(connect_req.encode())

            # Read the upstream proxy's response to CONNECT
            response = b""
            while b"\r\n\r\n" not in response:
                chunk = upstream.recv(BUFFER_SIZE)
                if not chunk:
                    break
                response += chunk

            status_line = response.split(b"\r\n")[0]
            if b"200" in status_line:
                self.send_response(200, "Connection Established")
                self.end_headers()
                self._tunnel(self.connection, upstream)
            else:
                self.send_error(
                    502,
                    "Upstream proxy rejected CONNECT: {}".format(
                        status_line.decode(errors="replace")
                    ),
                )
        finally:
            upstream.close()

    def do_GET(self):
        self._forward_request()

    def do_POST(self):
        self._forward_request()

    def do_PUT(self):
        self._forward_request()

    def do_DELETE(self):
        self._forward_request()

    def do_HEAD(self):
        self._forward_request()

    def _forward_request(self):
        """Forward an HTTP request through the upstream proxy with auth."""
        upstream = socket.create_connection((self.upstream_host, self.upstream_port))
        try:
            # Rebuild the request with absolute URL (as required by proxies)
            req = "{} {} {}\r\n".format(
                self.command, self.path, self.request_version
            )
            for key, val in self.headers.items():
                req += "{}: {}\r\n".format(key, val)
            if self.proxy_auth_header:
                req += "Proxy-Authorization: Basic {}\r\n".format(
                    self.proxy_auth_header
                )
            req += "\r\n"
            upstream.sendall(req.encode())

            # Forward request body if present
            content_length = int(self.headers.get("Content-Length", 0))
            if content_length > 0:
                body = self.rfile.read(content_length)
                upstream.sendall(body)

            # Stream the upstream response back to the client
            while True:
                chunk = upstream.recv(BUFFER_SIZE)
                if not chunk:
                    break
                self.wfile.write(chunk)
        finally:
            upstream.close()

    def _tunnel(self, client_sock, upstream_sock):
        """Bidirectional data tunnel between client and upstream."""
        sockets = [client_sock, upstream_sock]
        timeout = 60
        while True:
            readable, _, errors = select.select(sockets, [], sockets, timeout)
            if errors or not readable:
                break
            for sock in readable:
                other = upstream_sock if sock is client_sock else client_sock
                try:
                    data = sock.recv(BUFFER_SIZE)
                    if not data:
                        return
                    other.sendall(data)
                except (ConnectionResetError, BrokenPipeError, OSError):
                    return

    def log_message(self, format, *args):
        """Suppress per-request logging to keep output clean."""
        pass


class ThreadedHTTPServer(HTTPServer):
    """Handle each request in a new thread for concurrent connections."""

    def process_request(self, request, client_address):
        thread = threading.Thread(target=self._handle, args=(request, client_address))
        thread.daemon = True
        thread.start()

    def _handle(self, request, client_address):
        try:
            self.finish_request(request, client_address)
        except Exception:
            self.handle_error(request, client_address)
        finally:
            self.shutdown_request(request)


def main():
    local_port = int(sys.argv[1]) if len(sys.argv) > 1 else 18080

    proxy_url = os.environ.get("http_proxy") or os.environ.get("HTTP_PROXY")
    if not proxy_url:
        print(
            "Error: No http_proxy or HTTP_PROXY environment variable set",
            file=sys.stderr,
        )
        sys.exit(1)

    host, port, user, password = parse_proxy_url(proxy_url)

    ProxyHandler.upstream_host = host
    ProxyHandler.upstream_port = port
    if user:
        creds = "{}:{}".format(user, password)
        ProxyHandler.proxy_auth_header = base64.b64encode(creds.encode()).decode()

    server = ThreadedHTTPServer(("127.0.0.1", local_port), ProxyHandler)
    print("Local proxy listening on 127.0.0.1:{}".format(local_port), flush=True)
    print("Forwarding to {}:{}".format(host, port), flush=True)

    try:
        server.serve_forever()
    except KeyboardInterrupt:
        server.shutdown()


if __name__ == "__main__":
    main()
