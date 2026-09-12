import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { authApi, driverApi } from '../../api/endpoints.js';
import { driverRegistrationPayload, userRegistrationPayload } from '../../api/contracts.js';
import { messageOf } from '../../api/client.js';
import Logo from '../../components/Logo.js';
import { Button, Input } from '../../components/UI.js';
export default function Register() {
    const [params] = useSearchParams();
    const navigate = useNavigate();
    const [type, setType] = useState(params.get('type') === 'driver' ? 'DRIVER' : 'USER');
    const [form, setForm] = useState({ username: '', email: '', password: '', confirmPassword: '' });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const isDriver = type === 'DRIVER';
    const change = (e) => setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
    const submit = async (e) => {
        e.preventDefault();
        setError('');
        if (!form.username.trim())
            return setError(isDriver ? 'Driver name is required.' : 'Username is required.');
        if (!form.email.trim())
            return setError('Email is required.');
        if (!form.password)
            return setError('Password is required.');
        if (form.password !== form.confirmPassword)
            return setError('Passwords do not match.');
        setLoading(true);
        try {
            if (isDriver) {
                await driverApi.register(driverRegistrationPayload(form));
                navigate('/login?type=driver', { replace: true });
            }
            else {
                await authApi.register(userRegistrationPayload(form));
                navigate('/login?type=user', { replace: true });
            }
        }
        catch (err) {
            setError(messageOf(err));
        }
        finally {
            setLoading(false);
        }
    };
    return (_jsxs("div", { className: "simple-auth", children: [_jsxs("header", { children: [_jsx(Logo, {}), _jsx(Link, { to: "/login", children: "Already have an account? Sign in" })] }), _jsxs("div", { className: "register-card", children: [_jsxs("div", { className: "register-copy", children: [_jsx("span", { className: "eyebrow", children: "Create account" }), _jsx("h1", { children: isDriver ? 'Join as a driver' : 'Start shipping with LogiTrack' }), _jsx("p", { children: isDriver ? 'Create your driver profile and manage delivery work from one dashboard.' : 'Create your customer account to submit and track shipments.' }), _jsxs("div", { className: "segmented", children: [_jsx("button", { type: "button", className: !isDriver ? 'active' : '', onClick: () => setType('USER'), children: "Customer" }), _jsx("button", { type: "button", className: isDriver ? 'active' : '', onClick: () => setType('DRIVER'), children: "Driver" })] })] }), _jsxs("form", { className: "register-form", onSubmit: submit, children: [_jsx(Input, { label: isDriver ? 'Driver name' : 'Username', name: "username", value: form.username, onChange: change, placeholder: isDriver ? 'Enter driver name' : 'Enter username', required: true }), _jsx(Input, { label: "Email", name: "email", type: "email", value: form.email, onChange: change, placeholder: "you@example.com", required: true }), _jsx(Input, { label: "Password", name: "password", type: "password", value: form.password, onChange: change, placeholder: "Enter password", required: true }), _jsx(Input, { label: "Confirm password", name: "confirmPassword", type: "password", value: form.confirmPassword, onChange: change, placeholder: "Enter password again", required: true }), error && _jsx("div", { className: "alert error", children: error }), _jsxs(Button, { loading: loading, className: "full", children: ["Create ", isDriver ? 'driver' : 'customer', " account"] }), _jsx("small", { className: "form-note", children: isDriver ? 'Sends DriverDto to POST /driver/register.' : 'Sends AuthDto with role USER to POST /auth/register.' })] })] })] }));
}
