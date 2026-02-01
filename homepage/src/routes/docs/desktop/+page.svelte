<svelte:head>
	<title>Desktop Setup - Habit Evaluator</title>
</svelte:head>

<p><a href="/docs">&larr; Back to Setup Guide</a></p>

<h1>Desktop Application Setup</h1>
<p class="intro">The desktop module is a JavaFX 21 application with a local H2 database. It runs entirely offline with no server required.</p>

<section>
	<h2>Prerequisites</h2>
	<ul>
		<li><strong>Java 17</strong> (JDK, not just JRE, as JavaFX modules are needed)</li>
		<li>JavaFX 21.0.2 is pulled automatically by Gradle</li>
	</ul>
</section>

<section>
	<h2>Running</h2>
	<pre><code>./gradlew :desktop:run</code></pre>
	<p>This launches the JavaFX application window (800x600 default size).</p>
</section>

<section>
	<h2>Data Storage</h2>
	<p>The desktop app stores its H2 database at:</p>
	<pre><code>~/.habit-evaluator/data</code></pre>
	<p>The schema is managed by Hibernate with <code>ddl-auto=update</code>, so migrations are applied automatically on startup.</p>
</section>

<section>
	<h2>Architecture</h2>
	<ul>
		<li>Uses the <code>shared</code> module for models, services, and repository interfaces.</li>
		<li>Implements <code>H2HabitRepository</code>, <code>H2HabitCategoryRepository</code>, and <code>H2UserRepository</code> for local persistence.</li>
		<li>UI is defined in FXML layouts (<code>main.fxml</code>, <code>settings.fxml</code>) with CSS styling.</li>
		<li>Logging via Logback 1.4.14.</li>
	</ul>
</section>

<section>
	<h2>Building a JAR</h2>
	<pre><code>./gradlew :desktop:build</code></pre>
	<p>The output JAR is located in <code>desktop/build/libs/</code>.</p>
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
	}
	li {
		margin-bottom: 0.3rem;
	}
</style>
