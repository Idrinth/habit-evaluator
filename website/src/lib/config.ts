/**
 * Runtime configuration for the application.
 * Values are injected at deployment time via the /config.js script.
 */

interface RuntimeConfig {
	API_BASE_URL: string;
}

declare global {
	interface Window {
		__RUNTIME_CONFIG__?: Partial<RuntimeConfig>;
	}
}

const DEFAULT_CONFIG: RuntimeConfig = {
	API_BASE_URL: '/api'
};

/**
 * Gets the runtime configuration, merging defaults with any injected values.
 * The config.js script is generated at container startup from environment variables.
 */
export function getConfig(): RuntimeConfig {
	const runtimeConfig = typeof window !== 'undefined' ? window.__RUNTIME_CONFIG__ : undefined;
	return {
		...DEFAULT_CONFIG,
		...runtimeConfig
	};
}

/**
 * Gets the API base URL from runtime configuration.
 */
export function getApiBaseUrl(): string {
	return getConfig().API_BASE_URL;
}
