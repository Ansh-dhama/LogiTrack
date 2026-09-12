import axios from 'axios';
const configuredBaseUrl = (import.meta.env?.VITE_API_BASE_URL || '').trim();
const PUBLIC_API_PATHS = [
    '/auth/login',
    '/auth/verify-login-otp',
    '/auth/register',
    '/driver/login',
    '/driver/register',
    '/admin/login'
];
const isPublicApiPath = (url = '') => PUBLIC_API_PATHS.includes(url) || url.startsWith('/track/');
export const api = axios.create({
    // Production: leave empty because Vite dist is served by Spring Boot on :8090.
    // Development: vite.config.js proxies API paths to http://localhost:8090.
    baseURL: configuredBaseUrl,
    timeout: 15000,
    headers: { 'Content-Type': 'application/json' }
});
api.interceptors.request.use((config) => {
    const token = globalThis.localStorage?.getItem('logitrack_token');
    // Never send an old/stale JWT to public login/register/track endpoints.
    if (token && !isPublicApiPath(config.url || '')) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    else if (config.headers?.Authorization) {
        delete config.headers.Authorization;
    }
    return config;
});
api.interceptors.response.use((response) => response, (error) => {
    const url = error?.config?.url || '';
    const hasToken = Boolean(globalThis.localStorage?.getItem('logitrack_token'));
    if (error.response?.status === 401 && hasToken && !isPublicApiPath(url)) {
        globalThis.localStorage?.removeItem('logitrack_token');
        globalThis.localStorage?.removeItem('logitrack_role');
        globalThis.window?.dispatchEvent?.(new Event('logitrack:unauthorized'));
    }
    return Promise.reject(error);
});
// ApiResponse<T> => response.data.data. Bare controller responses => response.data.
export const unwrap = (response) => response?.data?.data ?? response?.data;
export const messageOf = (error) => {
    const body = error?.response?.data;
    if (typeof body === 'string' && body.trim())
        return body;
    if (body?.data && typeof body.data === 'object' && !Array.isArray(body.data)) {
        const details = Object.values(body.data).filter(Boolean).join(' • ');
        if (details)
            return body.message ? `${body.message}: ${details}` : details;
    }
    return body?.message || body?.error || error?.message || 'Something went wrong';
};
