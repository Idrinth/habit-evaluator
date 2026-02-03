<script lang="ts">
	import { onMount } from 'svelte';
	import { stats, type DashboardData } from '$lib/api';
	import { goto } from '$app/navigation';

	let data: DashboardData | null = $state(null);
	let error: string | null = $state(null);

	onMount(async () => {
		try {
			data = await stats.dashboard();
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

	@media (max-width: 600px) {
		.charts-grid {
			grid-template-columns: 1fr;
		}
	}
</style>
