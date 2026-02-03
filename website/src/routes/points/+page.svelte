<script lang="ts">
	import { onMount } from 'svelte';
	import { habits, type Habit, type PointDevelopmentData } from '$lib/api';

	let habitList: Habit[] = $state([]);
	let selectedHabitId: string = $state('');
	let period: 'week' | 'month' = $state('week');
	let chartData: PointDevelopmentData | null = $state(null);
	let loading = $state(true);
	let error = $state('');

	onMount(async () => {
		try {
			habitList = await habits.list();
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to load habits';
		} finally {
			loading = false;
		}
	});

	async function loadChartData() {
		if (!selectedHabitId) {
			chartData = null;
			return;
		}
		error = '';
		try {
			chartData = await habits.pointDevelopment(selectedHabitId, period);
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to load point data';
			chartData = null;
		}
	}

	function handleHabitChange(event: Event) {
		selectedHabitId = (event.target as HTMLSelectElement).value;
		loadChartData();
	}

	function handlePeriodChange(newPeriod: 'week' | 'month') {
		period = newPeriod;
		if (selectedHabitId) {
			loadChartData();
		}
	}

	function barChartHeight(value: number, maxVal: number, minVal: number): number {
		const range = Math.max(maxVal - minVal, 1);
		return Math.abs(value) / range * 100;
	}

	function getMax(data: number[]): number {
		return Math.max(0, ...data);
	}

	function getMin(data: number[]): number {
		return Math.min(0, ...data);
	}
</script>

<div class="container charts-container">
	<h1>Point Development</h1>

	{#if error}
		<p class="error">{error}</p>
	{/if}

	{#if loading}
		<p class="loading-text">Loading habits...</p>
	{:else}
		<div class="controls">
			<select value={selectedHabitId} onchange={handleHabitChange}>
				<option value="">Select a habit</option>
				{#each habitList as habit (habit.id)}
					<option value={habit.id}>{habit.name}</option>
				{/each}
			</select>

			<div class="period-toggle">
				<button
					class:active={period === 'week'}
					onclick={() => handlePeriodChange('week')}
				>Week</button>
				<button
					class:active={period === 'month'}
					onclick={() => handlePeriodChange('month')}
				>Month</button>
			</div>
		</div>

		{#if chartData}
			<div class="stats-row">
				<div class="stat">
					<span class="stat-label">Total Points</span>
					<span class="stat-value">{chartData.totalPoints}</span>
				</div>
				<div class="stat">
					<span class="stat-label">Average</span>
					<span class="stat-value">{chartData.average}</span>
				</div>
			</div>

			<div class="chart-section">
				<h2>Daily Points</h2>
				{@const maxVal = getMax(chartData.dailyPoints)}
				{@const minVal = getMin(chartData.dailyPoints)}
				{@const range = Math.max(maxVal - minVal, 1)}
				{@const positiveRatio = maxVal / range}
				{@const hasNeg = minVal < 0}
				<div class="chart-wrapper">
					<div class="chart" style="--positive-ratio: {positiveRatio}; --has-neg: {hasNeg ? 1 : 0}">
						{#each chartData.dailyPoints as value, i}
							{@const barH = barChartHeight(value, maxVal, minVal)}
							<div class="bar-group">
								{#if value >= 0}
									<div class="bar-area positive">
										<span class="bar-value">{value}</span>
										<div class="bar positive-bar" style="height: {barH}%"></div>
									</div>
								{:else}
									<div class="bar-area negative">
										<div class="bar negative-bar" style="height: {barH}%"></div>
										<span class="bar-value neg-value">{value}</span>
									</div>
								{/if}
								<span class="bar-label">{chartData.labels[i]}</span>
							</div>
						{/each}
						{#if chartData.average !== 0}
							{@const avgPos = ((maxVal - chartData.average) / range) * 100}
							<div class="average-line" style="top: {avgPos}%">
								<span class="average-label">{chartData.average}</span>
							</div>
						{/if}
					</div>
				</div>
			</div>

			<div class="chart-section">
				<h2>Running Average</h2>
				{@const avgMax = getMax(chartData.runningAverages)}
				{@const avgMin = getMin(chartData.runningAverages)}
				{@const avgRange = Math.max(avgMax - avgMin, 1)}
				{@const avgPosRatio = avgMax / avgRange}
				{@const avgHasNeg = avgMin < 0}
				<div class="chart-wrapper">
					<div class="chart" style="--positive-ratio: {avgPosRatio}; --has-neg: {avgHasNeg ? 1 : 0}">
						{#each chartData.runningAverages as value, i}
							{@const barH = barChartHeight(value, avgMax, avgMin)}
							<div class="bar-group">
								{#if value >= 0}
									<div class="bar-area positive">
										<span class="bar-value">{value}</span>
										<div class="bar avg-bar" style="height: {barH}%"></div>
									</div>
								{:else}
									<div class="bar-area negative">
										<div class="bar avg-bar-neg" style="height: {barH}%"></div>
										<span class="bar-value neg-value">{value}</span>
									</div>
								{/if}
								<span class="bar-label">{chartData.labels[i]}</span>
							</div>
						{/each}
					</div>
				</div>
			</div>

			<div class="chart-section">
				<h2>Cumulative Total</h2>
				{@const cumMax = getMax(chartData.cumulativeTotals)}
				{@const cumMin = getMin(chartData.cumulativeTotals)}
				{@const cumRange = Math.max(cumMax - cumMin, 1)}
				{@const cumPosRatio = cumMax / cumRange}
				{@const cumHasNeg = cumMin < 0}
				<div class="chart-wrapper">
					<div class="chart" style="--positive-ratio: {cumPosRatio}; --has-neg: {cumHasNeg ? 1 : 0}">
						{#each chartData.cumulativeTotals as value, i}
							{@const barH = barChartHeight(value, cumMax, cumMin)}
							<div class="bar-group">
								{#if value >= 0}
									<div class="bar-area positive">
										<span class="bar-value">{value}</span>
										<div class="bar cumulative-bar" style="height: {barH}%"></div>
									</div>
								{:else}
									<div class="bar-area negative">
										<div class="bar cumulative-bar-neg" style="height: {barH}%"></div>
										<span class="bar-value neg-value">{value}</span>
									</div>
								{/if}
								<span class="bar-label">{chartData.labels[i]}</span>
							</div>
						{/each}
					</div>
				</div>
			</div>
		{:else if selectedHabitId}
			<p class="loading-text">Loading chart data...</p>
		{:else}
			<p class="loading-text">Select a habit to view point development charts.</p>
		{/if}
	{/if}
</div>

<style>
	.charts-container {
		max-width: 700px;
		padding-bottom: 2rem;
	}

	.controls {
		display: flex;
		gap: 1rem;
		align-items: center;
		margin-bottom: 1.5rem;
		flex-wrap: wrap;
	}

	.controls select {
		flex: 1;
		min-width: 180px;
		margin-bottom: 0;
	}

	.period-toggle {
		display: flex;
		border: 1px solid var(--color-border);
		border-radius: 4px;
		overflow: hidden;
	}

	.period-toggle button {
		padding: 0.4rem 1rem;
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

	.stats-row {
		display: flex;
		gap: 1.5rem;
		margin-bottom: 1.5rem;
	}

	.stat {
		display: flex;
		flex-direction: column;
		align-items: center;
		background: var(--color-bg-hover);
		border: 1px solid var(--color-border-light);
		border-radius: 6px;
		padding: 0.75rem 1.5rem;
		flex: 1;
	}

	.stat-label {
		font-size: 0.8rem;
		color: var(--color-text-muted);
	}

	.stat-value {
		font-size: 1.5rem;
		font-weight: bold;
		color: var(--color-text);
	}

	.chart-section {
		margin-bottom: 2rem;
	}

	.chart-section h2 {
		font-size: 1rem;
		color: var(--color-text-secondary);
		margin-bottom: 0.5rem;
	}

	.chart-wrapper {
		border: 1px solid var(--color-border-light);
		border-radius: 6px;
		padding: 1rem 0.5rem 0.25rem 0.5rem;
		background: var(--color-bg-hover);
		overflow-x: auto;
	}

	.chart {
		display: flex;
		align-items: flex-end;
		height: 180px;
		gap: 2px;
		position: relative;
		padding-bottom: 1.5rem;
	}

	.bar-group {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		height: 100%;
		min-width: 18px;
		position: relative;
	}

	.bar-area {
		display: flex;
		flex-direction: column;
		align-items: center;
		width: 100%;
		flex: 1;
	}

	.bar-area.positive {
		justify-content: flex-end;
	}

	.bar-area.negative {
		justify-content: flex-start;
	}

	.bar {
		width: 70%;
		min-width: 8px;
		max-width: 40px;
		border-radius: 3px 3px 0 0;
		transition: height 0.3s ease;
	}

	.positive-bar {
		background-color: var(--color-primary);
	}

	.negative-bar {
		background-color: var(--color-error);
		border-radius: 0 0 3px 3px;
	}

	.avg-bar {
		background-color: var(--color-success);
	}

	.avg-bar-neg {
		background-color: var(--color-error);
		border-radius: 0 0 3px 3px;
	}

	.cumulative-bar {
		background-color: #8b5cf6;
	}

	.cumulative-bar-neg {
		background-color: var(--color-error);
		border-radius: 0 0 3px 3px;
	}

	.bar-value {
		font-size: 0.65rem;
		color: var(--color-text-muted);
		margin-bottom: 2px;
	}

	.neg-value {
		margin-top: 2px;
		margin-bottom: 0;
	}

	.bar-label {
		font-size: 0.65rem;
		color: var(--color-text-muted);
		position: absolute;
		bottom: 0;
		white-space: nowrap;
	}

	.average-line {
		position: absolute;
		left: 0;
		right: 0;
		border-top: 2px dashed var(--color-error);
		pointer-events: none;
	}

	.average-label {
		position: absolute;
		right: 0;
		top: -14px;
		font-size: 0.65rem;
		color: var(--color-error);
	}

	.loading-text {
		text-align: center;
		color: var(--color-text-placeholder);
		padding: 2rem 0;
	}
</style>
