<script lang="ts">
	import { onMount } from 'svelte';
	import { getLanguage, t, type Language } from '$lib/i18n';

	let lang: Language = $state('en');

	onMount(() => {
		lang = getLanguage();
	});
</script>

<svelte:head>
	<title>{t('imprint.title', lang)} - Habit Evaluator</title>
</svelte:head>

<h1>{t('imprint.title', lang)}</h1>

<section>
	<h2>{t('imprint.contact', lang)}</h2>
	<dl>
		<dt>{t('imprint.name', lang)}</dt>
		<dd>Bj&ouml;rn &lsquo;Idrinth&rsquo; Buttner</dd>
		<dt>{t('imprint.email', lang)}</dt>
		<dd><a href="mailto:self@idrinth.de">self@idrinth.de</a></dd>
	</dl>
</section>

<section>
	<h2>{t('imprint.license', lang)}</h2>
	<p>{t('imprint.licenseText', lang)}</p>
</section>

<section>
	<h2>{t('imprint.dataProtection', lang)}</h2>
	<ul>
		<li>{t('imprint.noRemoteStorage', lang)}</li>
		<li>{t('imprint.noTracking', lang)}</li>
		<li>{t('imprint.noDataUsage', lang)}</li>
	</ul>
</section>

<style>
	h1 {
		font-size: 2rem;
		margin-bottom: 1.5rem;
	}
	section {
		margin-bottom: 2rem;
	}
	h2 {
		font-size: 1.3rem;
		margin-bottom: 0.75rem;
		color: var(--color-primary);
	}
	dl {
		margin: 0;
	}
	dt {
		font-weight: 600;
		margin-top: 0.5rem;
	}
	dd {
		margin-left: 0;
		color: var(--color-text-muted);
	}
	ul {
		padding-left: 1.5rem;
	}
	li {
		margin-bottom: 0.4rem;
		color: var(--color-text-muted);
	}
</style>
