import { api, unwrap } from './client.js';
import { extractTokenFromBody } from './contracts.js';
export const extractToken = (response) => extractTokenFromBody(response?.data);
const profileUpdateResult = (response) => {
    const data = unwrap(response) || {};
    return {
        profile: data?.profile ?? null,
        token: data?.token ?? null,
        raw: response?.data
    };
};
export const authApi = {
    // STEP 1: current production backend sends an OTP and normally returns no token.
    login: async (payload) => {
        const response = await api.post('/auth/login', payload);
        return { token: extractToken(response), raw: response.data };
    },
    // STEP 2: VerifyOtpRequest {email, otp} -> ApiResponse<LoginResponse>.
    verifyLoginOtp: async (payload) => {
        const response = await api.post('/auth/verify-login-otp', payload);
        return { token: extractToken(response), raw: response.data };
    },
    register: (payload) => api.post('/auth/register', payload),
    me: async () => unwrap(await api.get('/auth/me')),
    update: async (payload) => profileUpdateResult(await api.put('/auth/update', payload)),
    remove: () => api.delete('/auth/delete')
};
export const driverApi = {
    login: async (payload) => {
        const response = await api.post('/driver/login', payload);
        return { token: extractToken(response), raw: response.data };
    },
    register: (payload) => api.post('/driver/register', payload),
    profile: async () => unwrap(await api.get('/driver/profile')),
    update: async (payload) => profileUpdateResult(await api.put('/driver/update', payload)),
    remove: () => api.delete('/driver/delete'),
    updateStatus: (payload) => api.post('/driver/status', payload),
    checkStatus: async () => unwrap(await api.get('/driver/checkStatus'))
};
export const shipmentApi = {
    create: async (payload) => unwrap(await api.post('/shipment', payload)),
    mine: async () => unwrap(await api.get('/shipment')),
    updates: async (trackingNumber) => unwrap(await api.get(`/shipment/${encodeURIComponent(trackingNumber)}/updates`)),
    updateStatus: async (payload) => unwrap(await api.patch('/shipment/status', payload)),
    updateLocation: async (id, payload) => unwrap(await api.post(`/shipment/${id}/location`, payload)),
    publicTrack: async (trackingNumber) => unwrap(await api.get(`/track/${encodeURIComponent(trackingNumber)}`))
};
export const adminApi = {
    login: async (payload) => {
        const response = await api.post('/admin/login', payload);
        return { token: extractToken(response), raw: response.data };
    },
    dashboard: async () => unwrap(await api.get('/admin/dashboard')),
    shipments: async () => unwrap(await api.get('/admin/shipments')),
    assignDriver: async (shipmentId, driverId) => unwrap(await api.put(`/admin/assign/${shipmentId}/${driverId}`)),
    toggleDriverStatus: async (driverId) => unwrap(await api.put(`/admin/driver-status/${driverId}`))
};
