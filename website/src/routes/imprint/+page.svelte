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

<section>
	<h2>{t('imprint.disclaimers', lang)}</h2>
	<ul>
		<li>{t('imprint.notProfessionalHelp', lang)}</li>
		<li>{t('imprint.noDataSharing', lang)}</li>
		<li>{t('imprint.variantDisclaimer', lang)}</li>
	</ul>
</section>

<section>
	<h2>{t('imprint.libraries', lang)}</h2>
	<table>
		<thead>
			<tr>
				<th>{t('imprint.libraryName', lang)}</th>
				<th>{t('imprint.libraryVersion', lang)}</th>
				<th>{t('imprint.libraryLicense', lang)}</th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td>Svelte</td>
				<td>5.49.1</td>
				<td><a href="https://github.com/sveltejs/svelte/blob/main/LICENSE.md">MIT</a></td>
			</tr>
			<tr>
				<td>SvelteKit</td>
				<td>2.50.1</td>
				<td><a href="https://github.com/sveltejs/kit/blob/main/LICENSE">MIT</a></td>
			</tr>
			<tr>
				<td>SvelteKit Adapter Static</td>
				<td>3.0.10</td>
				<td><a href="https://github.com/sveltejs/kit/blob/main/LICENSE">MIT</a></td>
			</tr>
			<tr>
				<td>Svelte Vite Plugin</td>
				<td>6.2.4</td>
				<td><a href="https://github.com/sveltejs/vite-plugin-svelte/blob/main/LICENSE">MIT</a></td>
			</tr>
			<tr>
				<td>svelte-check</td>
				<td>4.3.6</td>
				<td><a href="https://github.com/sveltejs/language-tools/blob/master/LICENSE">MIT</a></td>
			</tr>
			<tr>
				<td>TypeScript</td>
				<td>5.9.3</td>
				<td><a href="https://github.com/microsoft/TypeScript/blob/main/LICENSE.txt">Apache-2.0</a></td>
			</tr>
			<tr>
				<td>Vite</td>
				<td>7.3.1</td>
				<td><a href="https://github.com/vitejs/vite/blob/main/LICENSE">MIT</a></td>
			</tr>
		</tbody>
	</table>
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
	table {
		width: 100%;
		border-collapse: collapse;
		margin-top: 0.5rem;
	}
	th, td {
		text-align: left;
		padding: 0.4rem 0.75rem;
		border-bottom: 1px solid var(--color-border, #ddd);
	}
	th {
		font-weight: 600;
	}
	td {
		color: var(--color-text-muted);
	}
</style>
