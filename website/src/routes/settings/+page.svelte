<script lang="ts">
	import { onMount } from 'svelte';
	import { reminderSettings, type ReminderSettings } from '$lib/api';

	let settings: ReminderSettings = $state({
		sleepReminderEnabled: false,
		sleepReminderTime: '08:00',
		diaryReminderEnabled: false,
		diaryReminderTime: '20:00',
		emotionReminderEnabled: false,
		emotionReminderCount: 3,
		wakingHoursStart: '07:00',
		wakingHoursEnd: '22:00'
	});

	let error = $state('');
	let success = $state('');
	let loading = $state(true);

	onMount(async () => {
		try {
			const data = await reminderSettings.get();
			settings = {
				sleepReminderEnabled: data.sleepReminderEnabled,
				sleepReminderTime: formatTime(data.sleepReminderTime) || '08:00',
				diaryReminderEnabled: data.diaryReminderEnabled,
				diaryReminderTime: formatTime(data.diaryReminderTime) || '20:00',
				emotionReminderEnabled: data.emotionReminderEnabled,
				emotionReminderCount: data.emotionReminderCount || 3,
				wakingHoursStart: formatTime(data.wakingHoursStart) || '07:00',
				wakingHoursEnd: formatTime(data.wakingHoursEnd) || '22:00'
			};
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to load settings';
		} finally {
			loading = false;
		}
	});

	function formatTime(time: string | null): string {
		if (!time) return '';
		return time.substring(0, 5);
	}

	async function handleSave(e: Event) {
		e.preventDefault();
		error = '';
		success = '';
		try {
			await reminderSettings.update(settings);
			success = 'Settings saved successfully';
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to save settings';
		}
	}
</script>

<h1>Reminder Settings</h1>

<p class="description">
	Configure reminders to help you track your habits consistently.
	All reminders are disabled by default.
</p>

{#if loading}
	<p>Loading...</p>
{:else}
	{#if error}
		<p class="error">{error}</p>
	{/if}
	{#if success}
		<p class="success">{success}</p>
	{/if}

	<form onsubmit={handleSave}>
		<section>
			<h2>Sleep Reminder</h2>
			<p class="hint">Get reminded to log last night's sleep data.</p>
			<label class="toggle-row">
				<input type="checkbox" bind:checked={settings.sleepReminderEnabled} />
				<span>Enable sleep reminder</span>
			</label>
			{#if settings.sleepReminderEnabled}
				<label class="field-row">
					<span>Reminder time:</span>
					<input type="time" bind:value={settings.sleepReminderTime} />
				</label>
			{/if}
		</section>

		<section>
			<h2>Diary Reminder</h2>
			<p class="hint">Get reminded in the evening if you haven't made a diary entry today.</p>
			<label class="toggle-row">
				<input type="checkbox" bind:checked={settings.diaryReminderEnabled} />
				<span>Enable diary reminder</span>
			</label>
			{#if settings.diaryReminderEnabled}
				<label class="field-row">
					<span>Reminder time:</span>
					<input type="time" bind:value={settings.diaryReminderTime} />
				</label>
			{/if}
		</section>

		<section>
			<h2>Emotion Check Reminders</h2>
			<p class="hint">Get random reminders during your waking hours to record your emotional state.</p>
			<label class="toggle-row">
				<input type="checkbox" bind:checked={settings.emotionReminderEnabled} />
				<span>Enable emotion reminders</span>
			</label>
			{#if settings.emotionReminderEnabled}
				<label class="field-row">
					<span>Reminders per day:</span>
					<input
						type="number"
						min="1"
						max="10"
						bind:value={settings.emotionReminderCount}
					/>
				</label>
				<div class="field-row">
					<span>Waking hours:</span>
					<input type="time" bind:value={settings.wakingHoursStart} />
					<span>to</span>
					<input type="time" bind:value={settings.wakingHoursEnd} />
				</div>
			{/if}
		</section>

		<button type="submit" class="save-button">Save Settings</button>
	</form>
{/if}

<style>
	h1 {
		margin-bottom: 0.5rem;
	}
	.description {
		color: var(--color-text-muted, #666);
		margin-bottom: 1.5rem;
	}
	section {
		margin-bottom: 1.5rem;
		padding: 1rem;
		border: 1px solid var(--color-border, #ddd);
		border-radius: 8px;
	}
	h2 {
		font-size: 1.1rem;
		margin: 0 0 0.25rem;
	}
	.hint {
		font-size: 0.85rem;
		color: var(--color-text-muted, #888);
		margin: 0 0 0.75rem;
	}
	.toggle-row {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		margin-bottom: 0.5rem;
		cursor: pointer;
	}
	.field-row {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		margin-top: 0.5rem;
		margin-left: 1.5rem;
	}
	.field-row input[type='time'],
	.field-row input[type='number'] {
		padding: 0.3rem 0.5rem;
		border: 1px solid var(--color-border, #ccc);
		border-radius: 4px;
		background: var(--color-bg);
		color: var(--color-text);
	}
	.field-row input[type='number'] {
		width: 4rem;
	}
	.save-button {
		padding: 0.5rem 1.5rem;
		background: var(--color-primary, #4a90d9);
		color: white;
		border: none;
		border-radius: 6px;
		font-size: 1rem;
		cursor: pointer;
	}
	.save-button:hover {
		opacity: 0.9;
	}
	.error {
		color: var(--color-error, #c00);
		padding: 0.5rem;
		border: 1px solid var(--color-error, #c00);
		border-radius: 4px;
		margin-bottom: 1rem;
	}
	.success {
		color: var(--color-success, #080);
		padding: 0.5rem;
		border: 1px solid var(--color-success, #080);
		border-radius: 4px;
		margin-bottom: 1rem;
	}
</style>
