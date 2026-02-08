describe('Imprint Page', () => {
	beforeEach(() => {
		cy.intercept('GET', '/api/auth/me', {
			statusCode: 200,
			body: { userId: '1', username: 'demo' }
		});
		cy.intercept('GET', '/api/reminder-settings', { statusCode: 200, body: {} });
		cy.visit('/imprint');
	});

	it('displays the page heading', () => {
		cy.get('h1').should('be.visible');
	});

	it('shows contact information', () => {
		cy.contains('Idrinth').should('be.visible');
		cy.contains('Buttner').should('be.visible');
		cy.get('a[href="mailto:self@idrinth.de"]').should('exist');
	});

	it('lists third-party libraries in a table', () => {
		cy.get('table').should('exist');
		cy.get('table').should('contain.text', 'Svelte');
	});
});
