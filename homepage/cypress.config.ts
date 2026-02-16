import { defineConfig } from 'cypress';
import codeCoverageTask from '@cypress/code-coverage/task';

export default defineConfig({
	e2e: {
		baseUrl: 'http://localhost:4173',
		supportFile: 'cypress/support/e2e.ts',
		specPattern: 'cypress/e2e/**/*.cy.ts',
		video: false,
		screenshotOnRunFailure: false,
		setupNodeEvents(on, config) {
			codeCoverageTask(on, config);
			return config;
		}
	}
});
