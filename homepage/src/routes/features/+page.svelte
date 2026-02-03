<svelte:head>
	<title>Features - Habit Evaluator</title>
</svelte:head>

<h1>Features</h1>

<section>
	<h2>Habit Management</h2>
	<ul>
		<li>Create, update, and delete habits with custom names and descriptions.</li>
		<li>Organise habits into user-defined categories with colour coding.</li>
		<li>Track habit completions with optional notes and configurable value/weight.</li>
		<li>Support for daily, weekly, and monthly frequency types.</li>
		<li>Support for negative habits (avoidance tracking).</li>
		<li>Habit deselection toggle with category display in evaluation cards.</li>
	</ul>
</section>

<section>
	<h2>Evaluation and Scoring</h2>
	<ul>
		<li>Evaluate habit performance over custom date ranges, current week, or current month.</li>
		<li>Completion rate calculation as a percentage of your target.</li>
		<li>Streak tracking for both current and longest streaks.</li>
		<li>Configurable scoring rules with 0, 1, 2, 4, and 8 point thresholds.</li>
		<li>Weekly score aggregation across all habits and per category.</li>
		<li>Score predictions based on current progress.</li>
	</ul>
</section>

<section>
	<h2>Diary Tracking</h2>
	<ul>
		<li>Log positive events with minor, normal, and major significance levels.</li>
		<li>Automatic date picker defaulting to the current date.</li>
		<li>Daily, weekly, and monthly point tracking with weekly averages.</li>
		<li>Monthly trend tracking (up, down, or stable).</li>
	</ul>
</section>

<section>
	<h2>Sleep Tracking</h2>
	<ul>
		<li>Manual sleep entry logging with from/until times.</li>
		<li>Overlap detection for conflicting entries.</li>
		<li>Daily sleep duration statistics including average, minimum, and maximum.</li>
		<li>Weekly and monthly sleep statistics aggregation.</li>
		<li>Sleep interruptions tracking.</li>
		<li>Graph visualisation with daily duration and interruptions.</li>
	</ul>
</section>

<section>
	<h2>Analytics and Reports</h2>
	<ul>
		<li>Combined stats view with habit points, diary points, and sleep duration.</li>
		<li>Point development visualisation with daily and average bar charts.</li>
		<li>PDF export with user-selectable date range.</li>
		<li>PDF export with section selection for habits, diary entries, and sleep logs.</li>
	</ul>
</section>

<section>
	<h2>Authentication and Sharing</h2>
	<ul>
		<li>Session-based authentication with BCrypt password encoding.</li>
		<li>Magic links for read-only shared data access.</li>
		<li>Optional time-based and category-based filtering on shared links.</li>
		<li>Token expiration support for shared links.</li>
	</ul>
</section>

<section>
	<h2>Multi-Platform Support</h2>
	<ul>
		<li><strong>Web</strong> &mdash; Spring Boot REST API with a SvelteKit frontend.</li>
		<li><strong>Desktop</strong> &mdash; JavaFX application with a local H2 database.</li>
		<li><strong>Android</strong> &mdash; Native app with habit tracking, diary, sleep logging, PDF export, and optional server sync.</li>
		<li><strong>Shared Core</strong> &mdash; Common models, services, and repository interfaces used across all platforms.</li>
	</ul>
</section>

<section>
	<h2>Synchronisation</h2>
	<ul>
		<li>Automatic bidirectional sync with a self-hosted remote server.</li>
		<li>Local backup for remote-enabled clients.</li>
		<li>Local-only storage mode with no network required.</li>
	</ul>
</section>

<section>
	<h2>Customisation</h2>
	<ul>
		<li>Theme switching between system default, light, and dark modes.</li>
		<li>Language selection with support for English, Deutsch, Espa&ntilde;ol, and Fran&ccedil;ais.</li>
		<li>Custom translations for habit and category names.</li>
	</ul>
</section>

<section>
	<h2>Database Options</h2>
	<ul>
		<li>H2 for development and local/desktop use.</li>
		<li>MariaDB support for production deployments via environment variables.</li>
		<li>H2 console available at <code>/h2-console</code> during development.</li>
	</ul>
</section>

<section>
	<h2>Deployment</h2>
	<ul>
		<li>Two-stage Dockerfile for the webserver (Eclipse Temurin JDK 17 build, JRE 17 runtime).</li>
		<li>GitHub Actions CI workflows for automated builds.</li>
		<li>Gradle multi-module build system.</li>
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
	ul {
		padding-left: 1.5rem;
	}
	li {
		margin-bottom: 0.4rem;
		color: var(--color-text-muted);
	}
</style>
