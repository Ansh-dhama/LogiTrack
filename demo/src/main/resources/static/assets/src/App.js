import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Navigate, Route, Routes } from 'react-router-dom';
import Home from './pages/public/Home.js';
import TrackShipment from './pages/public/TrackShipment.js';
import Login from './pages/auth/Login.js';
import Register from './pages/auth/Register.js';
import ProtectedRoute from './components/ProtectedRoute.js';
import AppShell from './layouts/AppShell.js';
import UserDashboard from './pages/user/UserDashboard.js';
import Shipments from './pages/user/Shipments.js';
import CreateShipment from './pages/user/CreateShipment.js';
import Profile from './pages/user/Profile.js';
import DriverDashboard from './pages/driver/DriverDashboard.js';
import DriverProfile from './pages/driver/DriverProfile.js';
import AdminDashboard from './pages/admin/AdminDashboard.js';
import AdminShipments from './pages/admin/AdminShipments.js';
import DriverOperations from './pages/admin/DriverOperations.js';
import { useAuth } from './context/AuthContext.js';
function LoginGuard() { const { token, role } = useAuth(); if (token)
    return _jsx(Navigate, { to: role === 'ADMIN' ? '/admin' : role === 'DRIVER' ? '/driver' : '/app', replace: true }); return _jsx(Login, {}); }
export default function App() { return _jsxs(Routes, { children: [_jsx(Route, { path: "/", element: _jsx(Home, {}) }), _jsx(Route, { path: "/track", element: _jsx(TrackShipment, {}) }), _jsx(Route, { path: "/login", element: _jsx(LoginGuard, {}) }), _jsx(Route, { path: "/register", element: _jsx(Register, {}) }), _jsxs(Route, { element: _jsx(ProtectedRoute, { roles: ['USER', 'CUSTOMER'] }), children: [" ", _jsxs(Route, { element: _jsx(AppShell, {}), children: [_jsx(Route, { path: "/app", element: _jsx(UserDashboard, {}) }), _jsx(Route, { path: "/app/shipments", element: _jsx(Shipments, {}) }), _jsx(Route, { path: "/app/create", element: _jsx(CreateShipment, {}) }), _jsx(Route, { path: "/app/profile", element: _jsx(Profile, {}) })] })] }), _jsxs(Route, { element: _jsx(ProtectedRoute, { roles: ['DRIVER'] }), children: [" ", _jsxs(Route, { element: _jsx(AppShell, {}), children: [_jsx(Route, { path: "/driver", element: _jsx(DriverDashboard, {}) }), _jsx(Route, { path: "/driver/profile", element: _jsx(DriverProfile, {}) })] })] }), _jsxs(Route, { element: _jsx(ProtectedRoute, { roles: ['ADMIN'] }), children: [" ", _jsxs(Route, { element: _jsx(AppShell, {}), children: [_jsx(Route, { path: "/admin", element: _jsx(AdminDashboard, {}) }), _jsx(Route, { path: "/admin/shipments", element: _jsx(AdminShipments, {}) }), _jsx(Route, { path: "/admin/operations", element: _jsx(DriverOperations, {}) })] })] }), _jsx(Route, { path: "*", element: _jsx(Navigate, { to: "/", replace: true }) })] }); }
