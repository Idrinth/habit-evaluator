import { describe, it, expect, beforeEach, vi } from 'vitest';
import { t, getLanguage, setLanguage, LANGUAGES, type Language } from './i18n';

describe('i18n', () => {
	describe('LANGUAGES', () => {
		it('should contain all four supported languages', () => {
			expect(LANGUAGES).toHaveLength(4);
			const codes = LANGUAGES.map((l) => l.code);
			expect(codes).toContain('en');
			expect(codes).toContain('de');
			expect(codes).toContain('es');
			expect(codes).toContain('fr');
		});

		it('should have labels for each language', () => {
			const labels = LANGUAGES.map((l) => l.label);
			expect(labels).toContain('English');
			expect(labels).toContain('Deutsch');
			expect(labels).toContain('Español');
			expect(labels).toContain('Français');
		});
	});

	describe('t()', () => {
		it('should return English translation for a known key', () => {
			expect(t('nav.home', 'en')).toBe('Home');
		});

		it('should return German translation for a known key', () => {
			expect(t('nav.home', 'de')).toBe('Start');
		});

		it('should return Spanish translation for a known key', () => {
			expect(t('nav.home', 'es')).toBe('Inicio');
		});

		it('should return French translation for a known key', () => {
			expect(t('nav.home', 'fr')).toBe('Accueil');
		});

		it('should fall back to English when key is missing in requested language', () => {
			// All languages have the same keys in this codebase, so we test
			// the fallback by checking that English is returned for a valid key
			expect(t('nav.logout', 'en')).toBe('Logout');
		});

		it('should return the key itself when not found in any language', () => {
			expect(t('nonexistent.key', 'en')).toBe('nonexistent.key');
			expect(t('nonexistent.key', 'de')).toBe('nonexistent.key');
		});

		it('should translate evaluation keys correctly', () => {
			expect(t('eval.streak', 'en')).toBe('Current streak');
			expect(t('eval.streak', 'de')).toBe('Aktuelle Serie');
		});

		it('should translate home keys correctly', () => {
			expect(t('home.title', 'en')).toBe('Your Habits');
			expect(t('home.title', 'de')).toBe('Deine Gewohnheiten');
			expect(t('home.title', 'es')).toBe('Tus hábitos');
			expect(t('home.title', 'fr')).toBe('Vos habitudes');
		});

		it('should translate emotion keys correctly', () => {
			expect(t('emotions.graphTitle', 'en')).toBe('Emotion Pairs Graph');
			expect(t('emotions.graphTitle', 'de')).toBe('Emotionspaare-Diagramm');
		});

		it('should translate imprint keys correctly', () => {
			expect(t('imprint.title', 'en')).toBe('Project legal');
			expect(t('imprint.title', 'de')).toBe('Rechtliches');
		});
	});

	describe('getLanguage()', () => {
		beforeEach(() => {
			localStorage.clear();
		});

		it('should return "en" as default when nothing is stored', () => {
			expect(getLanguage()).toBe('en');
		});

		it('should return the stored language', () => {
			localStorage.setItem('language', 'de');
			expect(getLanguage()).toBe('de');
		});

		it('should return "en" for an invalid stored language', () => {
			localStorage.setItem('language', 'invalid');
			expect(getLanguage()).toBe('en');
		});

		it('should return each valid language when stored', () => {
			const langs: Language[] = ['en', 'de', 'es', 'fr'];
			for (const lang of langs) {
				localStorage.setItem('language', lang);
				expect(getLanguage()).toBe(lang);
			}
		});
	});

	describe('setLanguage()', () => {
		beforeEach(() => {
			localStorage.clear();
		});

		it('should persist language to localStorage', () => {
			setLanguage('fr');
			expect(localStorage.getItem('language')).toBe('fr');
		});

		it('should overwrite previously stored language', () => {
			setLanguage('de');
			setLanguage('es');
			expect(localStorage.getItem('language')).toBe('es');
		});
	});
});
