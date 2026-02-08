describe('Navigation', () => {
	beforeEach(() => {
		cy.intercept('GET', '/api/auth/me', {
			statusCode: 200,
			body: { userId: '1', username: 'demo' }
		});
		cy.intercept('GET', '/api/habits', { statusCode: 200, body: [] });
		cy.intercept('GET', '/api/categories', { statusCode: 200, body: [] });
		cy.intercept('GET', '/api/habits/predict', { statusCode: 200, body: [] });
		cy.intercept('GET', '/api/reminder-settings', { statusCode: 200, body: {} });

		cy.visit('/habits/home');
	});

	it('shows the navigation bar when logged in', () => {
		cy.get('nav').should('be.visible');
	});

	it('displays navigation links to main sections', () => {
		cy.get('nav a[href="/habits/home"]').should('exist');
		cy.get('nav a[href="/habits/add"]').should('exist');
		cy.get('nav a[href="/diary"]').should('exist');
		cy.get('nav a[href="/sleep"]').should('exist');
		cy.get('nav a[href="/stats"]').should('exist');
		cy.get('nav a[href="/settings"]').should('exist');
	});

	it('shows a theme selector', () => {
		cy.get('nav select').should('exist');
	});

	it('shows the imprint link', () => {
		cy.get('a[href="/imprint"]').should('exist');
	});

	it('navigates to the add habit page', () => {
		cy.intercept('GET', '/api/score-rules', { statusCode: 200, body: [] });
		cy.get('nav a[href="/habits/add"]').click();
		cy.url().should('include', '/habits/add');
	});
});
