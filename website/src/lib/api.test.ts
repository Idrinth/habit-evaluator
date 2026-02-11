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

		it('should predict scores', async () => {
			const prediction = { currentTotalScore: 10, predictedTotalScore: 15 };
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(prediction));

			await apiModule.habits.predict();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/habits/predict', expect.any(Object));
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

		it('should fetch correlations', async () => {
			const correlations = [{ eventA: 'Exercise', eventB: 'Sleep', correlation: 0.8, sharedDays: 30 }];
			(globalThis.fetch as Mock).mockReturnValue(mockFetchResponse(correlations));

			const result = await apiModule.stats.correlations();

			expect(globalThis.fetch).toHaveBeenCalledWith('/api/stats/correlations', expect.any(Object));
			expect(result).toEqual(correlations);
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
	});
});
