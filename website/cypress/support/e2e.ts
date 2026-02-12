// Cypress E2E support file

beforeEach(() => {
	// Catch-all for any API request not specifically intercepted by tests.
	// Prevents ECONNREFUSED proxy errors when no backend is running.
	// Test-specific cy.intercept() calls registered after this take precedence (LIFO).
	cy.intercept('/api/**', { statusCode: 200, body: {} });
});
