import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/svelte';
import DiaryPage from './+page.svelte';

const mockEntries = [
	{
		id: '1',
		description: 'Great workout',
		significance: 'NORMAL' as const,
		eventDate: '2025-01-15',
		createdAt: '2025-01-15T10:00:00'
	},
	{
		id: '2',
		description: 'Finished book',
		significance: 'MAJOR' as const,
		eventDate: '2025-01-14',
		createdAt: '2025-01-14T09:00:00'
	}
];

const mockStats = {
	todayPoints: 2,
	weekPoints: 10,
	monthPoints: 24,
	weeklyAverage: 8.5,
	monthlyTrend: 0.15
};

vi.mock('$lib/api', () => ({
	diary: {
		list: vi.fn(),
		stats: vi.fn(),
		suggestions: vi.fn(),
		create: vi.fn(),
		remove: vi.fn()
	}
}));

import { diary } from '$lib/api';

describe('Diary Page', () => {
	beforeEach(() => {
		vi.clearAllMocks();
		vi.mocked(diary.list).mockResolvedValue(mockEntries);
		vi.mocked(diary.stats).mockResolvedValue(mockStats);
		vi.mocked(diary.suggestions).mockResolvedValue(['Exercise', 'Reading']);
	});

	it('should render the page title', () => {
		render(DiaryPage);

		expect(screen.getByText('Diary / Journal')).toBeInTheDocument();
	});

	it('should show loading state initially', () => {
		render(DiaryPage);

		expect(screen.getByText('Loading...')).toBeInTheDocument();
	});

	it('should display diary entries after loading', async () => {
		render(DiaryPage);

		await waitFor(() => {
			expect(screen.getByText('Great workout')).toBeInTheDocument();
			expect(screen.getByText('Finished book')).toBeInTheDocument();
		});
	});

	it('should display stats cards', async () => {
		render(DiaryPage);

		await waitFor(() => {
			expect(screen.getByText('Today')).toBeInTheDocument();
			expect(screen.getByText('This Week')).toBeInTheDocument();
			expect(screen.getByText('This Month')).toBeInTheDocument();
			expect(screen.getByText('Weekly Avg')).toBeInTheDocument();
			expect(screen.getByText('Trend')).toBeInTheDocument();
		});
	});

	it('should display stat values', async () => {
		render(DiaryPage);

		await waitFor(() => {
			expect(screen.getByText('2')).toBeInTheDocument(); // todayPoints
			expect(screen.getByText('10')).toBeInTheDocument(); // weekPoints
			expect(screen.getByText('24')).toBeInTheDocument(); // monthPoints
			expect(screen.getByText('8.5')).toBeInTheDocument(); // weeklyAverage
			expect(screen.getByText('+15%')).toBeInTheDocument(); // monthlyTrend
		});
	});

	it('should display the add entry form', async () => {
		render(DiaryPage);

		await waitFor(() => {
			expect(screen.getByPlaceholderText('Positive event description')).toBeInTheDocument();
			expect(screen.getByRole('button', { name: 'Add Event' })).toBeInTheDocument();
		});
	});

	it('should display significance options', async () => {
		render(DiaryPage);

		await waitFor(() => {
			expect(screen.getByText('Minor (1pt)')).toBeInTheDocument();
			expect(screen.getByText('Normal (2pt)')).toBeInTheDocument();
			expect(screen.getByText('Major (4pt)')).toBeInTheDocument();
		});
	});

	it('should display entry dates and significance', async () => {
		render(DiaryPage);

		await waitFor(() => {
			expect(screen.getByText('2025-01-15')).toBeInTheDocument();
			expect(screen.getByText('2025-01-14')).toBeInTheDocument();
			expect(screen.getByText('NORMAL (2pt)')).toBeInTheDocument();
			expect(screen.getByText('MAJOR (4pt)')).toBeInTheDocument();
		});
	});

	it('should show empty state when no entries', async () => {
		vi.mocked(diary.list).mockResolvedValue([]);

		render(DiaryPage);

		await waitFor(() => {
			expect(screen.getByText('No diary entries yet. Add a positive event above.')).toBeInTheDocument();
		});
	});

	it('should call diary.create on form submission', async () => {
		vi.mocked(diary.create).mockResolvedValue({
			id: '3',
			description: 'New event',
			significance: 'MINOR',
			eventDate: '2025-01-16',
			createdAt: '2025-01-16T12:00:00'
		});

		render(DiaryPage);

		await waitFor(() => {
			expect(screen.getByPlaceholderText('Positive event description')).toBeInTheDocument();
		});

		const descInput = screen.getByPlaceholderText('Positive event description');
		await fireEvent.input(descInput, { target: { value: 'New event' } });
		await fireEvent.submit(screen.getByRole('button', { name: 'Add Event' }));

		await waitFor(() => {
			expect(diary.create).toHaveBeenCalledWith(
				expect.objectContaining({
					description: 'New event',
					significance: 'NORMAL'
				})
			);
		});
	});

	it('should call diary.remove when delete button is clicked', async () => {
		vi.mocked(diary.remove).mockResolvedValue(undefined as unknown as void);

		render(DiaryPage);

		await waitFor(() => {
			expect(screen.getByText('Great workout')).toBeInTheDocument();
		});

		const deleteButtons = screen.getAllByText('X');
		await fireEvent.click(deleteButtons[0]);

		await waitFor(() => {
			expect(diary.remove).toHaveBeenCalledWith('1');
		});
	});

	it('should display error when loading fails', async () => {
		vi.mocked(diary.list).mockRejectedValue(new Error('Failed to load'));

		render(DiaryPage);

		await waitFor(() => {
			expect(screen.getByText('Failed to load')).toBeInTheDocument();
		});
	});

	it('should format negative trend correctly', async () => {
		vi.mocked(diary.stats).mockResolvedValue({ ...mockStats, monthlyTrend: -0.1 });

		render(DiaryPage);

		await waitFor(() => {
			expect(screen.getByText('-10%')).toBeInTheDocument();
		});
	});

	it('should format stable trend correctly', async () => {
		vi.mocked(diary.stats).mockResolvedValue({ ...mockStats, monthlyTrend: 0 });

		render(DiaryPage);

		await waitFor(() => {
			expect(screen.getByText('Stable')).toBeInTheDocument();
		});
	});
});
