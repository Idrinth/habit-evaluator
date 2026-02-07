<script lang="ts">
	import { onMount } from 'svelte';
	import { foodLogs, type FoodLog } from '$lib/api';

	let entries: FoodLog[] = $state([]);
	let suggestions: string[] = $state([]);
	let error = $state('');
	let loading = $state(true);

	let foodItems = $state('');
	let kcal = $state('');
	let carbohydrates = $state('');
	let dateTime = $state(new Date().toISOString().slice(0, 16));
	let notes = $state('');

	onMount(async () => {
		await loadData();
	});

	async function loadData() {
		loading = true;
		error = '';
		try {
			const [e, sug] = await Promise.all([foodLogs.list(), foodLogs.suggestions()]);
			entries = e.sort(
				(a, b) => b.dateTime.localeCompare(a.dateTime) || b.createdAt.localeCompare(a.createdAt)
			);
			suggestions = sug || [];
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to load food log';
		} finally {
			loading = false;
		}
	}

	async function handleAdd(e: Event) {
		e.preventDefault();
		if (!foodItems.trim()) return;
		error = '';
		try {
			const createPayload: { carbohydrates: number; kcal: number; dateTime: string; foodItems: string; notes?: string } = {
				carbohydrates: parseFloat(carbohydrates) || 0,
				kcal: parseInt(kcal) || 0,
				dateTime: dateTime + ':00',
				foodItems: foodItems.trim()
			};
			if (notes.trim()) createPayload.notes = notes.trim();
			await foodLogs.create(createPayload);
			foodItems = '';
			kcal = '';
			carbohydrates = '';
			dateTime = new Date().toISOString().slice(0, 16);
			notes = '';
			await loadData();
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to add food log entry';
		}
	}

	async function handleDelete(id: string) {
		try {
			await foodLogs.delete(id);
			await loadData();
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to delete entry';
		}
	}

	function formatDateTime(dt: string): string {
		if (!dt) return '';
		return dt.replace('T', ' ').substring(0, 16);
	}
</script>

<div class="container food-log-container">
	<h1>Food Log</h1>

	{#if error}
		<p class="error">{error}</p>
	{/if}

	{#if loading}
		<p style="text-align: center; color: var(--color-text-placeholder);">Loading...</p>
	{:else}
		<form class="add-form" onsubmit={handleAdd}>
			<input type="text" bind:value={foodItems} placeholder="Food items (e.g. Rice, Chicken, Salad)" required list="food-suggestions" autocomplete="off" />
			<datalist id="food-suggestions">
				{#each suggestions as suggestion}
					<option value={suggestion}></option>
				{/each}
			</datalist>
			<div class="form-row">
				<div class="form-field">
					<label for="kcal">Kcal</label>
					<input type="number" id="kcal" bind:value={kcal} placeholder="0" min="0" />
				</div>
				<div class="form-field">
					<label for="carbs">Carbs (g)</label>
					<input type="number" id="carbs" bind:value={carbohydrates} placeholder="0" min="0" step="0.1" />
				</div>
				<div class="form-field">
					<label for="dateTime">Date & Time</label>
					<input type="datetime-local" id="dateTime" bind:value={dateTime} required />
				</div>
			</div>
			<div class="form-row">
				<input type="text" bind:value={notes} placeholder="Notes (optional)" style="flex: 1;" />
				<button type="submit">Add Entry</button>
			</div>
		</form>

		{#if entries.length === 0}
			<p class="empty">No food log entries yet. Add your first meal above.</p>
		{:else}
			<div class="entries-list">
				{#each entries as entry (entry.id)}
					<div class="entry-card">
						<div class="entry-main">
							<span class="entry-food">{entry.foodItems}</span>
							<span class="entry-nutrition">{entry.kcal} kcal, {entry.carbohydrates.toFixed(1)}g carbs</span>
							<span class="entry-date">{formatDateTime(entry.dateTime)}</span>
							{#if entry.notes}
								<span class="entry-notes">{entry.notes}</span>
							{/if}
						</div>
						<button class="delete-btn" onclick={() => handleDelete(entry.id)}>X</button>
					</div>
				{/each}
			</div>
		{/if}
	{/if}
</div>

<style>
	.food-log-container {
		max-width: 650px;
	}

	.add-form {
		margin-bottom: 1.5rem;
	}

	.add-form input[type='text']:first-child {
		width: 100%;
		margin-bottom: 0.5rem;
	}

	.form-row {
		display: flex;
		gap: 0.5rem;
		align-items: flex-end;
		margin-bottom: 0.5rem;
	}

	.form-field {
		display: flex;
		flex-direction: column;
		flex: 1;
		min-width: 100px;
	}

	.form-field label {
		font-size: 0.8rem;
		color: var(--color-text-muted);
		margin-bottom: 0.2rem;
	}

	.form-row button {
		white-space: nowrap;
	}

	.entries-list {
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
	}

	.entry-card {
		display: flex;
		align-items: center;
		justify-content: space-between;
		border: 1px solid var(--color-border-light);
		border-radius: 4px;
		padding: 0.5rem 0.75rem;
	}

	.entry-card:hover {
		background-color: var(--color-bg-hover);
	}

	.entry-main {
		display: flex;
		flex-direction: column;
		gap: 0.2rem;
		flex: 1;
		min-width: 0;
	}

	.entry-food {
		font-weight: bold;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.entry-nutrition {
		font-size: 0.85rem;
		color: var(--color-text-secondary);
	}

	.entry-date {
		font-size: 0.8rem;
		color: var(--color-text-muted);
	}

	.entry-notes {
		font-size: 0.8rem;
		color: var(--color-text-muted);
		font-style: italic;
	}

	.delete-btn {
		background: transparent;
		color: var(--color-error);
		border: 1px solid var(--color-error);
		padding: 0.15rem 0.5rem;
		font-size: 0.75rem;
		cursor: pointer;
		border-radius: 3px;
		margin-left: 0.5rem;
		flex-shrink: 0;
	}

	.delete-btn:hover {
		background-color: var(--color-error);
		color: #fff;
	}

	.empty {
		text-align: center;
		color: var(--color-text-placeholder);
		padding: 1rem;
	}
</style>
