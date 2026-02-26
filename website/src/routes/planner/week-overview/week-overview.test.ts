import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/svelte';
import WeekOverviewPage from './+page.svelte';

vi.mock('$lib/api', () => ({
	dayPlanner: {
		weekOverview: vi.fn()
	}
}));

import { dayPlanner } from '$lib/api';

describe('Week Overview Page', () => {
	beforeEach(() => {
		vi.clearAllMocks();
	});

	it('should show loading state initially', () => {
		vi.mocked(dayPlanner.weekOverview).mockReturnValue(new Promise(() => {}));
		render(WeekOverviewPage);

		expect(screen.getByText('Loading week overview...')).toBeInTheDocument();
	});

	it('should show empty state when no slots', async () => {
		vi.mocked(dayPlanner.weekOverview).mockResolvedValue({ slots: [] });
		render(WeekOverviewPage);

		await waitFor(() => {
			expect(screen.getByText(/No time slots assigned yet/)).toBeInTheDocument();
		});
	});

	it('should render the grid when slots exist', async () => {
		vi.mocked(dayPlanner.weekOverview).mockResolvedValue({
			slots: [
				{ dayOfWeek: 1, hour: 9, duration: 1 },
				{ dayOfWeek: 3, hour: 14, duration: 1 }
			]
		});
		render(WeekOverviewPage);

		await waitFor(() => {
			expect(screen.getByRole('grid')).toBeInTheDocument();
		});
	});

	it('should mark occupied cells correctly', async () => {
		vi.mocked(dayPlanner.weekOverview).mockResolvedValue({
			slots: [{ dayOfWeek: 1, hour: 9, duration: 1 }]
		});
		render(WeekOverviewPage);

		await waitFor(() => {
			const occupiedCell = screen.getByLabelText('Mon 09:00: occupied');
			expect(occupiedCell).toBeInTheDocument();
			expect(occupiedCell).toHaveClass('occupied');
		});
	});

	it('should mark free cells correctly', async () => {
		vi.mocked(dayPlanner.weekOverview).mockResolvedValue({
			slots: [{ dayOfWeek: 1, hour: 9, duration: 1 }]
		});
		render(WeekOverviewPage);

		await waitFor(() => {
			const freeCell = screen.getByLabelText('Tue 09:00: free');
			expect(freeCell).toBeInTheDocument();
			expect(freeCell).not.toHaveClass('occupied');
		});
	});

	it('should show error on API failure', async () => {
		vi.mocked(dayPlanner.weekOverview).mockRejectedValue(new Error('Network error'));
		render(WeekOverviewPage);

		await waitFor(() => {
			expect(screen.getByText('Network error')).toBeInTheDocument();
		});
	});

	it('should render day headers', async () => {
		vi.mocked(dayPlanner.weekOverview).mockResolvedValue({
			slots: [{ dayOfWeek: 1, hour: 0, duration: 1 }]
		});
		render(WeekOverviewPage);

		await waitFor(() => {
			expect(screen.getByText('Mon')).toBeInTheDocument();
			expect(screen.getByText('Tue')).toBeInTheDocument();
			expect(screen.getByText('Wed')).toBeInTheDocument();
			expect(screen.getByText('Thu')).toBeInTheDocument();
			expect(screen.getByText('Fri')).toBeInTheDocument();
			expect(screen.getByText('Sat')).toBeInTheDocument();
			expect(screen.getByText('Sun')).toBeInTheDocument();
		});
	});

	it('should show legend when slots exist', async () => {
		vi.mocked(dayPlanner.weekOverview).mockResolvedValue({
			slots: [{ dayOfWeek: 1, hour: 9, duration: 1 }]
		});
		render(WeekOverviewPage);

		await waitFor(() => {
			expect(screen.getByText('occupied')).toBeInTheDocument();
			expect(screen.getByText('free')).toBeInTheDocument();
		});
	});

	it('should show back to planner link', async () => {
		vi.mocked(dayPlanner.weekOverview).mockResolvedValue({ slots: [] });
		render(WeekOverviewPage);

		await waitFor(() => {
			const link = screen.getByText('Back to Planner');
			expect(link).toBeInTheDocument();
			expect(link.closest('a')).toHaveAttribute('href', '/planner');
		});
	});
});
