<svelte:head>
	<title>API Reference - Habit Evaluator</title>
</svelte:head>

<p><a href="/docs">&larr; Back to Setup Guide</a></p>

<h1>API Reference</h1>
<p class="intro">The webserver exposes a REST API under the <code>/api</code> prefix. All endpoints except authentication and shared links require an authenticated session.</p>

<section>
	<h2>Authentication</h2>
	<table>
		<thead>
			<tr><th>Method</th><th>Path</th><th>Description</th></tr>
		</thead>
		<tbody>
			<tr><td><code>POST</code></td><td><code>/api/auth/login</code></td><td>Log in with username and password. Returns user info and a session cookie.</td></tr>
			<tr><td><code>POST</code></td><td><code>/api/auth/logout</code></td><td>Invalidate the current session.</td></tr>
			<tr><td><code>GET</code></td><td><code>/api/auth/me</code></td><td>Get the currently authenticated user.</td></tr>
		</tbody>
	</table>
	<h3>Login Request Body</h3>
	<pre><code>{'{'}
  "username": "string",
  "password": "string"
{'}'}</code></pre>
	<h3>Login Response</h3>
	<pre><code>{'{'}
  "userId": "string",
  "username": "string",
  "message": "string",
  "success": true
{'}'}</code></pre>
</section>

<section>
	<h2>Habits</h2>
	<table>
		<thead>
			<tr><th>Method</th><th>Path</th><th>Description</th></tr>
		</thead>
		<tbody>
			<tr><td><code>GET</code></td><td><code>/api/habits</code></td><td>List all habits for the authenticated user.</td></tr>
			<tr><td><code>GET</code></td><td><code>/api/habits/{'{'}id{'}'}</code></td><td>Get a specific habit by ID.</td></tr>
			<tr><td><code>POST</code></td><td><code>/api/habits</code></td><td>Create a new habit.</td></tr>
			<tr><td><code>PUT</code></td><td><code>/api/habits/{'{'}id{'}'}</code></td><td>Update an existing habit.</td></tr>
			<tr><td><code>DELETE</code></td><td><code>/api/habits/{'{'}id{'}'}</code></td><td>Delete a habit and all its entries.</td></tr>
			<tr><td><code>POST</code></td><td><code>/api/habits/{'{'}id{'}'}/entries</code></td><td>Add a completion entry to a habit.</td></tr>
			<tr><td><code>GET</code></td><td><code>/api/habits/{'{'}id{'}'}/evaluate</code></td><td>Evaluate a habit's performance.</td></tr>
			<tr><td><code>GET</code></td><td><code>/api/habits/predict</code></td><td>Predict weekly scores based on current progress.</td></tr>
		</tbody>
	</table>
</section>

<section>
	<h2>Categories</h2>
	<table>
		<thead>
			<tr><th>Method</th><th>Path</th><th>Description</th></tr>
		</thead>
		<tbody>
			<tr><td><code>GET</code></td><td><code>/api/categories</code></td><td>List all habit categories.</td></tr>
			<tr><td><code>POST</code></td><td><code>/api/categories</code></td><td>Create a new category.</td></tr>
		</tbody>
	</table>
</section>

<section>
	<h2>Scoring Rules</h2>
	<table>
		<thead>
			<tr><th>Method</th><th>Path</th><th>Description</th></tr>
		</thead>
		<tbody>
			<tr><td><code>GET</code></td><td><code>/api/score-rules</code></td><td>List scoring rules for the authenticated user.</td></tr>
			<tr><td><code>POST</code></td><td><code>/api/score-rules</code></td><td>Create a new scoring rule.</td></tr>
		</tbody>
	</table>
</section>

<section>
	<h2>Magic Links</h2>
	<table>
		<thead>
			<tr><th>Method</th><th>Path</th><th>Description</th></tr>
		</thead>
		<tbody>
			<tr><td><code>GET</code></td><td><code>/api/magic-links</code></td><td>List magic links for the authenticated user.</td></tr>
			<tr><td><code>GET</code></td><td><code>/api/shared/{'{'}token{'}'}</code></td><td>Access shared data via a magic link token (public).</td></tr>
		</tbody>
	</table>
	<p>Magic links provide read-only access to habit data. They support optional category filtering, date range filtering, and token expiration.</p>
</section>

<section>
	<h2>Default Data</h2>
	<table>
		<thead>
			<tr><th>Method</th><th>Path</th><th>Description</th></tr>
		</thead>
		<tbody>
			<tr><td><code>POST</code></td><td><code>/api/default-data</code></td><td>Initialise default categories and habits for the current user.</td></tr>
		</tbody>
	</table>
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
		font-size: 1.05rem;
		margin: 1rem 0 0.5rem;
	}
	table {
		width: 100%;
		border-collapse: collapse;
		margin: 0.75rem 0;
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
