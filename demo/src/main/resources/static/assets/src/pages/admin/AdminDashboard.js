import { jsx as _jsx, jsxs as _jsxs, Fragment as _Fragment } from "react/jsx-runtime";
import { useEffect, useState } from 'react';
import { Boxes, CalendarDays, CheckCircle2, Clock3, Truck } from 'lucide-react';
import { adminApi } from '../../api/endpoints.js';
import { messageOf } from '../../api/client.js';
import { LoadingBlock, PageHeader, StatCard } from '../../components/UI.js';
export default function AdminDashboard() {
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    useEffect(() => { adminApi.dashboard().then(setStats).catch((e) => setError(messageOf(e))).finally(() => setLoading(false)); }, []);
    if (loading)
        return _jsx(LoadingBlock, {});
    return (_jsxs(_Fragment, { children: [_jsx(PageHeader, { eyebrow: "Administration", title: "Operations dashboard", description: "Live metrics returned by AdminDashboardDto." }), error && _jsx("div", { className: "alert error", children: error }), _jsxs("div", { className: "stats-grid", children: [_jsx(StatCard, { label: "Total shipments", value: stats?.totalShipments, icon: Boxes }), _jsx(StatCard, { label: "Pending shipments", value: stats?.pendingShipments, icon: Clock3 }), _jsx(StatCard, { label: "Delivered shipments", value: stats?.deliveredShipments, icon: CheckCircle2 }), _jsx(StatCard, { label: "Shipments today", value: stats?.shipmentsToday, icon: CalendarDays }), _jsx(StatCard, { label: "Total drivers", value: stats?.totalDrivers, icon: Truck })] })] }));
}
