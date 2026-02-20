<script lang="ts">
	import { onMount } from 'svelte';
	import { activityLogs, type ActivityLog } from '$lib/api';

	let entries: ActivityLog[] = $state([]);
	let error = $state('');
	let loading = $state(true);

	let persons = $state('');
	let location = $state('');
	let startTime = $state('');
	let endTime = $state('');
	let date = $state(new Date().toISOString().split('T')[0]);
	let activity = $state('');

	let editingId: string | null = $state(null);

	onMount(async () => {
		await loadData();
	});

	async function loadData() {
		loading = true;
		error = '';
		try {
			const e = await activityLogs.list();
			entries = e.sort(
				(a, b) => b.date.localeCompare(a.date) || b.startTime.localeCompare(a.startTime) || b.createdAt.localeCompare(a.createdAt)
			);
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to load activity log';
		} finally {
			loading = false;
		}
	}

	function startEdit(entry: ActivityLog) {
		editingId = entry.id;
		persons = entry.persons;
		location = entry.location;
		startTime = entry.startTime;
		endTime = entry.endTime;
		date = entry.date;
		activity = entry.activity ?? '';
	}

	function cancelEdit() {
		editingId = null;
		persons = '';
		location = '';
		startTime = '';
		endTime = '';
		date = new Date().toISOString().split('T')[0];
		activity = '';
	}

	async function handleSubmit(e: Event) {
		e.preventDefault();
		if (!persons.trim() || !location.trim() || !startTime || !endTime || !date) return;
		error = '';
		try {
			const payload: { persons: string; location: string; startTime: string; endTime: string; date: string; activity?: string } = {
				persons: persons.trim(),
				location: location.trim(),
				startTime,
				endTime,
				date
			};
			if (activity.trim()) payload.activity = activity.trim();
			if (editingId) {
				await activityLogs.update(editingId, payload);
			} else {
				await activityLogs.create(payload);
			}
			cancelEdit();
			await loadData();
		} catch (err) {
			error = err instanceof Error ? err.message : editingId ? 'Failed to update activity log entry' : 'Failed to add activity log entry';
		}
	}

	async function handleDelete(id: string) {
		try {
			await activityLogs.delete(id);
			if (editingId === id) {
				cancelEdit();
			}
			await loadData();
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to delete entry';
		}
	}

	function formatDuration(entry: ActivityLog): string {
		if (entry.durationMinutes != null) {
			const hours = Math.floor(entry.durationMinutes / 60);
			const minutes = entry.durationMinutes % 60;
			if (hours > 0 && minutes > 0) return `${hours}h ${minutes}m`;
			if (hours > 0) return `${hours}h`;
			return `${minutes}m`;
		}
		return '';
	}
</script>

<div class="container activity-log-container">
	<h1>Activity Log</h1>

	{#if error}
		<p class="error">{error}</p>
	{/if}

	{#if loading}
		<p style="text-align: center; color: var(--color-text-placeholder);">Loading...</p>
	{:else}
		<form class="add-form" onsubmit={handleSubmit}>
			<div class="form-row">
				<div class="form-field">
					<label for="persons">Persons</label>
					<input type="text" id="persons" bind:value={persons} placeholder="Who was there" required />
				</div>
				<div class="form-field">
					<label for="location">Location</label>
					<input type="text" id="location" bind:value={location} placeholder="Where" required />
				</div>
			</div>
			<div class="form-row">
				<div class="form-field">
					<label for="date">Date</label>
					<input type="date" id="date" bind:value={date} required />
				</div>
				<div class="form-field">
					<label for="startTime">Start</label>
					<input type="time" id="startTime" bind:value={startTime} required />
				</div>
				<div class="form-field">
					<label for="endTime">End</label>
					<input type="time" id="endTime" bind:value={endTime} required />
				</div>
			</div>
			<div class="form-row">
				<input type="text" bind:value={activity} placeholder="Activity (optional)" style="flex: 1;" />
				{#if editingId}
					<button type="submit">Save</button>
					<button type="button" class="cancel-btn" onclick={cancelEdit}>Cancel</button>
				{:else}
					<button type="submit">Add Entry</button>
				{/if}
			</div>
		</form>

		{#if entries.length === 0}
			<p class="empty">No activity log entries yet. Add your first activity above.</p>
		{:else}
			<div class="entries-list">
				{#each entries as entry (entry.id)}
					<div class="entry-card" class:editing={editingId === entry.id}>
						<div class="entry-main">
							<span class="entry-persons">{entry.persons}</span>
							<span class="entry-location">{entry.location}</span>
							<span class="entry-date">{entry.date}</span>
							<span class="entry-time">{entry.startTime}-{entry.endTime}</span>
							{#if entry.durationMinutes != null}
								<span class="entry-duration">({formatDuration(entry)})</span>
							{/if}
							{#if entry.activity}
								<span class="entry-activity">{entry.activity}</span>
							{/if}
						</div>
						<div class="entry-actions">
							<button class="edit-btn" onclick={() => startEdit(entry)}>Edit</button>
							<button class="delete-btn" onclick={() => handleDelete(entry.id)}>X</button>
						</div>
					</div>
				{/each}
			</div>
		{/if}
	{/if}
</div>

<style>
	.activity-log-container {
		max-width: 650px;
	}

	.add-form {
		margin-bottom: 1.5rem;
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

	.entry-card.editing {
		border-color: var(--color-primary, #4a90d9);
		background-color: var(--color-bg-hover);
	}

	.entry-main {
		display: flex;
		align-items: center;
		gap: 0.75rem;
		flex: 1;
		min-width: 0;
	}

	.entry-persons {
		font-weight: bold;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.entry-location {
		font-size: 0.85rem;
		color: var(--color-text-secondary);
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.entry-date {
		font-size: 0.8rem;
		color: var(--color-text-muted);
		white-space: nowrap;
	}

	.entry-time {
		font-size: 0.8rem;
		color: var(--color-text-muted);
		white-space: nowrap;
	}

	.entry-duration {
		font-size: 0.75rem;
		color: var(--color-text-muted);
		white-space: nowrap;
	}

	.entry-activity {
		font-size: 0.8rem;
		color: var(--color-text-muted);
		font-style: italic;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.entry-actions {
		display: flex;
		gap: 0.25rem;
		flex-shrink: 0;
		margin-left: 0.5rem;
	}

	.edit-btn {
		background: transparent;
		color: var(--color-primary, #4a90d9);
		border: 1px solid var(--color-primary, #4a90d9);
		padding: 0.15rem 0.5rem;
		font-size: 0.75rem;
		cursor: pointer;
		border-radius: 3px;
	}

	.edit-btn:hover {
		background-color: var(--color-primary, #4a90d9);
		color: #fff;
	}

	.delete-btn {
		background: transparent;
		color: var(--color-error);
		border: 1px solid var(--color-error);
		padding: 0.15rem 0.5rem;
		font-size: 0.75rem;
		cursor: pointer;
		border-radius: 3px;
	}

	.delete-btn:hover {
		background-color: var(--color-error);
		color: #fff;
	}

	.cancel-btn {
		background: transparent;
		color: var(--color-text-muted);
		border: 1px solid var(--color-text-muted);
		padding: 0.15rem 0.5rem;
		font-size: 0.75rem;
		cursor: pointer;
		border-radius: 3px;
	}

	.cancel-btn:hover {
		background-color: var(--color-text-muted);
		color: #fff;
	}

	.empty {
		text-align: center;
		color: var(--color-text-placeholder);
		padding: 1rem;
	}
</style>
