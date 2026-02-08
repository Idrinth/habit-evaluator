describe('Landing Page', () => {
	beforeEach(() => {
		cy.visit('/');
	});

	it('displays the main heading', () => {
		cy.get('h1').should('contain.text', 'Habit Evaluator');
	});

	it('shows the tagline', () => {
		cy.contains('privacy-focused').should('be.visible');
	});

	it('has call-to-action links', () => {
		cy.contains('a', 'Get Started').should('have.attr', 'href', '/docs');
		cy.contains('a', 'View Features').should('have.attr', 'href', '/features');
	});

	it('lists the available platforms', () => {
		cy.contains('h2', 'Available Platforms').should('be.visible');
		cy.contains('Web Application').should('be.visible');
		cy.contains('Desktop Application').should('be.visible');
		cy.contains('Android Application').should('be.visible');
	});

	it('highlights key selling points', () => {
		cy.contains('Privacy First').should('be.visible');
		cy.contains('Flexible Scoring').should('be.visible');
		cy.contains('Shared Core').should('be.visible');
	});
});
