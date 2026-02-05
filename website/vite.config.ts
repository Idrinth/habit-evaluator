import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vitest/config';

export default defineConfig({
	plugins: [sveltekit()],
	server: {
		proxy: {
			'/api': {
				target: 'http://localhost:8080',
				changeOrigin: true
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
		}
	},
	resolve: {
		conditions: ['browser']
	}
});
