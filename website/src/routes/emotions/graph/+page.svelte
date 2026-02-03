<script lang="ts">
	import { onMount } from 'svelte';
	import { emotions, type EmotionGraphData } from '$lib/api';
	import { goto } from '$app/navigation';
	import { getLanguage, t, type Language } from '$lib/i18n';

	let data: EmotionGraphData | null = $state(null);
	let error: string | null = $state(null);
	let period: 'week' | 'month' = $state('week');
	let loading = $state(true);
	let lang: Language = $state('en');

	const PAIR_COLORS = [
		'#4CAF50', '#2196F3', '#FF9800', '#E91E63', '#9C27B0',
		'#00BCD4', '#FF5722', '#795548', '#607D8B', '#8BC34A'
	];

	onMount(() => {
		lang = getLanguage();
		loadData();
	});

	async function loadData() {
		loading = true;
		error = null;
		try {
			data = await emotions.graph(period);
		} catch (e) {
			if (e instanceof Error && e.message.includes('401')) {
				goto('/login');
				return;
			}
			error = e instanceof Error ? e.message : 'Failed to load emotion graph data';
		} finally {
			loading = false;
		}
	}

	function handlePeriodChange(newPeriod: 'week' | 'month') {
		period = newPeriod;
		loadData();
	}

	function pairColor(index: number): string {
		return PAIR_COLORS[index % PAIR_COLORS.length];
	}
</script>

<div class="emotions-graph-container">
	<h1>{t('emotions.graphTitle', lang)}</h1>

	<div class="controls">
		<div class="period-toggle">
			<button
				class:active={period === 'week'}
				onclick={() => handlePeriodChange('week')}
			>{t('emotions.week', lang)}</button>
			<button
				class:active={period === 'month'}
				onclick={() => handlePeriodChange('month')}
			>{t('emotions.month', lang)}</button>
		</div>
	</div>

	{#if error}
		<p class="error">{error}</p>
	{:else if loading}
		<p class="loading">{t('emotions.loading', lang)}</p>
	{:else if data && data.pairs.length === 0}
		<p class="empty">{t('emotions.noPairs', lang)}</p>
	{:else if data}
		{#each data.pairs as pair, pairIndex}
			{@const color = pairColor(pairIndex)}
			{@const maxAbs = Math.max(10, ...pair.dailyAverages.map(Math.abs))}
			{@const chartHeight = 220}
			{@const plotTop = 10}
			{@const plotBottom = chartHeight - 30}
			{@const plotHeight = plotBottom - plotTop}
			{@const midY = plotTop + plotHeight / 2}
			{@const barWidth = Math.max(12, Math.min(20, 600 / data.labels.length - 4))}
			{@const chartWidth = data.labels.length * (barWidth + 4) + 50}
			{@const leftMargin = 40}
			{@const totalWidth = chartWidth + leftMargin}
			<div class="chart-card">
				<div class="chart-header">
					<h2>
						<span class="negative-label">{pair.negativeLabel}</span>
						<span class="separator"> — </span>
						<span class="positive-label">{pair.positiveLabel}</span>
					</h2>
					<div class="chart-stats">
						<span class="stat">
							{t('emotions.avg', lang)}: <strong>{pair.overallAverage.toFixed(2)}</strong>
						</span>
						<span class="stat">
							{t('emotions.entries', lang)}: <strong>{pair.totalEntries}</strong>
						</span>
					</div>
				</div>

				<div class="chart-wrapper">
					<svg viewBox="0 0 {totalWidth} {chartHeight}" class="emotion-chart" preserveAspectRatio="xMinYMid meet">
						<!-- Y-axis grid lines and labels -->
						{#each [-10, -5, 0, 5, 10] as tick}
							{@const y = midY - (tick / maxAbs) * (plotHeight / 2)}
							<line
								x1={leftMargin}
								y1={y}
								x2={totalWidth}
								y2={y}
								stroke="var(--color-border)"
								stroke-width={tick === 0 ? '1' : '0.5'}
								stroke-dasharray={tick === 0 ? 'none' : '3,3'}
							/>
							<text
								x={leftMargin - 4}
								y={y + 3}
								text-anchor="end"
								font-size="8"
								fill="var(--color-text-muted)"
							>
								{tick > 0 ? '+' : ''}{tick}
							</text>
						{/each}

						<!-- Bars -->
						{#each pair.dailyAverages as value, i}
							{@const barX = leftMargin + i * (barWidth + 4) + 2}
							{@const barH = Math.abs(value) / maxAbs * (plotHeight / 2)}
							{#if value >= 0}
								<rect
									x={barX}
									y={midY - barH}
									width={barWidth}
									height={Math.max(barH, 0.5)}
									fill={color}
									opacity="0.85"
									rx="2"
								>
									<title>{data.labels[i]}: {value.toFixed(2)}</title>
								</rect>
							{:else}
								<rect
									x={barX}
									y={midY}
									width={barWidth}
									height={Math.max(barH, 0.5)}
									fill={color}
									opacity="0.5"
									rx="2"
								>
									<title>{data.labels[i]}: {value.toFixed(2)}</title>
								</rect>
							{/if}
						{/each}

						<!-- Average line -->
						{#if pair.totalEntries > 0}
							{@const avgY = midY - (pair.overallAverage / maxAbs) * (plotHeight / 2)}
							<line
								x1={leftMargin}
								y1={avgY}
								x2={totalWidth}
								y2={avgY}
								stroke="#E91E63"
								stroke-width="2"
								stroke-dasharray="6,3"
							/>
							<text
								x={totalWidth - 2}
								y={avgY - 4}
								text-anchor="end"
								font-size="8"
								fill="#E91E63"
								font-weight="600"
							>
								avg: {pair.overallAverage.toFixed(2)}
							</text>
						{/if}

						<!-- X-axis labels -->
						{#each data.labels as label, i}
							{@const labelInterval = data.labels.length > 14 ? 5 : data.labels.length > 7 ? 2 : 1}
							{#if i % labelInterval === 0}
								<text
									x={leftMargin + i * (barWidth + 4) + barWidth / 2 + 2}
									y={chartHeight - 4}
									text-anchor="middle"
									font-size="7"
									fill="var(--color-text-muted)"
								>
									{label}
								</text>
							{/if}
						{/each}
					</svg>
				</div>

				<div class="chart-legend">
					<span class="legend-item">
						<span class="legend-bar" style="background: {color}; opacity: 0.85;"></span>
						{t('emotions.positive', lang)}
					</span>
					<span class="legend-item">
						<span class="legend-bar" style="background: {color}; opacity: 0.5;"></span>
						{t('emotions.negative', lang)}
					</span>
					<span class="legend-item">
						<span class="legend-line"></span>
						{t('emotions.avgLine', lang)}
					</span>
				</div>
			</div>
		{/each}
	{/if}
</div>

<style>
	.emotions-graph-container {
		width: 100%;
		max-width: 900px;
		padding: 1rem;
	}

	h1 {
		text-align: center;
		margin-bottom: 1rem;
	}

	.controls {
		display: flex;
		justify-content: center;
		margin-bottom: 1.5rem;
	}

	.period-toggle {
		display: flex;
		border: 1px solid var(--color-border);
		border-radius: 4px;
		overflow: hidden;
	}

	.period-toggle button {
		padding: 0.4rem 1.2rem;
		border: none;
		border-radius: 0;
		background: var(--color-input-bg);
		color: var(--color-text);
		cursor: pointer;
		font-size: 0.9rem;
	}

	.period-toggle button.active {
		background: var(--color-primary);
		color: #fff;
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

	.chart-header {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		flex-wrap: wrap;
		gap: 0.5rem;
		margin-bottom: 0.75rem;
	}

	.chart-header h2 {
		font-size: 1rem;
		margin: 0;
		color: var(--color-text);
	}

	.negative-label {
		color: var(--color-text-muted);
	}

	.separator {
		color: var(--color-text-muted);
	}

	.positive-label {
		color: var(--color-text);
	}

	.chart-stats {
		display: flex;
		gap: 1rem;
		font-size: 0.8rem;
		color: var(--color-text-muted);
	}

	.chart-stats strong {
		color: var(--color-text);
	}

	.chart-wrapper {
		overflow-x: auto;
	}

	.emotion-chart {
		width: 100%;
		height: auto;
		min-width: 300px;
	}

	.chart-legend {
		display: flex;
		gap: 1rem;
		margin-top: 0.75rem;
		font-size: 0.75rem;
		color: var(--color-text-muted);
		justify-content: center;
	}

	.legend-item {
		display: flex;
		align-items: center;
		gap: 0.3rem;
	}

	.legend-bar {
		display: inline-block;
		width: 14px;
		height: 10px;
		border-radius: 2px;
	}

	.legend-line {
		display: inline-block;
		width: 18px;
		height: 0;
		border-top: 2px dashed #E91E63;
	}

	@media (max-width: 600px) {
		.chart-header {
			flex-direction: column;
		}
	}
</style>
