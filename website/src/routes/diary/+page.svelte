<script lang="ts">
	import { onMount } from 'svelte';
	import { diary, type DiaryEntry, type DiaryStats } from '$lib/api';

	let entries: DiaryEntry[] = $state([]);
	let stats: DiaryStats | null = $state(null);
	let suggestions: string[] = $state([]);
	let error = $state('');
	let loading = $state(true);

	let description = $state('');
	let eventDate = $state(new Date().toISOString().split('T')[0]);
	let significance = $state('NORMAL');

	const POINTS: Record<string, number> = { MINOR: 1, NORMAL: 2, MAJOR: 4 };

	onMount(async () => {
		await loadData();
	});

	async function loadData() {
		loading = true;
		error = '';
		try {
			const [e, s, sug] = await Promise.all([diary.list(), diary.stats(), diary.suggestions()]);
			entries = e.sort(
				(a, b) => b.eventDate.localeCompare(a.eventDate) || b.createdAt.localeCompare(a.createdAt)
			);
			stats = s;
			suggestions = sug || [];
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to load diary';
		} finally {
			loading = false;
		}
	}

	async function handleAdd(e: Event) {
		e.preventDefault();
		if (!description.trim()) return;
		error = '';
		try {
			await diary.create({ description: description.trim(), significance, eventDate });
			description = '';
			eventDate = new Date().toISOString().split('T')[0];
			significance = 'NORMAL';
			await loadData();
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to add entry';
		}
	}

	async function handleDelete(id: string) {
		try {
			await diary.remove(id);
			await loadData();
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to delete entry';
		}
	}

	function formatTrend(trend: number): string {
		if (trend > 0.01) return `+${Math.round(trend * 100)}%`;
		if (trend < -0.01) return `${Math.round(trend * 100)}%`;
		return 'Stable';
	}
</script>

<div class="container diary-container">
	<h1>Diary / Journal</h1>

	{#if error}
		<p class="error">{error}</p>
	{/if}

	{#if loading}
		<p style="text-align: center; color: var(--color-text-placeholder);">Loading...</p>
	{:else}
		{#if stats}
			<div class="stats-grid">
				<div class="stat-card">
					<span class="stat-value">{stats.todayPoints}</span>
					<span class="stat-label">Today</span>
				</div>
				<div class="stat-card">
					<span class="stat-value">{stats.weekPoints}</span>
					<span class="stat-label">This Week</span>
				</div>
				<div class="stat-card">
					<span class="stat-value">{stats.monthPoints}</span>
					<span class="stat-label">This Month</span>
				</div>
				<div class="stat-card">
					<span class="stat-value">{stats.weeklyAverage.toFixed(1)}</span>
					<span class="stat-label">Weekly Avg</span>
				</div>
				<div class="stat-card">
					<span class="stat-value {stats.monthlyTrend > 0.01 ? 'trend-up' : stats.monthlyTrend < -0.01 ? 'trend-down' : ''}">{formatTrend(stats.monthlyTrend)}</span>
					<span class="stat-label">Trend</span>
				</div>
			</div>
		{/if}

		<form class="add-form" onsubmit={handleAdd}>
			<input type="text" bind:value={description} placeholder="Positive event description" required list="event-suggestions" autocomplete="off" />
			<datalist id="event-suggestions">
				{#each suggestions as suggestion}
					<option value={suggestion}></option>
				{/each}
			</datalist>
			<div class="form-row">
				<input type="date" bind:value={eventDate} />
				<select bind:value={significance}>
					<option value="MINOR">Minor (1pt)</option>
					<option value="NORMAL">Normal (2pt)</option>
					<option value="MAJOR">Major (4pt)</option>
				</select>
				<button type="submit">Add Event</button>
			</div>
		</form>

		{#if entries.length === 0}
			<p class="empty">No diary entries yet. Add a positive event above.</p>
		{:else}
			<div class="entries-list">
				{#each entries as entry (entry.id)}
					<div class="entry-card">
						<div class="entry-main">
							<span class="entry-date">{entry.eventDate}</span>
							<span class="entry-significance">{entry.significance} ({POINTS[entry.significance]}pt)</span>
							<span class="entry-description">{entry.description}</span>
						</div>
						<button class="delete-btn" onclick={() => handleDelete(entry.id)}>X</button>
					</div>
				{/each}
			</div>
		{/if}
	{/if}
</div>

<style>
	.diary-container {
		max-width: 600px;
	}

	.stats-grid {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(90px, 1fr));
		gap: 0.5rem;
		margin-bottom: 1.5rem;
	}

	.stat-card {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 0.75rem 0.5rem;
		border: 1px solid var(--color-border-light);
		border-radius: 6px;
	}

	.stat-value {
		font-size: 1.25rem;
		font-weight: bold;
		color: var(--color-text);
	}

	.stat-label {
		font-size: 0.75rem;
		color: var(--color-text-muted);
		margin-top: 0.2rem;
	}

	.trend-up {
		color: var(--color-success);
	}

	.trend-down {
		color: var(--color-error);
	}

	.add-form {
		margin-bottom: 1.5rem;
	}

	.add-form input[type='text'] {
		width: 100%;
		margin-bottom: 0.5rem;
	}

	.form-row {
		display: flex;
		gap: 0.5rem;
		align-items: center;
	}

	.form-row input[type='date'] {
		flex: 1;
		margin-bottom: 0;
	}

	.form-row select {
		flex: 1;
		margin-bottom: 0;
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
		align-items: center;
		gap: 0.75rem;
		flex: 1;
		min-width: 0;
	}

	.entry-date {
		font-size: 0.8rem;
		color: var(--color-text-muted);
		white-space: nowrap;
	}

	.entry-significance {
		font-size: 0.8rem;
		color: var(--color-text-secondary);
		white-space: nowrap;
	}

	.entry-description {
		flex: 1;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
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
