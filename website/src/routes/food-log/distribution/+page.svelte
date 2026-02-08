<script lang="ts">
	import { onMount } from 'svelte';
	import { stats, type FoodDistributionData } from '$lib/api';
	import { goto } from '$app/navigation';
	import { getLanguage, t, type Language } from '$lib/i18n';

	let data: FoodDistributionData | null = $state(null);
	let error: string | null = $state(null);
	let loading = $state(true);
	let lang: Language = $state('en');

	const BAR_COLOR = '#4CAF50';
	const KCAL_COLOR = '#FF9800';
	const CARBS_COLOR = '#2196F3';

	onMount(() => {
		lang = getLanguage();
		loadData();
	});

	async function loadData() {
		loading = true;
		error = null;
		try {
			data = await stats.foodDistribution();
		} catch (e) {
			if (e instanceof Error && e.message.includes('401')) {
				goto('/login');
				return;
			}
			error = e instanceof Error ? e.message : 'Failed to load food distribution data';
		} finally {
			loading = false;
		}
	}
</script>

<div class="food-distribution-container">
	<h1>{t('foodDistribution.title', lang)}</h1>
	<p class="description">{t('foodDistribution.description', lang)}</p>

	{#if error}
		<p class="error">{error}</p>
	{:else if loading}
		<p class="loading">{t('foodDistribution.loading', lang)}</p>
	{:else if data}
		{@const maxCount = Math.max(1, ...data.mealCounts)}
		{@const maxKcal = Math.max(1, ...data.avgKcal)}
		{@const maxCarbs = Math.max(1, ...data.avgCarbs)}
		{@const totalMeals = data.mealCounts.reduce((a, b) => a + b, 0)}

		{#if totalMeals === 0}
			<p class="empty">{t('foodDistribution.noData', lang)}</p>
		{:else}
			<div class="chart-card">
				<h2>{t('foodDistribution.mealsPerHour', lang)}</h2>
				<div class="chart-wrapper">
					<svg viewBox="0 0 760 250" class="distribution-chart" preserveAspectRatio="xMinYMid meet">
						<!-- Y-axis grid lines -->
						{#each Array.from({ length: 5 }, (_, i) => Math.round((maxCount / 4) * i)) as tick, i}
							{@const y = 200 - (tick / maxCount) * 180}
							<line x1="40" y1={y} x2="740" y2={y} stroke="var(--color-border)" stroke-width="0.5" stroke-dasharray="3,3" />
							<text x="36" y={y + 3} text-anchor="end" font-size="9" fill="var(--color-text-muted)">{tick}</text>
						{/each}

						<!-- Bars -->
						{#each data.mealCounts as count, i}
							{@const barWidth = 25}
							{@const x = 45 + i * 29}
							{@const barHeight = (count / maxCount) * 180}
							<rect
								x={x}
								y={200 - barHeight}
								width={barWidth}
								height={barHeight}
								fill={BAR_COLOR}
								rx="2"
								opacity="0.85"
							>
								<title>{data.labels[i]}: {count} {t('foodDistribution.meals', lang)}</title>
							</rect>
						{/each}

						<!-- X-axis labels -->
						{#each data.labels as label, i}
							{#if i % 3 === 0}
								<text
									x={45 + i * 29 + 12.5}
									y={220}
									text-anchor="middle"
									font-size="8"
									fill="var(--color-text-muted)"
								>
									{label}
								</text>
							{/if}
						{/each}
					</svg>
				</div>
				<div class="chart-stats">
					<span class="stat">{t('foodDistribution.totalMeals', lang)}: <strong>{totalMeals}</strong></span>
				</div>
			</div>

			<div class="chart-card">
				<h2>{t('foodDistribution.avgKcalPerHour', lang)}</h2>
				<div class="chart-wrapper">
					<svg viewBox="0 0 760 250" class="distribution-chart" preserveAspectRatio="xMinYMid meet">
						{#each Array.from({ length: 5 }, (_, i) => Math.round((maxKcal / 4) * i)) as tick}
							{@const y = 200 - (tick / maxKcal) * 180}
							<line x1="40" y1={y} x2="740" y2={y} stroke="var(--color-border)" stroke-width="0.5" stroke-dasharray="3,3" />
							<text x="36" y={y + 3} text-anchor="end" font-size="9" fill="var(--color-text-muted)">{tick}</text>
						{/each}

						{#each data.avgKcal as kcal, i}
							{@const barWidth = 25}
							{@const x = 45 + i * 29}
							{@const barHeight = (kcal / maxKcal) * 180}
							<rect
								x={x}
								y={200 - barHeight}
								width={barWidth}
								height={barHeight}
								fill={KCAL_COLOR}
								rx="2"
								opacity="0.85"
							>
								<title>{data.labels[i]}: {kcal} kcal</title>
							</rect>
						{/each}

						{#each data.labels as label, i}
							{#if i % 3 === 0}
								<text
									x={45 + i * 29 + 12.5}
									y={220}
									text-anchor="middle"
									font-size="8"
									fill="var(--color-text-muted)"
								>
									{label}
								</text>
							{/if}
						{/each}
					</svg>
				</div>
			</div>

			<div class="chart-card">
				<h2>{t('foodDistribution.avgCarbsPerHour', lang)}</h2>
				<div class="chart-wrapper">
					<svg viewBox="0 0 760 250" class="distribution-chart" preserveAspectRatio="xMinYMid meet">
						{#each Array.from({ length: 5 }, (_, i) => Math.round((maxCarbs / 4) * i)) as tick}
							{@const y = 200 - (tick / maxCarbs) * 180}
							<line x1="40" y1={y} x2="740" y2={y} stroke="var(--color-border)" stroke-width="0.5" stroke-dasharray="3,3" />
							<text x="36" y={y + 3} text-anchor="end" font-size="9" fill="var(--color-text-muted)">{tick}</text>
						{/each}

						{#each data.avgCarbs as carbs, i}
							{@const barWidth = 25}
							{@const x = 45 + i * 29}
							{@const barHeight = (carbs / maxCarbs) * 180}
							<rect
								x={x}
								y={200 - barHeight}
								width={barWidth}
								height={barHeight}
								fill={CARBS_COLOR}
								rx="2"
								opacity="0.85"
							>
								<title>{data.labels[i]}: {carbs}g</title>
							</rect>
						{/each}

						{#each data.labels as label, i}
							{#if i % 3 === 0}
								<text
									x={45 + i * 29 + 12.5}
									y={220}
									text-anchor="middle"
									font-size="8"
									fill="var(--color-text-muted)"
								>
									{label}
								</text>
							{/if}
						{/each}
					</svg>
				</div>
			</div>
		{/if}

		<div class="back-link">
			<a href="/food-log">{t('foodDistribution.backToFoodLog', lang)}</a>
		</div>
	{/if}
</div>

<style>
	.food-distribution-container {
		width: 100%;
		max-width: 900px;
		padding: 1rem;
	}

	h1 {
		text-align: center;
		margin-bottom: 0.25rem;
	}

	.description {
		text-align: center;
		color: var(--color-text-muted);
		font-size: 0.85rem;
		margin-bottom: 1.5rem;
	}

	.loading, .empty {
		text-align: center;
		color: var(--color-text-muted);
		padding: 2rem 0;
	}

	.chart-card {
		background: var(--color-bg-card);
		border-radius: 8px;
		padding: 1rem;
		box-shadow: 0 2px 8px var(--color-shadow);
		margin-bottom: 1.5rem;
	}

	.chart-card h2 {
		font-size: 1rem;
		margin: 0 0 0.75rem 0;
		color: var(--color-text);
	}

	.chart-wrapper {
		overflow-x: auto;
	}

	.distribution-chart {
		width: 100%;
		height: auto;
		min-width: 400px;
	}

	.chart-stats {
		display: flex;
		gap: 1rem;
		margin-top: 0.75rem;
		font-size: 0.85rem;
		color: var(--color-text-muted);
		justify-content: center;
	}

	.chart-stats strong {
		color: var(--color-text);
	}

	.back-link {
		text-align: center;
		margin-top: 1rem;
	}

	.back-link a {
		color: var(--color-link);
		text-decoration: none;
		font-size: 0.9rem;
	}

	.back-link a:hover {
		text-decoration: underline;
	}
</style>
