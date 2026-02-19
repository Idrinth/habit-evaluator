<script lang="ts">
	import { onMount } from 'svelte';
	import { categories, habits, type HabitCategory, type Habit } from '$lib/api';
	import { getLanguage, t, type Language } from '$lib/i18n';

	let lang: Language = $state('en');
	let categoryList: HabitCategory[] = $state([]);
	let habitList: Habit[] = $state([]);
	let loading = $state(true);
	let error = $state('');
	let success = $state('');

	let editingId: string | null = $state(null);
	let editName = $state('');
	let editDescription = $state('');
	let editColor = $state('#4a90d9');

	let deleteId: string | null = $state(null);
	let deleteStep: 'choose' | 'confirm' = $state('choose');
	let reassignTargetId = $state('');

	let habitsPerCategory: Record<string, number> = $derived.by(() => {
		const counts: Record<string, number> = {};
		for (const habit of habitList) {
			if (habit.categoryId) {
				counts[habit.categoryId] = (counts[habit.categoryId] || 0) + 1;
			}
		}
		return counts;
	});

	function isValidColor(color: string | null | undefined): boolean {
		if (!color) return false;
		return (
			/^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$/.test(color) ||
			/^rgb\(\s*\d{1,3}\s*,\s*\d{1,3}\s*,\s*\d{1,3}\s*\)$/.test(color) ||
			/^[a-zA-Z]{1,20}$/.test(color)
		);
	}

	onMount(async () => {
		lang = getLanguage();
		try {
			const [c, h] = await Promise.all([categories.list(), habits.list()]);
			categoryList = c;
			habitList = h;
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to load data';
		} finally {
			loading = false;
		}
	});

	function startEdit(cat: HabitCategory) {
		editingId = cat.id;
		editName = cat.name;
		editDescription = cat.description ?? '';
		editColor = cat.color ?? '#4a90d9';
	}

	function cancelEdit() {
		editingId = null;
		editName = '';
		editDescription = '';
		editColor = '#4a90d9';
	}

	async function saveEdit() {
		if (!editingId) return;
		if (!editName.trim()) {
			error = t('categories.name', lang) + ' is required';
			return;
		}
		error = '';
		success = '';
		try {
			const updated = await categories.update(editingId, {
				name: editName,
				description: editDescription || undefined,
				color: editColor
			});
			categoryList = categoryList.map((c) => (c.id === updated.id ? updated : c));
			success = t('categories.saved', lang);
			cancelEdit();
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to save category';
		}
	}

	function startDelete(cat: HabitCategory) {
		deleteId = cat.id;
		deleteStep = 'choose';
		reassignTargetId = '';
		error = '';
		success = '';
	}

	function cancelDelete() {
		deleteId = null;
		deleteStep = 'choose';
		reassignTargetId = '';
	}

	async function deleteWithReassign() {
		if (!deleteId || !reassignTargetId) return;
		error = '';
		success = '';
		try {
			await categories.delete(deleteId, { reassignTo: reassignTargetId });
			categoryList = categoryList.filter((c) => c.id !== deleteId);
			habitList = habitList.map((h) =>
				h.categoryId === deleteId ? { ...h, categoryId: reassignTargetId } : h
			);
			success = t('categories.deleted', lang);
			cancelDelete();
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to delete category';
		}
	}

	async function deleteWithHabits() {
		if (!deleteId) return;
		if (deleteStep === 'choose') {
			deleteStep = 'confirm';
			return;
		}
		error = '';
		success = '';
		try {
			await categories.delete(deleteId, { confirm: true });
			const removedCategoryId = deleteId;
			categoryList = categoryList.filter((c) => c.id !== removedCategoryId);
			habitList = habitList.filter((h) => h.categoryId !== removedCategoryId);
			success = t('categories.deleted', lang);
			cancelDelete();
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to delete category';
		}
	}

	async function deleteEmpty() {
		if (!deleteId) return;
		error = '';
		success = '';
		try {
			await categories.delete(deleteId);
			categoryList = categoryList.filter((c) => c.id !== deleteId);
			success = t('categories.deleted', lang);
			cancelDelete();
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to delete category';
		}
	}
</script>

<div class="container">
	<h1>{t('categories.title', lang)}</h1>
	{#if error}
		<p class="error">{error}</p>
	{/if}
	{#if success}
		<p class="success">{success}</p>
	{/if}

	{#if loading}
		<p class="loading">{t('categories.loading', lang)}</p>
	{:else if categoryList.length === 0}
		<p class="empty">{t('categories.noCategories', lang)}</p>
	{:else}
		<div class="category-list">
			{#each categoryList as cat (cat.id)}
				<div class="category-card">
					{#if editingId === cat.id}
						<div class="edit-form">
							<label class="field">
								<span class="field-label">{t('categories.name', lang)}</span>
								<input type="text" bind:value={editName} required />
							</label>
							<label class="field">
								<span class="field-label">{t('categories.description', lang)}</span>
								<textarea bind:value={editDescription}></textarea>
							</label>
							<label class="field">
								<span class="field-label">{t('categories.color', lang)}</span>
								<input type="color" bind:value={editColor} />
							</label>
							<div class="edit-actions">
								<button class="btn-save" onclick={saveEdit}>{t('categories.save', lang)}</button>
								<button class="btn-cancel" onclick={cancelEdit}
									>{t('categories.cancel', lang)}</button
								>
							</div>
						</div>
					{:else if deleteId === cat.id}
						<div class="delete-dialog">
							<h3>{t('categories.confirmDeleteTitle', lang)}: {cat.name}</h3>
							{#if (habitsPerCategory[cat.id] || 0) > 0}
								<p class="delete-info">
									{t('categories.confirmDeleteWithHabits', lang)}
									({habitsPerCategory[cat.id]}
									{t('categories.habits', lang).toLowerCase()})
								</p>
								<div class="delete-options">
									<div class="reassign-option">
										<label class="field">
											<span class="field-label"
												>{t('categories.reassignTo', lang)}</span
											>
											<select bind:value={reassignTargetId}>
												<option value=""
													>-- {t('categories.selectCategory', lang)} --</option
												>
												{#each categoryList.filter((c) => c.id !== cat.id) as target (target.id)}
													<option value={target.id}>{target.name}</option>
												{/each}
											</select>
										</label>
										<button
											class="btn-save"
											disabled={!reassignTargetId}
											onclick={deleteWithReassign}
											>{t('categories.reassignTo', lang)}</button
										>
									</div>
									<div class="divider">or</div>
									{#if deleteStep === 'confirm'}
										<p class="confirm-warning">
											{t('categories.confirmDeleteHabits', lang)}
										</p>
									{/if}
									<button class="btn-delete" onclick={deleteWithHabits}>
										{deleteStep === 'choose'
											? t('categories.deleteHabits', lang)
											: t('categories.delete', lang)}
									</button>
								</div>
							{:else}
								<p class="delete-info">
									{t('categories.confirmDeleteTitle', lang)}?
								</p>
								<button class="btn-delete" onclick={deleteEmpty}
									>{t('categories.delete', lang)}</button
								>
							{/if}
							<button class="btn-cancel" onclick={cancelDelete}
								>{t('categories.cancel', lang)}</button
							>
						</div>
					{:else}
						<div
							class="category-header"
							style={isValidColor(cat.color)
								? `border-left: 4px solid ${cat.color}; padding-left: 0.5rem;`
								: ''}
						>
							<div class="category-info">
								<span class="category-name">{cat.name}</span>
								{#if cat.description}
									<span class="category-description">{cat.description}</span>
								{/if}
								<span class="category-habits-count">
									{habitsPerCategory[cat.id] || 0}
									{t('categories.habits', lang).toLowerCase()}
								</span>
							</div>
							<div class="category-actions">
								<button class="btn-edit" onclick={() => startEdit(cat)}
									>{t('nav.edit', lang)}</button
								>
								<button class="btn-delete-sm" onclick={() => startDelete(cat)}
									>{t('categories.delete', lang)}</button
								>
							</div>
						</div>
					{/if}
				</div>
			{/each}
		</div>
	{/if}
	<div class="add-link">
		<a href="/categories/add">{t('nav.addCategory', lang)}</a>
	</div>
</div>

<style>
	.category-list {
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
	}

	.category-card {
		border: 1px solid var(--color-border-light);
		border-radius: 4px;
		padding: 0.75rem;
	}

	.category-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.category-info {
		display: flex;
		flex-direction: column;
		gap: 0.15rem;
	}

	.category-name {
		font-weight: bold;
		color: var(--color-text);
	}

	.category-description {
		font-size: 0.85rem;
		color: var(--color-text-muted);
	}

	.category-habits-count {
		font-size: 0.8rem;
		color: var(--color-text-secondary);
	}

	.category-actions {
		display: flex;
		gap: 0.5rem;
	}

	.btn-edit {
		padding: 0.3rem 0.6rem;
		font-size: 0.85rem;
		background: var(--color-primary);
		color: white;
		border: none;
		border-radius: 4px;
		cursor: pointer;
	}

	.btn-delete-sm {
		padding: 0.3rem 0.6rem;
		font-size: 0.85rem;
		background: var(--color-error, #d32f2f);
		color: white;
		border: none;
		border-radius: 4px;
		cursor: pointer;
	}

	.edit-form {
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
	}

	.edit-form textarea {
		min-height: 60px;
		resize: vertical;
	}

	.edit-actions {
		display: flex;
		gap: 0.5rem;
	}

	.field {
		display: flex;
		flex-direction: column;
		gap: 0.15rem;
	}

	.field-label {
		font-size: 0.8rem;
		font-weight: bold;
		color: var(--color-text-secondary);
	}

	.btn-save {
		padding: 0.4rem 0.8rem;
		background: var(--color-primary);
		color: white;
		border: none;
		border-radius: 4px;
		cursor: pointer;
	}

	.btn-save:disabled {
		opacity: 0.5;
		cursor: not-allowed;
	}

	.btn-cancel {
		padding: 0.4rem 0.8rem;
		background: var(--color-border);
		color: var(--color-text);
		border: none;
		border-radius: 4px;
		cursor: pointer;
	}

	.btn-delete {
		padding: 0.4rem 0.8rem;
		background: var(--color-error, #d32f2f);
		color: white;
		border: none;
		border-radius: 4px;
		cursor: pointer;
	}

	.delete-dialog {
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
	}

	.delete-dialog h3 {
		margin: 0;
		font-size: 1rem;
	}

	.delete-info {
		color: var(--color-text-secondary);
		font-size: 0.9rem;
		margin: 0;
	}

	.delete-options {
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
	}

	.reassign-option {
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
	}

	.reassign-option select {
		padding: 0.4rem;
		border: 1px solid var(--color-border);
		border-radius: 4px;
		background: var(--color-input-bg);
		color: var(--color-text);
		font: inherit;
	}

	.divider {
		text-align: center;
		color: var(--color-text-muted);
		font-size: 0.85rem;
		padding: 0.25rem 0;
	}

	.confirm-warning {
		color: var(--color-error, #d32f2f);
		font-weight: bold;
		font-size: 0.9rem;
		margin: 0;
	}

	.loading {
		text-align: center;
		color: var(--color-text-placeholder);
	}

	.empty {
		text-align: center;
		color: var(--color-text-placeholder);
		padding: 1rem;
	}

	.add-link {
		margin-top: 1rem;
		text-align: center;
	}

	.add-link a {
		color: var(--color-primary);
	}
</style>
