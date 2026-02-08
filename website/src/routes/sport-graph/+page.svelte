<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { sportLogs, type SportLogGraphData } from '$lib/api';

	let data: SportLogGraphData | null = $state(null);
	let error: string | null = $state(null);

	onMount(async () => {
		try {
			data = await sportLogs.graph();
		} catch (e) {
			if (e instanceof Error && e.message.includes('401')) {
				goto('/login');
				return;
			}
			error = e instanceof Error ? e.message : 'Failed to load sport data';
		}
	});

	function maxValue(values: number[]): number {
		const m = Math.max(...values);
		return m > 0 ? m : 1;
	}

	function average(values: number[]): number {
		const nonZero = values.filter((v) => v > 0);
		if (nonZero.length === 0) return 0;
		return nonZero.reduce((a, b) => a + b, 0) / nonZero.length;
	}

	function calculateTrendLine(values: number[]): { slope: number; intercept: number } {
		const totalValues = values.length;
		if (totalValues < 2) return { slope: 0, intercept: values[0] || 0 };

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

	function findDataBounds(values: number[]): { first: number; last: number } {
		let first = -1;
		let last = -1;
		for (let i = 0; i < values.length; i++) {
			if (values[i] !== 0) {
				if (first === -1) first = i;
				last = i;
			}
		}
		return { first, last };
	}
</script>

<div class="sport-graph-container">
	<h1>Sport Activity Graph</h1>
	<p class="subtitle">Daily sport duration and measurement over the past 30 days</p>

	{#if error}
		<p class="error">{error}</p>
	{:else if !data}
		<p class="loading">Loading...</p>
	{:else if data.activities.length === 0}
		<p class="no-data">No sport log entries found. Add sport activities to see graphs here.</p>
	{:else}
		{#each data.activities as activity}
			<div class="activity-section">
				<h2 class="activity-name" style="border-left: 4px solid {activity.color}; padding-left: 0.5rem;">
					{activity.name}
				</h2>

				<div class="charts-grid">
					<div class="chart-card">
						<h3>Duration (hours)</h3>
						<p class="chart-avg">Avg: {average(activity.dailyDuration).toFixed(1)}h</p>
						{@const durTrend = calculateTrendLine(activity.dailyDuration)}
						{@const durMax = maxValue(activity.dailyDuration)}
						<div class="chart-wrapper">
							<svg viewBox="0 0 {data.labels.length * 20} 200" class="bar-chart">
								{#each activity.dailyDuration as value, i}
									{@const height = durMax > 0 ? (value / durMax) * 170 : 0}
									<rect
										x={i * 20 + 2}
										y={180 - height}
										width="16"
										height={Math.max(height, 0)}
										fill={activity.color}
										rx="2"
									>
										<title>{data.labels[i]}: {value.toFixed(1)}h</title>
									</rect>
								{/each}
								<line x1="0" y1="180" x2={data.labels.length * 20} y2="180" stroke="var(--color-border)" stroke-width="1" />
								{#if activity.dailyDuration.length >= 2}
									{@const bounds = findDataBounds(activity.dailyDuration)}
									{#if bounds.first !== -1 && bounds.last !== -1 && bounds.first !== bounds.last}
										{@const startY = durMax > 0 ? 180 - (getTrendY(durTrend, bounds.first) / durMax) * 170 : 180}
										{@const endY = durMax > 0 ? 180 - (getTrendY(durTrend, bounds.last) / durMax) * 170 : 180}
										<line
											x1={bounds.first * 20 + 10}
											y1={Math.max(10, Math.min(180, startY))}
											x2={bounds.last * 20 + 10}
											y2={Math.max(10, Math.min(180, endY))}
											stroke="#E91E63"
											stroke-width="2"
											stroke-dasharray="4,2"
										/>
									{/if}
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
					</div>

					<div class="chart-card">
						<h3>Measurement ({activity.unit})</h3>
						<p class="chart-avg">Avg: {average(activity.dailyMeasurement).toFixed(1)} {activity.unit}</p>
						{@const measTrend = calculateTrendLine(activity.dailyMeasurement)}
						{@const measMax = maxValue(activity.dailyMeasurement)}
						<div class="chart-wrapper">
							<svg viewBox="0 0 {data.labels.length * 20} 200" class="bar-chart">
								{#each activity.dailyMeasurement as value, i}
									{@const height = measMax > 0 ? (value / measMax) * 170 : 0}
									<rect
										x={i * 20 + 2}
										y={180 - height}
										width="16"
										height={Math.max(height, 0)}
										fill={activity.color}
										opacity="0.7"
										rx="2"
									>
										<title>{data.labels[i]}: {value.toFixed(1)} {activity.unit}</title>
									</rect>
								{/each}
								<line x1="0" y1="180" x2={data.labels.length * 20} y2="180" stroke="var(--color-border)" stroke-width="1" />
								{#if activity.dailyMeasurement.length >= 2}
									{@const bounds = findDataBounds(activity.dailyMeasurement)}
									{#if bounds.first !== -1 && bounds.last !== -1 && bounds.first !== bounds.last}
										{@const startY = measMax > 0 ? 180 - (getTrendY(measTrend, bounds.first) / measMax) * 170 : 180}
										{@const endY = measMax > 0 ? 180 - (getTrendY(measTrend, bounds.last) / measMax) * 170 : 180}
										<line
											x1={bounds.first * 20 + 10}
											y1={Math.max(10, Math.min(180, startY))}
											x2={bounds.last * 20 + 10}
											y2={Math.max(10, Math.min(180, endY))}
											stroke="#E91E63"
											stroke-width="2"
											stroke-dasharray="4,2"
										/>
									{/if}
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
					</div>
				</div>
			</div>
		{/each}
	{/if}
</div>

<style>
	.sport-graph-container {
		width: 100%;
		max-width: 900px;
		padding: 1rem;
	}

	h1 {
		text-align: center;
		margin-bottom: 0.25rem;
	}

	.subtitle {
		text-align: center;
		color: var(--color-text-muted);
		font-size: 0.9rem;
		margin-bottom: 1.5rem;
	}

	.loading {
		text-align: center;
		color: var(--color-text-muted);
	}

	.no-data {
		text-align: center;
		color: var(--color-text-muted);
		padding: 2rem;
	}

	.activity-section {
		margin-bottom: 2rem;
	}

	.activity-name {
		font-size: 1.1rem;
		margin: 0 0 0.75rem 0;
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

	.chart-card h3 {
		font-size: 0.95rem;
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

	@media (max-width: 600px) {
		.charts-grid {
			grid-template-columns: 1fr;
		}
	}
</style>
