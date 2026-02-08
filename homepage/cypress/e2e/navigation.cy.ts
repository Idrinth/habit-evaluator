describe('Navigation', () => {
	beforeEach(() => {
		cy.visit('/');
	});

	it('shows the site header with logo', () => {
		cy.get('header').should('be.visible');
		cy.get('header').contains('Habit Evaluator').should('be.visible');
	});

	it('has working navigation links', () => {
		cy.get('header').contains('a', 'Features').should('have.attr', 'href', '/features');
		cy.get('header').contains('a', 'Setup Guide').should('have.attr', 'href', '/docs');
		cy.get('header').contains('a', 'API Reference').should('have.attr', 'href', '/docs/api');
	});

	it('has a link to the imprint page', () => {
		cy.get('a[title="Project legal"]').should('have.attr', 'href', '/imprint');
	});

	it('has a theme selector', () => {
		cy.get('select').should('exist');
	});

	it('shows the footer with license info', () => {
		cy.get('footer').should('be.visible');
		cy.get('footer').should('contain.text', 'MIT License');
	});

	it('navigates to the features page', () => {
		cy.get('header').contains('a', 'Features').click();
		cy.url().should('include', '/features');
		cy.get('h1').should('contain.text', 'Features');
	});

	it('navigates to the setup guide', () => {
		cy.get('header').contains('a', 'Setup Guide').click();
		cy.url().should('include', '/docs');
	});
});
