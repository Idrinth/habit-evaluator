<script lang="ts">
	import { onMount } from 'svelte';
	import { habits, categories, type HabitCategory } from '$lib/api';

	let name = $state('');
	let description = $state('');
	let categoryId = $state('');
	let frequencyType = $state('DAILY');
	let targetFrequency = $state(1);
	let positiveScoring = $state(true);
	let error = $state('');
	let success = $state('');
	let categoryList: HabitCategory[] = $state([]);

	onMount(async () => {
		try {
			categoryList = await categories.list();
		} catch {
			// categories are optional
		}
	});

	async function handleSubmit(e: Event) {
		e.preventDefault();
		error = '';
		success = '';

		try {
			await habits.create({
				name,
				description: description || undefined,
				categoryId: categoryId || undefined,
				frequencyType,
				targetFrequency,
				positiveScoring
			});
			success = `Habit "${name}" created successfully`;
			name = '';
			description = '';
			categoryId = '';
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

		<label for="categoryId">Category</label>
		<select id="categoryId" bind:value={categoryId}>
			<option value="">No category</option>
			{#each categoryList as cat (cat.id)}
				<option value={cat.id}>{cat.name}</option>
			{/each}
		</select>

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
