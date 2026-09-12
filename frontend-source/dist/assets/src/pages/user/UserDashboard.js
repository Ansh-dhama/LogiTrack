import { jsx as _jsx, jsxs as _jsxs, Fragment as _Fragment } from "react/jsx-runtime";
import { useEffect, useState } from 'react';
import { ArrowRight, CheckCircle2, Clock3, Package, Plus, Truck } from 'lucide-react';
import { Link } from 'react-router-dom';
import { shipmentApi } from '../../api/endpoints.js';
import { messageOf } from '../../api/client.js';
import { Badge, Button, Card, Empty, LoadingBlock, PageHeader, StatCard } from '../../components/UI.js';
const tone = (s = '') => /deliver/i.test(s) ? 'success' : /transit|pick/i.test(s) ? 'info' : /cancel|fail/i.test(s) ? 'danger' : 'warning';
export default function UserDashboard() {
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    useEffect(() => {
        shipmentApi.mine().then((d) => setItems(Array.isArray(d) ? d : [])).catch((e) => setError(messageOf(e))).finally(() => setLoading(false));
    }, []);
    const delivered = items.filter((x) => /deliver/i.test(x.status || '')).length;
    const active = items.length - delivered;
    return (_jsxs(_Fragment, { children: [_jsx(PageHeader, { eyebrow: "Customer dashboard", title: "Your shipments at a glance", description: "Create deliveries, follow progress and keep every tracking number in one place.", action: _jsx(Link, { to: "/app/create", children: _jsxs(Button, { children: [_jsx(Plus, { size: 17 }), " New shipment"] }) }) }), _jsxs("div", { className: "stats-grid", children: [_jsx(StatCard, { label: "Total shipments", value: items.length, icon: Package }), _jsx(StatCard, { label: "Active", value: active, icon: Truck }), _jsx(StatCard, { label: "Delivered", value: delivered, icon: CheckCircle2 }), _jsx(StatCard, { label: "Pending updates", value: Math.max(active, 0), icon: Clock3 })] }), _jsxs(Card, { children: [_jsxs("div", { className: "section-head", children: [_jsxs("div", { children: [_jsx("h2", { children: "Recent shipments" }), _jsx("p", { children: "Your latest delivery activity." })] }), _jsxs(Link, { to: "/app/shipments", children: ["View all ", _jsx(ArrowRight, { size: 16 })] })] }), loading ? _jsx(LoadingBlock, {}) : error ? _jsx("div", { className: "alert error", children: error }) : items.length === 0 ? _jsx(Empty, { title: "No shipments yet", text: "Create your first shipment to start tracking." }) : (_jsx("div", { className: "table-wrap", children: _jsxs("table", { children: [_jsx("thead", { children: _jsxs("tr", { children: [_jsx("th", { children: "Tracking" }), _jsx("th", { children: "Receiver address" }), _jsx("th", { children: "Weight" }), _jsx("th", { children: "Status" })] }) }), _jsx("tbody", { children: items.slice(0, 6).map((s, i) => _jsxs("tr", { children: [_jsx("td", { children: _jsx("strong", { children: s.trackingNumber || `Shipment ${i + 1}` }) }), _jsx("td", { children: s.receiverAddress || '—' }), _jsx("td", { children: s.weight != null ? `${s.weight} kg` : '—' }), _jsx("td", { children: _jsx(Badge, { tone: tone(s.status), children: s.status || 'Created' }) })] }, s.trackingNumber || i)) })] }) }))] })] }));
}
