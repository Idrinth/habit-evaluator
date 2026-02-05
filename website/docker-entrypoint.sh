#!/bin/sh
set -e

# Generate runtime configuration from environment variables
# Default to '/api' if API_BASE_URL is not set
API_BASE_URL="${API_BASE_URL:-/api}"

cat > /usr/share/nginx/html/config.js << EOF
window.__RUNTIME_CONFIG__ = {
  API_BASE_URL: "${API_BASE_URL}"
};
EOF

echo "Runtime config generated with API_BASE_URL=${API_BASE_URL}"

# Execute the CMD (nginx)
exec "$@"
