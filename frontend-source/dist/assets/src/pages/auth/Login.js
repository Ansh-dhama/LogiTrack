import { jsx as _jsx, jsxs as _jsxs, Fragment as _Fragment } from "react/jsx-runtime";
import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { ArrowLeft, KeyRound, LockKeyhole, Mail, Shield, Truck, UserRound } from 'lucide-react';
import { useAuth } from '../../context/AuthContext.js';
import { messageOf } from '../../api/client.js';
import Logo from '../../components/Logo.js';
import { Button, Input } from '../../components/UI.js';
export default function Login() {
    const { login, verifyUserOtp } = useAuth();
    const navigate = useNavigate();
    const [params] = useSearchParams();
    const initial = params.get('type') === 'driver' ? 'DRIVER' : params.get('type') === 'admin' ? 'ADMIN' : 'USER';
    const [portal, setPortal] = useState(initial);
    const [form, setForm] = useState({ email: '', password: '' });
    const [otp, setOtp] = useState('');
    const [otpStep, setOtpStep] = useState(false);
    const [pendingEmail, setPendingEmail] = useState('');
    const [notice, setNotice] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const destinationFor = (role) => role === 'ADMIN' ? '/admin' : role === 'DRIVER' ? '/driver' : '/app';
    const choosePortal = (nextPortal) => {
        setPortal(nextPortal);
        setOtpStep(false);
        setOtp('');
        setPendingEmail('');
        setNotice('');
        setError('');
    };
    const submitPassword = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        setNotice('');
        try {
            const result = await login(portal, form);
            if (result?.otpRequired) {
                setPendingEmail(result.email || form.email.trim());
                setNotice(result.message || 'OTP sent to your email.');
                setOtpStep(true);
                return;
            }
            navigate(destinationFor(result?.role || portal), { replace: true });
        }
        catch (err) {
            setError(messageOf(err));
        }
        finally {
            setLoading(false);
        }
    };
    const submitOtp = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            const role = await verifyUserOtp({ email: pendingEmail, otp });
            navigate(destinationFor(role), { replace: true });
        }
        catch (err) {
            setError(messageOf(err));
        }
        finally {
            setLoading(false);
        }
    };
    const resendOtp = async () => {
        setLoading(true);
        setError('');
        setNotice('');
        try {
            const result = await login('USER', form);
            if (result?.otpRequired) {
                setPendingEmail(result.email || form.email.trim());
                setNotice(result.message || 'A new OTP was sent to your email.');
            }
            else {
                navigate('/app', { replace: true });
            }
        }
        catch (err) {
            setError(messageOf(err));
        }
        finally {
            setLoading(false);
        }
    };
    return (_jsxs("div", { className: "auth-page", children: [_jsxs("div", { className: "auth-side", children: [_jsx(Logo, {}), _jsxs("div", { children: [_jsx("span", { className: "pill", children: "Professional shipment management" }), _jsx("h1", { children: "Welcome back to your logistics workspace." }), _jsx("p", { children: "Securely access shipments, tracking, driver operations and administration from one interface." })] }), _jsx("div", { className: "auth-side-foot", children: "LogiTrack \u2022 Light workspace" })] }), _jsx("div", { className: "auth-panel", children: _jsx("div", { className: "auth-box", children: !otpStep ? (_jsxs(_Fragment, { children: [_jsx("h2", { children: "Sign in" }), _jsx("p", { children: "Select your portal and continue with your account." }), _jsx("div", { className: "portal-tabs", children: [
                                    ['USER', UserRound, 'Customer'],
                                    ['DRIVER', Truck, 'Driver'],
                                    ['ADMIN', Shield, 'Admin']
                                ].map(([key, Icon, label]) => (_jsxs("button", { type: "button", onClick: () => choosePortal(key), className: portal === key ? 'active' : '', children: [_jsx(Icon, { size: 18 }), label] }, key))) }), _jsxs("form", { onSubmit: submitPassword, children: [_jsxs("div", { className: "input-icon-wrap", children: [_jsx(Mail, { size: 17 }), _jsx(Input, { label: "Email address", type: "email", value: form.email, onChange: (e) => setForm({ ...form, email: e.target.value }), placeholder: "you@example.com", required: true })] }), _jsxs("div", { className: "input-icon-wrap", children: [_jsx(LockKeyhole, { size: 17 }), _jsx(Input, { label: "Password", type: "password", value: form.password, onChange: (e) => setForm({ ...form, password: e.target.value }), placeholder: "Enter password", required: true })] }), error && _jsx("div", { className: "alert error", children: error }), _jsxs(Button, { loading: loading, className: "full", children: ["Sign in to ", portal.toLowerCase(), " portal"] })] }), portal === 'USER' && _jsx("p", { className: "otp-hint", children: "Customer login uses email OTP verification after your password is accepted." }), portal !== 'ADMIN' && _jsxs("p", { className: "auth-switch", children: ["New here? ", _jsx(Link, { to: `/register?type=${portal.toLowerCase()}`, children: "Create an account" })] }), _jsx(Link, { className: "auth-track", to: "/track", children: "Track a shipment without signing in" })] })) : (_jsxs(_Fragment, { children: [_jsxs("button", { type: "button", className: "otp-back", onClick: () => { setOtpStep(false); setOtp(''); setNotice(''); setError(''); }, children: [_jsx(ArrowLeft, { size: 16 }), " Back to sign in"] }), _jsx("div", { className: "otp-icon", children: _jsx(KeyRound, { size: 24 }) }), _jsx("h2", { children: "Verify your OTP" }), _jsxs("p", { children: ["Enter the OTP sent to ", _jsx("strong", { children: pendingEmail }), "."] }), notice && _jsx("div", { className: "alert success", children: notice }), _jsxs("form", { onSubmit: submitOtp, children: [_jsx(Input, { label: "One-time password", inputMode: "numeric", autoComplete: "one-time-code", value: otp, onChange: (e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6)), maxLength: 6, pattern: "[0-9]{6}", autoFocus: true, placeholder: "Enter 6-digit OTP", required: true }), error && _jsx("div", { className: "alert error", children: error }), _jsx(Button, { loading: loading, className: "full", children: "Verify OTP & continue" })] }), _jsx("button", { type: "button", className: "resend-link", onClick: resendOtp, disabled: loading, children: "Resend OTP" })] })) }) })] }));
}
