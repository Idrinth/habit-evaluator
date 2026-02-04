<script lang="ts">
	import { onMount } from 'svelte';
	import {
		habits,
		categories,
		defaults,
		type Habit,
		type HabitCategory,
		type Evaluation,
		type PredictedWeeklyScore
	} from '$lib/api';
	import { t, getLanguage, type Language } from '$lib/i18n';

	let lang: Language = $state('en');
	let habitList: Habit[] = $state([]);
	let categoryList: HabitCategory[] = $state([]);
	let loading = $state(true);
	let error = $state('');
	let success = $state('');
	let selectedCategoryId = $state('');
	let selectedHabitId: string | null = $state(null);
	let evaluation: Evaluation | null = $state(null);
	let prediction: PredictedWeeklyScore | null = $state(null);
	let loadingDefaults = $state(false);
	let loadingEval = $state(false);
	let actionInProgress: string | null = $state(null);
	let showFirstStart = $state(false);

	let filteredHabits: Habit[] = $derived.by(() => {
		if (!selectedCategoryId) return habitList;
		return habitList.filter((h) => h.categoryId === selectedCategoryId);
	});

	interface CategoryGroup {
		category: HabitCategory | null;
		habits: Habit[];
	}

	let groupedHabits: CategoryGroup[] = $derived.by(() => {
		const categoryMap = new Map<string, HabitCategory>();
		for (const cat of categoryList) {
			categoryMap.set(cat.id, cat);
		}

		const groups = new Map<string, CategoryGroup>();
		const uncategorized: Habit[] = [];

		for (const h of filteredHabits) {
			if (h.categoryId && categoryMap.has(h.categoryId)) {
				const cat = categoryMap.get(h.categoryId)!;
				if (!groups.has(cat.id)) {
					groups.set(cat.id, { category: cat, habits: [] });
				}
				groups.get(cat.id)!.habits.push(h);
			} else {
				uncategorized.push(h);
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
		return (
			/^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$/.test(color) ||
			/^rgb\(\s*\d{1,3}\s*,\s*\d{1,3}\s*,\s*\d{1,3}\s*\)$/.test(color) ||
			/^[a-zA-Z]{1,20}$/.test(color)
		);
	}

	function todayString(): string {
		return new Date().toISOString().slice(0, 10);
	}

	function getTodayEntryCount(habit: Habit): number {
		const today = todayString();
		return (habit.entries ?? []).filter((e) => e.completedAt.startsWith(today)).length;
	}

	function hasReachedDailyLimit(habit: Habit): boolean {
		if (habit.maxEntriesPerDay <= 0) return false;
		return getTodayEntryCount(habit) >= habit.maxEntriesPerDay;
	}

	function checkFirstStart() {
		if (typeof localStorage !== 'undefined' && !localStorage.getItem('firstStartCompleted')) {
			showFirstStart = true;
		}
	}

	function acknowledgeFirstStart() {
		localStorage.setItem('firstStartCompleted', 'true');
		showFirstStart = false;
	}

	onMount(async () => {
		lang = getLanguage();
		checkFirstStart();
		try {
			const [h, c] = await Promise.all([habits.list(), categories.list()]);
			habitList = h;
			categoryList = c;
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to load habits';
		} finally {
			loading = false;
		}
	});

	async function loadData() {
		try {
			const [h, c] = await Promise.all([habits.list(), categories.list()]);
			habitList = h;
			categoryList = c;
		} catch {
			// keep existing data
		}
	}

	async function handleLoadDefaults() {
		loadingDefaults = true;
		error = '';
		success = '';
		try {
			await defaults.init();
			await loadData();
			success = t('home.defaultsLoaded', lang);
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to load defaults';
		} finally {
			loadingDefaults = false;
		}
	}

	async function handleComplete(habit: Habit) {
		actionInProgress = habit.id;
		error = '';
		try {
			await habits.addEntry(habit.id);
			await loadData();
			if (selectedHabitId === habit.id) {
				await loadEvaluation(habit.id);
			}
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to complete habit';
		} finally {
			actionInProgress = null;
		}
	}

	async function handleUndo(habit: Habit) {
		actionInProgress = habit.id;
		error = '';
		try {
			await habits.removeLastEntry(habit.id, todayString());
			await loadData();
			if (selectedHabitId === habit.id) {
				await loadEvaluation(habit.id);
			}
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to undo completion';
		} finally {
			actionInProgress = null;
		}
	}

	async function handleDelete(habit: Habit) {
		if (!confirm(t('home.confirmDelete', lang))) return;
		actionInProgress = habit.id;
		error = '';
		try {
			await habits.delete(habit.id);
			if (selectedHabitId === habit.id) {
				selectedHabitId = null;
				evaluation = null;
			}
			await loadData();
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to delete habit';
		} finally {
			actionInProgress = null;
		}
	}

	async function loadEvaluation(habitId: string) {
		loadingEval = true;
		evaluation = null;
		try {
			const [evalResult, predResult] = await Promise.all([
				habits.evaluate(habitId),
				habits.predict()
			]);
			evaluation = evalResult;
			prediction = predResult;
		} catch {
			evaluation = null;
			prediction = null;
		} finally {
			loadingEval = false;
		}
	}

	async function selectHabit(habitId: string) {
		if (selectedHabitId === habitId) {
			selectedHabitId = null;
			evaluation = null;
			prediction = null;
			return;
		}
		selectedHabitId = habitId;
		await loadEvaluation(habitId);
	}

	function getHabitPrediction(habitId: string): { currentScore: number; predictedScore: number } | null {
		if (!prediction) return null;
		const hp = prediction.habitPredictions.find((p) => p.habitId === habitId);
		return hp ? { currentScore: hp.currentScore, predictedScore: hp.predictedScore } : null;
	}
</script>

<div class="container home-container">
	<h1>{t('home.title', lang)}</h1>

	{#if error}
		<p class="error">{error}</p>
	{/if}
	{#if success}
		<p class="success">{success}</p>
	{/if}

	{#if loading}
		<p class="loading-text">{t('home.loading', lang)}</p>
	{:else}
		{#if categoryList.length > 1}
			<div class="filter-bar">
				<select bind:value={selectedCategoryId} class="category-filter">
					<option value="">{t('home.allCategories', lang)}</option>
					{#each categoryList as cat (cat.id)}
						<option value={cat.id}>{cat.name}</option>
					{/each}
				</select>
			</div>
		{/if}

		{#if habitList.length === 0}
			<div class="empty-state">
				<p>{t('home.noHabits', lang)}</p>
				<button onclick={handleLoadDefaults} disabled={loadingDefaults} class="defaults-button">
					{loadingDefaults ? t('home.loadingDefaults', lang) : t('home.loadDefaults', lang)}
				</button>
			</div>
		{:else}
			{#each groupedHabits as group}
				<div class="category-group">
					<h2
						class="category-header"
						style={isValidColor(group.category?.color)
							? `border-left: 4px solid ${group.category!.color}; padding-left: 0.5rem;`
							: ''}
					>
						{group.category?.name ?? 'Uncategorized'}
					</h2>
					{#each group.habits as habit (habit.id)}
						{@const todayCount = getTodayEntryCount(habit)}
						{@const atLimit = hasReachedDailyLimit(habit)}
						{@const isSelected = selectedHabitId === habit.id}
						{@const isActioning = actionInProgress === habit.id}
						<div class="habit-card" class:selected={isSelected}>
							<button class="habit-select-area" onclick={() => selectHabit(habit.id)}>
								<div class="habit-info">
									<span class="habit-name">{habit.name}</span>
									{#if habit.description}
										<span class="habit-description">{habit.description}</span>
									{/if}
									{#if todayCount > 0}
										<span class="today-count">{todayCount} {t('home.todayEntries', lang)}</span>
									{/if}
								</div>
							</button>
							<div class="habit-actions">
								{#if atLimit}
									<span class="limit-badge">{t('home.dailyLimit', lang)}</span>
								{:else}
									<button
										class="action-btn complete-btn"
										onclick={() => handleComplete(habit)}
										disabled={isActioning}
									>
										{t('home.complete', lang)}
									</button>
								{/if}
								{#if todayCount > 0}
									<button
										class="action-btn undo-btn"
										onclick={() => handleUndo(habit)}
										disabled={isActioning}
									>
										{t('home.undo', lang)}
									</button>
								{/if}
								<button
									class="action-btn delete-btn"
									onclick={() => handleDelete(habit)}
									disabled={isActioning}
								>
									{t('home.delete', lang)}
								</button>
							</div>

							{#if isSelected}
								<div class="evaluation-panel">
									{#if loadingEval}
										<p class="eval-loading">...</p>
									{:else if evaluation}
										{@const pred = getHabitPrediction(habit.id)}
										<h3>{t('eval.title', lang)}</h3>
										<div class="eval-grid">
											{#if evaluation.positiveScoring}
												<div class="eval-item">
													<span class="eval-label">{t('eval.completionRate', lang)}</span>
													<span class="eval-value"
														>{Math.round(evaluation.completionRate * 100)}%</span
													>
												</div>
											{:else}
												<div class="eval-item">
													<span class="eval-label">{t('eval.avoidanceRate', lang)}</span>
													<span class="eval-value"
														>{Math.round(evaluation.completionRate * 100)}%</span
													>
												</div>
											{/if}
											<div class="eval-item">
												<span class="eval-label">
													{evaluation.positiveScoring
														? t('eval.streak', lang)
														: t('eval.avoidanceStreak', lang)}
												</span>
												<span class="eval-value"
													>{evaluation.currentStreak} {t('eval.days', lang)}</span
												>
											</div>
											<div class="eval-item">
												<span class="eval-label">{t('eval.longestStreak', lang)}</span>
												<span class="eval-value"
													>{evaluation.longestStreak} {t('eval.days', lang)}</span
												>
											</div>
											<div class="eval-item">
												<span class="eval-label">{t('eval.completions', lang)}</span>
												<span class="eval-value"
													>{evaluation.totalEntries} / {evaluation.targetEntries}</span
												>
											</div>
										</div>
										<div class="progress-container">
											<div
												class="progress-bar"
												class:on-track={evaluation.onTrack}
												class:behind={!evaluation.onTrack}
												style="width: {Math.min(
													Math.round(evaluation.completionRate * 100),
													100
												)}%"
											></div>
										</div>
										<span class="track-status" class:on-track={evaluation.onTrack}>
											{evaluation.onTrack ? t('eval.onTrack', lang) : t('eval.behind', lang)}
										</span>
										{#if pred}
											<div class="score-section">
												<h4>{t('eval.weeklyScore', lang)}</h4>
												<div class="score-grid">
													<div class="score-item">
														<span class="score-label">{t('eval.currentScore', lang)}</span>
														<span class="score-value"
															>{pred.currentScore} {t('eval.points', lang)}</span
														>
													</div>
													<div class="score-item">
														<span class="score-label"
															>{t('eval.predictedScore', lang)}</span
														>
														<span class="score-value"
															>{pred.predictedScore} {t('eval.points', lang)}</span
														>
													</div>
												</div>
											</div>
										{/if}
									{/if}
								</div>
							{/if}
						</div>
					{/each}
				</div>
			{/each}
		{/if}
	{/if}
</div>

{#if showFirstStart}
	<div class="first-start-overlay">
		<div class="first-start-modal">
			<h2>{t('firstStart.title', lang)}</h2>
			<p>{t('firstStart.notProfessionalHelp', lang)}</p>
			<p>{t('firstStart.noDataSharing', lang)}</p>
			<button class="first-start-btn" onclick={acknowledgeFirstStart}>
				{t('firstStart.acknowledge', lang)}
			</button>
		</div>
	</div>
{/if}

<style>
	.home-container {
		max-width: 600px;
	}

	.filter-bar {
		margin-bottom: 1rem;
	}

	.category-filter {
		width: 100%;
		padding: 0.5rem;
		border: 1px solid var(--color-border);
		border-radius: 4px;
		font-size: 1rem;
		background-color: var(--color-input-bg);
		color: var(--color-text);
	}

	.empty-state {
		text-align: center;
		padding: 2rem 1rem;
	}

	.empty-state p {
		color: var(--color-text-placeholder);
		margin-bottom: 1rem;
	}

	.defaults-button {
		padding: 0.6rem 1.2rem;
	}

	.loading-text {
		text-align: center;
		color: var(--color-text-placeholder);
	}

	.category-group {
		margin-bottom: 1rem;
	}

	.category-header {
		font-size: 1rem;
		color: var(--color-text-muted);
		margin: 0.75rem 0 0.5rem 0;
	}

	.habit-card {
		border: 1px solid var(--color-border-light);
		border-radius: 4px;
		margin-bottom: 0.5rem;
		overflow: hidden;
	}

	.habit-card.selected {
		border-color: var(--color-primary);
	}

	.habit-select-area {
		display: block;
		width: 100%;
		padding: 0.75rem;
		background: none;
		border: none;
		cursor: pointer;
		text-align: left;
		color: inherit;
		font-size: inherit;
	}

	.habit-select-area:hover {
		background-color: var(--color-bg-hover);
	}

	.habit-info {
		display: flex;
		flex-direction: column;
	}

	.habit-name {
		font-weight: bold;
		color: var(--color-text);
	}

	.habit-description {
		font-size: 0.85rem;
		color: var(--color-text-muted);
		margin-top: 0.15rem;
	}

	.today-count {
		font-size: 0.8rem;
		color: var(--color-primary);
		margin-top: 0.25rem;
	}

	.habit-actions {
		display: flex;
		gap: 0.5rem;
		padding: 0 0.75rem 0.75rem;
		flex-wrap: wrap;
	}

	.action-btn {
		padding: 0.3rem 0.6rem;
		font-size: 0.8rem;
		border-radius: 4px;
		border: none;
		cursor: pointer;
		color: #fff;
	}

	.action-btn:disabled {
		opacity: 0.5;
		cursor: not-allowed;
	}

	.complete-btn {
		background-color: var(--color-success);
	}

	.complete-btn:hover:not(:disabled) {
		opacity: 0.9;
	}

	.undo-btn {
		background-color: var(--color-text-muted);
	}

	.undo-btn:hover:not(:disabled) {
		opacity: 0.9;
	}

	.delete-btn {
		background-color: var(--color-error);
	}

	.delete-btn:hover:not(:disabled) {
		opacity: 0.9;
	}

	.limit-badge {
		font-size: 0.8rem;
		color: var(--color-text-muted);
		font-style: italic;
	}

	.evaluation-panel {
		padding: 0.75rem;
		border-top: 1px solid var(--color-border-light);
		background-color: var(--color-bg-hover);
	}

	.evaluation-panel h3 {
		margin: 0 0 0.5rem 0;
		font-size: 0.95rem;
		color: var(--color-text);
	}

	.eval-loading {
		text-align: center;
		color: var(--color-text-placeholder);
		margin: 0;
	}

	.eval-grid {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 0.5rem;
		margin-bottom: 0.5rem;
	}

	.eval-item {
		display: flex;
		flex-direction: column;
	}

	.eval-label {
		font-size: 0.75rem;
		color: var(--color-text-muted);
	}

	.eval-value {
		font-size: 0.95rem;
		font-weight: bold;
		color: var(--color-text);
	}

	.progress-container {
		height: 8px;
		background-color: var(--color-border-light);
		border-radius: 4px;
		overflow: hidden;
		margin-bottom: 0.25rem;
	}

	.progress-bar {
		height: 100%;
		border-radius: 4px;
		transition: width 0.3s ease;
	}

	.progress-bar.on-track {
		background-color: var(--color-success);
	}

	.progress-bar.behind {
		background-color: var(--color-error);
	}

	.track-status {
		font-size: 0.8rem;
		font-weight: bold;
	}

	.track-status.on-track {
		color: var(--color-success);
	}

	.track-status:not(.on-track) {
		color: var(--color-error);
	}

	.score-section {
		margin-top: 0.75rem;
		padding-top: 0.5rem;
		border-top: 1px solid var(--color-border-light);
	}

	.score-section h4 {
		margin: 0 0 0.25rem 0;
		font-size: 0.85rem;
		color: var(--color-text-muted);
	}

	.score-grid {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 0.5rem;
	}

	.score-item {
		display: flex;
		flex-direction: column;
	}

	.score-label {
		font-size: 0.75rem;
		color: var(--color-text-muted);
	}

	.score-value {
		font-size: 0.95rem;
		font-weight: bold;
		color: var(--color-text);
	}

	.first-start-overlay {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.6);
		display: flex;
		align-items: center;
		justify-content: center;
		z-index: 1000;
	}

	.first-start-modal {
		background: var(--color-bg, #fff);
		border-radius: 8px;
		padding: 2rem;
		max-width: 500px;
		width: 90%;
		color: var(--color-text, #222);
	}

	.first-start-modal h2 {
		margin: 0 0 1rem 0;
	}

	.first-start-modal p {
		margin: 0 0 1rem 0;
		line-height: 1.5;
	}

	.first-start-btn {
		padding: 0.6rem 1.2rem;
		font-size: 1rem;
		border: none;
		border-radius: 4px;
		background-color: var(--color-primary, #4a90d9);
		color: #fff;
		cursor: pointer;
		width: 100%;
	}

	.first-start-btn:hover {
		opacity: 0.9;
	}
</style>
