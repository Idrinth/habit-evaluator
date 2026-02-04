<svelte:head>
	<title>Imprint - Habit Evaluator</title>
</svelte:head>

<h1>Imprint</h1>

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
</style>
