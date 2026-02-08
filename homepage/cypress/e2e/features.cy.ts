describe('Features Page', () => {
	beforeEach(() => {
		cy.visit('/features');
	});

	it('displays the features heading', () => {
		cy.get('h1').should('contain.text', 'Features');
	});

	it('lists feature sections', () => {
		cy.contains('h2', 'Habit Management').should('be.visible');
		cy.contains('h2', 'Evaluation and Scoring').should('be.visible');
		cy.contains('h2', 'Diary Tracking').should('be.visible');
		cy.contains('h2', 'Sleep Tracking').should('be.visible');
		cy.contains('h2', 'Analytics and Reports').should('be.visible');
		cy.contains('h2', 'Multi-Platform Support').should('be.visible');
	});

	it('describes the scoring system', () => {
		cy.contains('0, 1, 2, 4, and 8 point').should('be.visible');
	});
});
