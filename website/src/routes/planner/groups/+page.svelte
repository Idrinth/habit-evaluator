<script lang="ts">
	import { onMount } from 'svelte';
	import { dayPlanner, type PlannerGroup, type PlannerActivity } from '$lib/api';
	import { getLanguage, t, type Language } from '$lib/i18n';

	let lang: Language = $state('en');
	let loading = $state(true);
	let error = $state('');
	let groups: PlannerGroup[] = $state([]);
	let activities: PlannerActivity[] = $state([]);

	let formVisible = $state(false);
	let editingId: string | null = $state(null);
	let formName = $state('');
	let formDescription = $state('');
	let formError = $state('');

	function activitiesForGroup(groupId: string): PlannerActivity[] {
		return activities.filter((a) => a.groups.some((g) => g.id === groupId));
	}

	async function loadGroups() {
		try {
			[groups, activities] = await Promise.all([
				dayPlanner.listGroups(),
				dayPlanner.listActivities()
			]);
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to load groups';
		} finally {
			loading = false;
		}
	}

	function resetForm() {
		formVisible = false;
		editingId = null;
		formName = '';
		formDescription = '';
		formError = '';
	}

	function startAdd() {
		resetForm();
		formVisible = true;
	}

	function startEdit(group: PlannerGroup) {
		editingId = group.id;
		formName = group.name;
		formDescription = group.description ?? '';
		formError = '';
		formVisible = true;
	}

	async function saveGroup() {
		if (!formName.trim()) {
			formError = t('planner.groupNameRequired', lang);
			return;
		}
		try {
			const payload = {
				name: formName.trim(),
				description: formDescription.trim() || null
			};
			if (editingId) {
				await dayPlanner.updateGroup(editingId, payload);
			} else {
				await dayPlanner.createGroup(payload);
			}
			resetForm();
			await loadGroups();
		} catch (err) {
			formError = err instanceof Error ? err.message : 'Failed to save group';
		}
	}

	async function deleteGroup(id: string) {
		try {
			await dayPlanner.deleteGroup(id);
			await loadGroups();
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to delete group';
		}
	}

	onMount(() => {
		lang = getLanguage();
		loadGroups();
	});
</script>

<div class="container groups-container">
	<h1>{t('planner.groups', lang)}</h1>
	<p class="description">{t('planner.groupsDescription', lang)}</p>

	{#if error}
		<p class="error">{error}</p>
	{/if}

	{#if loading}
		<p class="loading">{t('planner.groupsLoading', lang)}</p>
	{:else}
		<button class="add-btn" onclick={startAdd}>{t('planner.addGroup', lang)}</button>

		{#if formVisible}
			<div class="form-card">
				<h2>{editingId ? t('planner.editGroup', lang) : t('planner.addGroup', lang)}</h2>
				{#if formError}
					<p class="form-error">{formError}</p>
				{/if}
				<label class="form-label">
					{t('planner.groupName', lang)}
					<input type="text" bind:value={formName} class="form-input" />
				</label>
				<label class="form-label">
					{t('planner.groupDescription', lang)}
					<textarea bind:value={formDescription} class="form-input" rows="2"></textarea>
				</label>
				<div class="form-actions">
					<button class="btn-save" onclick={saveGroup}>{t('planner.saveGroup', lang)}</button>
					<button class="btn-cancel" onclick={resetForm}>{t('planner.cancelGroup', lang)}</button>
				</div>
			</div>
		{/if}

		{#if groups.length === 0}
			<p class="empty">{t('planner.noGroups', lang)}</p>
		{:else}
			<ul class="group-list">
				{#each groups as group (group.id)}
					{@const groupActivities = activitiesForGroup(group.id)}
					<li class="group-item">
						<div class="group-content">
							<div class="group-info">
								<strong>{group.name}</strong>
								{#if group.description}
									<span class="group-desc">{group.description}</span>
								{/if}
							</div>
							{#if groupActivities.length > 0}
								<div class="group-activities">
									{#each groupActivities as activity (activity.id)}
										<span class="activity-tag">{activity.name}</span>
									{/each}
								</div>
							{:else}
								<span class="no-activities">{t('planner.noGroupActivities', lang)}</span>
							{/if}
						</div>
						<div class="group-actions">
							<button class="btn-edit" onclick={() => startEdit(group)}>{t('planner.editGroup', lang)}</button>
							<button class="btn-delete" onclick={() => deleteGroup(group.id)}>{t('planner.deleteGroup', lang)}</button>
						</div>
					</li>
				{/each}
			</ul>
		{/if}
	{/if}

	<a href="/planner" class="back-link">{t('planner.groupBackToPlanner', lang)}</a>
</div>

<style>
	.groups-container {
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

	.group-list {
		list-style: none;
		padding: 0;
		margin: 0;
	}

	.group-item {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		border: 1px solid var(--color-border-light);
		border-radius: 8px;
		padding: 0.75rem 1rem;
		margin-bottom: 0.5rem;
	}

	.group-content {
		display: flex;
		flex-direction: column;
		gap: 0.4rem;
		flex: 1;
		min-width: 0;
	}

	.group-info {
		display: flex;
		flex-direction: column;
		gap: 0.15rem;
	}

	.group-desc {
		font-size: 0.8rem;
		color: var(--color-text-muted);
	}

	.group-activities {
		display: flex;
		flex-wrap: wrap;
		gap: 0.3rem;
	}

	.activity-tag {
		display: inline-block;
		padding: 0.15rem 0.5rem;
		background: var(--color-primary, #4a90d9);
		color: #fff;
		border-radius: 12px;
		font-size: 0.75rem;
		line-height: 1.4;
	}

	.no-activities {
		font-size: 0.8rem;
		color: var(--color-text-placeholder);
		font-style: italic;
	}

	.group-actions {
		display: flex;
		gap: 0.5rem;
		flex-shrink: 0;
		margin-left: 0.5rem;
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
