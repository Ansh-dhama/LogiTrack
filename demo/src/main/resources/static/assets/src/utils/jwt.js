export function parseJwt(token) {
    try {
        const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
        return JSON.parse(decodeURIComponent(atob(base64).split('').map((c) => `%${('00' + c.charCodeAt(0).toString(16)).slice(-2)}`).join('')));
    }
    catch {
        return {};
    }
}
export function roleFromToken(token, fallback = 'USER') {
    const p = parseJwt(token);
    const raw = p.role || p.roles?.[0] || p.authorities?.[0] || fallback;
    return String(raw).replace('ROLE_', '').toUpperCase();
}
