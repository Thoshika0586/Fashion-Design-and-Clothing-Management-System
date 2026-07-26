// Central API helper - talks to the Java backend (ApiServer) over REST/JSON.
const API_BASE = "http://localhost:8080/api";

async function apiRequest(path, method = "GET", body = null) {
    const options = { method, headers: {} };
    if (body !== null) {
        options.headers["Content-Type"] = "application/json";
        options.body = JSON.stringify(body);
    }
    const res = await fetch(`${API_BASE}${path}`, options);
    let data = null;
    try { data = await res.json(); } catch (e) { data = null; }
    if (!res.ok) {
        const message = (data && (data.error || data.message)) || `Request failed (${res.status})`;
        throw new Error(message);
    }
    return data;
}

const Api = {
    login: (email, password) => apiRequest("/login", "POST", { email, password }),
    register: (name, email, password, phone, address) =>
        apiRequest("/register", "POST", { name, email, password, phone, address }),
    updateProfile: (userId, name, phone, address) =>
        apiRequest("/profile", "PUT", { userId, name, phone, address }),

    getDesigns: (category) => apiRequest(`/designs${category ? `?category=${category}` : ""}`),
    getDesign: (designId) => apiRequest(`/designs?designId=${designId}`),
    createDesign: (design) => apiRequest("/designs", "POST", design),
    updateDesign: (design) => apiRequest("/designs", "PUT", design),
    deleteDesign: (designId) => apiRequest(`/designs?designId=${designId}`, "DELETE"),

    getFabrics: () => apiRequest("/fabrics"),
    addFabric: (fabric) => apiRequest("/fabrics", "POST", fabric),

    getCart: (customerId) => apiRequest(`/cart?customerId=${customerId}`),
    addToCart: (item) => apiRequest("/cart", "POST", item),
    removeFromCart: (cartItemId) => apiRequest(`/cart?cartItemId=${cartItemId}`, "DELETE"),

    getOrders: (customerId) => apiRequest(customerId ? `/orders?customerId=${customerId}` : "/orders"),
    placeOrder: (customerId) => apiRequest("/orders", "POST", { customerId }),
    updateOrderStatus: (orderId, status) => apiRequest("/orders", "PUT", { orderId, status }),
    cancelOrder: (orderId) => apiRequest(`/orders?orderId=${orderId}`, "DELETE"),

    getBill: (orderId) => apiRequest(`/bill?orderId=${orderId}`),
    getPayments: (orderId) => apiRequest(`/payments?orderId=${orderId}`),
    makePayment: (orderId, method, amount) => apiRequest("/payments", "POST", { orderId, method, amount }),

    getDeliveries: () => apiRequest("/deliveries"),
    updateDelivery: (deliveryId, status, deliveryDate) =>
        apiRequest("/deliveries", "PUT", { deliveryId, status, deliveryDate }),

    getInventory: () => apiRequest("/inventory"),
    addInventory: (item) => apiRequest("/inventory", "POST", item),
    updateInventoryStock: (inventoryId, quantity) => apiRequest("/inventory", "PUT", { inventoryId, quantity }),
    deleteInventory: (inventoryId) => apiRequest(`/inventory?inventoryId=${inventoryId}`, "DELETE"),

    getAllUsers: () => apiRequest("/admin/users"),
    getReport: () => apiRequest("/admin/report"),
};
