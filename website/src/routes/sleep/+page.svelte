<script lang="ts">
	import { onMount } from 'svelte';
	import { sleepEntries, type SleepEntry, type SleepStats } from '$lib/api';

	let entries: SleepEntry[] = $state([]);
	let weeklyStats: SleepStats | null = $state(null);
	let monthlyStats: SleepStats | null = $state(null);

	let date = $state(new Date().toISOString().split('T')[0]);
	let fromTime = $state('22:00');
	let untilTime = $state('06:00');
	let notes = $state('');
	let error = $state('');
	let success = $state('');
	let loading = $state(true);

	onMount(async () => {
		await loadData();
	});

	async function loadData() {
		try {
			const [entryList, statsData] = await Promise.all([
				sleepEntries.list(),
				sleepEntries.stats()
			]);
			entries = entryList.sort((a, b) => {
				const dateCmp = b.date.localeCompare(a.date);
				if (dateCmp !== 0) return dateCmp;
				return (b.fromTime || '').localeCompare(a.fromTime || '');
			});
			weeklyStats = statsData.weekly;
			monthlyStats = statsData.monthly;
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to load sleep data';
		} finally {
			loading = false;
		}
	}

	async function handleSubmit(e: Event) {
		e.preventDefault();
		error = '';
		success = '';

		if (!date || !fromTime || !untilTime) {
			error = 'Date, from time, and until time are required';
			return;
		}

		try {
			await sleepEntries.create({
				fromTime: fromTime + ':00',
				untilTime: untilTime + ':00',
				date,
				notes: notes || undefined
			});
			success = 'Sleep entry added successfully';
			notes = '';
			await loadData();
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to add sleep entry';
		}
	}

	async function handleDelete(id: string) {
		error = '';
		success = '';
		try {
			await sleepEntries.delete(id);
			success = 'Entry deleted';
			await loadData();
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to delete entry';
		}
	}

	function formatTime(time: string): string {
		if (!time) return '';
		return time.substring(0, 5);
	}

	function formatHours(hours: number): string {
		return hours.toFixed(1);
	}
</script>

<div class="container">
	<h1>Sleep Tracking</h1>

	{#if error}
		<p class="error">{error}</p>
	{/if}
	{#if success}
		<p class="success">{success}</p>
	{/if}

	<form onsubmit={handleSubmit}>
		<div class="form-row">
			<div class="form-field">
				<label for="date">Date</label>
				<input type="date" id="date" bind:value={date} required />
			</div>
			<div class="form-field">
				<label for="fromTime">From</label>
				<input type="time" id="fromTime" bind:value={fromTime} required />
			</div>
			<div class="form-field">
				<label for="untilTime">Until</label>
				<input type="time" id="untilTime" bind:value={untilTime} required />
			</div>
		</div>
		<label for="notes">Notes</label>
		<textarea id="notes" bind:value={notes} maxlength="500" placeholder="Optional notes"></textarea>
		<button type="submit">Add Sleep Entry</button>
	</form>

	<Separator />

	{#if loading}
		<p style="text-align: center; color: var(--color-text-placeholder);">Loading...</p>
	{:else}
		<h2>Statistics</h2>
		<div class="stats-grid">
			<div class="stats-card">
				<h3>This Week</h3>
				{#if weeklyStats && weeklyStats.totalEntries > 0}
					<div class="stat-row"><span>Average:</span> <span>{formatHours(weeklyStats.averageHours)} hrs</span></div>
					<div class="stat-row"><span>Min:</span> <span>{formatHours(weeklyStats.minHours)} hrs</span></div>
					<div class="stat-row"><span>Max:</span> <span>{formatHours(weeklyStats.maxHours)} hrs</span></div>
					<div class="stat-row"><span>Entries:</span> <span>{weeklyStats.totalEntries}</span></div>
				{:else}
					<p class="no-data">No data this week</p>
				{/if}
			</div>
			<div class="stats-card">
				<h3>This Month</h3>
				{#if monthlyStats && monthlyStats.totalEntries > 0}
					<div class="stat-row"><span>Average:</span> <span>{formatHours(monthlyStats.averageHours)} hrs</span></div>
					<div class="stat-row"><span>Min:</span> <span>{formatHours(monthlyStats.minHours)} hrs</span></div>
					<div class="stat-row"><span>Max:</span> <span>{formatHours(monthlyStats.maxHours)} hrs</span></div>
					<div class="stat-row"><span>Entries:</span> <span>{monthlyStats.totalEntries}</span></div>
				{:else}
					<p class="no-data">No data this month</p>
				{/if}
			</div>
		</div>

		<h2>Sleep Entries</h2>
		{#if entries.length > 0}
			<div class="entry-list">
				{#each entries as entry (entry.id)}
					<div class="entry-card">
						<div class="entry-info">
							<span class="entry-date">{entry.date}</span>
							<span class="entry-time">{formatTime(entry.fromTime)} - {formatTime(entry.untilTime)}</span>
							<span class="entry-hours">{formatHours(entry.hours)} hrs</span>
							{#if entry.notes}
								<span class="entry-notes">{entry.notes}</span>
							{/if}
						</div>
						<button class="delete-btn" onclick={() => handleDelete(entry.id)}>Delete</button>
					</div>
				{/each}
			</div>
		{:else}
			<p class="empty">No sleep entries yet. Add your first entry above.</p>
		{/if}
	{/if}
</div>

<style>
	.form-row {
		display: flex;
		gap: 1rem;
		flex-wrap: wrap;
	}

	.form-field {
		display: flex;
		flex-direction: column;
		flex: 1;
		min-width: 120px;
	}

	.form-field label {
		margin-bottom: 0.25rem;
	}

	.stats-grid {
		display: flex;
		gap: 1rem;
		flex-wrap: wrap;
		margin-bottom: 1.5rem;
	}

	.stats-card {
		flex: 1;
		min-width: 200px;
		border: 1px solid var(--color-border-light);
		border-radius: 4px;
		padding: 1rem;
	}

	.stats-card h3 {
		margin: 0 0 0.5rem 0;
		font-size: 1rem;
	}

	.stat-row {
		display: flex;
		justify-content: space-between;
		padding: 0.2rem 0;
		font-size: 0.9rem;
	}

	.no-data {
		color: var(--color-text-placeholder);
		font-size: 0.9rem;
	}

	.entry-list {
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
	}

	.entry-card {
		display: flex;
		justify-content: space-between;
		align-items: center;
		border: 1px solid var(--color-border-light);
		border-radius: 4px;
		padding: 0.75rem;
	}

	.entry-card:hover {
		background-color: var(--color-bg-hover);
	}

	.entry-info {
		display: flex;
		align-items: center;
		gap: 1rem;
		flex-wrap: wrap;
	}

	.entry-date {
		font-weight: bold;
	}

	.entry-time {
		color: var(--color-text-muted);
	}

	.entry-hours {
		font-weight: bold;
		color: var(--color-text);
	}

	.entry-notes {
		font-size: 0.85rem;
		color: var(--color-text-muted);
	}

	.delete-btn {
		padding: 0.25rem 0.5rem;
		font-size: 0.8rem;
		background: #c0392b;
		color: white;
		border: none;
		border-radius: 4px;
		cursor: pointer;
	}

	.delete-btn:hover {
		background: #e74c3c;
	}

	.empty {
		text-align: center;
		color: var(--color-text-placeholder);
		padding: 1rem;
	}

	h2 {
		font-size: 1.1rem;
		margin: 1.5rem 0 0.75rem 0;
	}
</style>
