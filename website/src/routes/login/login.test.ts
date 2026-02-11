import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/svelte';
import LoginPage from './+page.svelte';

vi.mock('$lib/api', () => ({
	auth: {
		login: vi.fn()
	}
}));

vi.mock('$app/navigation', () => ({
	goto: vi.fn().mockResolvedValue(undefined)
}));

import { auth } from '$lib/api';
import { goto } from '$app/navigation';

describe('Login Page', () => {
	beforeEach(() => {
		vi.clearAllMocks();
	});

	it('should render the login form', () => {
		render(LoginPage);

		expect(screen.getByText('Habit Evaluator')).toBeInTheDocument();
		expect(screen.getByLabelText('Username')).toBeInTheDocument();
		expect(screen.getByLabelText('Password')).toBeInTheDocument();
		expect(screen.getByRole('button', { name: 'Login' })).toBeInTheDocument();
	});

	it('should have required fields', () => {
		render(LoginPage);

		expect(screen.getByLabelText('Username')).toBeRequired();
		expect(screen.getByLabelText('Password')).toBeRequired();
	});

	it('should have correct input types', () => {
		render(LoginPage);

		expect(screen.getByLabelText('Username')).toHaveAttribute('type', 'text');
		expect(screen.getByLabelText('Password')).toHaveAttribute('type', 'password');
	});

	it('should call auth.login on form submission', async () => {
		vi.mocked(auth.login).mockResolvedValue({
			success: true,
			userId: '1',
			username: 'testuser',
			message: 'OK'
		});

		render(LoginPage);

		const usernameInput = screen.getByLabelText('Username');
		const passwordInput = screen.getByLabelText('Password');

		await fireEvent.input(usernameInput, { target: { value: 'testuser' } });
		await fireEvent.input(passwordInput, { target: { value: 'pass123' } });
		await fireEvent.submit(screen.getByRole('button', { name: 'Login' }));

		await waitFor(() => {
			expect(auth.login).toHaveBeenCalledWith('testuser', 'pass123');
		});
	});

	it('should redirect to habits/home on successful login', async () => {
		vi.mocked(auth.login).mockResolvedValue({
			success: true,
			userId: '1',
			username: 'testuser',
			message: 'OK'
		});

		render(LoginPage);

		await fireEvent.input(screen.getByLabelText('Username'), { target: { value: 'user' } });
		await fireEvent.input(screen.getByLabelText('Password'), { target: { value: 'pass' } });
		await fireEvent.submit(screen.getByRole('button', { name: 'Login' }));

		await waitFor(() => {
			expect(goto).toHaveBeenCalledWith('/habits/home', { invalidateAll: true });
		});
	});

	it('should display error on failed login response', async () => {
		vi.mocked(auth.login).mockResolvedValue({
			success: false,
			userId: null,
			username: null,
			message: 'Invalid credentials'
		});

		render(LoginPage);

		await fireEvent.input(screen.getByLabelText('Username'), { target: { value: 'bad' } });
		await fireEvent.input(screen.getByLabelText('Password'), { target: { value: 'bad' } });
		await fireEvent.submit(screen.getByRole('button', { name: 'Login' }));

		await waitFor(() => {
			expect(screen.getByText('Invalid credentials')).toBeInTheDocument();
		});
	});

	it('should display error on network failure', async () => {
		vi.mocked(auth.login).mockRejectedValue(new Error('Network error'));

		render(LoginPage);

		await fireEvent.input(screen.getByLabelText('Username'), { target: { value: 'user' } });
		await fireEvent.input(screen.getByLabelText('Password'), { target: { value: 'pass' } });
		await fireEvent.submit(screen.getByRole('button', { name: 'Login' }));

		await waitFor(() => {
			expect(screen.getByText('Network error')).toBeInTheDocument();
		});
	});

	it('should not display error initially', () => {
		const { container } = render(LoginPage);

		expect(container.querySelector('.error')).toBeNull();
	});
});
