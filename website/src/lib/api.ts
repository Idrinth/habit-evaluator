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
	create(habit: { name: string; description?: string; categoryId?: string; frequencyType: string; targetFrequency: number; maxEntriesPerDay: number; positiveScoring: boolean; nameTranslations?: Record<string, string>; descriptionTranslations?: Record<string, string> }) {
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
