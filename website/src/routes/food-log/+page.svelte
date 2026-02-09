<script lang="ts">
	import { onMount } from 'svelte';
	import { foodLogs, type FoodLog } from '$lib/api';

	let entries: FoodLog[] = $state([]);
	let suggestions: string[] = $state([]);
	let error = $state('');
	let loading = $state(true);

	let selectedFoodItems: string[] = $state([]);
	let currentFoodItem = $state('');
	let showSuggestions = $state(false);
	let kcal = $state('');
	let carbohydrates = $state('');
	let dateTime = $state(new Date().toISOString().slice(0, 16));
	let notes = $state('');

	let filteredSuggestions = $derived(
		currentFoodItem.trim()
			? suggestions.filter(s =>
				s.toLowerCase().includes(currentFoodItem.trim().toLowerCase()) &&
				!selectedFoodItems.some(sel => sel.toLowerCase() === s.toLowerCase())
			)
			: []
	);

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

	function addFoodItem(item: string) {
		const trimmed = item.trim();
		if (trimmed && !selectedFoodItems.some(s => s.toLowerCase() === trimmed.toLowerCase())) {
			selectedFoodItems = [...selectedFoodItems, trimmed];
		}
		currentFoodItem = '';
		showSuggestions = false;
	}

	function removeFoodItem(index: number) {
		selectedFoodItems = selectedFoodItems.filter((_, i) => i !== index);
	}

	function handleFoodKeydown(e: KeyboardEvent) {
		if (e.key === ',' || e.key === 'Enter') {
			if (currentFoodItem.trim()) {
				e.preventDefault();
				addFoodItem(currentFoodItem);
			} else if (e.key === ',') {
				e.preventDefault();
			}
		}
		if (e.key === 'Backspace' && !currentFoodItem && selectedFoodItems.length > 0) {
			selectedFoodItems = selectedFoodItems.slice(0, -1);
		}
	}

	async function handleAdd(e: Event) {
		e.preventDefault();
		if (currentFoodItem.trim()) {
			addFoodItem(currentFoodItem);
		}
		if (selectedFoodItems.length === 0) return;
		error = '';
		try {
			const createPayload: { carbohydrates?: number; kcal?: number; dateTime: string; foodItems: string; notes?: string } = {
				dateTime: dateTime + ':00',
				foodItems: selectedFoodItems.join(', ')
			};
			if (kcal.trim()) createPayload.kcal = parseInt(kcal);
			if (carbohydrates.trim()) createPayload.carbohydrates = parseFloat(carbohydrates);
			if (notes.trim()) createPayload.notes = notes.trim();
			await foodLogs.create(createPayload);
			selectedFoodItems = [];
			currentFoodItem = '';
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
			<div class="food-items-input" role="combobox" aria-controls="food-suggestions-list" aria-expanded={showSuggestions && filteredSuggestions.length > 0}>
				{#each selectedFoodItems as item, i}
					<span class="food-chip-input">
						{item}
						<button type="button" class="chip-remove" onclick={() => removeFoodItem(i)}>&times;</button>
					</span>
				{/each}
				<input
					type="text"
					class="food-item-text"
					bind:value={currentFoodItem}
					placeholder={selectedFoodItems.length === 0 ? 'Food items (e.g. Rice, Chicken, Salad)' : 'Add more...'}
					autocomplete="off"
					onkeydown={handleFoodKeydown}
					onfocus={() => showSuggestions = true}
					onblur={() => setTimeout(() => showSuggestions = false, 200)}
				/>
				{#if showSuggestions && filteredSuggestions.length > 0}
					<ul id="food-suggestions-list" class="suggestions-dropdown" role="listbox">
						{#each filteredSuggestions as suggestion}
							<li><button type="button" onmousedown={() => addFoodItem(suggestion)}>{suggestion}</button></li>
						{/each}
					</ul>
				{/if}
			</div>
			<div class="form-row">
				<div class="form-field">
					<label for="kcal">Kcal (optional)</label>
					<input type="number" id="kcal" bind:value={kcal} placeholder="" min="0" />
				</div>
				<div class="form-field">
					<label for="carbs">Carbs in g (optional)</label>
					<input type="number" id="carbs" bind:value={carbohydrates} placeholder="" min="0" step="0.1" />
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
							<div class="entry-tags">
								{#if entry.tags && entry.tags.length > 0}
									{#each entry.tags as tag}
										<span class="food-tag">{tag.name}</span>
									{/each}
								{:else}
									<span class="entry-food">{entry.foodItems}</span>
								{/if}
							</div>
							{#if entry.kcal != null || entry.carbohydrates != null}
								<span class="entry-nutrition">{#if entry.kcal != null}{entry.kcal} kcal{/if}{#if entry.kcal != null && entry.carbohydrates != null}, {/if}{#if entry.carbohydrates != null}{entry.carbohydrates.toFixed(1)}g carbs{/if}</span>
							{/if}
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

	.food-items-input {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 0.3rem;
		border: 1px solid var(--color-border-light);
		border-radius: 4px;
		padding: 0.3rem 0.5rem;
		margin-bottom: 0.5rem;
		position: relative;
		cursor: text;
		background: var(--color-bg, #fff);
	}

	.food-items-input:focus-within {
		border-color: var(--color-primary, #4a90d9);
		outline: none;
	}

	.food-chip-input {
		display: inline-flex;
		align-items: center;
		gap: 0.2rem;
		background-color: var(--color-bg-hover, #f0f0f0);
		border: 1px solid var(--color-border-light);
		border-radius: 12px;
		padding: 0.1rem 0.4rem;
		font-size: 0.85rem;
	}

	.chip-remove {
		background: none;
		border: none;
		cursor: pointer;
		font-size: 0.9rem;
		line-height: 1;
		padding: 0 0.1rem;
		color: var(--color-text-muted);
	}

	.chip-remove:hover {
		color: var(--color-error);
	}

	.food-item-text {
		flex: 1;
		min-width: 120px;
		border: none;
		outline: none;
		padding: 0.25rem 0;
		font-size: inherit;
		background: transparent;
	}

	.suggestions-dropdown {
		position: absolute;
		top: 100%;
		left: 0;
		right: 0;
		background: var(--color-bg, #fff);
		border: 1px solid var(--color-border-light);
		border-radius: 4px;
		margin-top: 2px;
		max-height: 200px;
		overflow-y: auto;
		list-style: none;
		padding: 0;
		z-index: 10;
	}

	.suggestions-dropdown li button {
		display: block;
		width: 100%;
		padding: 0.4rem 0.6rem;
		border: none;
		background: none;
		text-align: left;
		cursor: pointer;
		font-size: 0.9rem;
	}

	.suggestions-dropdown li button:hover {
		background-color: var(--color-bg-hover, #f0f0f0);
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

	.entry-tags {
		display: flex;
		flex-wrap: wrap;
		gap: 0.3rem;
	}

	.food-tag {
		display: inline-block;
		background-color: var(--color-bg-hover, #f0f0f0);
		border: 1px solid var(--color-border-light);
		border-radius: 12px;
		padding: 0.1rem 0.5rem;
		font-size: 0.8rem;
		font-weight: 500;
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
