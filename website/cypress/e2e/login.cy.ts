describe('Login Page', () => {
	beforeEach(() => {
		cy.intercept('GET', '/api/auth/me', { statusCode: 401, body: {} });
		cy.visit('/login');
	});

	it('displays the login form', () => {
		cy.get('h1').should('contain.text', 'Habit Evaluator');
		cy.get('#username').should('be.visible');
		cy.get('#password').should('be.visible');
		cy.get('button[type="submit"]').should('be.visible');
	});

	it('requires username and password fields', () => {
		cy.get('#username').should('have.attr', 'required');
		cy.get('#password').should('have.attr', 'required');
	});

	it('allows typing into form fields', () => {
		cy.get('#username').type('testuser');
		cy.get('#password').type('testpass');
		cy.get('#username').should('have.value', 'testuser');
		cy.get('#password').should('have.value', 'testpass');
	});

	it('shows an error on failed login', () => {
		cy.intercept('POST', '/api/auth/login', {
			statusCode: 401,
			body: { success: false, message: 'Invalid credentials' }
		});

		cy.get('#username').type('wrong');
		cy.get('#password').type('wrong');
		cy.get('button[type="submit"]').click();

		cy.get('.error').should('be.visible');
	});

	it('redirects to habits home on successful login', () => {
		cy.intercept('POST', '/api/auth/login', {
			statusCode: 200,
			body: { success: true, userId: '1', username: 'demo' }
		});
		cy.intercept('GET', '/api/auth/me', {
			statusCode: 200,
			body: { userId: '1', username: 'demo', success: true }
		});
		cy.intercept('GET', '/api/habits', { statusCode: 200, body: [] });
		cy.intercept('GET', '/api/categories', { statusCode: 200, body: [] });
		cy.intercept('GET', '/api/habits/predict', { statusCode: 200, body: [] });
		cy.intercept('GET', '/api/reminder-settings', { statusCode: 200, body: {} });
		cy.intercept('GET', '/api/module-visibility', {
			statusCode: 200,
			body: {
				diaryVisible: true,
				sleepVisible: true,
				emotionsVisible: true,
				pointsVisible: true,
				statisticsVisible: true,
				foodLogVisible: true,
				sportLogVisible: true,
				medicationVisible: true,
				backupVisible: true,
				pdfExportVisible: true
			}
		});

		cy.get('#username').type('demo');
		cy.get('#password').type('demo123');
		cy.get('button[type="submit"]').click();

		cy.url().should('include', '/habits/home');
	});
});
