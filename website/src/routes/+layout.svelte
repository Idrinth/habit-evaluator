<script lang="ts">
	import '../app.css';
	import { auth } from '$lib/api';
	import type { Snippet } from 'svelte';

	let { children }: { children: Snippet } = $props();
	let loggedIn = $state(false);
	let username = $state('');

	async function checkAuth() {
		try {
			const res = await auth.me();
			if (res.success && res.username) {
				loggedIn = true;
				username = res.username;
			}
		} catch {
			loggedIn = false;
		}
	}

	async function handleLogout() {
		try {
			await auth.logout();
		} catch {
			// ignore
		}
		loggedIn = false;
		username = '';
		window.location.href = '/login';
	}

	$effect(() => {
		checkAuth();
	});
</script>

{#if loggedIn}
	<nav>
		<a href="/habits/track">Track</a>
		<a href="/habits/add">Add Habit</a>
		<a href="/categories/add">Add Category</a>
		<a href="/score-rules/add">Scoring Rules</a>
		<span style="margin-left: auto; color: #fff;">{username}</span>
		<button onclick={handleLogout} style="padding: 0.25rem 0.5rem; font-size: 0.85rem;">Logout</button>
	</nav>
{/if}

<div class="page-wrapper">
	{@render children()}
</div>
