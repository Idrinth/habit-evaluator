import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/svelte';
import DocsPage from './+page.svelte';

describe('Setup Guide Page', () => {
	it('should render the page heading', () => {
		render(DocsPage);

		expect(screen.getByRole('heading', { level: 1, name: 'Setup Guide' })).toBeInTheDocument();
	});

	it('should describe prerequisites', () => {
		render(DocsPage);

		expect(screen.getByRole('heading', { level: 2, name: 'Prerequisites' })).toBeInTheDocument();
		expect(screen.getAllByText(/Java 17/).length).toBeGreaterThanOrEqual(1);
		expect(screen.getByText(/Gradle 8.5/)).toBeInTheDocument();
		expect(screen.getByText(/Node.js 22/)).toBeInTheDocument();
		expect(screen.getByText('Git')).toBeInTheDocument();
	});

	it('should show the clone command', () => {
		render(DocsPage);

		expect(screen.getByRole('heading', { level: 2, name: 'Clone the Repository' })).toBeInTheDocument();
		expect(screen.getByText(/git clone/)).toBeInTheDocument();
	});

	it('should show the build command', () => {
		render(DocsPage);

		expect(screen.getByRole('heading', { level: 2, name: 'Build Everything' })).toBeInTheDocument();
		expect(screen.getByText(/\.\/gradlew build/)).toBeInTheDocument();
	});

	it('should show the test command', () => {
		render(DocsPage);

		expect(screen.getByRole('heading', { level: 2, name: 'Run Tests' })).toBeInTheDocument();
		expect(screen.getByText(/\.\/gradlew test/)).toBeInTheDocument();
	});

	it('should link to platform-specific guides', () => {
		render(DocsPage);

		expect(screen.getByRole('heading', { level: 2, name: 'Platform Guides' })).toBeInTheDocument();

		const webLink = screen.getByRole('link', { name: /Web Server/ });
		expect(webLink).toHaveAttribute('href', '/docs/webserver');

		const desktopLink = screen.getByRole('link', { name: /Desktop/ });
		expect(desktopLink).toHaveAttribute('href', '/docs/desktop');

		const androidLink = screen.getByRole('link', { name: /Android/ });
		expect(androidLink).toHaveAttribute('href', '/docs/android');

		const apiLink = screen.getByRole('link', { name: /API Reference/ });
		expect(apiLink).toHaveAttribute('href', '/docs/api');
	});
});
