<script lang="ts">
	import { onMount } from 'svelte';
	import { habits, categories, type HabitCategory } from '$lib/api';

	let name = $state('');
	let description = $state('');
	let categoryId = $state('');
	let newCategoryName = $state('');
	let frequencyType = $state('DAILY');
	let targetFrequency = $state(1);
	let maxEntriesPerDay = $state(1);
	let positiveScoring = $state(true);
	let error = $state('');
	let success = $state('');
	let categoryList: HabitCategory[] = $state([]);

	onMount(async () => {
		try {
			categoryList = await categories.list();
			if (categoryList.length > 0) {
				categoryId = categoryList[0].id;
			}
		} catch {
			// categories will be empty, user can create new ones
		}
	});

	async function handleSubmit(e: Event) {
		e.preventDefault();
		error = '';
		success = '';

		try {
			let resolvedCategoryId = categoryId;

			if (categoryId === '__new__') {
				if (!newCategoryName.trim()) {
					error = 'Please enter a category name';
					return;
				}
				const newCat = await categories.create({ name: newCategoryName.trim() });
				categoryList = [...categoryList, newCat];
				resolvedCategoryId = newCat.id;
				newCategoryName = '';
			}

			if (!resolvedCategoryId) {
				error = 'Please select or create a category';
				return;
			}

			await habits.create({
				name,
				description: description || undefined,
				categoryId: resolvedCategoryId,
				frequencyType,
				targetFrequency,
				maxEntriesPerDay,
				positiveScoring
			});
			success = `Habit "${name}" created successfully`;
			name = '';
			description = '';
			categoryId = resolvedCategoryId;
			frequencyType = 'DAILY';
			targetFrequency = 1;
			maxEntriesPerDay = 1;
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
		<select id="categoryId" bind:value={categoryId} required>
			{#each categoryList as cat (cat.id)}
				<option value={cat.id}>{cat.name}</option>
			{/each}
			<option value="__new__">New category</option>
		</select>
		{#if categoryId === '__new__'}
			<label for="newCategoryName">New category name</label>
			<input type="text" id="newCategoryName" bind:value={newCategoryName} required maxlength="255" placeholder="Enter category name" />
		{/if}

		<label for="frequencyType">Frequency</label>
		<select id="frequencyType" bind:value={frequencyType} required>
			<option value="DAILY">Daily</option>
			<option value="WEEKLY">Weekly</option>
			<option value="MONTHLY">Monthly</option>
		</select>

		<label for="targetFrequency">Target frequency</label>
		<input type="number" id="targetFrequency" bind:value={targetFrequency} required min="1" />

		<label for="maxEntriesPerDay">Max entries per day (0 = unlimited)</label>
		<input type="number" id="maxEntriesPerDay" bind:value={maxEntriesPerDay} required min="0" />

		<label for="positiveScoring">
			<input type="checkbox" id="positiveScoring" bind:checked={positiveScoring} />
			Positive scoring
		</label>

		<button type="submit">Add Habit</button>
	</form>
</div>
