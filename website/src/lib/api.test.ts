import { describe, it, expect, beforeEach, vi, type Mock } from 'vitest';

// We need to re-import the module fresh for each test to reset circuit breaker state
let apiModule: typeof import('./api');

function mockFetchResponse(body: unknown, status = 200, headers: Record<string, string> = {}) {
	const responseHeaders = new Headers(headers);
	return Promise.resolve({
		ok: status >= 200 && status < 300,
		status,
		headers: responseHeaders,
		json: () => Promise.resolve(body),
		text: () => Promise.resolve(JSON.stringify(body))
	} as Response);
}

function mockFetchNoContent() {
	return Promise.resolve({
		ok: true,
		status: 204,
		headers: new Headers({ 'content-length': '0' }),
		json: () => Promise.resolve(null),
		text: () => Promise.resolve('')
	} as Response);
}

describe('api', () => {
	beforeEach(async () => {
		vi.restoreAllMocks();
		// Reset the module to clear circuit breaker state
		vi.resetModules();
		globalThis.fetch = vi.fn();
		apiModule = await import('./api');
	});

	describe('auth', () => {
		it('should call login endpoint with credentials', async () => {
			const mockResponse = { success: true, userId: '1', username: 'test', message: 'OK' };
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(mockResponse));

			const result = await apiModule.auth.login('testuser', 'password123');

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/auth/login', expect.objectContaining({
				method: 'POST',
				body: JSON.stringify({ username: 'testuser', password: 'password123' })
			}));
			expect(result).toEqual(mockResponse);
		});

		it('should call logout endpoint', async () => {
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse({ success: true, message: 'Logged out' }));

			await apiModule.auth.logout();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/auth/logout', expect.objectContaining({
				method: 'POST'
			}));
		});

		it('should call me endpoint', async () => {
			const mockResponse = { success: true, userId: '1', username: 'test', message: '' };
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(mockResponse));

			const result = await apiModule.auth.me();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/auth/me', expect.any(Object));
			expect(result).toEqual(mockResponse);
		});
	});

	describe('habits', () => {
		it('should list habits', async () => {
			const habits = [{ id: '1', name: 'Exercise' }];
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(habits));

			const result = await apiModule.habits.list();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/habits', expect.any(Object));
			expect(result).toEqual(habits);
		});

		it('should create a habit', async () => {
			const newHabit = {
				name: 'Read',
				frequencyType: 'DAILY',
				targetFrequency: 1,
				maxEntriesPerDay: 1,
				positiveScoring: true
			};
			const created = { id: '2', ...newHabit };
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(created));

			const result = await apiModule.habits.create(newHabit);

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/habits', expect.objectContaining({
				method: 'POST',
				body: JSON.stringify(newHabit)
			}));
			expect(result).toEqual(created);
		});

		it('should update a habit', async () => {
			const updates = { name: 'Updated' };
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse({ id: '1', ...updates }));

			await apiModule.habits.update('1', updates);

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/habits/1', expect.objectContaining({
				method: 'PUT',
				body: JSON.stringify(updates)
			}));
		});

		it('should delete a habit', async () => {
			(globalThis.fetch as Mock).mockReturnValue(mockFetchNoContent());

			await apiModule.habits.delete('1');

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/habits/1', expect.objectContaining({
				method: 'DELETE'
			}));
		});

		it('should add an entry to a habit', async () => {
			const entry = { id: 'e1', completedAt: '2025-01-01T12:00:00' };
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(entry));

			const result = await apiModule.habits.addEntry('h1');

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/habits/h1/entries', expect.objectContaining({
				method: 'POST',
				body: JSON.stringify({})
			}));
			expect(result).toEqual(entry);
		});

		it('should remove the last entry for a date', async () => {
			(globalThis.fetch as Mock).mockReturnValue(mockFetchNoContent());

			await apiModule.habits.removeLastEntry('h1', '2025-03-15');

			expect(globalThis.fetch).toHaveBeenCalledWith(
				'/api/habits/h1/entries/last?date=2025-03-15',
				expect.objectContaining({ method: 'DELETE' })
			);
		});

		it('should evaluate a habit without date range', async () => {
			const evaluation = { habitId: '1', completionRate: 0.8 };
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(evaluation));

			await apiModule.habits.evaluate('1');

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/habits/1/evaluate', expect.any(Object));
		});

		it('should evaluate a habit with date range', async () => {
			const evaluation = { habitId: '1', completionRate: 0.9 };
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(evaluation));

			await apiModule.habits.evaluate('1', '2025-01-01', '2025-01-31');

			expect(globalThis.fetch).toHaveBeenCalledWith(
				'/api/habits/1/evaluate?start=2025-01-01&end=2025-01-31',
				expect.any(Object)
			);
		});

		it('should evaluate a habit with only start date', async () => {
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse({ habitId: '1' }));

			await apiModule.habits.evaluate('1', '2025-01-01');

			expect(globalThis.fetch).toHaveBeenCalledWith(
				'/api/habits/1/evaluate?start=2025-01-01',
				expect.any(Object)
			);
		});

		it('should predict scores', async () => {
			const prediction = { currentTotalScore: 10, predictedTotalScore: 15 };
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(prediction));

			await apiModule.habits.predict();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/habits/predict', expect.any(Object));
		});

		it('should fetch point development data', async () => {
			const data = {
				dailyPoints: [1, 2, 3],
				labels: ['Mon', 'Tue', 'Wed'],
				runningAverages: [1, 1.5, 2],
				cumulativeTotals: [1, 3, 6],
				totalPoints: 6,
				average: 2
			};
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(data));

			const result = await apiModule.habits.pointDevelopment('h1', 'week');

			expect(globalThis.fetch).toHaveBeenCalledWith(
				'/api/habits/h1/point-development?period=week',
				expect.any(Object)
			);
			expect(result).toEqual(data);
		});

		it('should fetch point development data with month period', async () => {
			const data = { dailyPoints: [], labels: [], runningAverages: [], cumulativeTotals: [], totalPoints: 0, average: 0 };
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(data));

			await apiModule.habits.pointDevelopment('h2', 'month');

			expect(globalThis.fetch).toHaveBeenCalledWith(
				'/api/habits/h2/point-development?period=month',
				expect.any(Object)
			);
		});
	});

	describe('categories', () => {
		it('should list categories', async () => {
			const cats = [{ id: '1', name: 'Health' }];
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(cats));

			const result = await apiModule.categories.list();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/categories', expect.objectContaining({
				method: 'GET'
			}));
			expect(result).toEqual(cats);
		});

		it('should create a category', async () => {
			const cat = { name: 'Fitness', description: 'Exercise habits', color: '#ff0000' };
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse({ id: '2', ...cat }));

			await apiModule.categories.create(cat);

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/categories', expect.objectContaining({
				method: 'POST',
				body: JSON.stringify(cat)
			}));
		});
	});

	describe('diary', () => {
		it('should list diary entries', async () => {
			const entries = [{ id: '1', description: 'Good day' }];
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(entries));

			const result = await apiModule.diary.list();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/diary', expect.any(Object));
			expect(result).toEqual(entries);
		});

		it('should create a diary entry', async () => {
			const entry = { description: 'Great workout', significance: 'NORMAL', eventDate: '2025-01-15' };
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse({ id: '2', ...entry }));

			await apiModule.diary.create(entry);

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/diary', expect.objectContaining({
				method: 'POST',
				body: JSON.stringify(entry)
			}));
		});

		it('should remove a diary entry', async () => {
			(globalThis.fetch as Mock).mockReturnValue(mockFetchNoContent());

			await apiModule.diary.remove('1');

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/diary/1', expect.objectContaining({
				method: 'DELETE'
			}));
		});

		it('should fetch diary stats', async () => {
			const stats = { todayPoints: 4, weekPoints: 12, monthPoints: 30, weeklyAverage: 10, monthlyTrend: 0.05 };
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(stats));

			const result = await apiModule.diary.stats();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/diary/stats', expect.any(Object));
			expect(result).toEqual(stats);
		});

		it('should fetch diary suggestions', async () => {
			const suggestions = ['Exercise', 'Reading'];
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(suggestions));

			const result = await apiModule.diary.suggestions();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/diary/suggestions', expect.any(Object));
			expect(result).toEqual(suggestions);
		});
	});

	describe('sleepEntries', () => {
		it('should list sleep entries', async () => {
			const entries = [{ id: '1', fromTime: '23:00', untilTime: '07:00' }];
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(entries));

			const result = await apiModule.sleepEntries.list();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/sleep-entries', expect.any(Object));
			expect(result).toEqual(entries);
		});

		it('should create a sleep entry', async () => {
			const entry = { fromTime: '22:30', untilTime: '06:30', date: '2025-01-15' };
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse({ id: '2', ...entry }));

			await apiModule.sleepEntries.create(entry);

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/sleep-entries', expect.objectContaining({
				method: 'POST',
				body: JSON.stringify(entry)
			}));
		});

		it('should delete a sleep entry', async () => {
			(globalThis.fetch as Mock).mockReturnValue(mockFetchNoContent());

			await apiModule.sleepEntries.delete('1');

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/sleep-entries/1', expect.objectContaining({
				method: 'DELETE'
			}));
		});

		it('should fetch sleep stats', async () => {
			const stats = {
				weekly: { averageHours: 7.5, minHours: 6, maxHours: 9, totalEntries: 7 },
				monthly: { averageHours: 7.2, minHours: 5, maxHours: 10, totalEntries: 28 }
			};
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(stats));

			const result = await apiModule.sleepEntries.stats();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/sleep-entries/stats', expect.any(Object));
			expect(result).toEqual(stats);
		});
	});

	describe('stats', () => {
		it('should fetch dashboard data', async () => {
			const dashboard = { labels: ['Day1'], habitPoints: [5], diaryPoints: [3], sleepDuration: [7], sleepEntries: [1] };
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(dashboard));

			const result = await apiModule.stats.dashboard();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/stats/dashboard', expect.any(Object));
			expect(result).toEqual(dashboard);
		});

		it('should fetch daily timeline data', async () => {
			const timeline = {
				labels: ['2025-01-01', '2025-01-02'],
				habits: [{ habitId: 'h1', habitName: 'Exercise', color: '#ff0000', entries: [{ dayIndex: 0, hour: 8 }] }]
			};
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(timeline));

			const result = await apiModule.stats.dailyTimeline();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/stats/daily-timeline', expect.any(Object));
			expect(result).toEqual(timeline);
		});

		it('should fetch correlations', async () => {
			const correlations = [{ eventA: 'Exercise', eventB: 'Sleep', correlation: 0.8, sharedDays: 30 }];
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(correlations));

			const result = await apiModule.stats.correlations();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/stats/correlations', expect.any(Object));
			expect(result).toEqual(correlations);
		});

		it('should fetch emotion scatter data', async () => {
			const scatterData = {
				labels: ['00:00', '06:00', '12:00', '18:00'],
				pairs: [{
					pairId: 'p1',
					pairLabel: 'Sad-Happy',
					negativeLabel: 'Sad',
					positiveLabel: 'Happy',
					color: '#00ff00',
					entries: [{ hour: 8, strength: 5 }, { hour: 14, strength: -3 }]
				}]
			};
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(scatterData));

			const result = await apiModule.stats.emotionScatter();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/stats/emotion-scatter', expect.any(Object));
			expect(result).toEqual(scatterData);
		});

		it('should fetch food distribution data', async () => {
			const foodData = {
				labels: ['Breakfast', 'Lunch', 'Dinner'],
				mealCounts: [7, 7, 6],
				avgKcal: [400, 600, 500],
				avgCarbs: [50, 70, 60]
			};
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(foodData));

			const result = await apiModule.stats.foodDistribution();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/stats/food-distribution', expect.any(Object));
			expect(result).toEqual(foodData);
		});
	});

	describe('scoreRules', () => {
		it('should create a scoring rule', async () => {
			const rule = { name: 'Default', thresholdFor1Point: 1, thresholdFor2Points: 2, thresholdFor4Points: 4, thresholdFor8Points: 7 };
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse({ id: '1', ...rule }));

			await apiModule.scoreRules.create(rule);

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/score-rules', expect.objectContaining({
				method: 'POST',
				body: JSON.stringify(rule)
			}));
		});
	});

	describe('emotions', () => {
		it('should fetch emotion pairs', async () => {
			const pairs = [{ id: '1', negativeLabel: 'Sad', positiveLabel: 'Happy' }];
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(pairs));

			const result = await apiModule.emotions.pairs();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/emotions/pairs', expect.any(Object));
			expect(result).toEqual(pairs);
		});

		it('should fetch emotion graph data', async () => {
			const graphData = { labels: ['2025-01-01'], pairs: [] };
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(graphData));

			const result = await apiModule.emotions.graph();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/emotions/graph', expect.any(Object));
			expect(result).toEqual(graphData);
		});
	});

	describe('defaults', () => {
		it('should call init defaults endpoint', async () => {
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse({ success: true }));

			const result = await apiModule.defaults.init();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/init-defaults', expect.objectContaining({
				method: 'POST'
			}));
			expect(result).toEqual({ success: true });
		});
	});

	describe('pdfExport', () => {
		it('should download a PDF with query parameters', async () => {
			const blob = new Blob(['pdf-content'], { type: 'application/pdf' });
			(globalThis.fetch as Mock).mockReturnValue(Promise.resolve({
				ok: true,
				status: 200,
				blob: () => Promise.resolve(blob)
			}));

			const result = await apiModule.pdfExport.download({
				from: '2025-01-01',
				to: '2025-01-31',
				habits: true,
				sleep: false,
				diary: true
			});

			expect(globalThis.fetch).toHaveBeenCalledWith(
				expect.stringContaining('/api/export/pdf?')
			);
			const calledUrl = (globalThis.fetch as Mock).mock.calls[0][0] as string;
			expect(calledUrl).toContain('from=2025-01-01');
			expect(calledUrl).toContain('to=2025-01-31');
			expect(calledUrl).toContain('habits=true');
			expect(calledUrl).toContain('sleep=false');
			expect(calledUrl).toContain('diary=true');
			expect(result).toBeInstanceOf(Blob);
		});

		it('should throw on failed PDF export', async () => {
			(globalThis.fetch as Mock).mockReturnValue(Promise.resolve({
				ok: false,
				status: 500
			}));

			await expect(apiModule.pdfExport.download({
				from: '2025-01-01',
				to: '2025-01-31',
				habits: true,
				sleep: true,
				diary: true
			})).rejects.toThrow('Export failed with status 500');
		});
	});

	describe('backup', () => {
		it('should download a backup with password', async () => {
			const blob = new Blob(['backup-data']);
			(globalThis.fetch as Mock).mockReturnValue(Promise.resolve({
				ok: true,
				status: 200,
				blob: () => Promise.resolve(blob)
			}));

			const result = await apiModule.backup.download('mypassword');

			expect(globalThis.fetch).toHaveBeenCalledWith(
				expect.stringContaining('/api/backup?password=mypassword')
			);
			expect(result).toBeInstanceOf(Blob);
		});

		it('should throw on failed backup download', async () => {
			(globalThis.fetch as Mock).mockReturnValue(Promise.resolve({
				ok: false,
				status: 403
			}));

			await expect(apiModule.backup.download('wrong')).rejects.toThrow('Backup download failed with status 403');
		});

		it('should upload a backup file with password', async () => {
			const responseData = { success: true, message: 'Restored' };
			(globalThis.fetch as Mock).mockReturnValue(Promise.resolve({
				ok: true,
				status: 200,
				json: () => Promise.resolve(responseData)
			}));

			const file = new File(['backup-content'], 'backup.hez', { type: 'application/octet-stream' });
			const result = await apiModule.backup.upload(file, 'mypassword');

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/backup', expect.objectContaining({
				method: 'POST'
			}));
			const callArgs = (globalThis.fetch as Mock).mock.calls[0];
			const body = callArgs[1].body as FormData;
			expect(body.get('file')).toBeInstanceOf(File);
			expect(body.get('password')).toBe('mypassword');
			expect(result).toEqual(responseData);
		});

		it('should upload a backup with selective restore options', async () => {
			(globalThis.fetch as Mock).mockReturnValue(Promise.resolve({
				ok: true,
				status: 200,
				json: () => Promise.resolve({ success: true })
			}));

			const file = new File(['data'], 'backup.hez');
			await apiModule.backup.upload(file, 'pass', {
				categories: true,
				habits: true,
				diary: false,
				sleep: true,
				sportLogs: false,
				foodLogs: true
			});

			const callArgs = (globalThis.fetch as Mock).mock.calls[0];
			const body = callArgs[1].body as FormData;
			expect(body.get('categories')).toBe('true');
			expect(body.get('habits')).toBe('true');
			expect(body.get('diary')).toBe('false');
			expect(body.get('sleep')).toBe('true');
			expect(body.get('sportLogs')).toBe('false');
			expect(body.get('foodLogs')).toBe('true');
		});

		it('should upload a backup without options', async () => {
			(globalThis.fetch as Mock).mockReturnValue(Promise.resolve({
				ok: true,
				status: 200,
				json: () => Promise.resolve({ success: true })
			}));

			const file = new File(['data'], 'backup.hez');
			await apiModule.backup.upload(file, 'pass');

			const callArgs = (globalThis.fetch as Mock).mock.calls[0];
			const body = callArgs[1].body as FormData;
			expect(body.get('categories')).toBeNull();
			expect(body.get('habits')).toBeNull();
		});

		it('should throw with server message on failed backup upload', async () => {
			(globalThis.fetch as Mock).mockReturnValue(Promise.resolve({
				ok: false,
				status: 400,
				json: () => Promise.resolve({ message: 'Invalid backup file' })
			}));

			const file = new File(['bad'], 'backup.hez');
			await expect(apiModule.backup.upload(file, 'pass')).rejects.toThrow('Invalid backup file');
		});

		it('should throw generic message on failed backup upload when no JSON body', async () => {
			(globalThis.fetch as Mock).mockReturnValue(Promise.resolve({
				ok: false,
				status: 500,
				json: () => Promise.reject(new Error('not json'))
			}));

			const file = new File(['bad'], 'backup.hez');
			await expect(apiModule.backup.upload(file, 'pass')).rejects.toThrow('Backup upload failed with status 500');
		});

		it('should include X-Request-ID header in backup upload', async () => {
			(globalThis.fetch as Mock).mockReturnValue(Promise.resolve({
				ok: true,
				status: 200,
				json: () => Promise.resolve({ success: true })
			}));

			const file = new File(['data'], 'backup.hez');
			await apiModule.backup.upload(file, 'pass');

			const callArgs = (globalThis.fetch as Mock).mock.calls[0];
			expect(callArgs[1].headers['X-Request-ID']).toBeDefined();
		});
	});

	describe('reminderSettings', () => {
		it('should fetch reminder settings', async () => {
			const settings = {
				sleepReminderEnabled: true,
				sleepReminderTime: '22:00',
				diaryReminderEnabled: false,
				diaryReminderTime: null,
				emotionReminderEnabled: true,
				emotionReminderCount: 3,
				wakingHoursStart: '07:00',
				wakingHoursEnd: '23:00'
			};
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(settings));

			const result = await apiModule.reminderSettings.get();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/reminder-settings', expect.any(Object));
			expect(result).toEqual(settings);
		});

		it('should update reminder settings', async () => {
			const settings = {
				sleepReminderEnabled: false,
				sleepReminderTime: null,
				diaryReminderEnabled: true,
				diaryReminderTime: '20:00',
				emotionReminderEnabled: false,
				emotionReminderCount: 1,
				wakingHoursStart: '08:00',
				wakingHoursEnd: '22:00'
			};
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(settings));

			const result = await apiModule.reminderSettings.update(settings);

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/reminder-settings', expect.objectContaining({
				method: 'PUT',
				body: JSON.stringify(settings)
			}));
			expect(result).toEqual(settings);
		});
	});

	describe('moduleVisibility', () => {
		it('should fetch module visibility settings', async () => {
			const visibility = {
				diaryVisible: true,
				sleepVisible: true,
				emotionsVisible: false,
				pointsVisible: true,
				statisticsVisible: true,
				foodLogVisible: false,
				sportLogVisible: false,
				medicationVisible: false,
				backupVisible: true,
				pdfExportVisible: true,
				activityLogVisible: true
			};
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(visibility));

			const result = await apiModule.moduleVisibility.get();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/module-visibility', expect.any(Object));
			expect(result).toEqual(visibility);
		});

		it('should update module visibility settings', async () => {
			const visibility = {
				diaryVisible: false,
				sleepVisible: false,
				emotionsVisible: true,
				pointsVisible: false,
				statisticsVisible: true,
				foodLogVisible: true,
				sportLogVisible: true,
				medicationVisible: true,
				backupVisible: false,
				pdfExportVisible: false,
				activityLogVisible: true
			};
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(visibility));

			const result = await apiModule.moduleVisibility.update(visibility);

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/module-visibility', expect.objectContaining({
				method: 'PUT',
				body: JSON.stringify(visibility)
			}));
			expect(result).toEqual(visibility);
		});
	});

	describe('sportLogs', () => {
		it('should fetch sport log graph data', async () => {
			const graphData = {
				labels: ['2025-01-01', '2025-01-02'],
				activities: [{
					name: 'Running',
					unit: 'km',
					color: '#0000ff',
					dailyDuration: [30, 45],
					dailyMeasurement: [5.0, 7.5],
					dailyEntries: [1, 1]
				}]
			};
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(graphData));

			const result = await apiModule.sportLogs.graph();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/sport-logs/graph', expect.any(Object));
			expect(result).toEqual(graphData);
		});

		it('should fetch sport log suggestions', async () => {
			const suggestions = {
				names: ['Running', 'Swimming'],
				units: ['km', 'm']
			};
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(suggestions));

			const result = await apiModule.sportLogs.suggestions();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/sport-logs/suggestions', expect.any(Object));
			expect(result).toEqual(suggestions);
		});
	});

	describe('foodLogs', () => {
		it('should list food logs', async () => {
			const logs = [{
				id: '1',
				carbohydrates: 50,
				kcal: 400,
				dateTime: '2025-01-15T12:00:00',
				foodItems: 'Rice, Chicken',
				createdAt: '2025-01-15T12:00:00',
				notes: null,
				foodItemList: ['Rice', 'Chicken'],
				tags: []
			}];
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(logs));

			const result = await apiModule.foodLogs.list();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/food-logs', expect.any(Object));
			expect(result).toEqual(logs);
		});

		it('should create a food log', async () => {
			const entry = { carbohydrates: 30, kcal: 250, dateTime: '2025-01-15T08:00:00', foodItems: 'Oatmeal', notes: 'Breakfast' };
			const created = { id: '2', ...entry, createdAt: '2025-01-15T08:00:00', foodItemList: ['Oatmeal'], tags: [] };
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(created));

			const result = await apiModule.foodLogs.create(entry);

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/food-logs', expect.objectContaining({
				method: 'POST',
				body: JSON.stringify(entry)
			}));
			expect(result).toEqual(created);
		});

		it('should create a food log with minimal fields', async () => {
			const entry = { dateTime: '2025-01-15T08:00:00', foodItems: 'Apple' };
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse({ id: '3', ...entry }));

			await apiModule.foodLogs.create(entry);

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/food-logs', expect.objectContaining({
				method: 'POST',
				body: JSON.stringify(entry)
			}));
		});

		it('should delete a food log', async () => {
			(globalThis.fetch as Mock).mockReturnValue(mockFetchNoContent());

			await apiModule.foodLogs.delete('1');

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/food-logs/1', expect.objectContaining({
				method: 'DELETE'
			}));
		});

		it('should fetch food log suggestions', async () => {
			const suggestions = ['Oatmeal', 'Rice', 'Chicken'];
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(suggestions));

			const result = await apiModule.foodLogs.suggestions();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/food-logs/suggestions', expect.any(Object));
			expect(result).toEqual(suggestions);
		});

		it('should migrate food log tags', async () => {
			(globalThis.fetch as Mock).mockReturnValue(mockFetchNoContent());

			await apiModule.foodLogs.migrateTags();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/food-logs/migrate-tags', expect.objectContaining({
				method: 'POST'
			}));
		});
	});

	describe('circuit breaker', () => {
		it('should activate circuit breaker after a failure', async () => {
			(globalThis.fetch as Mock).mockRejectedValueOnce(new Error('Network error'));

			await expect(apiModule.auth.me()).rejects.toThrow('Network error');

			// Subsequent requests should be blocked
			await expect(apiModule.auth.me()).rejects.toThrow(/Requests paused after failure/);
		});

		it('should include remaining cooldown time in error message', async () => {
			(globalThis.fetch as Mock).mockRejectedValueOnce(new Error('Network error'));

			await expect(apiModule.auth.me()).rejects.toThrow('Network error');

			try {
				await apiModule.auth.me();
			} catch (e) {
				expect((e as Error).message).toMatch(/retrying in \d+s/);
			}
		});

		it('should activate circuit breaker after HTTP error response', async () => {
			(globalThis.fetch as Mock).mockReturnValueOnce(Promise.resolve({
				ok: false,
				status: 503,
				headers: new Headers(),
				json: () => Promise.resolve({ message: 'Service unavailable' })
			} as Response));

			await expect(apiModule.auth.me()).rejects.toThrow('Service unavailable');

			// Circuit breaker should now be open
			await expect(apiModule.auth.me()).rejects.toThrow(/Requests paused after failure/);
		});

		it('should reset circuit breaker after a successful request', async () => {
			// First: trigger a failure to activate the circuit breaker
			(globalThis.fetch as Mock).mockRejectedValueOnce(new Error('Network error'));
			await expect(apiModule.auth.me()).rejects.toThrow('Network error');

			// Manually reset the cooldown by advancing time past the 5-minute window
			vi.useFakeTimers();
			vi.advanceTimersByTime(5 * 60 * 1000 + 1);

			// Now a successful request should reset the breaker
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse({ success: true, userId: '1', username: 'test', message: '' }));
			const result = await apiModule.auth.me();
			expect(result).toEqual({ success: true, userId: '1', username: 'test', message: '' });

			// Another request should also succeed (breaker was reset)
			const result2 = await apiModule.auth.me();
			expect(result2).toEqual({ success: true, userId: '1', username: 'test', message: '' });

			vi.useRealTimers();
		});

		it('should block all API namespaces when circuit is open', async () => {
			(globalThis.fetch as Mock).mockRejectedValueOnce(new Error('Network error'));
			await expect(apiModule.auth.me()).rejects.toThrow('Network error');

			// All endpoints should be blocked
			await expect(apiModule.habits.list()).rejects.toThrow(/Requests paused after failure/);
			await expect(apiModule.diary.list()).rejects.toThrow(/Requests paused after failure/);
			await expect(apiModule.sleepEntries.list()).rejects.toThrow(/Requests paused after failure/);
			await expect(apiModule.categories.list()).rejects.toThrow(/Requests paused after failure/);
		});
	});

	describe('request headers', () => {
		it('should include Content-Type header on all requests', async () => {
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse([]));

			await apiModule.habits.list();

			const callArgs = (globalThis.fetch as Mock).mock.calls[0];
			expect(callArgs[1].headers['Content-Type']).toBe('application/json');
		});

		it('should include X-Request-ID header for POST requests', async () => {
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse({ success: true }));

			await apiModule.defaults.init();

			const callArgs = (globalThis.fetch as Mock).mock.calls[0];
			expect(callArgs[1].headers['X-Request-ID']).toBeDefined();
			expect(callArgs[1].headers['X-Request-ID']).toMatch(
				/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/
			);
		});

		it('should include X-Request-ID header for PUT requests', async () => {
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse({}));

			await apiModule.reminderSettings.update({
				sleepReminderEnabled: false,
				sleepReminderTime: null,
				diaryReminderEnabled: false,
				diaryReminderTime: null,
				emotionReminderEnabled: false,
				emotionReminderCount: 1,
				wakingHoursStart: null,
				wakingHoursEnd: null
			});

			const callArgs = (globalThis.fetch as Mock).mock.calls[0];
			expect(callArgs[1].headers['X-Request-ID']).toBeDefined();
		});

		it('should include X-Request-ID header for DELETE requests', async () => {
			(globalThis.fetch as Mock).mockReturnValue(mockFetchNoContent());

			await apiModule.habits.delete('1');

			const callArgs = (globalThis.fetch as Mock).mock.calls[0];
			expect(callArgs[1].headers['X-Request-ID']).toBeDefined();
		});

		it('should not include X-Request-ID header for GET requests', async () => {
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse([]));

			await apiModule.habits.list();

			const callArgs = (globalThis.fetch as Mock).mock.calls[0];
			expect(callArgs[1].headers['X-Request-ID']).toBeUndefined();
		});
	});

	describe('error handling', () => {
		it('should throw error with message from response body', async () => {
			(globalThis.fetch as Mock).mockReturnValue(Promise.resolve({
				ok: false,
				status: 400,
				headers: new Headers(),
				json: () => Promise.resolve({ message: 'Bad request data' }),
				text: () => Promise.resolve('Bad request data')
			} as Response));

			await expect(apiModule.auth.me()).rejects.toThrow('Bad request data');
		});

		it('should throw generic error when response body has no message', async () => {
			(globalThis.fetch as Mock).mockReturnValue(Promise.resolve({
				ok: false,
				status: 500,
				headers: new Headers(),
				json: () => Promise.reject(new Error('not json')),
				text: () => Promise.resolve('')
			} as Response));

			await expect(apiModule.auth.me()).rejects.toThrow('Request failed with status 500');
		});

		it('should return null for 204 responses', async () => {
			(globalThis.fetch as Mock).mockReturnValue(mockFetchNoContent());

			const result = await apiModule.habits.delete('1');

			expect(result).toBeNull();
		});

		it('should return null for empty text responses', async () => {
			(globalThis.fetch as Mock).mockReturnValue(Promise.resolve({
				ok: true,
				status: 200,
				headers: new Headers(),
				text: () => Promise.resolve('')
			} as Response));

			const result = await apiModule.habits.list();

			expect(result).toBeNull();
		});
	});

	describe('maskBugfixVersion', () => {
		it('should mask three-part version', () => {
			expect(apiModule.maskBugfixVersion('1.2.3')).toBe('1.2.x');
		});

		it('should mask snapshot version', () => {
			expect(apiModule.maskBugfixVersion('0.1.0-SNAPSHOT')).toBe('0.1.x');
		});

		it('should return two-part version as-is', () => {
			expect(apiModule.maskBugfixVersion('1.2')).toBe('1.2');
		});

		it('should return single-part version as-is', () => {
			expect(apiModule.maskBugfixVersion('1')).toBe('1');
		});

		it('should mask four-part version', () => {
			expect(apiModule.maskBugfixVersion('1.2.3.4')).toBe('1.2.x');
		});
	});

	describe('apiVersion', () => {
		it('should fetch server version', async () => {
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse({ version: '0.1.x' }));

			const version = await apiModule.apiVersion.fetch();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/version', expect.any(Object));
			expect(version).toBe('0.1.x');
		});

		it('should throw VersionMismatchError on version mismatch', async () => {
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse({ version: '9.9.x' }));

			await expect(apiModule.apiVersion.check()).rejects.toThrow(apiModule.VersionMismatchError);
		});

		it('should not throw when versions match', async () => {
			const clientMasked = apiModule.maskBugfixVersion('0.1.0');
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse({ version: clientMasked }));

			await expect(apiModule.apiVersion.check()).resolves.toBeUndefined();
		});

		it('should expose version details on VersionMismatchError', async () => {
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse({ version: '9.9.x' }));

			try {
				await apiModule.apiVersion.check();
				expect.unreachable('should have thrown');
			} catch (e) {
				const err = e as InstanceType<typeof apiModule.VersionMismatchError>;
				expect(err.name).toBe('VersionMismatchError');
				expect(err.serverVersion).toBe('9.9.x');
				expect(err.clientVersion).toBeDefined();
				expect(err.message).toContain('9.9.x');
				expect(err.message).toContain('Version mismatch');
			}
		});
	});

	describe('VersionMismatchError', () => {
		it('should be an instance of Error', () => {
			const err = new apiModule.VersionMismatchError('1.0.x', '2.0.x');
			expect(err).toBeInstanceOf(Error);
		});

		it('should store client and server versions', () => {
			const err = new apiModule.VersionMismatchError('1.0.x', '2.0.x');
			expect(err.clientVersion).toBe('1.0.x');
			expect(err.serverVersion).toBe('2.0.x');
		});

		it('should format a descriptive message', () => {
			const err = new apiModule.VersionMismatchError('1.0.x', '2.0.x');
			expect(err.message).toBe('Version mismatch: client 1.0.x does not match server 2.0.x');
		});

		it('should have name set to VersionMismatchError', () => {
			const err = new apiModule.VersionMismatchError('1.0.x', '2.0.x');
			expect(err.name).toBe('VersionMismatchError');
		});
	});
});
