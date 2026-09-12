import { jsx as _jsx, jsxs as _jsxs, Fragment as _Fragment } from "react/jsx-runtime";
import { useEffect, useMemo, useState } from 'react';
import { AlertTriangle, CheckCircle2, CircleDot, Crosshair, MapPin, Navigation, PackageCheck, Radio, RefreshCcw, RotateCcw, Search, ShieldCheck, Truck } from 'lucide-react';
import { driverApi, shipmentApi } from '../../api/endpoints.js';
import { driverLocationPayload, driverStatusPayload, shipmentStatusPayload } from '../../api/contracts.js';
import { messageOf } from '../../api/client.js';
import { useAuth } from '../../context/AuthContext.js';
import { Badge, Button, Card, Input, PageHeader, StatCard } from '../../components/UI.js';
const STATUS_META = {
    PENDING: {
        label: 'Pending',
        tone: 'neutral',
        description: 'Waiting for the shipment workflow to begin.'
    },
    CREATED: {
        label: 'Created',
        tone: 'info',
        description: 'Shipment created and waiting for assignment.'
    },
    ASSIGNED: {
        label: 'Assigned',
        tone: 'info',
        description: 'This shipment is assigned and ready for pickup.'
    },
    IN_TRANSIT: {
        label: 'In transit',
        tone: 'info',
        description: 'The parcel is on the way to the customer.'
    },
    DELIVERY_ATTEMPTED: {
        label: 'Delivery attempted',
        tone: 'warning',
        description: 'Delivery could not be completed. You can retry or complete it later.'
    },
    DELIVERED: {
        label: 'Delivered',
        tone: 'success',
        description: 'Delivery is complete. No further driver update is required.'
    },
    CANCELLED: {
        label: 'Cancelled',
        tone: 'danger',
        description: 'This shipment has been cancelled.'
    },
    RETURNED: {
        label: 'Returned',
        tone: 'danger',
        description: 'The shipment is being returned to the sender.'
    }
};
const DRIVER_ACTIONS = {
    ASSIGNED: [
        {
            status: 'IN_TRANSIT',
            label: 'Start delivery',
            description: 'Parcel picked up — begin the delivery journey.',
            icon: Navigation,
            tone: 'primary'
        }
    ],
    IN_TRANSIT: [
        {
            status: 'DELIVERED',
            label: 'Mark delivered',
            description: 'Customer received the parcel successfully.',
            icon: PackageCheck,
            tone: 'success'
        },
        {
            status: 'DELIVERY_ATTEMPTED',
            label: 'Could not deliver',
            description: 'Customer unavailable or another delivery issue occurred.',
            icon: AlertTriangle,
            tone: 'warning'
        }
    ],
    DELIVERY_ATTEMPTED: [
        {
            status: 'IN_TRANSIT',
            label: 'Retry delivery',
            description: 'Start another delivery attempt for this shipment.',
            icon: RotateCcw,
            tone: 'primary'
        },
        {
            status: 'DELIVERED',
            label: 'Mark delivered',
            description: 'The retry succeeded and the customer received the parcel.',
            icon: PackageCheck,
            tone: 'success'
        }
    ]
};
const JOURNEY_STEPS = [
    { key: 'ASSIGNED', label: 'Assigned', icon: CircleDot },
    { key: 'IN_TRANSIT', label: 'In transit', icon: Navigation },
    { key: 'DELIVERED', label: 'Delivered', icon: CheckCircle2 }
];
function statusProgress(status) {
    if (status === 'DELIVERED' || status === 'RETURNED')
        return 100;
    if (status === 'IN_TRANSIT' || status === 'DELIVERY_ATTEMPTED')
        return 52;
    if (status === 'ASSIGNED')
        return 4;
    return 0;
}
function reachedStep(current, step) {
    if (step === 'ASSIGNED')
        return ['ASSIGNED', 'IN_TRANSIT', 'DELIVERY_ATTEMPTED', 'DELIVERED', 'RETURNED'].includes(current);
    if (step === 'IN_TRANSIT')
        return ['IN_TRANSIT', 'DELIVERY_ATTEMPTED', 'DELIVERED', 'RETURNED'].includes(current);
    if (step === 'DELIVERED')
        return current === 'DELIVERED';
    return false;
}
function formatStatus(value) {
    if (!value)
        return 'No shipment loaded';
    return STATUS_META[value]?.label ?? String(value).replaceAll('_', ' ').toLowerCase().replace(/^./, (c) => c.toUpperCase());
}
export default function DriverDashboard() {
    const { profile, refreshProfile } = useAuth();
    const [online, setOnline] = useState(false);
    const [loadingStatus, setLoadingStatus] = useState(true);
    const [msg, setMsg] = useState('');
    const [msgTone, setMsgTone] = useState('info');
    const [trackingNumber, setTrackingNumber] = useState('');
    const [shipment, setShipment] = useState(null);
    const [loadingShipment, setLoadingShipment] = useState(false);
    const [updatingStatus, setUpdatingStatus] = useState('');
    const [loc, setLoc] = useState({ shipmentId: '', latitude: '', longitude: '' });
    useEffect(() => {
        driverApi.checkStatus()
            .then((d) => setOnline(Boolean(d?.status)))
            .catch(() => { })
            .finally(() => setLoadingStatus(false));
        if (!profile)
            refreshProfile().catch(() => { });
    }, []);
    const currentStatus = shipment?.status || '';
    const currentMeta = STATUS_META[currentStatus] || null;
    const nextActions = useMemo(() => DRIVER_ACTIONS[currentStatus] || [], [currentStatus]);
    const showMessage = (text, tone = 'info') => {
        setMsg(text);
        setMsgTone(tone);
    };
    const toggle = async () => {
        setMsg('');
        try {
            const next = !online;
            await driverApi.updateStatus(driverStatusPayload({ status: next }));
            setOnline(next);
            showMessage(`You are now ${next ? 'online and available for assignments' : 'offline'}.`, next ? 'success' : 'info');
        }
        catch (e) {
            showMessage(messageOf(e), 'danger');
        }
    };
    const loadShipment = async ({ silent = false } = {}) => {
        const clean = trackingNumber.trim();
        if (!clean) {
            showMessage('Enter a tracking number first.', 'danger');
            return null;
        }
        setLoadingShipment(true);
        if (!silent)
            setMsg('');
        try {
            const data = await shipmentApi.publicTrack(clean);
            setShipment(data);
            if (data?.shipmentId) {
                setLoc((prev) => ({ ...prev, shipmentId: String(data.shipmentId) }));
            }
            if (!silent)
                showMessage(`Shipment ${clean} loaded. Choose the next delivery action below.`, 'success');
            return data;
        }
        catch (err) {
            setShipment(null);
            showMessage(messageOf(err), 'danger');
            return null;
        }
        finally {
            setLoadingShipment(false);
        }
    };
    const updateShipmentStatus = async (nextStatus) => {
        if (!trackingNumber.trim()) {
            showMessage('Load a shipment before updating its status.', 'danger');
            return;
        }
        setUpdatingStatus(nextStatus);
        setMsg('');
        try {
            const payload = shipmentStatusPayload({ trackingNumber, status: nextStatus });
            await shipmentApi.updateStatus(payload);
            await loadShipment({ silent: true });
            showMessage(`Shipment moved to ${formatStatus(nextStatus)}.`, 'success');
        }
        catch (err) {
            showMessage(messageOf(err), 'danger');
        }
        finally {
            setUpdatingStatus('');
        }
    };
    const useCurrentLocation = () => navigator.geolocation?.getCurrentPosition((p) => {
        setLoc((prev) => ({
            ...prev,
            latitude: String(p.coords.latitude),
            longitude: String(p.coords.longitude)
        }));
        showMessage('Current device location captured. Review it, then send.', 'success');
    }, () => showMessage('Unable to read your current location. Check browser location permission.', 'danger'));
    const sendLoc = async (e) => {
        e.preventDefault();
        setMsg('');
        try {
            if (!loc.shipmentId)
                throw new Error('Shipment ID is required. Loading a shipment fills it automatically.');
            const payload = driverLocationPayload(loc);
            if (!Number.isFinite(payload.latitude) || !Number.isFinite(payload.longitude)) {
                throw new Error('Valid latitude and longitude are required.');
            }
            await shipmentApi.updateLocation(loc.shipmentId, payload);
            showMessage('Driver location published successfully.', 'success');
            if (trackingNumber.trim())
                await loadShipment({ silent: true });
        }
        catch (err) {
            showMessage(messageOf(err), 'danger');
        }
    };
    return (_jsxs(_Fragment, { children: [_jsx(PageHeader, { eyebrow: "Driver workspace", title: "Delivery operations", description: "Manage availability, move shipments through valid delivery stages and share live location.", action: _jsx(Badge, { tone: online ? 'success' : 'neutral', children: loadingStatus ? 'Checking…' : online ? 'Online' : 'Offline' }) }), _jsxs("div", { className: "stats-grid", children: [_jsx(StatCard, { label: "Availability", value: online ? 'Live' : 'Offline', icon: Radio }), _jsx(StatCard, { label: "Shipment", value: currentStatus ? formatStatus(currentStatus) : 'Not loaded', icon: RefreshCcw }), _jsx(StatCard, { label: "Location sharing", value: loc.shipmentId ? `#${loc.shipmentId}` : 'Ready', icon: MapPin }), _jsx(StatCard, { label: "Driver mode", value: "Secure", icon: ShieldCheck })] }), msg && _jsx("div", { className: `alert ${msgTone}`, children: msg }), _jsxs("div", { className: "driver-control-grid", children: [_jsxs(Card, { className: "availability-card", children: [_jsxs("div", { className: "section-head compact-head", children: [_jsxs("div", { children: [_jsx("span", { className: "card-kicker", children: "Availability" }), _jsx("h2", { children: "Driver status" })] }), _jsx(Badge, { tone: online ? 'success' : 'neutral', children: online ? 'Live' : 'Offline' })] }), _jsxs("div", { className: `availability-panel ${online ? 'is-online' : ''}`, children: [_jsx("div", { className: `availability-visual ${online ? 'online' : ''}`, children: _jsx(Radio, { size: 22 }) }), _jsxs("div", { children: [_jsx("strong", { children: online ? 'You are available' : 'You are offline' }), _jsx("p", { children: online ? 'You can receive shipment assignments.' : 'Go online when you are ready to deliver.' })] })] }), _jsxs(Button, { className: "full", onClick: toggle, disabled: loadingStatus, variant: online ? 'secondary' : 'primary', children: [_jsx(Radio, { size: 16 }), online ? 'Go offline' : 'Go online'] })] }), _jsxs(Card, { className: "shipment-control-card", children: [_jsxs("div", { className: "section-head compact-head shipment-control-head", children: [_jsxs("div", { children: [_jsx("span", { className: "card-kicker", children: "Shipment progress" }), _jsx("h2", { children: "Update delivery stage" }), _jsx("p", { className: "muted", children: "No enum typing. Load a shipment and choose only a valid next action." })] }), currentMeta && _jsx(Badge, { tone: currentMeta.tone, children: currentMeta.label })] }), _jsxs("div", { className: "shipment-lookup-row", children: [_jsx(Input, { label: "Tracking number", placeholder: "e.g. TRK-3C012378", value: trackingNumber, onChange: (e) => {
                                            setTrackingNumber(e.target.value.toUpperCase());
                                            if (shipment)
                                                setShipment(null);
                                        }, onKeyDown: (e) => {
                                            if (e.key === 'Enter') {
                                                e.preventDefault();
                                                loadShipment();
                                            }
                                        } }), _jsxs(Button, { type: "button", onClick: () => loadShipment(), loading: loadingShipment, children: [_jsx(Search, { size: 16 }), " Load shipment"] })] }), !shipment ? (_jsxs("div", { className: "shipment-empty-state", children: [_jsx("div", { className: "shipment-empty-icon", children: _jsx(Truck, { size: 22 }) }), _jsxs("div", { children: [_jsx("strong", { children: "Load an assigned shipment" }), _jsx("p", { children: "Enter its tracking number to see the current stage and available driver actions." })] })] })) : (_jsxs(_Fragment, { children: [_jsxs("div", { className: `current-shipment-state state-${currentMeta?.tone || 'neutral'}`, children: [_jsx("div", { className: "current-state-icon", children: currentStatus === 'DELIVERED' ? _jsx(CheckCircle2, { size: 21 }) : currentStatus === 'DELIVERY_ATTEMPTED' ? _jsx(AlertTriangle, { size: 21 }) : _jsx(Truck, { size: 21 }) }), _jsxs("div", { className: "current-state-copy", children: [_jsx("span", { children: "Current stage" }), _jsx("strong", { children: formatStatus(currentStatus) }), _jsx("p", { children: currentMeta?.description || 'Shipment status loaded from LogiTrack.' })] }), _jsxs("div", { className: "shipment-id-chip", children: ["ID #", shipment.shipmentId ?? '—'] })] }), _jsxs("div", { className: "delivery-stepper", "aria-label": "Delivery progress", children: [_jsx("div", { className: "delivery-stepper-track", children: _jsx("div", { className: "delivery-stepper-fill", style: { width: `${statusProgress(currentStatus)}%` } }) }), JOURNEY_STEPS.map(({ key, label, icon: Icon }) => {
                                                const reached = reachedStep(currentStatus, key);
                                                const active = currentStatus === key || (currentStatus === 'DELIVERY_ATTEMPTED' && key === 'IN_TRANSIT');
                                                return (_jsxs("div", { className: `delivery-step ${reached ? 'reached' : ''} ${active ? 'active' : ''}`, children: [_jsx("div", { className: "delivery-step-dot", children: _jsx(Icon, { size: 16 }) }), _jsx("span", { children: label })] }, key));
                                            })] }), currentStatus === 'DELIVERY_ATTEMPTED' && (_jsxs("div", { className: "attempt-note", children: [_jsx(AlertTriangle, { size: 17 }), _jsxs("div", { children: [_jsx("strong", { children: "Delivery attempt recorded" }), _jsx("span", { children: "Retry the delivery or mark it delivered when successful. After repeated failed attempts, the backend may return the shipment automatically." })] })] })), nextActions.length > 0 ? (_jsxs("div", { className: "next-action-area", children: [_jsx("div", { className: "next-action-label", children: "Choose the next valid action" }), _jsx("div", { className: `status-action-grid ${nextActions.length === 1 ? 'one-action' : ''}`, children: nextActions.map((action) => {
                                                    const Icon = action.icon;
                                                    return (_jsxs("button", { type: "button", className: `status-action-card action-${action.tone}`, disabled: Boolean(updatingStatus), onClick: () => updateShipmentStatus(action.status), children: [_jsx("span", { className: "status-action-icon", children: updatingStatus === action.status ? _jsx(RefreshCcw, { className: "spin", size: 20 }) : _jsx(Icon, { size: 20 }) }), _jsxs("span", { className: "status-action-copy", children: [_jsx("strong", { children: updatingStatus === action.status ? 'Updating…' : action.label }), _jsx("small", { children: action.description })] }), _jsx("span", { className: "status-action-arrow", children: "\u2192" })] }, action.status));
                                                }) })] })) : (_jsxs("div", { className: `terminal-state ${['DELIVERED'].includes(currentStatus) ? 'success' : ['RETURNED', 'CANCELLED'].includes(currentStatus) ? 'danger' : 'neutral'}`, children: [currentStatus === 'DELIVERED' ? _jsx(CheckCircle2, { size: 19 }) : _jsx(CircleDot, { size: 19 }), _jsxs("div", { children: [_jsx("strong", { children: ['DELIVERED', 'RETURNED', 'CANCELLED'].includes(currentStatus) ? 'No further driver action' : 'Waiting for the next workflow stage' }), _jsx("span", { children: ['PENDING', 'CREATED'].includes(currentStatus) ? 'The shipment must be assigned before a driver can start delivery.' : 'This status is terminal or managed by the system/admin.' })] })] }))] }))] })] }), _jsxs(Card, { className: "location-card", children: [_jsxs("div", { className: "section-head compact-head", children: [_jsxs("div", { children: [_jsx("span", { className: "card-kicker", children: "Live location" }), _jsx("h2", { children: "Publish driver location" }), _jsx("p", { className: "muted", children: "Loading a shipment above fills its shipment ID automatically." })] }), loc.shipmentId && _jsxs(Badge, { tone: "info", children: ["Shipment #", loc.shipmentId] })] }), _jsxs("form", { className: "location-form-4", onSubmit: sendLoc, children: [_jsx(Input, { label: "Shipment ID", type: "number", min: "1", value: loc.shipmentId, onChange: (e) => setLoc({ ...loc, shipmentId: e.target.value }), required: true }), _jsx(Input, { label: "Latitude", type: "number", step: "any", placeholder: "28.6139", value: loc.latitude, onChange: (e) => setLoc({ ...loc, latitude: e.target.value }), required: true }), _jsx(Input, { label: "Longitude", type: "number", step: "any", placeholder: "77.2090", value: loc.longitude, onChange: (e) => setLoc({ ...loc, longitude: e.target.value }), required: true }), _jsxs("div", { className: "location-actions", children: [_jsxs(Button, { type: "button", variant: "secondary", onClick: useCurrentLocation, children: [_jsx(Crosshair, { size: 16 }), " Use current"] }), _jsxs(Button, { children: [_jsx(MapPin, { size: 16 }), " Send location"] })] })] })] })] }));
}
