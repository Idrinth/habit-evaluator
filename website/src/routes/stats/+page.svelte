<script lang="ts">
	import { onMount } from 'svelte';
	import { stats, type DashboardData, type DailyTimelineData, type CorrelationEntry, type EmotionScatterData } from '$lib/api';
	import { goto } from '$app/navigation';

	let data: DashboardData | null = $state(null);
	let timelineData: DailyTimelineData | null = $state(null);
	let correlationData: CorrelationEntry[] | null = $state(null);
	let emotionScatterData: EmotionScatterData | null = $state(null);
	let error: string | null = $state(null);

	onMount(async () => {
		try {
			const [dashboardResult, timelineResult, correlationResult, emotionScatterResult] = await Promise.all([
				stats.dashboard(),
				stats.dailyTimeline(),
				stats.correlations(),
				stats.emotionScatter()
			]);
			data = dashboardResult;
			timelineData = timelineResult;
			correlationData = correlationResult;
			emotionScatterData = emotionScatterResult;
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

	function formatStrength(strength: number): string {
		if (strength === 0) return '0%';
		if (strength > 0) return `+${strength * 10}%`;
		return `${strength * 10}%`;
	}

	function calculateTrendLine(values: number[]): { slope: number; intercept: number } {
		const totalValues = values.length;
		if (totalValues < 2) return { slope: 0, intercept: values[0] || 0 };

		// Only include non-zero data points in the regression (0 means no data)
		let sumX = 0;
		let sumY = 0;
		let sumXY = 0;
		let sumXX = 0;
		let n = 0;

		for (let i = 0; i < totalValues; i++) {
			const value = values[i];
			if (value !== 0) {
				sumX += i;
				sumY += value;
				sumXY += i * value;
				sumXX += i * i;
				n++;
			}
		}

		if (n < 2) {
			// Not enough data points with values, return flat line at average
			const avg = n > 0 ? sumY / n : 0;
			return { slope: 0, intercept: avg };
		}

		const denom = n * sumXX - sumX * sumX;
		if (denom === 0) return { slope: 0, intercept: sumY / n };

		const slope = (n * sumXY - sumX * sumY) / denom;
		const intercept = (sumY - slope * sumX) / n;

		return { slope, intercept };
	}

	function getTrendY(trend: { slope: number; intercept: number }, x: number): number {
		return trend.slope * x + trend.intercept;
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
				{#if true}
					{@const habitTrend = calculateTrendLine(data.habitPoints)}
					{@const habitMax = maxValue(data.habitPoints)}
					<div class="chart-wrapper">
						<svg viewBox="0 0 {data.labels.length * 20} 200" class="bar-chart">
							{#each data.habitPoints as value, i}
								{@const height = habitMax > 0 ? (value / habitMax) * 170 : 0}
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
							{#if data.habitPoints.length >= 2}
								{@const startY = habitMax > 0 ? 180 - (getTrendY(habitTrend, 0) / habitMax) * 170 : 180}
								{@const endY = habitMax > 0 ? 180 - (getTrendY(habitTrend, data.habitPoints.length - 1) / habitMax) * 170 : 180}
								<line
									x1="10"
									y1={Math.max(10, Math.min(180, startY))}
									x2={data.labels.length * 20 - 10}
									y2={Math.max(10, Math.min(180, endY))}
									stroke="#E91E63"
									stroke-width="2"
									stroke-dasharray="4,2"
								/>
							{/if}
						</svg>
						<div class="chart-labels">
							{#each data.labels as label, i}
								{#if i % 5 === 0}
									<span style="left: {(i / data.labels.length) * 100}%">{label}</span>
								{/if}
							{/each}
						</div>
					</div>
				{/if}
			</div>

			<div class="chart-card">
				<h2>Diary Points</h2>
				<p class="chart-avg">Avg: {formatValue(average(data.diaryPoints), 1)}</p>
				{#if true}
					{@const diaryTrend = calculateTrendLine(data.diaryPoints)}
					{@const diaryMax = maxValue(data.diaryPoints)}
					<div class="chart-wrapper">
						<svg viewBox="0 0 {data.labels.length * 20} 200" class="bar-chart">
							{#each data.diaryPoints as value, i}
								{@const height = diaryMax > 0 ? (value / diaryMax) * 170 : 0}
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
							{#if data.diaryPoints.length >= 2}
								{@const startY = diaryMax > 0 ? 180 - (getTrendY(diaryTrend, 0) / diaryMax) * 170 : 180}
								{@const endY = diaryMax > 0 ? 180 - (getTrendY(diaryTrend, data.diaryPoints.length - 1) / diaryMax) * 170 : 180}
								<line
									x1="10"
									y1={Math.max(10, Math.min(180, startY))}
									x2={data.labels.length * 20 - 10}
									y2={Math.max(10, Math.min(180, endY))}
									stroke="#E91E63"
									stroke-width="2"
									stroke-dasharray="4,2"
								/>
							{/if}
						</svg>
						<div class="chart-labels">
							{#each data.labels as label, i}
								{#if i % 5 === 0}
									<span style="left: {(i / data.labels.length) * 100}%">{label}</span>
								{/if}
							{/each}
						</div>
					</div>
				{/if}
			</div>

			<div class="chart-card">
				<h2>Sleep Duration (hours)</h2>
				<p class="chart-avg">Avg: {formatValue(average(data.sleepDuration), 1)}h</p>
				{#if true}
					{@const sleepDurTrend = calculateTrendLine(data.sleepDuration)}
					{@const sleepDurMax = maxValue(data.sleepDuration)}
					<div class="chart-wrapper">
						<svg viewBox="0 0 {data.labels.length * 20} 200" class="bar-chart">
							{#each data.sleepDuration as value, i}
								{@const height = sleepDurMax > 0 ? (value / sleepDurMax) * 170 : 0}
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
							{#if data.sleepDuration.length >= 2}
								{@const startY = sleepDurMax > 0 ? 180 - (getTrendY(sleepDurTrend, 0) / sleepDurMax) * 170 : 180}
								{@const endY = sleepDurMax > 0 ? 180 - (getTrendY(sleepDurTrend, data.sleepDuration.length - 1) / sleepDurMax) * 170 : 180}
								<line
									x1="10"
									y1={Math.max(10, Math.min(180, startY))}
									x2={data.labels.length * 20 - 10}
									y2={Math.max(10, Math.min(180, endY))}
									stroke="#E91E63"
									stroke-width="2"
									stroke-dasharray="4,2"
								/>
							{/if}
						</svg>
						<div class="chart-labels">
							{#each data.labels as label, i}
								{#if i % 5 === 0}
									<span style="left: {(i / data.labels.length) * 100}%">{label}</span>
								{/if}
							{/each}
						</div>
					</div>
				{/if}
			</div>

			<div class="chart-card">
				<h2>Sleep Entries</h2>
				<p class="chart-avg">Avg: {formatValue(average(data.sleepEntries), 1)}</p>
				{#if true}
					{@const sleepEntTrend = calculateTrendLine(data.sleepEntries)}
					{@const sleepEntMax = maxValue(data.sleepEntries)}
					<div class="chart-wrapper">
						<svg viewBox="0 0 {data.labels.length * 20} 200" class="bar-chart">
							{#each data.sleepEntries as value, i}
								{@const height = sleepEntMax > 0 ? (value / sleepEntMax) * 170 : 0}
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
							{#if data.sleepEntries.length >= 2}
								{@const startY = sleepEntMax > 0 ? 180 - (getTrendY(sleepEntTrend, 0) / sleepEntMax) * 170 : 180}
								{@const endY = sleepEntMax > 0 ? 180 - (getTrendY(sleepEntTrend, data.sleepEntries.length - 1) / sleepEntMax) * 170 : 180}
								<line
									x1="10"
									y1={Math.max(10, Math.min(180, startY))}
									x2={data.labels.length * 20 - 10}
									y2={Math.max(10, Math.min(180, endY))}
									stroke="#E91E63"
									stroke-width="2"
									stroke-dasharray="4,2"
								/>
							{/if}
						</svg>
						<div class="chart-labels">
							{#each data.labels as label, i}
								{#if i % 5 === 0}
									<span style="left: {(i / data.labels.length) * 100}%">{label}</span>
								{/if}
							{/each}
						</div>
					</div>
				{/if}
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

		{#if emotionScatterData && emotionScatterData.pairs.length > 0}
			{@const scatterWidth = 480}
			{@const scatterTop = 10}
			{@const scatterBottom = 220}
			{@const scatterHeight = scatterBottom - scatterTop}
			{@const scatterLeftMargin = 35}
			{@const scatterTotalWidth = scatterWidth + scatterLeftMargin}
			{@const scatterCenter = scatterTop + scatterHeight / 2}
			<div class="chart-card scatter-card">
				<h2>Daytime Emotion Distribution</h2>
				<p class="chart-avg">Emotion recordings by time of day over the past 30 days</p>
				<div class="scatter-wrapper">
					<svg viewBox="0 0 {scatterTotalWidth} 250" class="scatter-chart">
						<!-- Y-axis grid lines for emotion strength (-10 to +10) -->
						{#each [-10, -5, 0, 5, 10] as strength}
							{@const y = scatterCenter - (strength / 10) * (scatterHeight / 2)}
							<line
								x1={scatterLeftMargin}
								y1={y}
								x2={scatterTotalWidth}
								y2={y}
								stroke="var(--color-border)"
								stroke-width={strength === 0 ? '1' : '0.5'}
								stroke-dasharray={strength === 0 ? '0' : '2,2'}
							/>
							<text
								x={scatterLeftMargin - 3}
								y={y + 3}
								text-anchor="end"
								font-size="7"
								fill="var(--color-text-muted)"
							>
								{strength > 0 ? '+' : ''}{strength}
							</text>
						{/each}
						<!-- X-axis labels (hours: 00:00, 06:00, 12:00, 18:00, 24:00) -->
						{#each emotionScatterData.labels as label, i}
							{@const hour = i * 6}
							<text
								x={scatterLeftMargin + (hour / 24) * scatterWidth}
								y={248}
								text-anchor="middle"
								font-size="6"
								fill="var(--color-text-muted)"
							>
								{label}
							</text>
						{/each}
						<!-- Scatter points for each emotion pair -->
						{#each emotionScatterData.pairs as pair}
							{#each pair.entries as entry}
								<circle
									cx={scatterLeftMargin + (entry.hour / 24) * scatterWidth}
									cy={scatterCenter - (entry.strength / 10) * (scatterHeight / 2)}
									r="4"
									fill={pair.color}
									opacity="0.7"
								>
									<title>{pair.pairLabel} at {formatHour(entry.hour)}: {formatStrength(entry.strength)}</title>
								</circle>
							{/each}
						{/each}
					</svg>
				</div>
				<div class="scatter-legend">
					{#each emotionScatterData.pairs as pair}
						<span class="legend-item">
							<span class="legend-dot" style="background: {pair.color}"></span>
							{pair.pairLabel}
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

	.scatter-card {
		grid-column: 1 / -1;
		margin-top: 1rem;
	}

	.scatter-wrapper {
		overflow-x: auto;
	}

	.scatter-chart {
		width: 100%;
		height: auto;
		min-width: 400px;
	}

	.scatter-legend {
		display: flex;
		flex-wrap: wrap;
		gap: 0.75rem;
		margin-top: 0.75rem;
		font-size: 0.8rem;
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
