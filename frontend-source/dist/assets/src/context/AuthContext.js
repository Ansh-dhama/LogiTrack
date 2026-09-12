import { jsx as _jsx } from "react/jsx-runtime";
import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { adminApi, authApi, driverApi } from '../api/endpoints.js';
import { loginPayload, verifyOtpPayload } from '../api/contracts.js';
const AuthContext = createContext(null);
const validRole = (role) => ['USER', 'DRIVER', 'ADMIN'].includes(role) ? role : null;
export function AuthProvider({ children }) {
    const initialRole = validRole(localStorage.getItem('logitrack_role'));
    const initialToken = initialRole ? localStorage.getItem('logitrack_token') : null;
    const [token, setToken] = useState(initialToken);
    const [role, setRole] = useState(initialRole);
    const [profile, setProfile] = useState(null);
    const [loadingProfile, setLoadingProfile] = useState(false);
    const persist = (newToken, newRole) => {
        localStorage.setItem('logitrack_token', newToken);
        localStorage.setItem('logitrack_role', newRole);
        setToken(newToken);
        setRole(newRole);
    };
    const login = async (portal, credentials) => {
        const normalizedPortal = validRole(portal) || 'USER';
        const payload = loginPayload(credentials);
        if (normalizedPortal === 'USER') {
            const result = await authApi.login(payload);
            // Customer login is ALWAYS two-step. /auth/login only verifies the
            // password and starts OTP verification. A JWT is accepted only from
            // /auth/verify-login-otp.
            if (result?.raw?.success === true) {
                return {
                    role: 'USER',
                    otpRequired: true,
                    email: payload.email,
                    message: result.raw?.message || 'OTP sent to your email.'
                };
            }
            console.error('Unexpected user login response:', result?.raw);
            throw new Error(result?.raw?.message || 'Unable to start OTP login.');
        }
        const service = normalizedPortal === 'ADMIN' ? adminApi : driverApi;
        const result = await service.login(payload);
        if (!result?.token || typeof result.token !== 'string') {
            console.error('Unexpected login response:', result?.raw);
            throw new Error('Login succeeded but no JWT token was found in the response.');
        }
        persist(result.token, normalizedPortal);
        return { role: normalizedPortal, otpRequired: false };
    };
    const verifyUserOtp = async ({ email, otp }) => {
        const result = await authApi.verifyLoginOtp(verifyOtpPayload({ email, otp }));
        if (!result?.token || typeof result.token !== 'string') {
            console.error('Unexpected OTP verification response:', result?.raw);
            throw new Error(result?.raw?.message || 'OTP verified but no JWT token was returned.');
        }
        persist(result.token, 'USER');
        return 'USER';
    };
    const logout = () => {
        localStorage.removeItem('logitrack_token');
        localStorage.removeItem('logitrack_role');
        setToken(null);
        setRole(null);
        setProfile(null);
    };
    const refreshProfile = async () => {
        if (!token || !role || role === 'ADMIN') {
            setProfile(null);
            return null;
        }
        setLoadingProfile(true);
        try {
            const data = role === 'DRIVER' ? await driverApi.profile() : await authApi.me();
            setProfile(data);
            return data;
        }
        finally {
            setLoadingProfile(false);
        }
    };
    const updateUserProfile = async (payload) => {
        const result = await authApi.update(payload);
        if (!result?.token)
            throw new Error('Profile updated but refreshed JWT was not returned.');
        persist(result.token, 'USER');
        if (result.profile)
            setProfile(result.profile);
        return result.profile;
    };
    const updateDriverProfile = async (payload) => {
        const result = await driverApi.update(payload);
        if (!result?.token)
            throw new Error('Driver profile updated but refreshed JWT was not returned.');
        persist(result.token, 'DRIVER');
        if (result.profile)
            setProfile(result.profile);
        return result.profile;
    };
    useEffect(() => {
        const unauthorized = () => logout();
        window.addEventListener('logitrack:unauthorized', unauthorized);
        return () => window.removeEventListener('logitrack:unauthorized', unauthorized);
    }, []);
    useEffect(() => {
        if (token && role && role !== 'ADMIN')
            refreshProfile().catch(() => { });
    }, [token, role]);
    const value = useMemo(() => ({
        token,
        role,
        profile,
        setProfile,
        loadingProfile,
        login,
        verifyUserOtp,
        logout,
        refreshProfile,
        updateUserProfile,
        updateDriverProfile
    }), [token, role, profile, loadingProfile]);
    return _jsx(AuthContext.Provider, { value: value, children: children });
}
export const useAuth = () => useContext(AuthContext);
