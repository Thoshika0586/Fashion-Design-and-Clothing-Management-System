
function saveSession(user) {
    document.cookie = `fd_session=${encodeURIComponent(JSON.stringify(user))}; path=/; max-age=86400`;
}

function getSession() {
    const match = document.cookie.match(/(?:^|; )fd_session=([^;]*)/);
    if (!match) return null;
    try { return JSON.parse(decodeURIComponent(match[1])); } catch (e) { return null; }
}

function clearSession() {
    document.cookie = "fd_session=; path=/; max-age=0";
}

function requireRole(role) {
    const user = getSession();
    if (!user || user.role !== role) {
        window.location.href = "login.html";
        return null;
    }
    return user;
}

function logout() {
    clearSession();
    window.location.href = "login.html";
}
