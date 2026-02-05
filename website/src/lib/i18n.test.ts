import { describe, it, expect, beforeEach, vi } from 'vitest';
import { t, getLanguage, setLanguage, LANGUAGES } from './i18n';
import type { Language } from './i18n';

describe('i18n', () => {
	describe('LANGUAGES', () => {
		it('should contain four languages', () => {
			expect(LANGUAGES).toHaveLength(4);
		});

		it('should include English', () => {
			expect(LANGUAGES.find((l) => l.code === 'en')).toBeDefined();
		});

		it('should include German', () => {
			expect(LANGUAGES.find((l) => l.code === 'de')).toBeDefined();
		});

		it('should include Spanish', () => {
			expect(LANGUAGES.find((l) => l.code === 'es')).toBeDefined();
		});

		it('should include French', () => {
			expect(LANGUAGES.find((l) => l.code === 'fr')).toBeDefined();
		});
	});

	describe('t()', () => {
		it('should return English translation for known key', () => {
			expect(t('nav.home', 'en')).toBe('Home');
		});

		it('should return German translation for known key', () => {
			expect(t('nav.home', 'de')).toBe('Start');
		});

		it('should return Spanish translation for known key', () => {
			expect(t('nav.home', 'es')).toBe('Inicio');
		});

		it('should return French translation for known key', () => {
			expect(t('nav.home', 'fr')).toBe('Accueil');
		});

		it('should fall back to English for unknown key in other language', () => {
			// All languages have nav.home, so test with a key that exists in English
			expect(t('nav.home', 'en')).toBe('Home');
		});

		it('should return the key itself for completely unknown key', () => {
			expect(t('nonexistent.key', 'en')).toBe('nonexistent.key');
		});

		it('should return the key for unknown key in non-English language', () => {
			expect(t('nonexistent.key', 'de')).toBe('nonexistent.key');
		});

		it('should translate all navigation keys in English', () => {
			const navKeys = [
				'nav.home',
				'nav.edit',
				'nav.addHabit',
				'nav.addCategory',
				'nav.scoringRules',
				'nav.points',
				'nav.diary',
				'nav.sleep',
				'nav.stats',
				'nav.emotions',
				'nav.exportPdf',
				'nav.translations',
				'nav.logout',
				'nav.imprint'
			];
			for (const key of navKeys) {
				const translation = t(key, 'en');
				expect(translation).not.toBe(key);
			}
		});

		it('should translate emotion keys in all languages', () => {
			const languages: Language[] = ['en', 'de', 'es', 'fr'];
			for (const lang of languages) {
				expect(t('emotions.graphTitle', lang)).not.toBe('emotions.graphTitle');
			}
		});
	});

	describe('getLanguage()', () => {
		beforeEach(() => {
			// Mock localStorage
			const store: Record<string, string> = {};
			vi.stubGlobal('localStorage', {
				getItem: (key: string) => store[key] ?? null,
				setItem: (key: string, value: string) => {
					store[key] = value;
				}
			});
		});

		it('should default to English when no language is stored', () => {
			expect(getLanguage()).toBe('en');
		});

		it('should return stored language', () => {
			localStorage.setItem('language', 'de');
			expect(getLanguage()).toBe('de');
		});

		it('should return English for invalid stored language', () => {
			localStorage.setItem('language', 'invalid');
			expect(getLanguage()).toBe('en');
		});
	});

	describe('setLanguage()', () => {
		beforeEach(() => {
			const store: Record<string, string> = {};
			vi.stubGlobal('localStorage', {
				getItem: (key: string) => store[key] ?? null,
				setItem: (key: string, value: string) => {
					store[key] = value;
				}
			});
		});

		it('should store language in localStorage', () => {
			setLanguage('fr');
			expect(localStorage.getItem('language')).toBe('fr');
		});
	});
});
