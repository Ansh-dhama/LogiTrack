import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { PackageCheck } from 'lucide-react';
export default function Logo({ compact = false }) {
    return _jsxs("div", { className: "brand", children: [_jsx("div", { className: "brand-mark", children: _jsx(PackageCheck, { size: 20 }) }), !compact && _jsx("span", { children: "LogiTrack" })] });
}
