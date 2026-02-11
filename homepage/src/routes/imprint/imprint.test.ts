import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/svelte';
import ImprintPage from './+page.svelte';

describe('Imprint Page', () => {
	it('should render the page heading', () => {
		render(ImprintPage);

		expect(screen.getByRole('heading', { level: 1, name: 'Project legal' })).toBeInTheDocument();
	});

	it('should render the contact section', () => {
		render(ImprintPage);

		expect(screen.getByRole('heading', { level: 2, name: 'Contact' })).toBeInTheDocument();
		expect(screen.getByText('Name')).toBeInTheDocument();
		expect(screen.getByText(/Idrinth/)).toBeInTheDocument();
		expect(screen.getByText(/Buttner/)).toBeInTheDocument();
	});

	it('should render the contact email link', () => {
		render(ImprintPage);

		const emailLink = screen.getByRole('link', { name: 'self@idrinth.de' });
		expect(emailLink).toBeInTheDocument();
		expect(emailLink).toHaveAttribute('href', 'mailto:self@idrinth.de');
	});

	it('should render the license section with MIT', () => {
		render(ImprintPage);

		expect(screen.getByRole('heading', { level: 2, name: 'License' })).toBeInTheDocument();
		expect(screen.getByText(/MIT License/)).toBeInTheDocument();
	});

	it('should render the data protection section', () => {
		render(ImprintPage);

		expect(screen.getByRole('heading', { level: 2, name: 'Data Protection' })).toBeInTheDocument();
		expect(screen.getByText(/No tracking/)).toBeInTheDocument();
		expect(screen.getByText(/No remote storing of data/)).toBeInTheDocument();
	});

	it('should render the disclaimers section', () => {
		render(ImprintPage);

		expect(screen.getByRole('heading', { level: 2, name: 'Disclaimers' })).toBeInTheDocument();
		expect(screen.getByText(/does not replace professional medical/)).toBeInTheDocument();
	});

	it('should render the third-party libraries table', () => {
		render(ImprintPage);

		expect(screen.getByRole('heading', { level: 2, name: 'Third-Party Libraries' })).toBeInTheDocument();

		const table = screen.getByRole('table');
		expect(table).toBeInTheDocument();

		expect(screen.getByText('Svelte')).toBeInTheDocument();
		expect(screen.getByText('SvelteKit')).toBeInTheDocument();
		expect(screen.getByText('TypeScript')).toBeInTheDocument();
		expect(screen.getByText('Vite')).toBeInTheDocument();
	});
});
