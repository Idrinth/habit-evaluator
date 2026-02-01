<script lang="ts">
	import { onMount } from 'svelte';
	import { habits, categories, type Habit, type HabitCategory } from '$lib/api';

	let habitList: Habit[] = $state([]);
	let categoryList: HabitCategory[] = $state([]);
	let selected: Set<string> = $state(new Set());
	let error = $state('');
	let success = $state('');
	let loading = $state(true);

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

		for (const habit of habitList) {
			if (habit.categoryId && categoryMap.has(habit.categoryId)) {
				const cat = categoryMap.get(habit.categoryId)!;
				if (!groups.has(cat.id)) {
					groups.set(cat.id, { category: cat, habits: [] });
				}
				groups.get(cat.id)!.habits.push(habit);
			} else {
				uncategorized.push(habit);
			}
		}

		const result: CategoryGroup[] = [...groups.values()];
		if (uncategorized.length > 0) {
			result.push({ category: null, habits: uncategorized });
		}
		return result;
	});

	onMount(async () => {
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
		<p style="text-align: center; color: #999;">Loading habits...</p>
	{:else}
		<form onsubmit={handleSubmit}>
			{#if habitList.length > 0}
				{#each groupedHabits as group}
					<div class="category-group">
						<h2 class="category-header" style={group.category?.color ? `border-left: 4px solid ${group.category.color}; padding-left: 0.5rem;` : ''}>
							{group.category?.name ?? 'Uncategorized'}
						</h2>
						<ul class="habit-list">
							{#each group.habits as habit (habit.id)}
								<li class="habit-item">
									<input
										type="checkbox"
										id={habit.id}
										checked={selected.has(habit.id)}
										onchange={() => toggleHabit(habit.id)}
									/>
									<label for={habit.id} class="habit-info">
										<span class="habit-name">{habit.name}</span>
										{#if habit.description}
											<span class="habit-description">{habit.description}</span>
										{/if}
									</label>
								</li>
							{/each}
						</ul>
					</div>
				{/each}
				<button type="submit">Submit</button>
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

	.habit-list {
		list-style: none;
		padding: 0;
		margin: 0;
	}

	.habit-item {
		display: flex;
		align-items: center;
		padding: 0.75rem;
		border: 1px solid #eee;
		border-radius: 4px;
		margin-bottom: 0.5rem;
	}

	.habit-item:hover {
		background-color: #f9f9f9;
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
		color: #333;
	}

	.habit-description {
		font-size: 0.85rem;
		color: #777;
		margin-top: 0.15rem;
	}

	.empty {
		text-align: center;
		color: #999;
		padding: 1rem;
	}
</style>
