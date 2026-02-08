describe('Imprint Page', () => {
	beforeEach(() => {
		cy.visit('/imprint');
	});

	it('displays the page heading', () => {
		cy.get('h1').should('contain.text', 'Project legal');
	});

	it('shows contact information', () => {
		cy.contains('Idrinth').should('be.visible');
		cy.contains('Buttner').should('be.visible');
		cy.get('a[href="mailto:self@idrinth.de"]').should('exist');
	});

	it('describes the license', () => {
		cy.contains('MIT').should('be.visible');
	});

	it('lists data protection principles', () => {
		cy.contains('h2', 'Data Protection').should('be.visible');
		cy.contains('No tracking').should('be.visible');
	});

	it('lists third-party libraries', () => {
		cy.get('table').should('exist');
		cy.get('table').should('contain.text', 'Svelte');
		cy.get('table').should('contain.text', 'SvelteKit');
		cy.get('table').should('contain.text', 'TypeScript');
	});
});
