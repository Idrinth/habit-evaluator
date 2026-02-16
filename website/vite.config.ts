import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vitest/config';
import istanbul from 'vite-plugin-istanbul';
import { readFileSync } from 'fs';

const pkg = JSON.parse(readFileSync('package.json', 'utf-8'));

export default defineConfig({
	define: {
		__APP_VERSION__: JSON.stringify(pkg.version)
	},
	plugins: [
		sveltekit(),
		istanbul({
			include: 'src/*',
			exclude: ['node_modules', 'cypress', 'src/tests'],
			extension: ['.ts', '.svelte'],
			requireEnv: true,
			forceBuildInstrument: true
		})
	],
	server: {
		proxy: {
			'/api': {
				target: 'http://localhost:8080',
				changeOrigin: true,
				configure: (proxy) => {
					proxy.on('error', (_err, _req, res) => {
						if ('writeHead' in res && 'headersSent' in res && !res.headersSent) {
							(res as import('http').ServerResponse).writeHead(502, {
								'Content-Type': 'application/json'
							});
							res.end(JSON.stringify({ message: 'Backend server is not available' }));
						}
					});
				}
			}
		}
	},
	test: {
		include: ['src/**/*.test.ts'],
		environment: 'jsdom',
		setupFiles: ['src/tests/setup.ts'],
		globals: true,
		server: {
			deps: {
				inline: [/svelte/]
			}
		},
		alias: {
			'$app/navigation': new URL('./src/tests/mocks/app-navigation.ts', import.meta.url).pathname
		},
		coverage: {
			provider: 'v8',
			reporter: ['text', 'json', 'html', 'lcov'],
			include: ['src/lib/**/*.ts'],
			thresholds: {
				lines: 0,
				functions: 0,
				branches: 0,
				statements: 0
			}
		}
	},
	resolve: {
		conditions: ['browser']
	}
});
