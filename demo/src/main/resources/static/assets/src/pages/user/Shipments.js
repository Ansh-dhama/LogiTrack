import { jsx as _jsx, jsxs as _jsxs, Fragment as _Fragment } from "react/jsx-runtime";
import { useEffect, useState } from 'react';
import { ExternalLink, History, RefreshCcw, X } from 'lucide-react';
import { Link } from 'react-router-dom';
import { shipmentApi } from '../../api/endpoints.js';
import { messageOf } from '../../api/client.js';
import { Badge, Button, Card, Empty, LoadingBlock, PageHeader } from '../../components/UI.js';
const tone = (s = '') => /deliver/i.test(s) ? 'success' : /transit|pick/i.test(s) ? 'info' : /cancel|fail/i.test(s) ? 'danger' : 'warning';
export default function Shipments() {
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [updateView, setUpdateView] = useState(null);
    const [updatesLoading, setUpdatesLoading] = useState(false);
    const load = () => {
        setLoading(true);
        setError('');
        shipmentApi.mine()
            .then((d) => setItems(Array.isArray(d) ? d : []))
            .catch((e) => setError(messageOf(e)))
            .finally(() => setLoading(false));
    };
    useEffect(load, []);
    const showUpdates = async (trackingNumber) => {
        setUpdatesLoading(true);
        setError('');
        try {
            const updates = await shipmentApi.updates(trackingNumber);
            setUpdateView({ trackingNumber, updates: updates || {} });
        }
        catch (e) {
            setError(messageOf(e));
        }
        finally {
            setUpdatesLoading(false);
        }
    };
    return (_jsxs(_Fragment, { children: [_jsx(PageHeader, { eyebrow: "Shipments", title: "All shipments", description: "Review every shipment created from your account.", action: _jsxs(Button, { variant: "secondary", onClick: load, children: [_jsx(RefreshCcw, { size: 16 }), " Refresh"] }) }), _jsx(Card, { children: loading ? _jsx(LoadingBlock, {}) : error ? _jsx("div", { className: "alert error", children: error }) : items.length === 0 ? _jsx(Empty, {}) : (_jsx("div", { className: "table-wrap", children: _jsxs("table", { children: [_jsx("thead", { children: _jsxs("tr", { children: [_jsx("th", { children: "Tracking #" }), _jsx("th", { children: "Sender" }), _jsx("th", { children: "Receiver" }), _jsx("th", { children: "Weight" }), _jsx("th", { children: "Status" }), _jsx("th", { children: "Driver" }), _jsx("th", { children: "Actions" })] }) }), _jsx("tbody", { children: items.map((s, i) => _jsxs("tr", { children: [_jsx("td", { children: _jsx("strong", { children: s.trackingNumber || '—' }) }), _jsx("td", { children: s.senderAddress || '—' }), _jsx("td", { children: s.receiverAddress || '—' }), _jsx("td", { children: s.weight != null ? `${s.weight} kg` : '—' }), _jsx("td", { children: _jsx(Badge, { tone: tone(s.status), children: s.status || 'Created' }) }), _jsx("td", { children: s.driverName || s.driverId || 'Unassigned' }), _jsx("td", { children: _jsxs("div", { className: "table-actions", children: [s.trackingNumber && _jsxs("button", { className: "table-action", onClick: () => showUpdates(s.trackingNumber), disabled: updatesLoading, children: [_jsx(History, { size: 15 }), " Updates"] }), s.trackingNumber && _jsxs(Link, { className: "table-action", to: `/track?number=${encodeURIComponent(s.trackingNumber)}`, children: [_jsx(ExternalLink, { size: 15 }), " Public track"] })] }) })] }, s.trackingNumber || i)) })] }) })) }), updateView && _jsx("div", { className: "modal-backdrop", onMouseDown: () => setUpdateView(null), children: _jsxs("div", { className: "modal", onMouseDown: (e) => e.stopPropagation(), children: [_jsxs("div", { className: "section-head", children: [_jsxs("div", { children: [_jsx("span", { className: "eyebrow", children: "Authenticated history" }), _jsx("h3", { children: updateView.trackingNumber })] }), _jsx("button", { className: "icon-btn", onClick: () => setUpdateView(null), children: _jsx(X, { size: 18 }) })] }), Object.keys(updateView.updates).length === 0 ? _jsx(Empty, { title: "No updates", text: "No status history was returned." }) : _jsx("div", { className: "timeline", children: Object.entries(updateView.updates).sort(([a], [b]) => new Date(b) - new Date(a)).map(([time, status]) => _jsxs("div", { className: "timeline-item", children: [_jsx("div", { className: "timeline-dot", children: _jsx(History, { size: 13 }) }), _jsxs("div", { children: [_jsx("strong", { children: String(status) }), _jsx("span", { children: new Date(time).toLocaleString() })] })] }, time)) })] }) })] }));
}
