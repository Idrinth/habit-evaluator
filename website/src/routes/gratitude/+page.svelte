<script lang="ts">
	import { onMount } from 'svelte';
	import { gratitude, type GratitudeEntry, type GratitudeStats } from '$lib/api';
	import { getLanguage, t } from '$lib/i18n';

	let entries: GratitudeEntry[] = $state([]);
	let stats: GratitudeStats | null = $state(null);
	let error = $state('');
	let loading = $state(true);

	let description = $state('');
	let reason = $state('');
	let eventDate = $state(new Date().toISOString().split('T')[0]);

	const lang = getLanguage();

	const prompts = [
		() => t('gratitude.promptGrateful', lang),
		() => t('gratitude.promptThankful', lang)
	];

	onMount(async () => {
		await loadData();
	});

	async function loadData() {
		loading = true;
		error = '';
		try {
			const [e, s] = await Promise.all([gratitude.list(), gratitude.stats()]);
			entries = e.sort(
				(a, b) => b.eventDate.localeCompare(a.eventDate) || b.createdAt.localeCompare(a.createdAt)
			);
			stats = s;
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to load gratitude entries';
		} finally {
			loading = false;
		}
	}

	async function handleAdd(e: Event) {
		e.preventDefault();
		if (!description.trim()) return;
		error = '';
		const today = new Date().toISOString().split('T')[0];
		const clampedDate = eventDate > today ? today : eventDate;
		try {
			const createPayload: { description: string; reason?: string; eventDate: string } = {
				description: description.trim(),
				eventDate: clampedDate
			};
			if (reason.trim()) {
				createPayload.reason = reason.trim();
			}
			await gratitude.create(createPayload);
			description = '';
			reason = '';
			eventDate = new Date().toISOString().split('T')[0];
			await loadData();
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to add entry';
		}
	}

	async function handleDelete(id: string) {
		try {
			await gratitude.delete(id);
			await loadData();
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to delete entry';
		}
	}

	function applyPrompt(prompt: string) {
		description = prompt;
	}
</script>

<div class="container gratitude-container">
	<h1>{t('gratitude.title', lang)}</h1>

	{#if error}
		<p class="error">{error}</p>
	{/if}

	{#if loading}
		<p style="text-align: center; color: var(--color-text-placeholder);">{t('gratitude.loading', lang)}</p>
	{:else}
		{#if stats}
			<div class="stats-grid">
				<div class="stat-card">
					<span class="stat-value">{stats.todayCount}</span>
					<span class="stat-label">{t('gratitude.todayCount', lang)}</span>
				</div>
				<div class="stat-card">
					<span class="stat-value">{stats.weekCount}</span>
					<span class="stat-label">{t('gratitude.weekCount', lang)}</span>
				</div>
				<div class="stat-card">
					<span class="stat-value">{stats.monthCount}</span>
					<span class="stat-label">{t('gratitude.monthCount', lang)}</span>
				</div>
				<div class="stat-card">
					<span class="stat-value">{(stats.dailyAverage ?? 0).toFixed(1)}</span>
					<span class="stat-label">{t('gratitude.dailyAverage', lang)}</span>
				</div>
				<div class="stat-card">
					<span class="stat-value">{stats.currentStreak}</span>
					<span class="stat-label">{t('gratitude.currentStreak', lang)} ({t('gratitude.streakDays', lang)})</span>
				</div>
			</div>
		{/if}

		<div class="prompt-suggestions">
			{#each prompts as prompt}
				<button class="prompt-btn" onclick={() => applyPrompt(prompt())}>{prompt()}</button>
			{/each}
		</div>

		<form class="add-form" onsubmit={handleAdd}>
			<input type="text" bind:value={description} placeholder={t('gratitude.placeholder', lang)} required autocomplete="off" />
			<input type="text" bind:value={reason} placeholder={t('gratitude.reasonPlaceholder', lang)} autocomplete="off" />
			<div class="form-row">
				<input type="date" bind:value={eventDate} />
				<button type="submit">{t('gratitude.addEntry', lang)}</button>
			</div>
		</form>

		{#if entries.length === 0}
			<p class="empty">{t('gratitude.noEntries', lang)}</p>
		{:else}
			<div class="entries-list">
				{#each entries as entry (entry.id)}
					<div class="entry-card">
						<div class="entry-main">
							<span class="entry-date">{entry.eventDate}</span>
							<div class="entry-text">
								<span class="entry-description">{entry.description}</span>
								{#if entry.reason}
									<span class="entry-reason">{t('gratitude.because', lang)} {entry.reason}</span>
								{/if}
							</div>
						</div>
						<button class="delete-btn" aria-label={t('gratitude.deleteEntry', lang)} onclick={() => handleDelete(entry.id)}>X</button>
					</div>
				{/each}
			</div>
		{/if}
	{/if}
</div>

<style>
	.gratitude-container {
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
		text-align: center;
	}

	.prompt-suggestions {
		display: flex;
		gap: 0.5rem;
		margin-bottom: 1rem;
		flex-wrap: wrap;
	}

	.prompt-btn {
		background: transparent;
		border: 1px solid var(--color-border-light);
		border-radius: 16px;
		padding: 0.3rem 0.75rem;
		font-size: 0.85rem;
		color: var(--color-text-secondary);
		cursor: pointer;
	}

	.prompt-btn:hover {
		background-color: var(--color-bg-hover);
		border-color: var(--color-text-secondary);
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

	.entry-text {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
	}

	.entry-description {
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.entry-reason {
		font-size: 0.85rem;
		color: var(--color-text-muted);
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
