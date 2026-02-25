<script lang="ts">
	import { onMount } from 'svelte';
	import { dayPlanner, type PlannerActivity, type PlannerGroup } from '$lib/api';
	import { getLanguage, t, type Language } from '$lib/i18n';

	let lang: Language = $state('en');
	let loading = $state(true);
	let error = $state('');
	let activities: PlannerActivity[] = $state([]);
	let allGroups: PlannerGroup[] = $state([]);

	let formVisible = $state(false);
	let editingId: string | null = $state(null);
	let formName = $state('');
	let formDescription = $state('');
	let selectedGroupIds: Set<string> = $state(new Set());
	let formError = $state('');

	async function loadData() {
		try {
			const [loadedActivities, loadedGroups] = await Promise.all([
				dayPlanner.listActivities(),
				dayPlanner.listGroups()
			]);
			activities = loadedActivities;
			allGroups = loadedGroups;
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to load data';
		} finally {
			loading = false;
		}
	}

	function resetForm() {
		formVisible = false;
		editingId = null;
		formName = '';
		formDescription = '';
		selectedGroupIds = new Set();
		formError = '';
	}

	function startAdd() {
		resetForm();
		formVisible = true;
	}

	function startEdit(activity: PlannerActivity) {
		editingId = activity.id;
		formName = activity.name;
		formDescription = activity.description ?? '';
		selectedGroupIds = new Set(activity.groups?.map((g) => g.id) ?? []);
		formError = '';
		formVisible = true;
	}

	function toggleGroup(groupId: string) {
		const next = new Set(selectedGroupIds);
		if (next.has(groupId)) {
			next.delete(groupId);
		} else {
			next.add(groupId);
		}
		selectedGroupIds = next;
	}

	async function saveActivity() {
		if (!formName.trim()) {
			formError = t('planner.activityNameRequired', lang);
			return;
		}
		try {
			const payload = {
				name: formName.trim(),
				description: formDescription.trim() || null,
				groups: Array.from(selectedGroupIds).map((id) => ({ id }))
			};
			if (editingId) {
				await dayPlanner.updateActivity(editingId, payload);
			} else {
				await dayPlanner.createActivity(payload);
			}
			resetForm();
			await loadData();
		} catch (err) {
			formError = err instanceof Error ? err.message : 'Failed to save activity';
		}
	}

	async function deleteActivity(id: string) {
		try {
			await dayPlanner.deleteActivity(id);
			await loadData();
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to delete activity';
		}
	}

	function formatGroupNames(activity: PlannerActivity): string {
		return activity.groups?.map((g) => g.name).join(', ') ?? '';
	}

	onMount(() => {
		lang = getLanguage();
		loadData();
	});
</script>

<div class="container activities-container">
	<h1>{t('planner.activities', lang)}</h1>
	<p class="description">{t('planner.activitiesDescription', lang)}</p>

	{#if error}
		<p class="error">{error}</p>
	{/if}

	{#if loading}
		<p class="loading">{t('planner.activitiesLoading', lang)}</p>
	{:else}
		<button class="add-btn" onclick={startAdd}>{t('planner.addActivity', lang)}</button>

		{#if formVisible}
			<div class="form-card">
				<h2>{editingId ? t('planner.editActivity', lang) : t('planner.addActivity', lang)}</h2>
				{#if formError}
					<p class="form-error">{formError}</p>
				{/if}
				<label class="form-label">
					{t('planner.activityName', lang)}
					<input type="text" bind:value={formName} class="form-input" />
				</label>
				<label class="form-label">
					{t('planner.activityDescription', lang)}
					<textarea bind:value={formDescription} class="form-input" rows="2"></textarea>
				</label>
				{#if allGroups.length > 0}
					<div class="form-label">
						{t('planner.activityGroups', lang)}
						<div class="chip-row">
							{#each allGroups as group (group.id)}
								<button
									class="chip"
									class:selected={selectedGroupIds.has(group.id)}
									onclick={() => toggleGroup(group.id)}
								>
									{group.name}
								</button>
							{/each}
						</div>
					</div>
				{/if}
				<div class="form-actions">
					<button class="btn-save" onclick={saveActivity}>{t('planner.saveActivity', lang)}</button>
					<button class="btn-cancel" onclick={resetForm}>{t('planner.cancelActivity', lang)}</button>
				</div>
			</div>
		{/if}

		{#if activities.length === 0}
			<p class="empty">{t('planner.noActivities', lang)}</p>
		{:else}
			<ul class="activity-list">
				{#each activities as activity (activity.id)}
					{@const groupNames = formatGroupNames(activity)}
					<li class="activity-item">
						<div class="activity-info">
							<strong>{activity.name}</strong>
							{#if activity.description}
								<span class="activity-desc">{activity.description}</span>
							{/if}
							{#if groupNames}
								<span class="activity-groups">{t('planner.activityGroups', lang)}: {groupNames}</span>
							{/if}
						</div>
						<div class="activity-actions">
							<button class="btn-edit" onclick={() => startEdit(activity)}>{t('planner.editActivity', lang)}</button>
							<button class="btn-delete" onclick={() => deleteActivity(activity.id)}>{t('planner.deleteActivity', lang)}</button>
						</div>
					</li>
				{/each}
			</ul>
		{/if}
	{/if}

	<a href="/planner" class="back-link">{t('planner.activityBackToPlanner', lang)}</a>
</div>

<style>
	.activities-container {
		max-width: 650px;
	}

	.description {
		font-size: 0.9rem;
		color: var(--color-text-muted);
		margin-bottom: 1rem;
	}

	.loading,
	.empty {
		text-align: center;
		color: var(--color-text-placeholder);
		padding: 1rem;
	}

	.add-btn {
		margin-bottom: 1rem;
		padding: 0.5rem 1rem;
		background: var(--color-primary, #4a90d9);
		color: #fff;
		border: none;
		border-radius: 6px;
		cursor: pointer;
		font-size: 0.9rem;
	}

	.add-btn:hover {
		opacity: 0.9;
	}

	.form-card {
		border: 1px solid var(--color-border-light);
		border-radius: 8px;
		padding: 1rem;
		margin-bottom: 1rem;
	}

	.form-card h2 {
		margin: 0 0 0.75rem 0;
		font-size: 1rem;
	}

	.form-error {
		color: var(--color-error, #d32f2f);
		font-size: 0.85rem;
		margin-bottom: 0.5rem;
	}

	.form-label {
		display: block;
		margin-bottom: 0.75rem;
		font-size: 0.85rem;
		font-weight: 500;
	}

	.form-input {
		display: block;
		width: 100%;
		margin-top: 0.25rem;
		padding: 0.4rem 0.5rem;
		border: 1px solid var(--color-border-light);
		border-radius: 4px;
		font-size: 0.9rem;
		box-sizing: border-box;
	}

	.chip-row {
		display: flex;
		flex-wrap: wrap;
		gap: 0.4rem;
		margin-top: 0.25rem;
	}

	.chip {
		padding: 0.25rem 0.6rem;
		border: 1px solid var(--color-border-light);
		border-radius: 16px;
		background: transparent;
		cursor: pointer;
		font-size: 0.8rem;
		transition: all 0.15s;
	}

	.chip.selected {
		background: var(--color-primary, #4a90d9);
		color: #fff;
		border-color: var(--color-primary, #4a90d9);
	}

	.form-actions {
		display: flex;
		gap: 0.5rem;
	}

	.btn-save {
		padding: 0.4rem 0.8rem;
		background: var(--color-primary, #4a90d9);
		color: #fff;
		border: none;
		border-radius: 4px;
		cursor: pointer;
		font-size: 0.85rem;
	}

	.btn-cancel {
		padding: 0.4rem 0.8rem;
		background: transparent;
		border: 1px solid var(--color-border-light);
		border-radius: 4px;
		cursor: pointer;
		font-size: 0.85rem;
	}

	.activity-list {
		list-style: none;
		padding: 0;
		margin: 0;
	}

	.activity-item {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		border: 1px solid var(--color-border-light);
		border-radius: 8px;
		padding: 0.75rem 1rem;
		margin-bottom: 0.5rem;
	}

	.activity-info {
		display: flex;
		flex-direction: column;
		gap: 0.15rem;
	}

	.activity-desc {
		font-size: 0.8rem;
		color: var(--color-text-muted);
	}

	.activity-groups {
		font-size: 0.8rem;
		color: var(--color-text-muted);
	}

	.activity-actions {
		display: flex;
		gap: 0.5rem;
		flex-shrink: 0;
		margin-left: 1rem;
	}

	.btn-edit {
		padding: 0.3rem 0.6rem;
		background: transparent;
		border: 1px solid var(--color-primary, #4a90d9);
		color: var(--color-primary, #4a90d9);
		border-radius: 4px;
		cursor: pointer;
		font-size: 0.8rem;
	}

	.btn-delete {
		padding: 0.3rem 0.6rem;
		background: transparent;
		border: 1px solid var(--color-error, #d32f2f);
		color: var(--color-error, #d32f2f);
		border-radius: 4px;
		cursor: pointer;
		font-size: 0.8rem;
	}

	.back-link {
		display: inline-block;
		margin-top: 1rem;
		font-size: 0.9rem;
		color: var(--color-primary, #4a90d9);
	}

	.error {
		color: var(--color-error, #d32f2f);
		font-size: 0.9rem;
	}
</style>
