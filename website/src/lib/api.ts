import { getApiBaseUrl } from './config';

const CIRCUIT_BREAKER_COOLDOWN_MS = 5 * 60 * 1000; // 5 minutes

let lastFailureTime: number | null = null;

function isCircuitOpen(): boolean {
	if (lastFailureTime === null) {
		return false;
	}
	return Date.now() - lastFailureTime < CIRCUIT_BREAKER_COOLDOWN_MS;
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
	if (isCircuitOpen()) {
		const remaining = CIRCUIT_BREAKER_COOLDOWN_MS - (Date.now() - lastFailureTime!);
		throw new Error(`Requests paused after failure, retrying in ${Math.ceil(remaining / 1000)}s`);
	}
	try {
		const response = await fetch(`${getApiBaseUrl()}${path}`, {
			headers: {
				'Content-Type': 'application/json',
				...options.headers
			},
			...options
		});
		if (!response.ok) {
			const body = await response.json().catch(() => null);
			throw new Error(body?.message || `Request failed with status ${response.status}`);
		}
		lastFailureTime = null;
		if (response.status === 204 || response.headers.get('content-length') === '0') {
			return null as T;
		}
		const text = await response.text();
		if (!text) {
			return null as T;
		}
		return JSON.parse(text);
	} catch (e) {
		lastFailureTime = Date.now();
		throw e;
	}
}

export interface LoginResponse {
	userId: string | null;
	username: string | null;
	message: string;
	success: boolean;
}

export interface Habit {
	id: string;
	name: string;
	description: string | null;
	categoryId: string | null;
	frequencyType: 'DAILY' | 'WEEKLY' | 'MONTHLY';
	targetFrequency: number;
	maxEntriesPerDay: number;
	positiveScoring: boolean;
	scoringRule?: ScoringRule | null;
	entries?: { id: string; completedAt: string; notes: string | null; value: number }[];
	nameTranslations?: Record<string, string>;
	descriptionTranslations?: Record<string, string>;
}

export interface HabitCategory {
	id: string;
	name: string;
	description: string | null;
	color: string | null;
}

export interface ScoringRule {
	id: string;
	name: string;
	thresholdFor1Point: number;
	thresholdFor2Points: number;
	thresholdFor4Points: number;
	thresholdFor8Points: number;
}

export interface Evaluation {
	habitId: string;
	habitName: string;
	periodStart: string;
	periodEnd: string;
	totalEntries: number;
	targetEntries: number;
	completionRate: number;
	currentStreak: number;
	longestStreak: number;
	positiveScoring: boolean;
	onTrack: boolean;
}

export interface PredictedHabitScore {
	habitId: string;
	habitName: string;
	categoryId: string | null;
	currentCompletionCount: number;
	currentScore: number;
	predictedCompletionCount: number;
	predictedScore: number;
	dailyRate: number;
}

export interface PredictedWeeklyScore {
	weekStart: string;
	weekEnd: string;
	weekNumber: number;
	year: number;
	calculatedAt: string;
	daysElapsed: number;
	daysRemaining: number;
	currentTotalScore: number;
	predictedTotalScore: number;
	habitPredictions: PredictedHabitScore[];
}

export const auth = {
	login(username: string, password: string) {
		return request<LoginResponse>('/auth/login', {
			method: 'POST',
			body: JSON.stringify({ username, password })
		});
	},
	logout() {
		return request<LoginResponse>('/auth/logout', { method: 'POST' });
	},
	me() {
		return request<LoginResponse>('/auth/me');
	}
};

export interface PointDevelopmentData {
	dailyPoints: number[];
	labels: string[];
	runningAverages: number[];
	cumulativeTotals: number[];
	totalPoints: number;
	average: number;
}

export const habits = {
	list() {
		return request<Habit[]>('/habits');
	},
	create(habit: { name: string; description?: string; categoryId?: string; frequencyType: string; targetFrequency: number; maxEntriesPerDay: number; positiveScoring: boolean; scoringRule?: { thresholdFor1Point: number; thresholdFor2Points: number; thresholdFor4Points: number; thresholdFor8Points: number }; nameTranslations?: Record<string, string>; descriptionTranslations?: Record<string, string> }) {
		return request<Habit>('/habits', {
			method: 'POST',
			body: JSON.stringify(habit)
		});
	},
	update(habitId: string, habit: Partial<Habit>) {
		return request<Habit>(`/habits/${habitId}`, {
			method: 'PUT',
			body: JSON.stringify(habit)
		});
	},
	delete(habitId: string) {
		return request<void>(`/habits/${habitId}`, {
			method: 'DELETE'
		});
	},
	addEntry(habitId: string) {
		return request<{ id: string; completedAt: string }>(`/habits/${habitId}/entries`, {
			method: 'POST',
			body: JSON.stringify({})
		});
	},
	removeLastEntry(habitId: string, date: string) {
		return request<void>(`/habits/${habitId}/entries/last?date=${date}`, {
			method: 'DELETE'
		});
	},
	evaluate(habitId: string, start?: string, end?: string) {
		const params = new URLSearchParams();
		if (start) params.set('start', start);
		if (end) params.set('end', end);
		const query = params.toString();
		return request<Evaluation>(`/habits/${habitId}/evaluate${query ? '?' + query : ''}`);
	},
	predict() {
		return request<PredictedWeeklyScore>('/habits/predict');
	},
	pointDevelopment(habitId: string, period: 'week' | 'month') {
		return request<PointDevelopmentData>(`/habits/${habitId}/point-development?period=${period}`);
	}
};

export const categories = {
	list() {
		return request<HabitCategory[]>('/categories', { method: 'GET' });
	},
	create(category: { name: string; description?: string; color?: string }) {
		return request<HabitCategory>('/categories', {
			method: 'POST',
			body: JSON.stringify(category)
		});
	}
};

export interface DashboardData {
	labels: string[];
	habitPoints: number[];
	diaryPoints: number[];
	sleepDuration: number[];
	sleepEntries: number[];
}

export interface TimelineEntry {
	dayIndex: number;
	hour: number;
}

export interface TimelineHabit {
	habitId: string;
	habitName: string;
	color: string;
	entries: TimelineEntry[];
}

export interface DailyTimelineData {
	labels: string[];
	habits: TimelineHabit[];
}

export interface CorrelationEntry {
	eventA: string;
	eventB: string;
	correlation: number;
	sharedDays: number;
}

export interface EmotionScatterEntry {
	hour: number;
	strength: number;
}

export interface EmotionScatterPair {
	pairId: string;
	pairLabel: string;
	color: string;
	entries: EmotionScatterEntry[];
}

export interface EmotionScatterData {
	labels: string[];
	pairs: EmotionScatterPair[];
}

export const stats = {
	dashboard() {
		return request<DashboardData>('/stats/dashboard');
	},
	dailyTimeline() {
		return request<DailyTimelineData>('/stats/daily-timeline');
	},
	correlations() {
		return request<CorrelationEntry[]>('/stats/correlations');
	},
	emotionScatter() {
		return request<EmotionScatterData>('/stats/emotion-scatter');
	}
};

export const pdfExport = {
	async download(params: { from: string; to: string; habits: boolean; sleep: boolean; diary: boolean }) {
		const query = new URLSearchParams({
			from: params.from,
			to: params.to,
			habits: String(params.habits),
			sleep: String(params.sleep),
			diary: String(params.diary)
		});
		const response = await fetch(`${getApiBaseUrl()}/export/pdf?${query.toString()}`);
		if (!response.ok) {
			throw new Error(`Export failed with status ${response.status}`);
		}
		return response.blob();
	}
};

export const backup = {
	async download(password: string) {
		const query = new URLSearchParams({ password });
		const response = await fetch(`${getApiBaseUrl()}/backup?${query.toString()}`);
		if (!response.ok) {
			throw new Error(`Backup download failed with status ${response.status}`);
		}
		return response.blob();
	},
	async upload(file: File, password: string) {
		const formData = new FormData();
		formData.append('file', file);
		formData.append('password', password);
		const response = await fetch(`${getApiBaseUrl()}/backup`, {
			method: 'POST',
			body: formData
		});
		if (!response.ok) {
			const body = await response.json().catch(() => null);
			throw new Error(body?.message || `Backup upload failed with status ${response.status}`);
		}
		return response.json();
	}
};

export interface DiaryEntry {
	id: string;
	description: string;
	significance: 'MINOR' | 'NORMAL' | 'MAJOR';
	eventDate: string;
	createdAt: string;
}

export interface DiaryStats {
	todayPoints: number;
	weekPoints: number;
	monthPoints: number;
	weeklyAverage: number;
	dailyAverage: number;
	monthlyTrend: number;
}

export const diary = {
	list() {
		return request<DiaryEntry[]>('/diary');
	},
	create(entry: { description: string; significance: string; eventDate: string }) {
		return request<DiaryEntry>('/diary', {
			method: 'POST',
			body: JSON.stringify(entry)
		});
	},
	remove(id: string) {
		return request<void>(`/diary/${id}`, { method: 'DELETE' });
	},
	stats() {
		return request<DiaryStats>('/diary/stats');
	},
	suggestions() {
		return request<string[]>('/diary/suggestions');
	}
};

export interface SleepEntry {
	id: string;
	fromTime: string;
	untilTime: string;
	date: string;
	createdAt: string;
	notes: string | null;
	hours: number;
}

export interface SleepStats {
	periodStart: string;
	periodEnd: string;
	averageHours: number;
	minHours: number;
	maxHours: number;
	totalEntries: number;
}

export const sleepEntries = {
	list() {
		return request<SleepEntry[]>('/sleep-entries');
	},
	create(entry: { fromTime: string; untilTime: string; date: string; notes?: string }) {
		return request<SleepEntry>('/sleep-entries', {
			method: 'POST',
			body: JSON.stringify(entry)
		});
	},
	delete(id: string) {
		return request<void>(`/sleep-entries/${id}`, { method: 'DELETE' });
	},
	stats() {
		return request<{ weekly: SleepStats; monthly: SleepStats }>('/sleep-entries/stats');
	}
};

export const scoreRules = {
	create(rule: {
		name: string;
		thresholdFor1Point: number;
		thresholdFor2Points: number;
		thresholdFor4Points: number;
		thresholdFor8Points: number;
	}) {
		return request<ScoringRule>('/score-rules', {
			method: 'POST',
			body: JSON.stringify(rule)
		});
	}
};

export interface EmotionPairInfo {
	id: string;
	negativeLabel: string;
	positiveLabel: string;
}

export interface EmotionPairSeries {
	pairId: string;
	negativeLabel: string;
	positiveLabel: string;
	dailyAverages: (number | null)[];
	overallAverage: number;
	totalEntries: number;
}

export interface EmotionGraphData {
	labels: string[];
	pairs: EmotionPairSeries[];
}

export const emotions = {
	pairs() {
		return request<EmotionPairInfo[]>('/emotions/pairs');
	},
	graph() {
		return request<EmotionGraphData>('/emotions/graph');
	}
};

export const defaults = {
	init() {
		return request<{ success: boolean }>('/init-defaults', {
			method: 'POST'
		});
	}
};
