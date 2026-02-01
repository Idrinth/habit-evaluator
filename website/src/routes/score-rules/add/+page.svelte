<script lang="ts">
	import { scoreRules } from '$lib/api';

	let name = $state('');
	let thresholdFor1Point = $state(1);
	let thresholdFor2Points = $state(2);
	let thresholdFor4Points = $state(4);
	let thresholdFor8Points = $state(8);
	let error = $state('');
	let success = $state('');

	async function handleSubmit(e: Event) {
		e.preventDefault();
		error = '';
		success = '';

		try {
			await scoreRules.create({
				name,
				thresholdFor1Point,
				thresholdFor2Points,
				thresholdFor4Points,
				thresholdFor8Points
			});
			success = 'Scoring rule created successfully';
			name = '';
			thresholdFor1Point = 1;
			thresholdFor2Points = 2;
			thresholdFor4Points = 4;
			thresholdFor8Points = 8;
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to create scoring rule';
		}
	}
</script>

<div class="container">
	<h1>Add Scoring Rule</h1>
	{#if error}
		<p class="error">{error}</p>
	{/if}
	{#if success}
		<p class="success">{success}</p>
	{/if}
	<form onsubmit={handleSubmit}>
		<label for="name">Name</label>
		<input type="text" id="name" bind:value={name} required />

		<label for="thresholdFor1Point">Threshold for 1 Point</label>
		<input type="number" id="thresholdFor1Point" bind:value={thresholdFor1Point} min="0" required />
		<p class="hint">Minimum weekly completions to earn 1 point</p>

		<label for="thresholdFor2Points">Threshold for 2 Points</label>
		<input type="number" id="thresholdFor2Points" bind:value={thresholdFor2Points} min="0" required />
		<p class="hint">Minimum weekly completions to earn 2 points</p>

		<label for="thresholdFor4Points">Threshold for 4 Points</label>
		<input type="number" id="thresholdFor4Points" bind:value={thresholdFor4Points} min="0" required />
		<p class="hint">Minimum weekly completions to earn 4 points</p>

		<label for="thresholdFor8Points">Threshold for 8 Points</label>
		<input type="number" id="thresholdFor8Points" bind:value={thresholdFor8Points} min="0" required />
		<p class="hint">Minimum weekly completions to earn 8 points</p>

		<button type="submit">Create Scoring Rule</button>
	</form>
</div>
