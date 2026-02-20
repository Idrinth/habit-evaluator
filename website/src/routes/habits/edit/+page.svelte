<script lang="ts">
	import { onMount } from 'svelte';
	import { browser } from '$app/environment';
	import { habits, categories, type Habit, type HabitCategory } from '$lib/api';

	const LANGUAGES = ['en', 'de', 'es', 'fr'] as const;
	const LANGUAGE_LABELS: Record<string, string> = { en: 'English', de: 'Deutsch', es: 'Español', fr: 'Français' };

	let habitList: Habit[] = $state([]);
	let categoryList: HabitCategory[] = $state([]);
	let error = $state('');
	let success = $state('');
	let loading = $state(true);
	let translationsEnabled = $state(false);

	interface EditableHabit {
		id: string;
		name: string;
		description: string | null;
		targetFrequency: number;
		maxEntriesPerDay: number;
		positiveScoring: boolean;
		threshold1: number;
		threshold2: number;
		threshold4: number;
		threshold8: number;
		nameTranslations: Record<string, string>;
		descriptionTranslations: Record<string, string>;
	}

	let editableHabits: EditableHabit[] = $state([]);

	interface CategoryGroup {
		category: HabitCategory | null;
		habits: EditableHabit[];
	}

	let groupedHabits: CategoryGroup[] = $derived.by(() => {
		const categoryMap = new Map<string, HabitCategory>();
		for (const cat of categoryList) {
			categoryMap.set(cat.id, cat);
		}

		const habitCategoryMap = new Map<string, string | null>();
		for (const h of habitList) {
			habitCategoryMap.set(h.id, h.categoryId);
		}

		const groups = new Map<string, CategoryGroup>();
		const uncategorized: EditableHabit[] = [];

		for (const eh of editableHabits) {
			const catId = habitCategoryMap.get(eh.id);
			if (catId && categoryMap.has(catId)) {
				const cat = categoryMap.get(catId)!;
				if (!groups.has(cat.id)) {
					groups.set(cat.id, { category: cat, habits: [] });
				}
				groups.get(cat.id)!.habits.push(eh);
			} else {
				uncategorized.push(eh);
			}
		}

		const result: CategoryGroup[] = [...groups.values()];
		if (uncategorized.length > 0) {
			result.push({ category: null, habits: uncategorized });
		}
		return result;
	});

	function isValidColor(color: string | null | undefined): boolean {
		if (!color) return false;
		return /^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$/.test(color)
			|| /^rgb\(\s*\d{1,3}\s*,\s*\d{1,3}\s*,\s*\d{1,3}\s*\)$/.test(color)
			|| /^[a-zA-Z]{1,20}$/.test(color);
	}

	onMount(async () => {
		if (browser) {
			translationsEnabled = localStorage.getItem('customTranslations') === 'true';
		}
		try {
			const [h, c] = await Promise.all([habits.list(), categories.list()]);
			habitList = h;
			categoryList = c;
			editableHabits = h.map((habit) => ({
				id: habit.id,
				name: habit.name,
				description: habit.description,
				targetFrequency: habit.targetFrequency,
				maxEntriesPerDay: habit.maxEntriesPerDay,
				positiveScoring: habit.positiveScoring,
				threshold1: habit.scoringRule?.thresholdFor1Point ?? 1,
				threshold2: habit.scoringRule?.thresholdFor2Points ?? 2,
				threshold4: habit.scoringRule?.thresholdFor4Points ?? 4,
				threshold8: habit.scoringRule?.thresholdFor8Points ?? 7,
				nameTranslations: { ...habit.nameTranslations },
				descriptionTranslations: { ...habit.descriptionTranslations }
			}));
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to load habits';
		} finally {
			loading = false;
		}
	});

	function cleanTranslations(translations: Record<string, string>): Record<string, string> {
		const result: Record<string, string> = {};
		for (const [lang, value] of Object.entries(translations)) {
			if (value && value.trim()) {
				result[lang] = value.trim();
			}
		}
		return result;
	}

	async function handleSubmit(e: Event) {
		e.preventDefault();
		error = '';
		success = '';

		let count = 0;
		for (const eh of editableHabits) {
			const original = habitList.find((h) => h.id === eh.id);
			if (!original) continue;

			if (eh.threshold1 < 0 || eh.threshold2 < eh.threshold1
				|| eh.threshold4 < eh.threshold2 || eh.threshold8 < eh.threshold4) {
				continue;
			}

			try {
				await habits.update(eh.id, {
					name: eh.name,
					description: eh.description,
					categoryId: original.categoryId,
					frequencyType: original.frequencyType,
					targetFrequency: eh.targetFrequency,
					maxEntriesPerDay: eh.maxEntriesPerDay,
					positiveScoring: eh.positiveScoring,
					scoringRule: {
						id: original.scoringRule?.id ?? '',
						name: original.scoringRule?.name ?? 'custom',
						thresholdFor1Point: eh.threshold1,
						thresholdFor2Points: eh.threshold2,
						thresholdFor4Points: eh.threshold4,
						thresholdFor8Points: eh.threshold8
					},
					nameTranslations: cleanTranslations(eh.nameTranslations),
					descriptionTranslations: cleanTranslations(eh.descriptionTranslations)
				});
				count++;
			} catch {
				// continue saving other habits
			}
		}

		success = `${count} habit(s) saved successfully`;

		try {
			habitList = await habits.list();
		} catch {
			// keep existing list
		}
	}
</script>

<div class="container">
	<h1>Edit Habits</h1>
	{#if error}
		<p class="error">{error}</p>
	{/if}
	{#if success}
		<p class="success">{success}</p>
	{/if}

	{#if loading}
		<p style="text-align: center; color: var(--color-text-placeholder);">Loading habits...</p>
	{:else}
		<form onsubmit={handleSubmit}>
			{#if editableHabits.length > 0}
				{#each groupedHabits as group}
					<div class="category-group">
						<h2 class="category-header" style={isValidColor(group.category?.color) ? `border-left: 4px solid ${group.category!.color}; padding-left: 0.5rem;` : ''}>
							{group.category?.name ?? 'Uncategorized'}
						</h2>
						{#each group.habits as habit (habit.id)}
							<div class="habit-card">
								<div class="habit-header">
									<input
										type="text"
										class="habit-name-input"
										bind:value={habit.name}
									/>
									<input
										type="text"
										class="habit-description-input"
										placeholder="Description (optional)"
										bind:value={habit.description}
									/>
								</div>
								<div class="habit-fields">
									<label class="field">
										<span class="field-label">Target</span>
										<input type="number" min="1" bind:value={habit.targetFrequency} />
									</label>
									<label class="field">
										<span class="field-label">Max/day</span>
										<input type="number" min="0" bind:value={habit.maxEntriesPerDay} />
									</label>
									<label class="field checkbox-field">
										<input type="checkbox" bind:checked={habit.positiveScoring} />
										<span class="field-label">Positive scoring</span>
									</label>
								</div>
								<div class="threshold-fields">
									<span class="threshold-label">Scoring thresholds:</span>
									<label class="field">
										<span class="field-label">1pt</span>
										<input type="number" min="0" bind:value={habit.threshold1} />
									</label>
									<label class="field">
										<span class="field-label">2pt</span>
										<input type="number" min="0" bind:value={habit.threshold2} />
									</label>
									<label class="field">
										<span class="field-label">4pt</span>
										<input type="number" min="0" bind:value={habit.threshold4} />
									</label>
									<label class="field">
										<span class="field-label">8pt</span>
										<input type="number" min="0" bind:value={habit.threshold8} />
									</label>
								</div>
								{#if translationsEnabled}
									<div class="translation-fields">
										<span class="threshold-label">Translations:</span>
										{#each LANGUAGES as lang}
											<div class="translation-row">
												<span class="translation-lang">{LANGUAGE_LABELS[lang]}</span>
												<input
													type="text"
													class="translation-input"
													placeholder="Name ({LANGUAGE_LABELS[lang]})"
													bind:value={habit.nameTranslations[lang]}
												/>
												<input
													type="text"
													class="translation-input translation-input-wide"
													placeholder="Description ({LANGUAGE_LABELS[lang]})"
													bind:value={habit.descriptionTranslations[lang]}
												/>
											</div>
										{/each}
									</div>
								{/if}
							</div>
						{/each}
					</div>
				{/each}
				<button type="submit">Save Changes</button>
			{:else}
				<p class="empty">No habits found. Add a habit first.</p>
			{/if}
		</form>
	{/if}
</div>

<style>
	.category-group {
		margin-bottom: 1rem;
	}

	.category-header {
		font-size: 1rem;
		color: #555;
		margin: 0.75rem 0 0.5rem 0;
	}

	.habit-card {
		border: 1px solid var(--color-border-light);
		border-radius: 4px;
		padding: 0.75rem;
		margin-bottom: 0.5rem;
	}

	.habit-card:hover {
		background-color: var(--color-bg-hover);
	}

	.habit-header {
		display: flex;
		flex-direction: column;
		margin-bottom: 0.5rem;
	}

	.habit-name-input {
		font-weight: bold;
		color: var(--color-text);
		font-size: 1rem;
		border: 1px solid var(--color-border-light);
		border-radius: 3px;
		padding: 0.25rem 0.4rem;
		width: 100%;
	}

	.habit-description-input {
		font-size: 0.85rem;
		color: var(--color-text-muted);
		margin-top: 0.15rem;
		border: 1px solid var(--color-border-light);
		border-radius: 3px;
		padding: 0.25rem 0.4rem;
		width: 100%;
	}

	.habit-fields {
		display: flex;
		align-items: center;
		gap: 1rem;
		flex-wrap: wrap;
		margin-bottom: 0.5rem;
	}

	.threshold-fields {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		flex-wrap: wrap;
	}

	.threshold-label {
		font-size: 0.85rem;
		color: var(--color-text-muted);
	}

	.field {
		display: flex;
		flex-direction: column;
		gap: 0.15rem;
	}

	.field input[type='number'] {
		width: 60px;
		padding: 0.25rem;
		text-align: center;
	}

	.field-label {
		font-size: 0.75rem;
		color: var(--color-text-muted);
	}

	.checkbox-field {
		flex-direction: row;
		align-items: center;
		gap: 0.4rem;
	}

	.checkbox-field input[type='checkbox'] {
		width: 16px;
		height: 16px;
	}

	.empty {
		text-align: center;
		color: var(--color-text-placeholder);
		padding: 1rem;
	}

	.translation-fields {
		margin-top: 0.5rem;
		display: flex;
		flex-direction: column;
		gap: 0.35rem;
	}

	.translation-row {
		display: flex;
		align-items: center;
		gap: 0.5rem;
	}

	.translation-lang {
		font-size: 0.75rem;
		color: var(--color-text-muted);
		min-width: 55px;
	}

	.translation-input {
		padding: 0.2rem 0.4rem;
		font-size: 0.85rem;
		width: 140px;
	}

	.translation-input-wide {
		width: 200px;
	}
</style>
