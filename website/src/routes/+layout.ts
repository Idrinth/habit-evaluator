import type { LayoutLoad } from './$types';

export const prerender = false;
export const ssr = false;

export const load: LayoutLoad = async ({ depends, fetch }) => {
	depends('app:session');
	try {
		const response = await fetch('/api/auth/me', {
			headers: { 'Content-Type': 'application/json' }
		});
		if (response.ok) {
			const data = await response.json();
			if (data.success && data.username) {
				return { loggedIn: true, username: data.username as string };
			}
		}
	} catch {
		// not authenticated
	}
	return { loggedIn: false, username: '' };
};
