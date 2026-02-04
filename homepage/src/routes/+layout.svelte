<script>
	import '../app.css';
	import { onMount } from 'svelte';

	let { children } = $props();
	let themeMode = $state('system');

	onMount(() => {
		const saved = localStorage.getItem('theme');
		if (saved === 'light' || saved === 'dark') {
			themeMode = saved;
			document.documentElement.setAttribute('data-theme', saved);
		}
	});

	/** @param {Event} event */
	function handleThemeChange(event) {
		const value = /** @type {HTMLSelectElement} */ (event.target).value;
		themeMode = value;
		if (value === 'system') {
			document.documentElement.removeAttribute('data-theme');
			localStorage.removeItem('theme');
		} else {
			document.documentElement.setAttribute('data-theme', value);
			localStorage.setItem('theme', value);
		}
	}
</script>

<nav>
	<div class="nav-inner">
		<a href="/" class="logo"><img src="/logo.svg" alt="Habit Evaluator" class="logo-icon" />Habit Evaluator</a>
		<div class="nav-links">
			<a href="/features">Features</a>
			<a href="/docs">Setup Guide</a>
			<a href="/docs/api">API Reference</a>
			<a href="/imprint">Project legal</a>
			<select class="theme-select" value={themeMode} onchange={handleThemeChange}>
				<option value="system">System</option>
				<option value="light">Light</option>
				<option value="dark">Dark</option>
			</select>
		</div>
	</div>
</nav>

<main>
	{@render children()}
</main>

<footer>
	<div class="footer-inner">
		<p>Habit Evaluator is open source software licensed under the <a href="https://opensource.org/licenses/MIT">MIT License</a>.</p>
	</div>
</footer>

<style>
	nav {
		border-bottom: 1px solid var(--color-border);
		padding: 0.75rem 1.5rem;
		position: sticky;
		top: 0;
		background: var(--color-bg);
		z-index: 10;
	}
	.nav-inner {
		max-width: var(--max-width);
		margin: 0 auto;
		display: flex;
		align-items: center;
		justify-content: space-between;
		flex-wrap: wrap;
		gap: 0.5rem;
	}
	.logo {
		font-weight: 700;
		font-size: 1.15rem;
		color: var(--color-primary);
		display: flex;
		align-items: center;
		gap: 0.5rem;
	}
	.logo-icon {
		width: 1.75rem;
		height: 1.75rem;
	}
	.logo:hover {
		text-decoration: none;
	}
	.nav-links {
		display: flex;
		gap: 1.5rem;
		align-items: center;
	}
	.nav-links a {
		color: var(--color-text-muted);
		font-size: 0.95rem;
	}
	.nav-links a:hover {
		color: var(--color-primary);
		text-decoration: none;
	}
	.theme-select {
		padding: 0.2rem 0.4rem;
		font-size: 0.8rem;
		border-radius: 4px;
		border: 1px solid var(--color-border);
		background: var(--color-bg);
		color: var(--color-text-muted);
		cursor: pointer;
	}
	.theme-select option {
		background: var(--color-bg);
		color: var(--color-text);
	}
	main {
		max-width: var(--max-width);
		margin: 0 auto;
		padding: 2rem 1.5rem;
	}
	footer {
		border-top: 1px solid var(--color-border);
		padding: 1.5rem;
		margin-top: 3rem;
	}
	.footer-inner {
		max-width: var(--max-width);
		margin: 0 auto;
		text-align: center;
		color: var(--color-text-muted);
		font-size: 0.875rem;
	}
</style>
