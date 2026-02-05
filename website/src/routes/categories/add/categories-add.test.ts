import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/svelte';
import AddCategoryPage from './+page.svelte';

vi.mock('$lib/api', () => ({
	categories: {
		create: vi.fn()
	}
}));

import { categories } from '$lib/api';

describe('Add Category Page', () => {
	beforeEach(() => {
		vi.clearAllMocks();
	});

	it('should render the form with all fields', () => {
		render(AddCategoryPage);

		expect(screen.getByRole('heading', { name: 'Add Category' })).toBeInTheDocument();
		expect(screen.getByLabelText('Name')).toBeInTheDocument();
		expect(screen.getByLabelText('Description')).toBeInTheDocument();
		expect(screen.getByLabelText('Color')).toBeInTheDocument();
		expect(screen.getByRole('button', { name: 'Add Category' })).toBeInTheDocument();
	});

	it('should have name as a required field', () => {
		render(AddCategoryPage);

		expect(screen.getByLabelText('Name')).toBeRequired();
	});

	it('should have a color input with default value', () => {
		render(AddCategoryPage);

		const colorInput = screen.getByLabelText('Color');
		expect(colorInput).toHaveAttribute('type', 'color');
	});

	it('should show validation error when name is empty', async () => {
		render(AddCategoryPage);

		// Set name to empty whitespace
		const nameInput = screen.getByLabelText('Name');
		await fireEvent.input(nameInput, { target: { value: '   ' } });
		await fireEvent.submit(screen.getByRole('button', { name: 'Add Category' }));

		await waitFor(() => {
			expect(screen.getByText('Category name is required')).toBeInTheDocument();
		});
		expect(categories.create).not.toHaveBeenCalled();
	});

	it('should call categories.create on valid submission', async () => {
		vi.mocked(categories.create).mockResolvedValue({
			id: '1',
			name: 'Health',
			description: 'Health habits',
			color: '#00ff00'
		});

		render(AddCategoryPage);

		await fireEvent.input(screen.getByLabelText('Name'), { target: { value: 'Health' } });
		await fireEvent.input(screen.getByLabelText('Description'), { target: { value: 'Health habits' } });
		await fireEvent.submit(screen.getByRole('button', { name: 'Add Category' }));

		await waitFor(() => {
			expect(categories.create).toHaveBeenCalledWith({
				name: 'Health',
				description: 'Health habits',
				color: '#4a90d9'
			});
		});
	});

	it('should show success message after creation', async () => {
		vi.mocked(categories.create).mockResolvedValue({
			id: '1',
			name: 'Health',
			description: null,
			color: '#4a90d9'
		});

		render(AddCategoryPage);

		await fireEvent.input(screen.getByLabelText('Name'), { target: { value: 'Health' } });
		await fireEvent.submit(screen.getByRole('button', { name: 'Add Category' }));

		await waitFor(() => {
			expect(screen.getByText('Category created successfully')).toBeInTheDocument();
		});
	});

	it('should reset form fields after successful creation', async () => {
		vi.mocked(categories.create).mockResolvedValue({
			id: '1',
			name: 'Health',
			description: null,
			color: '#4a90d9'
		});

		render(AddCategoryPage);

		const nameInput = screen.getByLabelText('Name') as HTMLInputElement;
		await fireEvent.input(nameInput, { target: { value: 'Health' } });
		await fireEvent.submit(screen.getByRole('button', { name: 'Add Category' }));

		await waitFor(() => {
			expect(nameInput.value).toBe('');
		});
	});

	it('should show error message on API failure', async () => {
		vi.mocked(categories.create).mockRejectedValue(new Error('Server error'));

		render(AddCategoryPage);

		await fireEvent.input(screen.getByLabelText('Name'), { target: { value: 'Test' } });
		await fireEvent.submit(screen.getByRole('button', { name: 'Add Category' }));

		await waitFor(() => {
			expect(screen.getByText('Server error')).toBeInTheDocument();
		});
	});

	it('should omit empty description from API call', async () => {
		vi.mocked(categories.create).mockResolvedValue({
			id: '1',
			name: 'Test',
			description: null,
			color: '#4a90d9'
		});

		render(AddCategoryPage);

		await fireEvent.input(screen.getByLabelText('Name'), { target: { value: 'Test' } });
		await fireEvent.submit(screen.getByRole('button', { name: 'Add Category' }));

		await waitFor(() => {
			expect(categories.create).toHaveBeenCalledWith({
				name: 'Test',
				description: undefined,
				color: '#4a90d9'
			});
		});
	});
});
