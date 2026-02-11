import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/svelte';
import FeaturesPage from './+page.svelte';

describe('Features Page', () => {
	it('should render the page heading', () => {
		render(FeaturesPage);

		expect(screen.getByRole('heading', { level: 1, name: 'Features' })).toBeInTheDocument();
	});

	it('should render all feature sections', () => {
		render(FeaturesPage);

		const expectedSections = [
			'Habit Management',
			'Evaluation and Scoring',
			'Diary Tracking',
			'Sleep Tracking',
			'Analytics and Reports',
			'Authentication and Sharing',
			'Multi-Platform Support',
			'Synchronisation',
			'Customisation',
			'Database Options',
			'Deployment'
		];

		for (const section of expectedSections) {
			expect(screen.getByRole('heading', { level: 2, name: section })).toBeInTheDocument();
		}
	});

	it('should describe scoring point thresholds', () => {
		render(FeaturesPage);

		expect(screen.getByText(/0, 1, 2, 4, and 8 point thresholds/)).toBeInTheDocument();
	});

	it('should list supported frequency types', () => {
		render(FeaturesPage);

		expect(screen.getByText(/daily, weekly, and monthly frequency/)).toBeInTheDocument();
	});

	it('should mention all platforms', () => {
		render(FeaturesPage);

		expect(screen.getByText(/Spring Boot REST API/)).toBeInTheDocument();
		expect(screen.getByText(/JavaFX application/)).toBeInTheDocument();
		expect(screen.getByText(/Native app with habit tracking/)).toBeInTheDocument();
	});

	it('should mention negative habit support', () => {
		render(FeaturesPage);

		expect(screen.getByText(/negative habits/)).toBeInTheDocument();
	});
});
