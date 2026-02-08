describe('Documentation Pages', () => {
	describe('Setup Guide', () => {
		beforeEach(() => {
			cy.visit('/docs');
		});

		it('shows prerequisites', () => {
			cy.contains('Java 17').should('be.visible');
			cy.contains('Node.js').should('be.visible');
		});

		it('links to platform-specific guides', () => {
			cy.contains('a', 'Web Server').should('have.attr', 'href', '/docs/webserver');
			cy.contains('a', 'Desktop').should('have.attr', 'href', '/docs/desktop');
			cy.contains('a', 'Android').should('have.attr', 'href', '/docs/android');
			cy.contains('a', 'API Reference').should('have.attr', 'href', '/docs/api');
		});
	});

	describe('Web Server Setup', () => {
		beforeEach(() => {
			cy.visit('/docs/webserver');
		});

		it('displays the page heading', () => {
			cy.get('h1').should('contain.text', 'Web Server Setup');
		});

		it('mentions Spring Boot', () => {
			cy.contains('Spring Boot').should('be.visible');
		});
	});

	describe('Desktop Setup', () => {
		beforeEach(() => {
			cy.visit('/docs/desktop');
		});

		it('displays the page heading', () => {
			cy.get('h1').should('contain.text', 'Desktop Application Setup');
		});

		it('mentions JavaFX', () => {
			cy.contains('JavaFX').should('be.visible');
		});
	});

	describe('Android Setup', () => {
		beforeEach(() => {
			cy.visit('/docs/android');
		});

		it('displays the page heading', () => {
			cy.get('h1').should('contain.text', 'Android Application Setup');
		});
	});

	describe('API Reference', () => {
		beforeEach(() => {
			cy.visit('/docs/api');
		});

		it('displays the page heading', () => {
			cy.get('h1').should('contain.text', 'API Reference');
		});

		it('documents the authentication endpoints', () => {
			cy.contains('/api/auth/login').should('be.visible');
			cy.contains('/api/auth/logout').should('be.visible');
		});

		it('documents the habits endpoints', () => {
			cy.contains('/api/habits').should('be.visible');
		});
	});
});
