<script lang="ts">
	import '../app.css';
	import { auth } from '$lib/api';
	import { invalidate } from '$app/navigation';
	import { onMount } from 'svelte';
	import { browser } from '$app/environment';
	import type { Snippet } from 'svelte';

	let { data, children }: { data: { loggedIn: boolean; username: string }; children: Snippet } =
		$props();

	let themeMode = $state('system');

	onMount(() => {
		const saved = localStorage.getItem('theme');
		if (saved === 'light' || saved === 'dark') {
			themeMode = saved;
			document.documentElement.setAttribute('data-theme', saved);
		}
	});

	function handleThemeChange(event: Event) {
		const value = (event.target as HTMLSelectElement).value;
		themeMode = value;
		if (value === 'system') {
			document.documentElement.removeAttribute('data-theme');
			localStorage.removeItem('theme');
		} else {
			document.documentElement.setAttribute('data-theme', value);
			localStorage.setItem('theme', value);
		}
	}

	async function handleLogout() {
		try {
			await auth.logout();
		} catch {
			// ignore
		}
		await invalidate('app:session');
		window.location.href = '/login';
	}
</script>

{#if data.loggedIn}
	<nav>
		<a href="/habits/edit">Edit</a>
		<a href="/habits/add">Add Habit</a>
		<a href="/categories/add">Add Category</a>
		<a href="/score-rules/add">Scoring Rules</a>
		<a href="/points">Point Charts</a>
		<span style="margin-left: auto; color: var(--color-nav-text);">{data.username}</span>
		<select class="theme-select" value={themeMode} onchange={handleThemeChange}>
			<option value="system">System</option>
			<option value="light">Light</option>
			<option value="dark">Dark</option>
		</select>
		<button onclick={handleLogout} style="padding: 0.25rem 0.5rem; font-size: 0.85rem;"
			>Logout</button
		>
	</nav>
{/if}

<div class="page-wrapper">
	{@render children()}
</div>

<style>
	.theme-select {
		padding: 0.2rem 0.4rem;
		font-size: 0.8rem;
		border-radius: 4px;
		border: 1px solid var(--color-nav-text);
		background: transparent;
		color: var(--color-nav-text);
		cursor: pointer;
	}
	.theme-select option {
		background: var(--color-bg);
		color: var(--color-text);
	}
</style>
