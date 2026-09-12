import { jsx as _jsx, jsxs as _jsxs, Fragment as _Fragment } from "react/jsx-runtime";
import { useEffect, useState } from 'react';
import { Save, Trash2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { messageOf } from '../../api/client.js';
import { authApi } from '../../api/endpoints.js';
import { useAuth } from '../../context/AuthContext.js';
import { Button, Card, Input, PageHeader } from '../../components/UI.js';
export default function Profile() {
    const { profile, updateUserProfile, logout } = useAuth();
    const navigate = useNavigate();
    const [form, setForm] = useState({ username: '', email: '', newPassword: '' });
    const [loading, setLoading] = useState(false);
    const [msg, setMsg] = useState('');
    useEffect(() => {
        if (profile) {
            setForm({ username: profile.username || '', email: profile.email || '', newPassword: '' });
        }
    }, [profile]);
    const save = async (e) => {
        e.preventDefault();
        setLoading(true);
        setMsg('');
        try {
            const payload = {
                username: form.username.trim(),
                email: form.email.trim(),
                role: profile?.role || 'USER'
            };
            if (form.newPassword)
                payload.password = form.newPassword;
            await updateUserProfile(payload);
            setForm((prev) => ({ ...prev, newPassword: '' }));
            setMsg('Profile updated successfully.');
        }
        catch (e) {
            setMsg(messageOf(e));
        }
        finally {
            setLoading(false);
        }
    };
    const remove = async () => {
        if (!confirm('Delete your account permanently?'))
            return;
        try {
            await authApi.remove();
            logout();
            navigate('/');
        }
        catch (e) {
            setMsg(messageOf(e));
        }
    };
    return (_jsxs(_Fragment, { children: [_jsx(PageHeader, { eyebrow: "Account", title: "Profile settings", description: "Safe editable fields from the AuthDto returned by /auth/me." }), _jsx(Card, { className: "profile-card", children: _jsxs("form", { className: "form-stack", onSubmit: save, children: [_jsx(Input, { label: "Username", value: form.username, onChange: (e) => setForm({ ...form, username: e.target.value }), required: true }), _jsx(Input, { label: "Email", type: "email", value: form.email, onChange: (e) => setForm({ ...form, email: e.target.value }), required: true }), _jsx(Input, { label: "New password (optional)", type: "password", value: form.newPassword, onChange: (e) => setForm({ ...form, newPassword: e.target.value }), autoComplete: "new-password" }), msg && _jsx("div", { className: "alert info", children: msg }), _jsxs("div", { className: "profile-actions", children: [_jsxs(Button, { loading: loading, children: [_jsx(Save, { size: 16 }), " Save changes"] }), _jsxs(Button, { type: "button", variant: "danger", onClick: remove, children: [_jsx(Trash2, { size: 16 }), " Delete account"] })] })] }) })] }));
}
