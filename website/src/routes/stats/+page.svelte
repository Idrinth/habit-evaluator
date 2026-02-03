<script lang="ts">
	import { onMount } from 'svelte';
	import { stats, type DashboardData, type DailyTimelineData, type CorrelationEntry } from '$lib/api';
	import { goto } from '$app/navigation';

	let data: DashboardData | null = $state(null);
	let timelineData: DailyTimelineData | null = $state(null);
	let correlationData: CorrelationEntry[] | null = $state(null);
	let error: string | null = $state(null);

	onMount(async () => {
		try {
			const [dashboardResult, timelineResult, correlationResult] = await Promise.all([
				stats.dashboard(),
				stats.dailyTimeline(),
				stats.correlations()
			]);
			data = dashboardResult;
			timelineData = timelineResult;
			correlationData = correlationResult;
		} catch (e) {
			if (e instanceof Error && e.message.includes('401')) {
				goto('/login');
				return;
			}
			error = e instanceof Error ? e.message : 'Failed to load statistics';
		}
	});

	function maxValue(values: number[]): number {
		const m = Math.max(...values);
		return m > 0 ? m : 1;
	}

	function formatValue(value: number, decimals: number = 0): string {
		return decimals > 0 ? value.toFixed(decimals) : String(value);
	}

	function average(values: number[]): number {
		const nonZero = values.filter((v) => v > 0);
		if (nonZero.length === 0) return 0;
		return nonZero.reduce((a, b) => a + b, 0) / nonZero.length;
	}

	function formatHour(hour: number): string {
		const h = Math.floor(hour);
		const m = Math.round((hour - h) * 60);
		return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
	}
</script>

<div class="stats-container">
	<h1>Statistics Dashboard</h1>

	{#if error}
		<p class="error">{error}</p>
	{:else if !data}
		<p class="loading">Loading...</p>
	{:else}
		<div class="charts-grid">
			<div class="chart-card">
				<h2>Habit Points</h2>
				<p class="chart-avg">Avg: {formatValue(average(data.habitPoints), 1)}</p>
				<div class="chart-wrapper">
					<svg viewBox="0 0 {data.labels.length * 20} 200" class="bar-chart">
						{#each data.habitPoints as value, i}
							{@const height = maxValue(data.habitPoints) > 0 ? (value / maxValue(data.habitPoints)) * 170 : 0}
							<rect
								x={i * 20 + 2}
								y={180 - height}
								width="16"
								height={Math.max(height, 0)}
								fill="#4CAF50"
								rx="2"
							>
								<title>{data.labels[i]}: {value}</title>
							</rect>
						{/each}
						<line x1="0" y1="180" x2={data.labels.length * 20} y2="180" stroke="var(--color-border)" stroke-width="1" />
					</svg>
					<div class="chart-labels">
						{#each data.labels as label, i}
							{#if i % 5 === 0}
								<span style="left: {(i / data.labels.length) * 100}%">{label}</span>
							{/if}
						{/each}
					</div>
				</div>
			</div>

			<div class="chart-card">
				<h2>Diary Points</h2>
				<p class="chart-avg">Avg: {formatValue(average(data.diaryPoints), 1)}</p>
				<div class="chart-wrapper">
					<svg viewBox="0 0 {data.labels.length * 20} 200" class="bar-chart">
						{#each data.diaryPoints as value, i}
							{@const height = maxValue(data.diaryPoints) > 0 ? (value / maxValue(data.diaryPoints)) * 170 : 0}
							<rect
								x={i * 20 + 2}
								y={180 - height}
								width="16"
								height={Math.max(height, 0)}
								fill="#66BB6A"
								rx="2"
							>
								<title>{data.labels[i]}: {value}</title>
							</rect>
						{/each}
						<line x1="0" y1="180" x2={data.labels.length * 20} y2="180" stroke="var(--color-border)" stroke-width="1" />
					</svg>
					<div class="chart-labels">
						{#each data.labels as label, i}
							{#if i % 5 === 0}
								<span style="left: {(i / data.labels.length) * 100}%">{label}</span>
							{/if}
						{/each}
					</div>
				</div>
			</div>

			<div class="chart-card">
				<h2>Sleep Duration (hours)</h2>
				<p class="chart-avg">Avg: {formatValue(average(data.sleepDuration), 1)}h</p>
				<div class="chart-wrapper">
					<svg viewBox="0 0 {data.labels.length * 20} 200" class="bar-chart">
						{#each data.sleepDuration as value, i}
							{@const height = maxValue(data.sleepDuration) > 0 ? (value / maxValue(data.sleepDuration)) * 170 : 0}
							<rect
								x={i * 20 + 2}
								y={180 - height}
								width="16"
								height={Math.max(height, 0)}
								fill="#42A5F5"
								rx="2"
							>
								<title>{data.labels[i]}: {value.toFixed(1)}h</title>
							</rect>
						{/each}
						<line x1="0" y1="180" x2={data.labels.length * 20} y2="180" stroke="var(--color-border)" stroke-width="1" />
					</svg>
					<div class="chart-labels">
						{#each data.labels as label, i}
							{#if i % 5 === 0}
								<span style="left: {(i / data.labels.length) * 100}%">{label}</span>
							{/if}
						{/each}
					</div>
				</div>
			</div>

			<div class="chart-card">
				<h2>Sleep Entries</h2>
				<p class="chart-avg">Avg: {formatValue(average(data.sleepEntries), 1)}</p>
				<div class="chart-wrapper">
					<svg viewBox="0 0 {data.labels.length * 20} 200" class="bar-chart">
						{#each data.sleepEntries as value, i}
							{@const height = maxValue(data.sleepEntries) > 0 ? (value / maxValue(data.sleepEntries)) * 170 : 0}
							<rect
								x={i * 20 + 2}
								y={180 - height}
								width="16"
								height={Math.max(height, 0)}
								fill="#81C784"
								rx="2"
							>
								<title>{data.labels[i]}: {value}</title>
							</rect>
						{/each}
						<line x1="0" y1="180" x2={data.labels.length * 20} y2="180" stroke="var(--color-border)" stroke-width="1" />
					</svg>
					<div class="chart-labels">
						{#each data.labels as label, i}
							{#if i % 5 === 0}
								<span style="left: {(i / data.labels.length) * 100}%">{label}</span>
							{/if}
						{/each}
					</div>
				</div>
			</div>
		</div>

		{#if timelineData && timelineData.habits.length > 0}
			{@const chartWidth = timelineData.labels.length * 20}
			{@const plotTop = 10}
			{@const plotBottom = 240}
			{@const plotHeight = plotBottom - plotTop}
			{@const leftMargin = 35}
			{@const totalWidth = chartWidth + leftMargin}
			<div class="chart-card timeline-card">
				<h2>Daily Activity Timeline</h2>
				<div class="timeline-wrapper">
					<svg viewBox="0 0 {totalWidth} 260" class="timeline-chart">
						{#each [0, 4, 8, 12, 16, 20, 24] as hour}
							{@const y = plotTop + (hour / 24) * plotHeight}
							<line
								x1={leftMargin}
								y1={y}
								x2={totalWidth}
								y2={y}
								stroke="var(--color-border)"
								stroke-width="0.5"
								stroke-dasharray="2,2"
							/>
							<text
								x={leftMargin - 3}
								y={y + 3}
								text-anchor="end"
								font-size="7"
								fill="var(--color-text-muted)"
							>
								{String(hour).padStart(2, '0')}:00
							</text>
						{/each}
						{#each timelineData.labels as label, i}
							{#if i % 5 === 0}
								<text
									x={leftMargin + i * 20 + 10}
									y={258}
									text-anchor="middle"
									font-size="6"
									fill="var(--color-text-muted)"
								>
									{label}
								</text>
							{/if}
						{/each}
						{#each timelineData.habits as habit}
							{#each habit.entries as entry}
								<circle
									cx={leftMargin + entry.dayIndex * 20 + 10}
									cy={plotTop + (entry.hour / 24) * plotHeight}
									r="3.5"
									fill={habit.color}
									opacity="0.8"
								>
									<title>{habit.habitName} - {timelineData.labels[entry.dayIndex]} at {formatHour(entry.hour)}</title>
								</circle>
							{/each}
						{/each}
					</svg>
				</div>
				<div class="timeline-legend">
					{#each timelineData.habits as habit}
						<span class="legend-item">
							<span class="legend-dot" style="background: {habit.color}"></span>
							{habit.habitName}
						</span>
					{/each}
				</div>
			</div>
		{/if}

		{#if correlationData && correlationData.length > 0}
			<div class="chart-card correlation-card">
				<h2>Event Correlations (Yearly, Time-Weighted)</h2>
				<p class="chart-avg">Top {correlationData.length} strongest correlations — newer data weighted higher</p>
				<table class="correlation-table">
					<thead>
						<tr>
							<th>Event A</th>
							<th>Event B</th>
							<th>Correlation</th>
							<th>Shared Days</th>
						</tr>
					</thead>
					<tbody>
						{#each correlationData as corr}
							<tr>
								<td>{corr.eventA}</td>
								<td>{corr.eventB}</td>
								<td>
									<span class="corr-bar-wrapper">
										<span
											class="corr-bar"
											class:positive={corr.correlation > 0}
											class:negative={corr.correlation < 0}
											style="width: {Math.abs(corr.correlation) * 100}%"
										></span>
									</span>
									<span class="corr-value">{corr.correlation > 0 ? '+' : ''}{corr.correlation.toFixed(3)}</span>
								</td>
								<td class="shared-days">{corr.sharedDays}</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		{/if}
	{/if}
</div>

<style>
	.stats-container {
		width: 100%;
		max-width: 900px;
		padding: 1rem;
	}

	h1 {
		text-align: center;
		margin-bottom: 1.5rem;
	}

	.loading {
		text-align: center;
		color: var(--color-text-muted);
	}

	.charts-grid {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 1rem;
	}

	.chart-card {
		background: var(--color-bg-card);
		border-radius: 8px;
		padding: 1rem;
		box-shadow: 0 2px 8px var(--color-shadow);
	}

	.chart-card h2 {
		font-size: 1rem;
		margin: 0 0 0.25rem 0;
		color: var(--color-text);
	}

	.chart-avg {
		font-size: 0.85rem;
		color: var(--color-text-muted);
		margin: 0 0 0.5rem 0;
	}

	.chart-wrapper {
		position: relative;
	}

	.bar-chart {
		width: 100%;
		height: auto;
	}

	.chart-labels {
		position: relative;
		height: 1.5rem;
		font-size: 0.65rem;
		color: var(--color-text-muted);
	}

	.chart-labels span {
		position: absolute;
		transform: translateX(-50%);
	}

	.timeline-card {
		grid-column: 1 / -1;
		margin-top: 1rem;
	}

	.timeline-wrapper {
		overflow-x: auto;
	}

	.timeline-chart {
		width: 100%;
		height: auto;
		min-width: 400px;
	}

	.timeline-legend {
		display: flex;
		flex-wrap: wrap;
		gap: 0.75rem;
		margin-top: 0.75rem;
		font-size: 0.8rem;
	}

	.legend-item {
		display: flex;
		align-items: center;
		gap: 0.3rem;
		color: var(--color-text);
	}

	.legend-dot {
		display: inline-block;
		width: 10px;
		height: 10px;
		border-radius: 50%;
	}

	.correlation-card {
		grid-column: 1 / -1;
		margin-top: 1rem;
	}

	.correlation-table {
		width: 100%;
		border-collapse: collapse;
		font-size: 0.85rem;
	}

	.correlation-table th,
	.correlation-table td {
		padding: 0.5rem 0.75rem;
		text-align: left;
		border-bottom: 1px solid var(--color-border);
	}

	.correlation-table th {
		font-weight: 600;
		color: var(--color-text-muted);
		font-size: 0.75rem;
		text-transform: uppercase;
		letter-spacing: 0.03em;
	}

	.correlation-table td {
		color: var(--color-text);
	}

	.corr-bar-wrapper {
		display: inline-block;
		width: 60px;
		height: 8px;
		background: var(--color-border);
		border-radius: 4px;
		vertical-align: middle;
		margin-right: 0.5rem;
		overflow: hidden;
	}

	.corr-bar {
		display: block;
		height: 100%;
		border-radius: 4px;
	}

	.corr-bar.positive {
		background: #4CAF50;
	}

	.corr-bar.negative {
		background: #E91E63;
	}

	.corr-value {
		font-variant-numeric: tabular-nums;
	}

	.shared-days {
		text-align: center;
	}

	@media (max-width: 600px) {
		.charts-grid {
			grid-template-columns: 1fr;
		}
	}
</style>
