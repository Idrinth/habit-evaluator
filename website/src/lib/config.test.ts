import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { getConfig, getApiBaseUrl } from './config';

describe('config', () => {
	beforeEach(() => {
		// Reset window.__RUNTIME_CONFIG__ before each test
		if (typeof window !== 'undefined') {
			delete window.__RUNTIME_CONFIG__;
		}
	});

	afterEach(() => {
		// Cleanup after each test
		if (typeof window !== 'undefined') {
			delete window.__RUNTIME_CONFIG__;
		}
	});

	describe('getConfig', () => {
		it('returns default config when no runtime config is set', () => {
			const config = getConfig();
			expect(config.API_BASE_URL).toBe('/api');
		});

		it('returns runtime config when set', () => {
			window.__RUNTIME_CONFIG__ = {
				API_BASE_URL: 'https://api.example.com'
			};
			const config = getConfig();
			expect(config.API_BASE_URL).toBe('https://api.example.com');
		});

		it('merges partial runtime config with defaults', () => {
			window.__RUNTIME_CONFIG__ = {};
			const config = getConfig();
			expect(config.API_BASE_URL).toBe('/api');
		});
	});

	describe('getApiBaseUrl', () => {
		it('returns default /api when no runtime config is set', () => {
			expect(getApiBaseUrl()).toBe('/api');
		});

		it('returns configured API base URL', () => {
			window.__RUNTIME_CONFIG__ = {
				API_BASE_URL: 'https://custom-api.example.com/v1'
			};
			expect(getApiBaseUrl()).toBe('https://custom-api.example.com/v1');
		});
	});
});
