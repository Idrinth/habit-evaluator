<svelte:head>
	<title>Project legal - Habit Evaluator</title>
</svelte:head>

<h1>Project legal</h1>

<section>
	<h2>Contact</h2>
	<dl>
		<dt>Name</dt>
		<dd>Bj&ouml;rn &lsquo;Idrinth&rsquo; Buttner</dd>
		<dt>Email</dt>
		<dd><a href="mailto:self@idrinth.de">self@idrinth.de</a></dd>
	</dl>
</section>

<section>
	<h2>License</h2>
	<p>This project is licensed under the <a href="https://opensource.org/licenses/MIT">MIT License</a>.</p>
</section>

<section>
	<h2>Data Protection</h2>
	<ul>
		<li>No remote storing of data unless explicitly enabled by the user.</li>
		<li>No tracking.</li>
		<li>No usage of any provided data besides providing the service.</li>
	</ul>
</section>

<section>
	<h2>Disclaimers</h2>
	<ul>
		<li>This app is a self-tracking tool and does not replace professional medical or psychological help. If you are struggling, please reach out to a qualified professional.</li>
		<li>By default, all your data stays on your device and is not shared with anyone. You can optionally configure remote storage in the settings.</li>
		<li>The authors of this project are not responsible for modifications made by third-party variants or the actions of webservice hosters.</li>
	</ul>
</section>

<section>
	<h2>Third-Party Libraries</h2>
	<table>
		<thead>
			<tr>
				<th>Library</th>
				<th>Version</th>
				<th>License</th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td>Svelte</td>
				<td>5.49.1</td>
				<td><a href="https://github.com/sveltejs/svelte/blob/main/LICENSE.md">MIT</a></td>
			</tr>
			<tr>
				<td>SvelteKit</td>
				<td>2.50.1</td>
				<td><a href="https://github.com/sveltejs/kit/blob/main/LICENSE">MIT</a></td>
			</tr>
			<tr>
				<td>SvelteKit Adapter Static</td>
				<td>3.0.10</td>
				<td><a href="https://github.com/sveltejs/kit/blob/main/LICENSE">MIT</a></td>
			</tr>
			<tr>
				<td>Svelte Vite Plugin</td>
				<td>6.2.4</td>
				<td><a href="https://github.com/sveltejs/vite-plugin-svelte/blob/main/LICENSE">MIT</a></td>
			</tr>
			<tr>
				<td>svelte-check</td>
				<td>4.3.6</td>
				<td><a href="https://github.com/sveltejs/language-tools/blob/master/LICENSE">MIT</a></td>
			</tr>
			<tr>
				<td>TypeScript</td>
				<td>5.9.3</td>
				<td><a href="https://github.com/microsoft/TypeScript/blob/main/LICENSE.txt">Apache-2.0</a></td>
			</tr>
			<tr>
				<td>Vite</td>
				<td>7.3.1</td>
				<td><a href="https://github.com/vitejs/vite/blob/main/LICENSE">MIT</a></td>
			</tr>
			<tr>
				<td>Vitest</td>
				<td>4.0.18</td>
				<td><a href="https://github.com/vitest-dev/vitest/blob/main/LICENSE">MIT</a></td>
			</tr>
			<tr>
				<td>Testing Library Svelte</td>
				<td>5.3.1</td>
				<td><a href="https://github.com/testing-library/svelte-testing-library/blob/main/LICENSE">MIT</a></td>
			</tr>
			<tr>
				<td>Testing Library jest-dom</td>
				<td>6.9.1</td>
				<td><a href="https://github.com/testing-library/jest-dom/blob/main/LICENSE">MIT</a></td>
			</tr>
			<tr>
				<td>jsdom</td>
				<td>28.0.0</td>
				<td><a href="https://github.com/jsdom/jsdom/blob/main/LICENSE.txt">MIT</a></td>
			</tr>
		</tbody>
	</table>
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
	dl {
		margin: 0;
	}
	dt {
		font-weight: 600;
		margin-top: 0.5rem;
	}
	dd {
		margin-left: 0;
		color: var(--color-text-muted);
	}
	ul {
		padding-left: 1.5rem;
	}
	li {
		margin-bottom: 0.4rem;
		color: var(--color-text-muted);
	}
	table {
		width: 100%;
		border-collapse: collapse;
		margin-top: 0.5rem;
	}
	th, td {
		text-align: left;
		padding: 0.4rem 0.75rem;
		border-bottom: 1px solid var(--color-border, #ddd);
	}
	th {
		font-weight: 600;
	}
	td {
		color: var(--color-text-muted);
	}
</style>
