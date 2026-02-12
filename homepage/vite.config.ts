import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vitest/config';

export default defineConfig({
	plugins: [sveltekit()],
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
