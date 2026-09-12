import { jsx as _jsx, jsxs as _jsxs, Fragment as _Fragment } from "react/jsx-runtime";
import { useState } from 'react';
import { CheckCircle2, PackagePlus } from 'lucide-react';
import { shipmentApi } from '../../api/endpoints.js';
import { shipmentCreatePayload } from '../../api/contracts.js';
import { messageOf } from '../../api/client.js';
import { Button, Card, Input, PageHeader, Textarea } from '../../components/UI.js';
export default function CreateShipment() {
    const [form, setForm] = useState({ senderAddress: '', receiverAddress: '', weight: '' });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [created, setCreated] = useState(null);
    const change = (e) => setForm({ ...form, [e.target.name]: e.target.value });
    const submit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        setCreated(null);
        try {
            const payload = shipmentCreatePayload(form);
            if (!payload.senderAddress || !payload.receiverAddress)
                throw new Error('Sender and receiver addresses are required.');
            if (!Number.isInteger(payload.weight) || payload.weight < 1)
                throw new Error('Weight must be at least 1 kg.');
            setCreated(await shipmentApi.create(payload));
            setForm({ senderAddress: '', receiverAddress: '', weight: '' });
        }
        catch (err) {
            setError(messageOf(err));
        }
        finally {
            setLoading(false);
        }
    };
    return (_jsxs(_Fragment, { children: [_jsx(PageHeader, { eyebrow: "Create shipment", title: "Book a new delivery", description: "The form matches ShipmentDto: senderAddress, receiverAddress and integer weight." }), _jsxs("div", { className: "two-col", children: [_jsx(Card, { children: _jsxs("form", { className: "form-stack", onSubmit: submit, children: [_jsx(Textarea, { label: "Sender address", name: "senderAddress", value: form.senderAddress, onChange: change, rows: "4", required: true }), _jsx(Textarea, { label: "Receiver address", name: "receiverAddress", value: form.receiverAddress, onChange: change, rows: "4", required: true }), _jsx(Input, { label: "Weight (kg)", name: "weight", type: "number", min: "1", step: "1", value: form.weight, onChange: change, required: true }), error && _jsx("div", { className: "alert error", children: error }), _jsxs(Button, { loading: loading, children: [_jsx(PackagePlus, { size: 17 }), " Create shipment"] })] }) }), _jsx(Card, { className: "helper-card", children: created ? (_jsxs("div", { className: "success-panel", children: [_jsx("div", { className: "success-icon", children: _jsx(CheckCircle2, {}) }), _jsx("h3", { children: "Shipment created" }), _jsx("p", { children: "The backend accepted the shipment." }), _jsx("div", { className: "created-number", children: created.trackingNumber || 'Created successfully' })] })) : (_jsxs(_Fragment, { children: [_jsx("h3", { children: "Required information" }), _jsxs("ul", { children: [_jsx("li", { children: "Complete sender address" }), _jsx("li", { children: "Complete receiver address" }), _jsx("li", { children: "Weight of at least 1 kg" })] })] })) })] })] }));
}
