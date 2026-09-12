import { jsx as _jsx, jsxs as _jsxs, Fragment as _Fragment } from "react/jsx-runtime";
import { useEffect, useState } from 'react';
import { Save, Trash2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { messageOf } from '../../api/client.js';
import { driverApi } from '../../api/endpoints.js';
import { useAuth } from '../../context/AuthContext.js';
import { Button, Card, Input, PageHeader } from '../../components/UI.js';
export default function DriverProfile() {
    const { profile, updateDriverProfile, logout } = useAuth();
    const navigate = useNavigate();
    const [form, setForm] = useState({ driverName: '', email: '', newPassword: '' });
    const [msg, setMsg] = useState('');
    const [loading, setLoading] = useState(false);
    useEffect(() => {
        if (profile)
            setForm({ driverName: profile.driverName || '', email: profile.email || '', newPassword: '' });
    }, [profile]);
    const save = async (e) => {
        e.preventDefault();
        setLoading(true);
        setMsg('');
        try {
            const payload = {
                driverName: form.driverName.trim(),
                email: form.email.trim()
            };
            if (form.newPassword)
                payload.password = form.newPassword;
            await updateDriverProfile(payload);
            setForm((prev) => ({ ...prev, newPassword: '' }));
            setMsg('Driver profile updated.');
        }
        catch (e) {
            setMsg(messageOf(e));
        }
        finally {
            setLoading(false);
        }
    };
    const remove = async () => {
        if (!confirm('Delete your driver account?'))
            return;
        try {
            await driverApi.remove();
            logout();
            navigate('/');
        }
        catch (e) {
            setMsg(messageOf(e));
        }
    };
    return (_jsxs(_Fragment, { children: [_jsx(PageHeader, { eyebrow: "Driver account", title: "Driver profile", description: "Edit driverName and email without exposing password data returned by the DTO." }), _jsx(Card, { className: "profile-card", children: _jsxs("form", { className: "form-stack", onSubmit: save, children: [_jsx(Input, { label: "Driver name", value: form.driverName, onChange: (e) => setForm({ ...form, driverName: e.target.value }), required: true }), _jsx(Input, { label: "Email", type: "email", value: form.email, onChange: (e) => setForm({ ...form, email: e.target.value }), required: true }), _jsx(Input, { label: "New password (optional)", type: "password", value: form.newPassword, onChange: (e) => setForm({ ...form, newPassword: e.target.value }), autoComplete: "new-password" }), msg && _jsx("div", { className: "alert info", children: msg }), _jsxs("div", { className: "profile-actions", children: [_jsxs(Button, { loading: loading, children: [_jsx(Save, { size: 16 }), " Save profile"] }), _jsxs(Button, { type: "button", variant: "danger", onClick: remove, children: [_jsx(Trash2, { size: 16 }), " Delete driver account"] })] })] }) })] }));
}
