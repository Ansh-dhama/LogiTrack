import { jsx as _jsx, jsxs as _jsxs, Fragment as _Fragment } from "react/jsx-runtime";
import { useState } from 'react';
import { Power, UserRoundCog } from 'lucide-react';
import { adminApi } from '../../api/endpoints.js';
import { messageOf } from '../../api/client.js';
import { Button, Card, Input, PageHeader } from '../../components/UI.js';
export default function DriverOperations() { const [driverId, setDriverId] = useState(''); const [result, setResult] = useState(''); const [loading, setLoading] = useState(false); const toggle = async (e) => { e.preventDefault(); setLoading(true); setResult(''); try {
    const r = await adminApi.toggleDriverStatus(driverId);
    setResult(typeof r === 'string' ? r : JSON.stringify(r));
}
catch (err) {
    setResult(messageOf(err));
}
finally {
    setLoading(false);
} }; return _jsxs(_Fragment, { children: [_jsx(PageHeader, { eyebrow: "Driver operations", title: "Manage driver availability", description: "Your AdminController exposes status toggling by driver ID." }), _jsxs("div", { className: "two-col", children: [_jsxs(Card, { children: [_jsx("div", { className: "feature-icon", children: _jsx(UserRoundCog, {}) }), _jsx("h2", { children: "Toggle driver status" }), _jsxs("p", { className: "muted", children: ["Use a driver ID to switch availability through PUT /admin/driver-status/", driverId, "."] }), _jsxs("form", { className: "form-stack", onSubmit: toggle, children: [_jsx(Input, { label: "Driver ID", type: "number", value: driverId, onChange: e => setDriverId(e.target.value), required: true }), _jsxs(Button, { loading: loading, children: [_jsx(Power, { size: 16 }), " Toggle availability"] })] }), result && _jsx("div", { className: "alert info", children: result })] }), _jsxs(Card, { children: [_jsx("h3", { children: "Why driver ID is manual here" }), _jsxs("p", { className: "muted", children: ["The uploaded controllers do not include an admin endpoint that lists all drivers. Once you add something like ", _jsx("code", { children: "GET /admin/drivers" }), ", this page can become a searchable driver table with one-click status controls."] })] })] })] }); }
