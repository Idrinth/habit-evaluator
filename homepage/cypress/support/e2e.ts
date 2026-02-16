// Cypress E2E support file
import '@cypress/code-coverage/support';

afterEach(() => {
	// SvelteKit loads modules via async dynamic import(), so window.__coverage__
	// is not available at window:load time when @cypress/code-coverage captures it.
	// This hook sends coverage data after each test when modules have fully loaded.
	cy.window({ log: false }).then((win) => {
		const coverage = (win as unknown as { __coverage__?: object }).__coverage__;
		if (coverage) {
			cy.task('combineCoverage', JSON.stringify(coverage), { log: false });
		}
	});
});
