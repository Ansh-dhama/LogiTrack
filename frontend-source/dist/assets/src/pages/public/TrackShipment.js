import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useEffect, useState } from 'react';
import { ArrowLeft, MapPin, PackageSearch, Search, Truck } from 'lucide-react';
import { Link, useSearchParams } from 'react-router-dom';
import { shipmentApi } from '../../api/endpoints.js';
import { messageOf } from '../../api/client.js';
import Logo from '../../components/Logo.js';
import { Button, Card, Input } from '../../components/UI.js';
function prettyKey(k) { return k.replace(/([A-Z])/g, ' $1').replace(/_/g, ' ').replace(/^./, (s) => s.toUpperCase()); }
export default function TrackShipment() {
    const [params] = useSearchParams();
    const initial = params.get('number') || '';
    const [trackingNumber, setTrackingNumber] = useState(initial);
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const load = async (number) => {
        if (!number?.trim())
            return;
        setLoading(true);
        setError('');
        setData(null);
        try {
            setData(await shipmentApi.publicTrack(number.trim()));
        }
        catch (err) {
            setError(messageOf(err));
        }
        finally {
            setLoading(false);
        }
    };
    useEffect(() => { if (initial)
        load(initial); }, []);
    const submit = (e) => { e.preventDefault(); load(trackingNumber); };
    const updates = data?.updates || {};
    return (_jsxs("div", { className: "track-page", children: [_jsxs("header", { className: "public-nav", children: [_jsx(Logo, {}), _jsxs(Link, { to: "/", children: [_jsx(ArrowLeft, { size: 17 }), " Back home"] })] }), _jsxs("div", { className: "track-wrap", children: [_jsxs("div", { className: "track-title", children: [_jsx("div", { className: "track-icon", children: _jsx(PackageSearch, {}) }), _jsx("h1", { children: "Track your shipment" }), _jsx("p", { children: "Enter your tracking number to see the latest delivery progress." })] }), _jsxs(Card, { className: "track-search", children: [_jsxs("form", { onSubmit: submit, children: [_jsx(Input, { label: "Tracking number", placeholder: "Enter tracking number", value: trackingNumber, onChange: (e) => setTrackingNumber(e.target.value), required: true }), _jsxs(Button, { loading: loading, children: [_jsx(Search, { size: 17 }), " Track shipment"] })] }), error && _jsx("div", { className: "alert error", children: error })] }), data && _jsxs(Card, { className: "tracking-result", children: [_jsxs("div", { className: "result-head", children: [_jsxs("div", { children: [_jsx("span", { className: "eyebrow", children: "Shipment details" }), _jsx("h2", { children: data.trackingNumber || trackingNumber })] }), _jsx("span", { className: "badge badge-info", children: String(data.status || 'Tracking') })] }), _jsx("div", { className: "detail-grid", children: Object.entries(data).filter(([k, v]) => k !== 'updates' && typeof v !== 'object').map(([k, v]) => _jsxs("div", { children: [_jsx("span", { children: prettyKey(k) }), _jsx("strong", { children: String(v ?? '—') })] }, k)) }), Object.keys(updates).length > 0 && _jsxs("div", { className: "timeline", children: [_jsxs("h3", { children: [_jsx(Truck, { size: 18 }), " Delivery timeline"] }), Object.entries(updates).sort(([a], [b]) => new Date(b) - new Date(a)).map(([time, status]) => _jsxs("div", { className: "timeline-item", children: [_jsx("div", { className: "timeline-dot", children: _jsx(MapPin, { size: 13 }) }), _jsxs("div", { children: [_jsx("strong", { children: String(status) }), _jsx("span", { children: new Date(time).toLocaleString() })] })] }, time))] })] })] })] }));
}
