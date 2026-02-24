<script lang="ts">
	import { getLanguage, t, type Language } from '$lib/i18n';

	let lang: Language = $state('en');

	import { onMount } from 'svelte';
	onMount(() => {
		lang = getLanguage();
	});
</script>

<div class="container planner-container">
	<h1>{t('nav.planner', lang)}</h1>

	<div class="planner-cards">
		<a href="/planner/week" class="planner-card">
			<h2>{t('planner.weekPlanner', lang)}</h2>
			<p>{t('planner.weekPlannerDescription', lang)}</p>
		</a>
		<a href="/planner/activities" class="planner-card">
			<h2>{t('planner.activities', lang)}</h2>
			<p>{t('planner.activitiesDescription', lang)}</p>
		</a>
		<a href="/planner/groups" class="planner-card">
			<h2>{t('planner.groups', lang)}</h2>
			<p>{t('planner.groupsDescription', lang)}</p>
		</a>
	</div>
</div>

<style>
	.planner-container {
		max-width: 650px;
	}

	.planner-cards {
		display: flex;
		flex-direction: column;
		gap: 1rem;
		margin-top: 1rem;
	}

	.planner-card {
		display: block;
		border: 1px solid var(--color-border-light);
		border-radius: 8px;
		padding: 1.25rem;
		text-decoration: none;
		color: inherit;
		transition: background-color 0.15s;
	}

	.planner-card:hover {
		background-color: var(--color-bg-hover);
	}

	.planner-card h2 {
		margin: 0 0 0.25rem 0;
		font-size: 1.1rem;
	}

	.planner-card p {
		margin: 0;
		font-size: 0.9rem;
		color: var(--color-text-muted);
	}
</style>
