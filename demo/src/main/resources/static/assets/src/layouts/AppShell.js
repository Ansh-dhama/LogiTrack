import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { LayoutDashboard, Package, PlusCircle, Route, UserRound, LogOut, Truck, ShieldCheck, Menu, X } from 'lucide-react';
import { useState } from 'react';
import { useAuth } from '../context/AuthContext.js';
import Logo from '../components/Logo.js';
const nav = {
    USER: [
        ['/app', LayoutDashboard, 'Overview'], ['/app/shipments', Package, 'My Shipments'], ['/app/create', PlusCircle, 'Create Shipment'], ['/app/profile', UserRound, 'Profile']
    ],
    DRIVER: [
        ['/driver', LayoutDashboard, 'Driver Home'], ['/driver/profile', UserRound, 'Profile']
    ],
    ADMIN: [
        ['/admin', LayoutDashboard, 'Dashboard'], ['/admin/shipments', Package, 'Shipments'], ['/admin/operations', Truck, 'Driver Ops']
    ]
};
export default function AppShell() {
    const { role, profile, logout } = useAuth();
    const [open, setOpen] = useState(false);
    const location = useLocation();
    const items = nav[role] || nav.USER;
    return _jsxs("div", { className: "app-shell", children: [_jsxs("aside", { className: `sidebar ${open ? 'open' : ''}`, children: [_jsxs("div", { className: "side-top", children: [_jsx(Logo, {}), _jsx("button", { className: "icon-btn mobile-only", onClick: () => setOpen(false), children: _jsx(X, { size: 20 }) })] }), _jsx("nav", { children: items.map(([to, Icon, label]) => _jsxs(NavLink, { end: to === '/app' || to === '/driver' || to === '/admin', to: to, onClick: () => setOpen(false), className: ({ isActive }) => isActive ? 'active' : '', children: [_jsx(Icon, { size: 19 }), _jsx("span", { children: label })] }, to)) }), _jsxs("div", { className: "side-bottom", children: [_jsxs("div", { className: "mini-profile", children: [_jsx("div", { className: "avatar", children: (profile?.username || profile?.driverName || profile?.name || role || 'U').slice(0, 1).toUpperCase() }), _jsxs("div", { children: [_jsx("strong", { children: profile?.username || profile?.driverName || profile?.name || (role === 'ADMIN' ? 'Administrator' : role) }), _jsx("span", { children: profile?.email || `${role} portal` })] })] }), _jsxs("button", { className: "logout", onClick: logout, children: [_jsx(LogOut, { size: 18 }), " Sign out"] })] })] }), _jsxs("main", { className: "main-area", children: [_jsxs("header", { className: "topbar", children: [_jsx("button", { className: "icon-btn mobile-only", onClick: () => setOpen(true), children: _jsx(Menu, { size: 21 }) }), _jsxs("div", { className: "top-context", children: [_jsx(ShieldCheck, { size: 18 }), _jsx("span", { children: role === 'ADMIN' ? 'Admin workspace' : role === 'DRIVER' ? 'Driver workspace' : 'Customer workspace' })] }), _jsxs(NavLink, { className: "track-link", to: "/track", children: [_jsx(Route, { size: 17 }), " Track shipment"] })] }), _jsx("div", { className: "content", children: _jsx(Outlet, {}) }, location.pathname)] })] });
}
