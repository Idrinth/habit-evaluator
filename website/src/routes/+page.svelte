<script lang="ts">
	import { onMount } from 'svelte';
	import { auth } from '$lib/api';
	import { goto } from '$app/navigation';

	onMount(async () => {
		try {
			const res = await auth.me();
			if (res.success) {
				goto('/habits/track');
			} else {
				goto('/login');
			}
		} catch {
			goto('/login');
		}
	});
</script>

<div class="container">
	<h1>Habit Evaluator</h1>
	<p style="text-align: center; color: var(--color-text-muted);">Loading...</p>
</div>
