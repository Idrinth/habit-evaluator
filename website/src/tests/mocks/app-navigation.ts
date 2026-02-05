import { vi } from 'vitest';

export const goto = vi.fn();
export const invalidate = vi.fn().mockResolvedValue(undefined);
export const invalidateAll = vi.fn().mockResolvedValue(undefined);
export const beforeNavigate = vi.fn();
export const afterNavigate = vi.fn();
export const onNavigate = vi.fn();
