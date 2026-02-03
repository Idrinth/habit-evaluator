const BASE = '/api';

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
	const response = await fetch(`${BASE}${path}`, {
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
	if (response.status === 204 || response.headers.get('content-length') === '0') {
		return null as T;
	}
	const text = await response.text();
	if (!text) {
		return null as T;
	}
	return JSON.parse(text);
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
	entries?: { completedAt: string }[];
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

export const habits = {
	list() {
		return request<Habit[]>('/habits');
	},
	create(habit: { name: string; description?: string; categoryId?: string; frequencyType: string; targetFrequency: number; maxEntriesPerDay: number; positiveScoring: boolean }) {
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

export const stats = {
	dashboard() {
		return request<DashboardData>('/stats/dashboard');
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
