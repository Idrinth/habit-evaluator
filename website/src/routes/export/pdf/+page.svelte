<script lang="ts">
	import { pdfExport } from '$lib/api';

	let fromDate = $state('');
	let toDate = $state('');
	let includeHabits = $state(true);
	let includeSleep = $state(true);
	let includeDiary = $state(true);
	let exporting = $state(false);
	let message = $state('');
	let messageType = $state<'success' | 'error' | ''>('');

	function initDates() {
		const now = new Date();
		const thirtyDaysAgo = new Date(now);
		thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 29);
		toDate = now.toISOString().split('T')[0];
		fromDate = thirtyDaysAgo.toISOString().split('T')[0];
	}

	$effect(() => {
		initDates();
	});

	async function handleExport() {
		if (!includeHabits && !includeSleep && !includeDiary) {
			message = 'Select at least one section to export.';
			messageType = 'error';
			return;
		}
		if (!fromDate || !toDate) {
			message = 'Select both dates.';
			messageType = 'error';
			return;
		}
		if (fromDate > toDate) {
			const temp = fromDate;
			fromDate = toDate;
			toDate = temp;
		}

		exporting = true;
		message = '';
		messageType = '';

		try {
			const blob = await pdfExport.download({
				from: fromDate,
				to: toDate,
				habits: includeHabits,
				sleep: includeSleep,
				diary: includeDiary
			});

			const url = URL.createObjectURL(blob);
			const a = document.createElement('a');
			a.href = url;
			a.download = `habit_report_${fromDate}_to_${toDate}.pdf`;
			document.body.appendChild(a);
			a.click();
			document.body.removeChild(a);
			URL.revokeObjectURL(url);

			message = 'PDF exported successfully.';
			messageType = 'success';
		} catch (err: unknown) {
			message = 'Export failed: ' + (err instanceof Error ? err.message : 'Unknown error');
			messageType = 'error';
		} finally {
			exporting = false;
		}
	}
</script>

<h2>Export to PDF</h2>

<div class="export-form">
	<div class="form-section">
		<h3>Date Range</h3>
		<div class="date-row">
			<label>
				From
				<input type="date" bind:value={fromDate} />
			</label>
			<label>
				To
				<input type="date" bind:value={toDate} />
			</label>
		</div>
	</div>

	<div class="form-section">
		<h3>Include Sections</h3>
		<div class="checkbox-group">
			<label class="checkbox-label">
				<input type="checkbox" bind:checked={includeHabits} />
				Habits
			</label>
			<label class="checkbox-label">
				<input type="checkbox" bind:checked={includeSleep} />
				Sleep
			</label>
			<label class="checkbox-label">
				<input type="checkbox" bind:checked={includeDiary} />
				Diary
			</label>
		</div>
	</div>

	<button class="export-button" onclick={handleExport} disabled={exporting}>
		{exporting ? 'Generating...' : 'Export PDF'}
	</button>

	{#if message}
		<p class="message {messageType}">{message}</p>
	{/if}
</div>

<style>
	h2 {
		margin-bottom: 1.5rem;
	}

	.export-form {
		max-width: 500px;
	}

	.form-section {
		margin-bottom: 1.5rem;
	}

	.form-section h3 {
		margin-bottom: 0.75rem;
		font-size: 1rem;
	}

	.date-row {
		display: flex;
		gap: 1rem;
	}

	.date-row label {
		display: flex;
		flex-direction: column;
		gap: 0.25rem;
		flex: 1;
		font-size: 0.9rem;
	}

	.date-row input[type='date'] {
		padding: 0.5rem;
		border: 1px solid var(--color-border);
		border-radius: 4px;
		background: var(--color-bg);
		color: var(--color-text);
		font-size: 0.9rem;
	}

	.checkbox-group {
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
	}

	.checkbox-label {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		cursor: pointer;
		font-size: 0.95rem;
	}

	.checkbox-label input[type='checkbox'] {
		width: 1.1rem;
		height: 1.1rem;
		cursor: pointer;
	}

	.export-button {
		padding: 0.6rem 1.5rem;
		background: var(--color-primary);
		color: white;
		border: none;
		border-radius: 4px;
		cursor: pointer;
		font-size: 1rem;
	}

	.export-button:hover:not(:disabled) {
		opacity: 0.9;
	}

	.export-button:disabled {
		opacity: 0.6;
		cursor: not-allowed;
	}

	.message {
		margin-top: 1rem;
		padding: 0.5rem;
		border-radius: 4px;
		font-size: 0.9rem;
	}

	.message.success {
		color: var(--color-success);
	}

	.message.error {
		color: var(--color-error);
	}
</style>
