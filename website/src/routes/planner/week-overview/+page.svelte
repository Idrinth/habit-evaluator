<script lang="ts">
	import { onMount } from 'svelte';
	import { dayPlanner, type WeekOverviewSlot } from '$lib/api';
	import { getLanguage, t, type Language } from '$lib/i18n';

	let lang: Language = $state('en');
	let loading = $state(true);
	let error = $state('');
	let occupiedSlots: WeekOverviewSlot[] = $state([]);

	const DAY_LABELS: Record<Language, string[]> = {
		en: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
		de: ['Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa', 'So'],
		es: ['Lun', 'Mar', 'Mi\u00e9', 'Jue', 'Vie', 'S\u00e1b', 'Dom'],
		fr: ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim']
	};

	function isOccupied(dayOfWeek: number, hour: number): boolean {
		return occupiedSlots.some(s => s.dayOfWeek === dayOfWeek && s.hour === hour);
	}

	function formatHour(hour: number): string {
		return `${hour.toString().padStart(2, '0')}:00`;
	}

	onMount(async () => {
		lang = getLanguage();
		try {
			const data = await dayPlanner.weekOverview();
			occupiedSlots = data.slots;
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to load week overview';
		} finally {
			loading = false;
		}
	});
</script>

<div class="container week-overview-container">
	<h1>{t('planner.weekOverview', lang)}</h1>
	<p class="description">{t('planner.weekOverviewDescription', lang)}</p>

	{#if error}
		<p class="error">{error}</p>
	{/if}

	{#if loading}
		<p class="loading">{t('planner.weekOverviewLoading', lang)}</p>
	{:else if occupiedSlots.length === 0}
		<p class="empty">{t('planner.weekOverviewEmpty', lang)}</p>
	{:else}
		<div class="grid-wrapper">
			<div class="week-grid" role="grid" aria-label={t('planner.weekOverview', lang)}>
				<div class="grid-header">
					<div class="hour-label"></div>
					{#each DAY_LABELS[lang] as day}
						<div class="day-header">{day}</div>
					{/each}
				</div>
				{#each Array.from({ length: 24 }, (_, i) => i) as hour}
					<div class="grid-row" role="row">
						<div class="hour-label">{formatHour(hour)}</div>
						{#each Array.from({ length: 7 }, (_, i) => i + 1) as dayOfWeek}
							{@const occupied = isOccupied(dayOfWeek, hour)}
							<div
								class="grid-cell"
								class:occupied
								role="gridcell"
								aria-label="{DAY_LABELS[lang][dayOfWeek - 1]} {formatHour(hour)}: {occupied ? t('planner.weekOverviewOccupied', lang) : t('planner.weekOverviewFree', lang)}"
							></div>
						{/each}
					</div>
				{/each}
			</div>
		</div>

		<div class="legend">
			<span class="legend-item"><span class="legend-swatch occupied"></span> {t('planner.weekOverviewOccupied', lang)}</span>
			<span class="legend-item"><span class="legend-swatch"></span> {t('planner.weekOverviewFree', lang)}</span>
		</div>
	{/if}

	<a href="/planner" class="back-link">{t('planner.weekOverviewBackToPlanner', lang)}</a>
</div>

<style>
	.week-overview-container {
		max-width: 750px;
	}

	.description {
		font-size: 0.9rem;
		color: var(--color-text-muted);
		margin-bottom: 1rem;
	}

	.loading {
		text-align: center;
		color: var(--color-text-placeholder);
		padding: 1rem;
	}

	.empty {
		text-align: center;
		color: var(--color-text-placeholder);
		padding: 1rem;
	}

	.grid-wrapper {
		overflow-x: auto;
		margin-bottom: 1rem;
	}

	.week-grid {
		display: flex;
		flex-direction: column;
		min-width: 400px;
	}

	.grid-header {
		display: flex;
		position: sticky;
		top: 0;
		background: var(--color-bg, #fff);
		z-index: 1;
	}

	.grid-header .hour-label {
		width: 50px;
		flex-shrink: 0;
	}

	.day-header {
		flex: 1;
		text-align: center;
		font-weight: bold;
		font-size: 0.85rem;
		padding: 0.3rem 0;
		border-bottom: 2px solid var(--color-border-light);
	}

	.grid-row {
		display: flex;
	}

	.hour-label {
		width: 50px;
		flex-shrink: 0;
		font-size: 0.7rem;
		color: var(--color-text-muted);
		text-align: right;
		padding-right: 0.4rem;
		line-height: 1.6rem;
	}

	.grid-cell {
		flex: 1;
		height: 1.6rem;
		border: 1px solid var(--color-border-light);
		border-radius: 2px;
		margin: 1px;
		transition: background-color 0.15s;
	}

	.grid-cell.occupied {
		background-color: var(--color-primary, #4a90d9);
		border-color: var(--color-primary, #4a90d9);
	}

	.legend {
		display: flex;
		gap: 1.5rem;
		justify-content: center;
		margin-bottom: 1rem;
		font-size: 0.85rem;
		color: var(--color-text-muted);
	}

	.legend-item {
		display: flex;
		align-items: center;
		gap: 0.3rem;
	}

	.legend-swatch {
		display: inline-block;
		width: 1rem;
		height: 1rem;
		border: 1px solid var(--color-border-light);
		border-radius: 2px;
	}

	.legend-swatch.occupied {
		background-color: var(--color-primary, #4a90d9);
		border-color: var(--color-primary, #4a90d9);
	}

	.back-link {
		display: inline-block;
		margin-top: 0.5rem;
		font-size: 0.9rem;
		color: var(--color-primary, #4a90d9);
	}
</style>
