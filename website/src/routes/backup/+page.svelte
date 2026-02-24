<script lang="ts">
	import { backup } from '$lib/api';

	let password = $state('');
	let downloading = $state(false);
	let uploading = $state(false);
	let message = $state('');
	let messageType = $state<'success' | 'error' | ''>('');
	let fileInput: HTMLInputElement;

	let restoreCategories = $state(true);
	let restoreHabits = $state(true);
	let restoreDiary = $state(true);
	let restoreSleep = $state(true);
	let restoreSportLogs = $state(true);
	let restoreFoodLogs = $state(true);
	let overwrite = $state(false);

	async function handleDownload() {
		if (!password) {
			message = 'Please enter a password.';
			messageType = 'error';
			return;
		}

		downloading = true;
		message = '';
		messageType = '';

		try {
			const blob = await backup.download(password);

			const now = new Date().toISOString().split('T')[0];
			const url = URL.createObjectURL(blob);
			const a = document.createElement('a');
			a.href = url;
			a.download = `habit-evaluator-${now}.hez`;
			document.body.appendChild(a);
			a.click();
			document.body.removeChild(a);
			URL.revokeObjectURL(url);

			message = 'Backup downloaded successfully.';
			messageType = 'success';
		} catch (err: unknown) {
			message = 'Download failed: ' + (err instanceof Error ? err.message : 'Unknown error');
			messageType = 'error';
		} finally {
			downloading = false;
		}
	}

	async function handleUpload() {
		if (!password) {
			message = 'Please enter a password.';
			messageType = 'error';
			return;
		}

		const files = fileInput?.files;
		if (!files || files.length === 0) {
			message = 'Please select a .hez file.';
			messageType = 'error';
			return;
		}

		uploading = true;
		message = '';
		messageType = '';

		try {
			const result = await backup.upload(files[0], password, {
				categories: restoreCategories,
				habits: restoreHabits,
				diary: restoreDiary,
				sleep: restoreSleep,
				sportLogs: restoreSportLogs,
				foodLogs: restoreFoodLogs,
				overwrite
			});

			if (result.success) {
				const parts = [];
				if (result.categoriesAdded > 0) parts.push(`${result.categoriesAdded} categories added`);
				if (result.habitsAdded > 0) parts.push(`${result.habitsAdded} habits added`);
				if (result.habitsMerged > 0) parts.push(`${result.habitsMerged} habits merged`);
				if (result.entriesAdded > 0) parts.push(`${result.entriesAdded} entries added`);
				if (result.diaryEntriesAdded > 0) parts.push(`${result.diaryEntriesAdded} diary entries added`);
				if (result.sleepEntriesAdded > 0) parts.push(`${result.sleepEntriesAdded} sleep entries added`);
				if (result.sportLogsAdded > 0) parts.push(`${result.sportLogsAdded} sport logs added`);
				if (result.foodLogsAdded > 0) parts.push(`${result.foodLogsAdded} food logs added`);
				message = 'Backup restored successfully.' + (parts.length > 0 ? ' ' + parts.join(', ') + '.' : '');
				messageType = 'success';
			} else {
				message = result.message || 'Restore failed.';
				messageType = 'error';
			}
		} catch (err: unknown) {
			message = 'Upload failed: ' + (err instanceof Error ? err.message : 'Unknown error');
			messageType = 'error';
		} finally {
			uploading = false;
		}
	}
</script>

<h2>Backup</h2>

<div class="backup-form">
	<div class="form-section">
		<h3>Password</h3>
		<p class="help-text">The password is used to encrypt and decrypt the backup file.</p>
		<input type="password" bind:value={password} placeholder="Enter backup password" class="password-input" />
	</div>

	<div class="form-section">
		<h3>Download Backup</h3>
		<p class="help-text">Download all your data as an encrypted .hez file.</p>
		<button class="action-button" onclick={handleDownload} disabled={downloading}>
			{downloading ? 'Downloading...' : 'Download Backup'}
		</button>
	</div>

	<div class="form-section">
		<h3>Restore Backup</h3>
		<p class="help-text">Upload a .hez file to restore data into your account.</p>
		<input type="file" accept=".hez" bind:this={fileInput} class="file-input" />
		<div class="restore-options">
			<p class="help-text">Select which data types to restore:</p>
			<label class="checkbox-label">
				<input type="checkbox" bind:checked={restoreCategories} />
				Categories
			</label>
			<label class="checkbox-label">
				<input type="checkbox" bind:checked={restoreHabits} />
				Habits
			</label>
			<label class="checkbox-label">
				<input type="checkbox" bind:checked={restoreDiary} />
				Diary entries
			</label>
			<label class="checkbox-label">
				<input type="checkbox" bind:checked={restoreSleep} />
				Sleep entries
			</label>
			<label class="checkbox-label">
				<input type="checkbox" bind:checked={restoreSportLogs} />
				Sport logs
			</label>
			<label class="checkbox-label">
				<input type="checkbox" bind:checked={restoreFoodLogs} />
				Food logs
			</label>
			<label class="checkbox-label overwrite-label">
				<input type="checkbox" bind:checked={overwrite} />
				Overwrite existing data
			</label>
			{#if overwrite}
				<p class="help-text warning-text">Warning: This will delete all existing data of the selected types before restoring from the backup.</p>
			{/if}
		</div>
		<button class="action-button" onclick={handleUpload} disabled={uploading}>
			{uploading ? 'Restoring...' : 'Restore Backup'}
		</button>
	</div>

	{#if message}
		<p class="message {messageType}">{message}</p>
	{/if}
</div>

<style>
	h2 {
		margin-bottom: 1.5rem;
	}

	.backup-form {
		max-width: 500px;
	}

	.form-section {
		margin-bottom: 1.5rem;
	}

	.form-section h3 {
		margin-bottom: 0.5rem;
		font-size: 1rem;
	}

	.help-text {
		font-size: 0.85rem;
		color: var(--color-text-secondary, #666);
		margin-bottom: 0.5rem;
	}

	.password-input {
		width: 100%;
		padding: 0.5rem;
		border: 1px solid var(--color-border);
		border-radius: 4px;
		background: var(--color-bg);
		color: var(--color-text);
		font-size: 0.9rem;
		box-sizing: border-box;
	}

	.file-input {
		display: block;
		margin-bottom: 0.5rem;
		font-size: 0.9rem;
	}

	.action-button {
		padding: 0.6rem 1.5rem;
		background: var(--color-primary);
		color: white;
		border: none;
		border-radius: 4px;
		cursor: pointer;
		font-size: 1rem;
	}

	.action-button:hover:not(:disabled) {
		opacity: 0.9;
	}

	.action-button:disabled {
		opacity: 0.6;
		cursor: not-allowed;
	}

	.restore-options {
		margin-bottom: 0.75rem;
	}

	.checkbox-label {
		display: block;
		margin-bottom: 0.25rem;
		font-size: 0.9rem;
		cursor: pointer;
	}

	.checkbox-label input[type='checkbox'] {
		margin-right: 0.4rem;
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

	.overwrite-label {
		margin-top: 0.5rem;
	}

	.warning-text {
		color: var(--color-error, #d32f2f);
	}
</style>
