<script lang="ts">
	import { auth } from '$lib/api';
	import { goto, invalidate } from '$app/navigation';

	let username = $state('');
	let password = $state('');
	let error = $state('');

	async function handleSubmit(e: Event) {
		e.preventDefault();
		error = '';
		try {
			const res = await auth.login(username, password);
			if (res.success) {
				await invalidate('app:session');
				goto('/habits/track');
			} else {
				error = res.message || 'Invalid username or password';
			}
		} catch (err) {
			error = err instanceof Error ? err.message : 'Invalid username or password';
		}
	}
</script>

<div class="container">
	<h1>Habit Evaluator</h1>
	{#if error}
		<p class="error">{error}</p>
	{/if}
	<form onsubmit={handleSubmit}>
		<label for="username">Username</label>
		<input type="text" id="username" bind:value={username} required />
		<label for="password">Password</label>
		<input type="password" id="password" bind:value={password} required />
		<button type="submit">Login</button>
	</form>
</div>
