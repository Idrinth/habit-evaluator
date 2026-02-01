<script lang="ts">
	import { categories } from '$lib/api';

	let name = $state('');
	let description = $state('');
	let color = $state('#4a90d9');
	let error = $state('');
	let success = $state('');

	async function handleSubmit(e: Event) {
		e.preventDefault();
		error = '';
		success = '';

		if (!name.trim()) {
			error = 'Category name is required';
			return;
		}

		try {
			await categories.create({ name, description: description || undefined, color });
			success = 'Category created successfully';
			name = '';
			description = '';
			color = '#4a90d9';
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to create category';
		}
	}
</script>

<div class="container">
	<h1>Add Category</h1>
	{#if error}
		<p class="error">{error}</p>
	{/if}
	{#if success}
		<p class="success">{success}</p>
	{/if}
	<form onsubmit={handleSubmit}>
		<label for="name">Name</label>
		<input type="text" id="name" bind:value={name} required />
		<label for="description">Description</label>
		<textarea id="description" bind:value={description}></textarea>
		<label for="color">Color</label>
		<input type="color" id="color" bind:value={color} />
		<button type="submit">Add Category</button>
	</form>
</div>
