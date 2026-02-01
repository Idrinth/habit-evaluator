<script lang="ts">
	import { habits } from '$lib/api';

	let name = $state('');
	let description = $state('');
	let frequencyType = $state('DAILY');
	let targetFrequency = $state(1);
	let positiveScoring = $state(true);
	let error = $state('');
	let success = $state('');

	async function handleSubmit(e: Event) {
		e.preventDefault();
		error = '';
		success = '';

		try {
			await habits.create({ name, description: description || undefined, frequencyType, targetFrequency, positiveScoring });
			success = `Habit "${name}" created successfully`;
			name = '';
			description = '';
			frequencyType = 'DAILY';
			targetFrequency = 1;
			positiveScoring = true;
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to create habit';
		}
	}
</script>

<div class="container">
	<h1>Add Habit</h1>
	{#if error}
		<p class="error">{error}</p>
	{/if}
	{#if success}
		<p class="success">{success}</p>
	{/if}
	<form onsubmit={handleSubmit}>
		<label for="name">Name</label>
		<input type="text" id="name" bind:value={name} required maxlength="255" />

		<label for="description">Description</label>
		<textarea id="description" bind:value={description} maxlength="1000"></textarea>

		<label for="frequencyType">Frequency</label>
		<select id="frequencyType" bind:value={frequencyType} required>
			<option value="DAILY">Daily</option>
			<option value="WEEKLY">Weekly</option>
			<option value="MONTHLY">Monthly</option>
		</select>

		<label for="targetFrequency">Target frequency</label>
		<input type="number" id="targetFrequency" bind:value={targetFrequency} required min="1" />

		<label for="positiveScoring">
			<input type="checkbox" id="positiveScoring" bind:checked={positiveScoring} />
			Positive scoring
		</label>

		<button type="submit">Add Habit</button>
	</form>
</div>
