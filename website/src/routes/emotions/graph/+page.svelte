<script lang="ts">
	import { onMount } from 'svelte';
	import { emotions, type EmotionGraphData } from '$lib/api';
	import { goto } from '$app/navigation';
	import { getLanguage, t, type Language } from '$lib/i18n';

	let data: EmotionGraphData | null = $state(null);
	let error: string | null = $state(null);
	let loading = $state(true);
	let lang: Language = $state('en');
	let expandedPairs: Set<string> = $state(new Set());

	function toggleHistory(pairId: string) {
		const next = new Set(expandedPairs);
		if (next.has(pairId)) {
			next.delete(pairId);
		} else {
			next.add(pairId);
		}
		expandedPairs = next;
	}

	function formatDateTime(isoString: string): string {
		const d = new Date(isoString);
		return d.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })
			+ ' ' + d.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' });
	}

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
			data = await emotions.graph();
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

	function pairColor(index: number): string {
		return PAIR_COLORS[index % PAIR_COLORS.length];
	}

	function formatStrength(value: number, negativeLabel: string, positiveLabel: string): string {
		const percentage = Math.round(Math.abs(value) * 10);
		if (value === 0) return '0%';
		if (value < 0) return `${percentage}% ${negativeLabel}`;
		return `${percentage}% ${positiveLabel}`;
	}
</script>

<div class="emotions-graph-container">
	<h1>{t('emotions.graphTitle', lang)}</h1>

	{#if error}
		<p class="error">{error}</p>
	{:else if loading}
		<p class="loading">{t('emotions.loading', lang)}</p>
	{:else if data && data.pairs.length === 0}
		<p class="empty">{t('emotions.noPairs', lang)}</p>
	{:else if data}
		{#each data.pairs as pair, pairIndex}
			{@const color = pairColor(pairIndex)}
			{@const nonNullValues = pair.dailyAverages.filter((v): v is number => v != null)}
			{@const maxAbs = Math.max(10, ...nonNullValues.map(Math.abs))}
			{@const chartHeight = 220}
			{@const plotTop = 10}
			{@const plotBottom = chartHeight - 30}
			{@const plotHeight = plotBottom - plotTop}
			{@const midY = plotTop + plotHeight / 2}
			{@const pointSpacing = Math.max(8, Math.min(20, 700 / Math.max(data.labels.length - 1, 1)))}
			{@const leftMargin = 40}
			{@const totalWidth = leftMargin + (data.labels.length - 1) * pointSpacing + 20}
			<div class="chart-card">
				<div class="chart-header">
					<h2>
						<span class="negative-label">{pair.negativeLabel}</span>
						<span class="separator"> — </span>
						<span class="positive-label">{pair.positiveLabel}</span>
					</h2>
					<div class="chart-stats">
						<span class="stat">
							{t('emotions.avg', lang)}: <strong>{formatStrength(pair.overallAverage, pair.negativeLabel, pair.positiveLabel)}</strong>
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
								{Math.abs(tick) * 10}%
							</text>
						{/each}

						<!-- Line path connecting non-null points -->
						{#if true}
							{@const points = pair.dailyAverages.map((v, i) => v != null ? { x: leftMargin + i * pointSpacing, y: midY - (v / maxAbs) * (plotHeight / 2), v } : null)}
							{@const segments = (() => {
								const segs: string[] = [];
								let current = '';
								for (const pt of points) {
									if (pt) {
										current += (current === '' ? 'M' : 'L') + pt.x + ',' + pt.y;
									} else if (current !== '') {
										segs.push(current);
										current = '';
									}
								}
								if (current !== '') segs.push(current);
								return segs;
							})()}
							{#each segments as segment}
								<path
									d={segment}
									fill="none"
									stroke={color}
									stroke-width="2"
									stroke-linejoin="round"
									stroke-linecap="round"
								/>
							{/each}

							<!-- Data points -->
							{#each points as pt, i}
								{#if pt}
									<circle
										cx={pt.x}
										cy={pt.y}
										r="3"
										fill={color}
									>
										<title>{data.labels[i]}: {formatStrength(pt.v, pair.negativeLabel, pair.positiveLabel)}</title>
									</circle>
								{/if}
							{/each}
						{/if}

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
								avg: {formatStrength(pair.overallAverage, pair.negativeLabel, pair.positiveLabel)}
							</text>
						{/if}

						<!-- X-axis labels -->
						{#each data.labels as label, i}
							{@const labelInterval = data.labels.length > 60 ? 14 : data.labels.length > 30 ? 7 : data.labels.length > 14 ? 3 : data.labels.length > 7 ? 2 : 1}
							{#if i % labelInterval === 0}
								<text
									x={leftMargin + i * pointSpacing}
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
						<span class="legend-dot" style="background: {color};"></span>
						{t('emotions.entries', lang)}
					</span>
					<span class="legend-item">
						<span class="legend-line"></span>
						{t('emotions.avgLine', lang)}
					</span>
				</div>

				<button
					class="history-toggle"
					onclick={() => toggleHistory(pair.pairId)}
					aria-expanded={expandedPairs.has(pair.pairId)}
				>
					<span class="toggle-arrow" class:expanded={expandedPairs.has(pair.pairId)}>&#9654;</span>
					{expandedPairs.has(pair.pairId) ? t('emotions.hideHistory', lang) : t('emotions.showHistory', lang)}
					({pair.totalEntries})
				</button>

				{#if expandedPairs.has(pair.pairId)}
					<div class="history-list">
						{#if pair.entries.length === 0}
							<p class="history-empty">{t('emotions.noEntries', lang)}</p>
						{:else}
							{#each pair.entries as entry (entry.id)}
								<div class="history-entry">
									<span class="history-date">{formatDateTime(entry.recordedAt)}</span>
									<span class="history-strength">{formatStrength(entry.strength, pair.negativeLabel, pair.positiveLabel)}</span>
									{#if entry.notes}
										<span class="history-notes">{entry.notes}</span>
									{/if}
								</div>
							{/each}
						{/if}
					</div>
				{/if}
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

	.legend-dot {
		display: inline-block;
		width: 8px;
		height: 8px;
		border-radius: 50%;
	}

	.legend-line {
		display: inline-block;
		width: 18px;
		height: 0;
		border-top: 2px dashed #E91E63;
	}

	.history-toggle {
		display: flex;
		align-items: center;
		gap: 0.4rem;
		margin-top: 0.75rem;
		padding: 0.4rem 0.6rem;
		background: transparent;
		border: 1px solid var(--color-border-light);
		border-radius: 4px;
		color: var(--color-text-muted);
		font-size: 0.8rem;
		cursor: pointer;
		width: 100%;
		text-align: left;
	}

	.history-toggle:hover {
		background-color: var(--color-bg-hover);
		color: var(--color-text);
	}

	.toggle-arrow {
		display: inline-block;
		font-size: 0.6rem;
		transition: transform 0.2s ease;
	}

	.toggle-arrow.expanded {
		transform: rotate(90deg);
	}

	.history-list {
		margin-top: 0.5rem;
		border-top: 1px solid var(--color-border-light);
		padding-top: 0.5rem;
		max-height: 300px;
		overflow-y: auto;
		display: flex;
		flex-direction: column;
		gap: 0.35rem;
	}

	.history-empty {
		text-align: center;
		color: var(--color-text-muted);
		font-size: 0.8rem;
		padding: 0.5rem 0;
	}

	.history-entry {
		display: flex;
		align-items: baseline;
		gap: 0.75rem;
		padding: 0.3rem 0.5rem;
		border-radius: 3px;
		font-size: 0.8rem;
	}

	.history-entry:hover {
		background-color: var(--color-bg-hover);
	}

	.history-date {
		color: var(--color-text-muted);
		white-space: nowrap;
		flex-shrink: 0;
	}

	.history-strength {
		font-weight: 600;
		white-space: nowrap;
		flex-shrink: 0;
	}

	.history-notes {
		color: var(--color-text-muted);
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
		flex: 1;
		min-width: 0;
	}

	@media (max-width: 600px) {
		.chart-header {
			flex-direction: column;
		}

		.history-entry {
			flex-wrap: wrap;
		}
	}
</style>
