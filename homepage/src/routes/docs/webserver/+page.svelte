<svelte:head>
	<title>Web Server Setup - Habit Evaluator</title>
</svelte:head>

<p><a href="/docs">&larr; Back to Setup Guide</a></p>

<h1>Web Server Setup</h1>
<p class="intro">The webserver module is a Spring Boot 4.0.2 application that serves a REST API consumed by the SvelteKit frontend.</p>

<section>
	<h2>Running Locally</h2>
	<p>Start the Spring Boot server (defaults to port 8080 with an H2 file database):</p>
	<pre><code>./gradlew :webserver:bootRun</code></pre>
	<p>The API is available at <code>http://localhost:8080/api</code> and the H2 console at <code>http://localhost:8080/h2-console</code>.</p>
</section>

<section>
	<h2>Running the Frontend</h2>
	<p>In a separate terminal, start the SvelteKit dev server which proxies API calls to the backend:</p>
	<pre><code>cd website
npm install
npm run dev</code></pre>
</section>

<section>
	<h2>Database Configuration</h2>
	<h3>Development (H2)</h3>
	<p>By default the server uses an H2 file database stored at <code>./data/habit-evaluator</code>. No configuration is needed.</p>

	<h3>Production (MariaDB)</h3>
	<p>Activate the <code>mariadb</code> Spring profile and set the required environment variables:</p>
	<pre><code>export MARIADB_URL=jdbc:mariadb://localhost:3306/habit_evaluator
export MARIADB_USERNAME=habit_user
export MARIADB_PASSWORD=your_password

java -jar webserver/build/libs/habit-evaluator-web.jar \
  --spring.profiles.active=mariadb</code></pre>

	<table>
		<thead>
			<tr><th>Variable</th><th>Default</th><th>Description</th></tr>
		</thead>
		<tbody>
			<tr><td><code>MARIADB_URL</code></td><td><code>jdbc:mariadb://localhost:3306/habit_evaluator</code></td><td>JDBC connection URL</td></tr>
			<tr><td><code>MARIADB_USERNAME</code></td><td><code>habit_user</code></td><td>Database username</td></tr>
			<tr><td><code>MARIADB_PASSWORD</code></td><td>(empty)</td><td>Database password</td></tr>
		</tbody>
	</table>
</section>

<section>
	<h2>Docker Deployment</h2>
	<p>A multi-stage Dockerfile is provided in <code>webserver/Dockerfile</code>:</p>
	<pre><code>docker build -t habit-evaluator-web ./webserver
docker run -p 8080:8080 habit-evaluator-web</code></pre>
	<p>For production, pass environment variables for MariaDB and the profile:</p>
	<pre><code>docker run -p 8080:8080 \
  -e MARIADB_URL=jdbc:mariadb://db:3306/habit_evaluator \
  -e MARIADB_USERNAME=habit_user \
  -e MARIADB_PASSWORD=secret \
  -e SPRING_PROFILES_ACTIVE=mariadb \
  habit-evaluator-web</code></pre>
</section>

<section>
	<h2>Security</h2>
	<ul>
		<li>Authentication is session-based with BCrypt password encoding via Spring Security.</li>
		<li>Public endpoints: <code>/api/auth/**</code>, <code>/api/shared/**</code>, <code>/login</code>, <code>/h2-console</code>, <code>/score-rules/**</code>.</li>
		<li>All other API endpoints require an authenticated session.</li>
		<li>CSRF protection is enabled by default.</li>
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
	h3 {
		font-size: 1.1rem;
		margin: 1rem 0 0.5rem;
	}
	ul {
		padding-left: 1.5rem;
	}
	li {
		margin-bottom: 0.3rem;
	}
	table {
		width: 100%;
		border-collapse: collapse;
		margin: 1rem 0;
		font-size: 0.9rem;
	}
	th, td {
		border: 1px solid var(--color-border);
		padding: 0.5rem 0.75rem;
		text-align: left;
	}
	th {
		background: var(--color-bg-alt);
		font-weight: 600;
	}
</style>
