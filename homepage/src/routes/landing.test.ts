import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/svelte';
import LandingPage from './+page.svelte';

describe('Landing Page', () => {
	it('should render the main heading', () => {
		render(LandingPage);

		expect(screen.getByRole('heading', { level: 1, name: 'Habit Evaluator' })).toBeInTheDocument();
	});

	it('should render the privacy-focused tagline', () => {
		render(LandingPage);

		expect(screen.getByText(/privacy-focused habit tracker/i)).toBeInTheDocument();
	});

	it('should render call-to-action links', () => {
		render(LandingPage);

		const getStarted = screen.getByRole('link', { name: 'Get Started' });
		expect(getStarted).toBeInTheDocument();
		expect(getStarted).toHaveAttribute('href', '/docs');

		const viewFeatures = screen.getByRole('link', { name: 'View Features' });
		expect(viewFeatures).toBeInTheDocument();
		expect(viewFeatures).toHaveAttribute('href', '/features');
	});

	it('should render the available platforms section', () => {
		render(LandingPage);

		expect(screen.getByRole('heading', { level: 2, name: 'Available Platforms' })).toBeInTheDocument();
		expect(screen.getByRole('heading', { level: 3, name: 'Web Application' })).toBeInTheDocument();
		expect(screen.getByRole('heading', { level: 3, name: 'Desktop Application' })).toBeInTheDocument();
		expect(screen.getByRole('heading', { level: 3, name: 'Android Application' })).toBeInTheDocument();
	});

	it('should render platform setup guide links', () => {
		render(LandingPage);

		const links = screen.getAllByRole('link', { name: 'Setup Guide' });
		expect(links).toHaveLength(3);
		expect(links[0]).toHaveAttribute('href', '/docs/webserver');
		expect(links[1]).toHaveAttribute('href', '/docs/desktop');
		expect(links[2]).toHaveAttribute('href', '/docs/android');
	});

	it('should render the highlights section', () => {
		render(LandingPage);

		expect(screen.getByRole('heading', { level: 2, name: 'Why Habit Evaluator?' })).toBeInTheDocument();
		expect(screen.getByRole('heading', { level: 3, name: 'Privacy First' })).toBeInTheDocument();
		expect(screen.getByRole('heading', { level: 3, name: 'Flexible Scoring' })).toBeInTheDocument();
		expect(screen.getByRole('heading', { level: 3, name: 'Shared Core' })).toBeInTheDocument();
	});

	it('should describe each platform', () => {
		render(LandingPage);

		expect(screen.getByText(/Spring Boot REST API/)).toBeInTheDocument();
		expect(screen.getByText(/JavaFX desktop app/)).toBeInTheDocument();
		expect(screen.getByText(/native Android app/i)).toBeInTheDocument();
	});
});
