import { describe, it, expect } from 'vitest';
import type {
	LoginResponse,
	Habit,
	HabitCategory,
	ScoringRule,
	Evaluation,
	PredictedHabitScore,
	PredictedWeeklyScore,
	DiaryEntry,
	DiaryStats,
	SleepEntry,
	SleepStats,
	DashboardData,
	DailyTimelineData,
	CorrelationEntry,
	EmotionPairInfo,
	EmotionPairSeries,
	EmotionGraphData,
	PointDevelopmentData
} from './api';

describe('api types', () => {
	it('should define LoginResponse interface correctly', () => {
		const response: LoginResponse = {
			userId: 'test-id',
			username: 'testuser',
			message: 'Login successful',
			success: true
		};
		expect(response.success).toBe(true);
		expect(response.userId).toBe('test-id');
	});

	it('should allow null userId and username in LoginResponse', () => {
		const response: LoginResponse = {
			userId: null,
			username: null,
			message: 'Not logged in',
			success: false
		};
		expect(response.userId).toBeNull();
		expect(response.username).toBeNull();
	});

	it('should define Habit interface with required fields', () => {
		const habit: Habit = {
			id: 'habit-1',
			name: 'Exercise',
			description: 'Daily exercise',
			categoryId: 'cat-1',
			frequencyType: 'DAILY',
			targetFrequency: 1,
			maxEntriesPerDay: 1,
			positiveScoring: true
		};
		expect(habit.frequencyType).toBe('DAILY');
	});

	it('should define Habit with all frequency types', () => {
		const types: Habit['frequencyType'][] = ['DAILY', 'WEEKLY', 'MONTHLY'];
		expect(types).toHaveLength(3);
	});

	it('should define HabitCategory interface', () => {
		const category: HabitCategory = {
			id: 'cat-1',
			name: 'Health',
			description: 'Health habits',
			color: '#FF0000'
		};
		expect(category.name).toBe('Health');
	});

	it('should define ScoringRule interface', () => {
		const rule: ScoringRule = {
			id: 'rule-1',
			name: 'Default',
			thresholdFor1Point: 1,
			thresholdFor2Points: 2,
			thresholdFor4Points: 4,
			thresholdFor8Points: 7
		};
		expect(rule.thresholdFor8Points).toBe(7);
	});

	it('should define Evaluation interface', () => {
		const evaluation: Evaluation = {
			habitId: 'habit-1',
			habitName: 'Exercise',
			periodStart: '2024-01-01',
			periodEnd: '2024-01-31',
			totalEntries: 20,
			targetEntries: 31,
			completionRate: 0.65,
			currentStreak: 3,
			longestStreak: 7,
			positiveScoring: true,
			onTrack: false
		};
		expect(evaluation.completionRate).toBe(0.65);
	});

	it('should define PredictedWeeklyScore with nested habits', () => {
		const prediction: PredictedWeeklyScore = {
			weekStart: '2024-01-01',
			weekEnd: '2024-01-07',
			weekNumber: 1,
			year: 2024,
			calculatedAt: '2024-01-03T12:00:00',
			daysElapsed: 3,
			daysRemaining: 4,
			currentTotalScore: 10,
			predictedTotalScore: 20,
			habitPredictions: [
				{
					habitId: 'h1',
					habitName: 'Exercise',
					categoryId: null,
					currentCompletionCount: 3,
					currentScore: 4,
					predictedCompletionCount: 7,
					predictedScore: 8,
					dailyRate: 1.0
				}
			]
		};
		expect(prediction.habitPredictions).toHaveLength(1);
	});

	it('should define DiaryEntry interface', () => {
		const entry: DiaryEntry = {
			id: 'entry-1',
			description: 'Good day',
			significance: 'MAJOR',
			eventDate: '2024-01-15',
			createdAt: '2024-01-15T10:00:00'
		};
		expect(entry.significance).toBe('MAJOR');
	});

	it('should define DiaryEntry significance values', () => {
		const values: DiaryEntry['significance'][] = ['MINOR', 'NORMAL', 'MAJOR'];
		expect(values).toHaveLength(3);
	});

	it('should define DiaryStats interface', () => {
		const stats: DiaryStats = {
			todayPoints: 4,
			weekPoints: 12,
			monthPoints: 40,
			weeklyAverage: 10,
			monthlyTrend: 1.5
		};
		expect(stats.todayPoints).toBe(4);
	});

	it('should define SleepEntry interface', () => {
		const entry: SleepEntry = {
			id: 'sleep-1',
			fromTime: '23:00',
			untilTime: '07:00',
			date: '2024-01-15',
			createdAt: '2024-01-15T23:00:00',
			notes: 'Good sleep',
			hours: 8
		};
		expect(entry.hours).toBe(8);
	});

	it('should define SleepStats interface', () => {
		const stats: SleepStats = {
			periodStart: '2024-01-01',
			periodEnd: '2024-01-07',
			averageHours: 7.5,
			minHours: 6,
			maxHours: 9,
			totalEntries: 7
		};
		expect(stats.averageHours).toBe(7.5);
	});

	it('should define DashboardData interface', () => {
		const data: DashboardData = {
			labels: ['Day 1', 'Day 2'],
			habitPoints: [5, 3],
			diaryPoints: [2, 4],
			sleepDuration: [7.5, 8],
			sleepEntries: [1, 1]
		};
		expect(data.labels).toHaveLength(2);
	});

	it('should define CorrelationEntry interface', () => {
		const entry: CorrelationEntry = {
			eventA: 'Exercise',
			eventB: 'Sleep Hours',
			correlation: 0.75,
			sharedDays: 30
		};
		expect(entry.correlation).toBe(0.75);
	});

	it('should define EmotionGraphData interface', () => {
		const data: EmotionGraphData = {
			labels: ['2024-01-01', '2024-01-02'],
			pairs: [
				{
					pairId: 'pair-1',
					negativeLabel: 'Sad',
					positiveLabel: 'Happy',
					dailyAverages: [3, null],
					overallAverage: 3,
					totalEntries: 1
				}
			]
		};
		expect(data.pairs).toHaveLength(1);
		expect(data.pairs[0].dailyAverages[1]).toBeNull();
	});

	it('should define PointDevelopmentData interface', () => {
		const data: PointDevelopmentData = {
			dailyPoints: [1, 2, 3],
			labels: ['Mon', 'Tue', 'Wed'],
			runningAverages: [1, 1.5, 2],
			cumulativeTotals: [1, 3, 6],
			totalPoints: 6,
			average: 2
		};
		expect(data.totalPoints).toBe(6);
	});
});
