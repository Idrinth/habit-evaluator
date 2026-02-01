<svelte:head>
	<title>Setup Guide - Habit Evaluator</title>
</svelte:head>

<h1>Setup Guide</h1>
<p class="intro">Habit Evaluator is a Gradle multi-module project targeting Java 17. Choose the platform that suits your needs.</p>

<section>
	<h2>Prerequisites</h2>
	<ul>
		<li><strong>Java 17</strong> (Eclipse Temurin recommended)</li>
		<li><strong>Gradle 8.5</strong> (included via the Gradle wrapper)</li>
		<li><strong>Node.js 22</strong> (for the SvelteKit website and homepage modules)</li>
		<li><strong>Git</strong> for cloning the repository</li>
	</ul>
</section>

<section>
	<h2>Clone the Repository</h2>
	<pre><code>git clone https://github.com/Idrinth/habit-evaluator.git
cd habit-evaluator</code></pre>
</section>

<section>
	<h2>Build Everything</h2>
	<p>The Gradle wrapper handles the full build including all submodules:</p>
	<pre><code>./gradlew build</code></pre>
	<p>This compiles the <code>shared</code> library, the <code>webserver</code>, the <code>desktop</code> app, the <code>android</code> app, and the <code>website</code> frontend.</p>
</section>

<section>
	<h2>Run Tests</h2>
	<pre><code>./gradlew test</code></pre>
	<p>Tests use JUnit 5 (Jupiter) and cover models, services, and Spring Boot context loading.</p>
</section>

<section>
	<h2>Platform Guides</h2>
	<div class="grid">
		<a href="/docs/webserver" class="card">
			<h3>Web Server</h3>
			<p>Spring Boot API + SvelteKit frontend. Run locally or deploy with Docker.</p>
		</a>
		<a href="/docs/desktop" class="card">
			<h3>Desktop</h3>
			<p>JavaFX application with a local H2 database. Fully offline.</p>
		</a>
		<a href="/docs/android" class="card">
			<h3>Android</h3>
			<p>Native Android app with optional server sync.</p>
		</a>
		<a href="/docs/api" class="card">
			<h3>API Reference</h3>
			<p>REST endpoint documentation for the webserver module.</p>
		</a>
	</div>
</section>

<style>
	h1 {
		font-size: 2rem;
		margin-bottom: 0.5rem;
	}
	.intro {
		color: var(--color-text-muted);
		margin-bottom: 2rem;
	}
	section {
		margin-bottom: 2rem;
	}
	h2 {
		font-size: 1.3rem;
		margin-bottom: 0.75rem;
	}
	ul {
		padding-left: 1.5rem;
		margin-bottom: 0.5rem;
	}
	li {
		margin-bottom: 0.3rem;
	}
	.grid {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
		gap: 1rem;
	}
	.card {
		display: block;
		border: 1px solid var(--color-border);
		border-radius: 8px;
		padding: 1.25rem;
		color: var(--color-text);
	}
	.card:hover {
		text-decoration: none;
		border-color: var(--color-primary);
	}
	.card h3 {
		margin-bottom: 0.4rem;
		color: var(--color-primary);
	}
	.card p {
		color: var(--color-text-muted);
		font-size: 0.9rem;
	}
</style>
