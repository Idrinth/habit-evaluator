<script lang="ts">
	import '../app.css';
	import { auth } from '$lib/api';
	import { invalidate } from '$app/navigation';
	import type { Snippet } from 'svelte';

	let { data, children }: { data: { loggedIn: boolean; username: string }; children: Snippet } =
		$props();

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
		<a href="/habits/track">Track</a>
		<a href="/habits/add">Add Habit</a>
		<a href="/categories/add">Add Category</a>
		<a href="/score-rules/add">Scoring Rules</a>
		<span style="margin-left: auto; color: var(--color-nav-text);">{data.username}</span>
		<button onclick={handleLogout} style="padding: 0.25rem 0.5rem; font-size: 0.85rem;"
			>Logout</button
		>
	</nav>
{/if}

<div class="page-wrapper">
	{@render children()}
</div>
