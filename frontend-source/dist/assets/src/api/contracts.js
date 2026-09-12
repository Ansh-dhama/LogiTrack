export const normalizeEmail = (value) => String(value || '').trim();
export const loginPayload = ({ email, password }) => ({
    email: normalizeEmail(email),
    password: String(password || '')
});
// VerifyOtpRequest.java -> { email, otp }
export const verifyOtpPayload = ({ email, otp }) => ({
    email: normalizeEmail(email),
    otp: String(otp || '').trim()
});
export const userRegistrationPayload = ({ username, email, password }) => ({
    username: String(username || '').trim(),
    email: normalizeEmail(email),
    password: String(password || ''),
    role: 'USER'
});
export const driverRegistrationPayload = ({ username, driverName, email, password }) => ({
    driverName: String(driverName ?? username ?? '').trim(),
    email: normalizeEmail(email),
    password: String(password || ''),
    isAvailable: false,
    role: 'DRIVER'
});
export const shipmentCreatePayload = ({ senderAddress, receiverAddress, weight }) => ({
    senderAddress: String(senderAddress || '').trim(),
    receiverAddress: String(receiverAddress || '').trim(),
    weight: Number.parseInt(weight, 10)
});
export const shipmentStatusPayload = ({ trackingNumber, status }) => ({
    trackingNumber: String(trackingNumber || '').trim(),
    status: String(status || '').trim()
});
export const driverLocationPayload = ({ latitude, longitude }) => ({
    latitude: Number(latitude),
    longitude: Number(longitude)
});
export const driverStatusPayload = ({ status }) => ({
    status: Boolean(status)
});
// Supports ApiResponse<LoginResponse> and bare LoginResponse.
export const extractTokenFromBody = (body) => (body?.data?.token ??
    body?.token ??
    body?.data?.accessToken ??
    body?.accessToken ??
    null);
