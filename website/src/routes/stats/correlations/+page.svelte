<script lang="ts">
	import { onMount } from 'svelte';
	import { stats, type CorrelationEntry } from '$lib/api';
	import { goto } from '$app/navigation';
	import { t, getLanguage, type Language } from '$lib/i18n';

	let correlationData: CorrelationEntry[] | null = $state(null);
	let error: string | null = $state(null);
	let lang: Language = $state(getLanguage());

	onMount(async () => {
		try {
			correlationData = await stats.correlations();
		} catch (e) {
			if (e instanceof Error && e.message.includes('401')) {
				goto('/login');
				return;
			}
			error = e instanceof Error ? e.message : 'Failed to load correlations';
		}
	});

	function confidenceLevel(corr: CorrelationEntry): 'strong' | 'moderate' | 'weak' {
		const abs = Math.abs(corr.correlation);
		if (abs >= 0.5) return 'strong';
		if (abs >= 0.3) return 'moderate';
		return 'weak';
	}

	function confidenceLabel(level: 'strong' | 'moderate' | 'weak'): string {
		return t(`correlations.${level}`, lang);
	}
</script>

<div class="correlations-container">
	<a href="/stats" class="back-link">{t('correlations.backToStats', lang)}</a>
	<h1>{t('correlations.title', lang)}</h1>
	<p class="description">{t('correlations.description', lang)}</p>

	{#if error}
		<p class="error">{error}</p>
	{:else if !correlationData}
		<p class="loading">{t('correlations.loading', lang)}</p>
	{:else if correlationData.length === 0}
		<p class="no-data">{t('correlations.noData', lang)}</p>
	{:else}
		<p class="disclaimer">{t('correlations.disclaimer', lang)}</p>

		<table class="correlation-table">
			<thead>
				<tr>
					<th>{t('correlations.eventA', lang)}</th>
					<th>{t('correlations.eventB', lang)}</th>
					<th>{t('correlations.correlation', lang)}</th>
					<th>{t('correlations.sharedDays', lang)}</th>
					<th>{t('correlations.confidence', lang)}</th>
				</tr>
			</thead>
			<tbody>
				{#each correlationData as corr}
					{@const level = confidenceLevel(corr)}
					<tr class:weak-row={level === 'weak'}>
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
						<td>
							<span class="confidence-badge confidence-{level}">
								{confidenceLabel(level)}
							</span>
						</td>
					</tr>
				{/each}
			</tbody>
		</table>
	{/if}
</div>

<style>
	.correlations-container {
		width: 100%;
		max-width: 900px;
		padding: 1rem;
	}

	.back-link {
		display: inline-block;
		margin-bottom: 1rem;
		color: var(--color-link, #4CAF50);
		text-decoration: none;
		font-size: 0.9rem;
	}

	.back-link:hover {
		text-decoration: underline;
	}

	h1 {
		margin-bottom: 0.5rem;
	}

	.description {
		color: var(--color-text-muted);
		margin-bottom: 1rem;
		font-size: 0.9rem;
	}

	.disclaimer {
		background: var(--color-bg-card);
		border-left: 4px solid #FF9800;
		padding: 0.75rem 1rem;
		margin-bottom: 1.5rem;
		font-size: 0.85rem;
		color: var(--color-text-muted);
		border-radius: 0 4px 4px 0;
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

	.correlation-table {
		width: 100%;
		border-collapse: collapse;
		font-size: 0.85rem;
		background: var(--color-bg-card);
		border-radius: 8px;
		overflow: hidden;
		box-shadow: 0 2px 8px var(--color-shadow);
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

	.weak-row {
		opacity: 0.7;
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

	.confidence-badge {
		display: inline-block;
		padding: 0.15rem 0.5rem;
		border-radius: 10px;
		font-size: 0.75rem;
		font-weight: 500;
	}

	.confidence-strong {
		background: #4CAF50;
		color: #fff;
	}

	.confidence-moderate {
		background: #FF9800;
		color: #fff;
	}

	.confidence-weak {
		background: var(--color-border);
		color: var(--color-text-muted);
	}

	@media (max-width: 600px) {
		.correlation-table {
			font-size: 0.75rem;
		}

		.correlation-table th,
		.correlation-table td {
			padding: 0.4rem 0.5rem;
		}

		.corr-bar-wrapper {
			width: 40px;
		}
	}
</style>
