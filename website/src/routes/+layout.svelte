<script lang="ts">
	import '../app.css';
	import { auth, moduleVisibility as moduleVisibilityApi, type ModuleVisibility } from '$lib/api';
	import { invalidate } from '$app/navigation';
	import { onMount, onDestroy } from 'svelte';
	import { browser } from '$app/environment';
	import { LANGUAGES, getLanguage, setLanguage, t, type Language } from '$lib/i18n';
	import type { Snippet } from 'svelte';

	let { data, children }: { data: { loggedIn: boolean; username: string }; children: Snippet } =
		$props();

	let themeMode = $state('system');
	let lang: Language = $state('en');
	let customTranslations = $state(false);

	let mv: ModuleVisibility = $state({
		diaryVisible: true,
		sleepVisible: true,
		emotionsVisible: true,
		pointsVisible: true,
		statisticsVisible: true,
		foodLogVisible: true,
		sportLogVisible: true,
		medicationVisible: true,
		backupVisible: true,
		pdfExportVisible: true,
		activityLogVisible: true
	});

	function handleVisibilityChanged(event: Event) {
		const detail = (event as CustomEvent<ModuleVisibility>).detail;
		mv = { ...detail };
	}

	onMount(() => {
		const saved = localStorage.getItem('theme');
		if (saved === 'light' || saved === 'dark') {
			themeMode = saved;
			document.documentElement.setAttribute('data-theme', saved);
		}
		lang = getLanguage();
		customTranslations = localStorage.getItem('customTranslations') === 'true';

		const savedVisibility = localStorage.getItem('moduleVisibility');
		if (savedVisibility) {
			try {
				const parsed = JSON.parse(savedVisibility);
				mv = { ...mv, ...parsed };
			} catch {
				// ignore
			}
		}

		if (data.loggedIn) {
			moduleVisibilityApi.get().then((settings) => {
				mv = { ...mv, ...settings };
				localStorage.setItem('moduleVisibility', JSON.stringify(mv));
			}).catch(() => {
				// use cached or defaults
			});
		}

		window.addEventListener('module-visibility-changed', handleVisibilityChanged);
	});

	onDestroy(() => {
		if (browser) {
			window.removeEventListener('module-visibility-changed', handleVisibilityChanged);
		}
	});

	function handleThemeChange(event: Event) {
		const value = (event.target as HTMLSelectElement).value;
		themeMode = value;
		if (value === 'system') {
			document.documentElement.removeAttribute('data-theme');
			localStorage.removeItem('theme');
		} else {
			document.documentElement.setAttribute('data-theme', value);
			localStorage.setItem('theme', value);
		}
	}

	function handleLanguageChange(event: Event) {
		const value = (event.target as HTMLSelectElement).value as Language;
		lang = value;
		setLanguage(value);
	}

	function handleTranslationsToggle(event: Event) {
		const checked = (event.target as HTMLInputElement).checked;
		customTranslations = checked;
		if (checked) {
			localStorage.setItem('customTranslations', 'true');
		} else {
			localStorage.removeItem('customTranslations');
		}
	}

	async function handleLogout() {
		try {
			await auth.logout();
		} catch {
			// ignore
		}
		await invalidate('app:session');
		window.location.href = '/login';
	}
</script>

{#if data.loggedIn}
	<nav>
		<a href="/habits/home">{t('nav.home', lang)}</a>
		<a href="/habits/edit">{t('nav.edit', lang)}</a>
		<a href="/habits/add">{t('nav.addHabit', lang)}</a>
		<a href="/categories/manage">{t('nav.manageCategories', lang)}</a>
		<a href="/categories/add">{t('nav.addCategory', lang)}</a>
		<a href="/score-rules/add">{t('nav.scoringRules', lang)}</a>
		{#if mv.pointsVisible}
			<a href="/points">{t('nav.points', lang)}</a>
		{/if}
		{#if mv.diaryVisible}
			<a href="/diary">{t('nav.diary', lang)}</a>
		{/if}
		{#if mv.sleepVisible}
			<a href="/sleep">{t('nav.sleep', lang)}</a>
		{/if}
		{#if mv.foodLogVisible}
			<a href="/food-log">{t('nav.foodLog', lang)}</a>
		{/if}
		{#if mv.sportLogVisible}
			<a href="/sport-graph">{t('nav.sportGraph', lang)}</a>
		{/if}
		{#if mv.foodLogVisible}
			<a href="/food-log/distribution">{t('nav.foodDistribution', lang)}</a>
		{/if}
		{#if mv.medicationVisible}
			<a href="/medication">{t('nav.medication', lang)}</a>
		{/if}
		{#if mv.activityLogVisible}
			<a href="/activity-log">{t('nav.activityLog', lang)}</a>
		{/if}
		{#if mv.emotionsVisible}
			<a href="/emotions/graph">{t('nav.emotions', lang)}</a>
		{/if}
		{#if mv.statisticsVisible}
			<a href="/stats">{t('nav.stats', lang)}</a>
		{/if}
		{#if mv.pdfExportVisible}
			<a href="/export/pdf">{t('nav.exportPdf', lang)}</a>
		{/if}
		{#if mv.backupVisible}
			<a href="/backup">{t('nav.backup', lang)}</a>
		{/if}
		<a href="/settings">{t('nav.settings', lang)}</a>
		<span style="margin-left: auto; color: var(--color-nav-text);">{data.username}</span>
		<label class="translations-toggle">
			<input type="checkbox" checked={customTranslations} onchange={handleTranslationsToggle} />
			<span>{t('nav.translations', lang)}</span>
		</label>
		<select class="nav-select" value={lang} onchange={handleLanguageChange}>
			{#each LANGUAGES as l (l.code)}
				<option value={l.code}>{l.label}</option>
			{/each}
		</select>
		<select class="nav-select" value={themeMode} onchange={handleThemeChange}>
			<option value="system">System</option>
			<option value="light">Light</option>
			<option value="dark">Dark</option>
		</select>
		<a href="/imprint" class="imprint-link" title={t('nav.imprint', lang)}>i</a>
		<button onclick={handleLogout} style="padding: 0.25rem 0.5rem; font-size: 0.85rem;"
			>{t('nav.logout', lang)}</button
		>
	</nav>
{/if}

<div class="page-wrapper">
	{@render children()}
</div>

<style>
	.nav-select {
		padding: 0.2rem 0.4rem;
		font-size: 0.8rem;
		border-radius: 4px;
		border: 1px solid var(--color-nav-text);
		background: transparent;
		color: var(--color-nav-text);
		cursor: pointer;
	}
	.nav-select option {
		background: var(--color-bg);
		color: var(--color-text);
	}
	.translations-toggle {
		display: flex;
		align-items: center;
		gap: 0.25rem;
		font-size: 0.8rem;
		color: var(--color-nav-text);
		cursor: pointer;
	}
	.translations-toggle input {
		width: 14px;
		height: 14px;
	}
	.imprint-link {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		width: 22px;
		height: 22px;
		border-radius: 50%;
		border: 1.5px solid var(--color-nav-text);
		color: var(--color-nav-text);
		font-size: 0.8rem;
		font-weight: bold;
		font-style: italic;
		font-family: serif;
		text-decoration: none;
		line-height: 1;
	}
	.imprint-link:hover {
		background: var(--color-nav-text);
		color: var(--color-bg);
	}
</style>
