<svelte:head>
	<title>Habit Evaluator - Privacy-Focused Habit Tracker</title>
</svelte:head>

<section class="hero">
	<h1>Habit Evaluator</h1>
	<p class="tagline">A privacy-focused habit tracker for analysing and evaluating your habits without leaking data anywhere.</p>
	<div class="cta">
		<a href="/docs" class="btn btn-primary">Get Started</a>
		<a href="/features" class="btn btn-secondary">View Features</a>
	</div>
</section>

<section class="platforms">
	<h2>Available Platforms</h2>
	<div class="grid">
		<div class="card">
			<h3>Web Application</h3>
			<p>A Spring Boot REST API backend paired with a SvelteKit frontend. Deploy it on your own server with Docker or run it locally for development.</p>
			<a href="/docs/webserver">Setup Guide</a>
		</div>
		<div class="card">
			<h3>Desktop Application</h3>
			<p>A JavaFX desktop app with a local H2 database. Your data stays on your machine with no network required.</p>
			<a href="/docs/desktop">Setup Guide</a>
		</div>
		<div class="card">
			<h3>Android Application</h3>
			<p>A native Android app that can work standalone or connect to your self-hosted webserver for synchronisation.</p>
			<a href="/docs/android">Setup Guide</a>
		</div>
	</div>
</section>

<section class="highlights">
	<h2>Why Habit Evaluator?</h2>
	<div class="grid">
		<div class="card">
			<h3>Privacy First</h3>
			<p>All data stays under your control. Self-host the server or use the desktop app entirely offline. No third-party analytics or tracking.</p>
		</div>
		<div class="card">
			<h3>Flexible Scoring</h3>
			<p>Configure custom scoring rules with tiered point thresholds (0/1/2/4/8). Track weekly scores, streaks, and completion rates.</p>
		</div>
		<div class="card">
			<h3>Shared Core</h3>
			<p>All platforms share a common Java library of models and services, ensuring consistent behaviour across web, desktop, and mobile.</p>
		</div>
	</div>
</section>

<style>
	.hero {
		text-align: center;
		padding: 3rem 0 2rem;
	}
	.hero h1 {
		font-size: 2.5rem;
		color: var(--color-primary);
		margin-bottom: 0.75rem;
	}
	.tagline {
		font-size: 1.15rem;
		color: var(--color-text-muted);
		max-width: 600px;
		margin: 0 auto 1.5rem;
	}
	.cta {
		display: flex;
		gap: 1rem;
		justify-content: center;
		flex-wrap: wrap;
	}
	.btn {
		display: inline-block;
		padding: 0.6rem 1.5rem;
		border-radius: 6px;
		font-weight: 600;
		font-size: 0.95rem;
	}
	.btn:hover {
		text-decoration: none;
	}
	.btn-primary {
		background: var(--color-primary);
		color: #fff;
	}
	.btn-primary:hover {
		background: var(--color-primary-dark);
	}
	.btn-secondary {
		border: 1px solid var(--color-border);
		color: var(--color-text);
	}
	.btn-secondary:hover {
		border-color: var(--color-primary);
		color: var(--color-primary);
	}
	section {
		margin-bottom: 3rem;
	}
	h2 {
		font-size: 1.5rem;
		margin-bottom: 1rem;
	}
	.grid {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
		gap: 1.25rem;
	}
	.card {
		border: 1px solid var(--color-border);
		border-radius: 8px;
		padding: 1.25rem;
	}
	.card h3 {
		margin-bottom: 0.5rem;
		font-size: 1.1rem;
	}
	.card p {
		color: var(--color-text-muted);
		font-size: 0.95rem;
		margin-bottom: 0.5rem;
	}
</style>
