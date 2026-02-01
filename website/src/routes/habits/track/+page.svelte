<script lang="ts">
	import { onMount } from 'svelte';
	import { habits, type Habit } from '$lib/api';

	let habitList: Habit[] = $state([]);
	let selected: Set<string> = $state(new Set());
	let error = $state('');
	let success = $state('');
	let loading = $state(true);

	function hasReachedDailyLimit(habit: Habit): boolean {
		if (!habit.maxEntriesPerDay || habit.maxEntriesPerDay <= 0) {
			return false;
		}
		const today = new Date().toISOString().slice(0, 10);
		const todayEntries = (habit.entries || []).filter(
			(e) => e.completedAt && e.completedAt.slice(0, 10) === today
		).length;
		return todayEntries >= habit.maxEntriesPerDay;
	}

	onMount(async () => {
		try {
			habitList = await habits.list();
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to load habits';
		} finally {
			loading = false;
		}
	});

	function toggleHabit(id: string) {
		const next = new Set(selected);
		if (next.has(id)) {
			next.delete(id);
		} else {
			next.add(id);
		}
		selected = next;
	}

	async function handleSubmit(e: Event) {
		e.preventDefault();
		error = '';
		success = '';

		if (selected.size === 0) {
			error = 'No habits were selected';
			return;
		}

		let count = 0;
		for (const habitId of selected) {
			try {
				await habits.track(habitId);
				count++;
			} catch {
				// continue tracking other habits
			}
		}

		success = `${count} habit(s) tracked successfully`;
		selected = new Set();

		try {
			habitList = await habits.list();
		} catch {
			// keep existing list
		}
	}
</script>

<div class="container">
	<h1>Track Habits</h1>
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
			{#if habitList.length > 0}
				<ul class="habit-list">
					{#each habitList as habit (habit.id)}
						{@const atLimit = hasReachedDailyLimit(habit)}
						<li class="habit-item" class:at-limit={atLimit}>
							<input
								type="checkbox"
								id={habit.id}
								checked={selected.has(habit.id)}
								disabled={atLimit}
								onchange={() => toggleHabit(habit.id)}
							/>
							<label for={habit.id} class="habit-info">
								<span class="habit-name">{habit.name}</span>
								{#if habit.description}
									<span class="habit-description">{habit.description}</span>
								{/if}
								{#if atLimit}
									<span class="limit-reached">Daily limit reached</span>
								{/if}
							</label>
						</li>
					{/each}
				</ul>
				<button type="submit">Submit</button>
			{:else}
				<p class="empty">No habits found. Add a habit first.</p>
			{/if}
		</form>
	{/if}
</div>

<style>
	.habit-list {
		list-style: none;
		padding: 0;
		margin: 0 0 1rem 0;
	}

	.habit-item {
		display: flex;
		align-items: center;
		padding: 0.75rem;
		border: 1px solid var(--color-border-light);
		border-radius: 4px;
		margin-bottom: 0.5rem;
	}

	.habit-item:hover {
		background-color: var(--color-bg-hover);
	}

	.habit-item input[type='checkbox'] {
		margin-right: 0.75rem;
		width: 18px;
		height: 18px;
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

	.at-limit {
		opacity: 0.5;
	}

	.limit-reached {
		font-size: 0.8rem;
		color: #e67e22;
		margin-top: 0.15rem;
	}

	.empty {
		text-align: center;
		color: var(--color-text-placeholder);
		padding: 1rem;
	}
</style>
