<svelte:head>
	<title>Android Setup - Habit Evaluator</title>
</svelte:head>

<p><a href="/docs">&larr; Back to Setup Guide</a></p>

<h1>Android Application Setup</h1>
<p class="intro">The Android module is a native app targeting SDK 34 (minimum SDK 26) that can work standalone or connect to a self-hosted webserver.</p>

<section>
	<h2>Prerequisites</h2>
	<ul>
		<li><strong>Android Studio</strong> (recommended) or the Android SDK command-line tools</li>
		<li><strong>Android SDK 34</strong></li>
		<li><strong>Java 17</strong></li>
	</ul>
</section>

<section>
	<h2>Building</h2>
	<pre><code>./gradlew :android:build</code></pre>
	<p>Or open the project in Android Studio and build the <code>android</code> module from there.</p>
</section>

<section>
	<h2>Running on a Device or Emulator</h2>
	<p>With an emulator running or a device connected via USB:</p>
	<pre><code>./gradlew :android:installDebug</code></pre>
	<p>The app launcher will show <strong>Habit Evaluator</strong>.</p>
</section>

<section>
	<h2>Connecting to a Server</h2>
	<p>Open the Settings screen inside the app to configure:</p>
	<ul>
		<li><strong>Server URL</strong> &mdash; the base URL of your self-hosted webserver (e.g. <code>https://habits.example.com</code>).</li>
		<li><strong>Username</strong> and <strong>Password</strong> &mdash; your webserver account credentials.</li>
	</ul>
	<p>When no server is configured the app falls back to a local H2 database on the device.</p>
</section>

<section>
	<h2>Architecture</h2>
	<ul>
		<li>Uses the <code>shared</code> module for models, API client, and repository interfaces.</li>
		<li><code>RemoteHabitRepository</code> and <code>RemoteUserRepository</code> communicate with the webserver REST API.</li>
		<li>UI built with RecyclerView, CardView, and Material Design 3 components.</li>
		<li>Activities: <code>MainActivity</code>, <code>TrackHabitsActivity</code>, <code>SettingsActivity</code>.</li>
		<li>Requires the <code>INTERNET</code> permission for server communication.</li>
	</ul>
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
