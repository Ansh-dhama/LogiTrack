import { jsx as _jsx } from "react/jsx-runtime";
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.js';
export default function ProtectedRoute({ roles }) {
    const { token, role } = useAuth();
    if (!token)
        return _jsx(Navigate, { to: "/login", replace: true });
    if (roles && !roles.includes(role))
        return _jsx(Navigate, { to: role === 'ADMIN' ? '/admin' : role === 'DRIVER' ? '/driver' : '/app', replace: true });
    return _jsx(Outlet, {});
}
