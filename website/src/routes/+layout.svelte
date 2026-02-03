<script lang="ts">
	import '../app.css';
	import { auth } from '$lib/api';
	import { invalidate } from '$app/navigation';
	import { onMount } from 'svelte';
	import { browser } from '$app/environment';
	import { LANGUAGES, getLanguage, setLanguage, t, type Language } from '$lib/i18n';
	import type { Snippet } from 'svelte';

	let { data, children }: { data: { loggedIn: boolean; username: string }; children: Snippet } =
		$props();

	let themeMode = $state('system');
	let lang: Language = $state('en');

	onMount(() => {
		const saved = localStorage.getItem('theme');
		if (saved === 'light' || saved === 'dark') {
			themeMode = saved;
			document.documentElement.setAttribute('data-theme', saved);
		}
		lang = getLanguage();
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
		<a href="/categories/add">{t('nav.addCategory', lang)}</a>
		<a href="/score-rules/add">{t('nav.scoringRules', lang)}</a>
		<a href="/diary">{t('nav.diary', lang)}</a>
		<a href="/sleep">{t('nav.sleep', lang)}</a>
		<span style="margin-left: auto; color: var(--color-nav-text);">{data.username}</span>
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
</style>
