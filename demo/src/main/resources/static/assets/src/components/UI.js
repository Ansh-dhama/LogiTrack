import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { LoaderCircle, Search } from 'lucide-react';
export function Button({ children, variant = 'primary', loading = false, className = '', ...props }) {
    return _jsxs("button", { className: `btn btn-${variant} ${className}`, disabled: loading || props.disabled, ...props, children: [loading && _jsx(LoaderCircle, { className: "spin", size: 16 }), " ", children] });
}
export function Input({ label, hint, ...props }) {
    return _jsxs("label", { className: "field", children: [_jsx("span", { children: label }), _jsx("input", { ...props }), hint && _jsx("small", { children: hint })] });
}
export function Select({ label, children, ...props }) {
    return _jsxs("label", { className: "field", children: [_jsx("span", { children: label }), _jsx("select", { ...props, children: children })] });
}
export function Textarea({ label, ...props }) {
    return _jsxs("label", { className: "field", children: [_jsx("span", { children: label }), _jsx("textarea", { ...props })] });
}
export function Card({ children, className = '' }) { return _jsx("div", { className: `card ${className}`, children: children }); }
export function Badge({ children, tone = 'neutral' }) { return _jsx("span", { className: `badge badge-${tone}`, children: children }); }
export function Empty({ title = 'Nothing here yet', text = 'New data will appear here.' }) { return _jsxs("div", { className: "empty", children: [_jsx("div", { className: "empty-icon", children: _jsx(Search, { size: 22 }) }), _jsx("strong", { children: title }), _jsx("p", { children: text })] }); }
export function PageHeader({ eyebrow, title, description, action }) { return _jsxs("div", { className: "page-header", children: [_jsxs("div", { children: [_jsx("span", { className: "eyebrow", children: eyebrow }), _jsx("h1", { children: title }), description && _jsx("p", { children: description })] }), action] }); }
export function StatCard({ label, value, icon: Icon, hint }) { return _jsxs(Card, { className: "stat-card", children: [_jsx("div", { className: "stat-icon", children: _jsx(Icon, { size: 20 }) }), _jsxs("div", { children: [_jsx("span", { children: label }), _jsx("strong", { children: String(value ?? 0) }), hint && _jsx("small", { children: hint })] })] }); }
export function LoadingBlock() { return _jsxs("div", { className: "loading-block", children: [_jsx(LoaderCircle, { className: "spin", size: 24 }), _jsx("span", { children: "Loading\u2026" })] }); }
