const user = requireRole("ADMIN");

renderNavbar('dashboard');

document.querySelectorAll(".nav-btn").forEach(btn => {
    btn.addEventListener("click", () => {
        document.querySelectorAll(".nav-btn").forEach(b => b.classList.remove("active"));
        btn.classList.add("active");
        document.querySelectorAll(".dash-main section").forEach(s => s.style.display = "none");
        const view = btn.dataset.view;
        document.getElementById(`view-${view}`).style.display = "block";
        if (view === "report") loadReport();
        if (view === "orders") loadOrders();
        if (view === "deliveries") loadDeliveries();
        if (view === "inventory") loadInventory();
        if (view === "users") loadUsers();
    });
});

function closeModal() { document.getElementById("modal").classList.remove("show"); }
document.getElementById("closeModal").addEventListener("click", closeModal);
document.getElementById("modal").addEventListener("click", (e) => { if (e.target.id === "modal") closeModal(); });

// ---------- Overview / report ----------
async function loadReport() {
    const statGrid = document.getElementById("statGrid");
    statGrid.innerHTML = `<div class="empty-state">Loading…</div>`;
    try {
        const r = await Api.getReport();
        statGrid.innerHTML = `
            <div class="stat-card"><div class="stat-label">Total orders</div><div class="stat-value">${r.totalOrders}</div></div>
            <div class="stat-card"><div class="stat-label">Completed</div><div class="stat-value">${r.completedOrders}</div></div>
            <div class="stat-card"><div class="stat-label">Pending / in progress</div><div class="stat-value">${r.pendingOrders}</div></div>
            <div class="stat-card"><div class="stat-label">Cancelled</div><div class="stat-value">${r.cancelledOrders}</div></div>
            <div class="stat-card"><div class="stat-label">Revenue (completed)</div><div class="stat-value">Rs. ${Number(r.totalRevenue).toLocaleString()}</div></div>
        `;
        const tbody = document.querySelector("#reportInventoryTable tbody");
        tbody.innerHTML = r.inventory.map(i => `
            <tr><td>${i.itemType}</td><td>${i.itemName}</td><td>${i.quantity}</td><td>Rs. ${Number(i.unitPrice).toLocaleString()}</td></tr>
        `).join("") || `<tr><td colspan="4">No inventory yet.</td></tr>`;
    } catch (e) {
        statGrid.innerHTML = `<div class="empty-state">Couldn't load report: ${e.message}</div>`;
    }
}

// ---------- Orders ----------
async function loadOrders() {
    const list = document.getElementById("ordersList");
    list.innerHTML = `<div class="empty-state">Loading orders…</div>`;
    try {
        const orders = await Api.getOrders();
        if (!orders.length) {
            list.innerHTML = `<div class="empty-state"><div class="icon">📦</div>No orders yet.</div>`;
            return;
        }
        list.innerHTML = orders.map(o => `
            <div class="card" style="margin-bottom:1rem;">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:0.5rem;">
                    <div><strong>${o.orderId}</strong> · ${o.customerName} <span class="status-badge status-${o.status}">${o.status}</span></div>
                    <div style="font-weight:700;">Rs. ${Number(o.totalAmount).toLocaleString()}</div>
                </div>
                <div style="font-size:0.85rem; color:var(--ink-soft); margin-bottom:0.5rem;">${o.orderDate}</div>
                ${o.items.map(i => `<div style="font-size:0.9rem; padding:0.3rem 0; border-top:1px solid var(--line);">
                    ${i.designName} · ${i.category} · Size ${i.size} · Qty ${i.quantity}
                </div>`).join("")}
                <div style="margin-top:0.75rem; display:flex; gap:0.5rem; flex-wrap:wrap; align-items:center;">
                    <a class="btn btn-outline btn-sm" href="bill.html?orderId=${o.orderId}" target="_blank">🧾 View invoice</a>
                    ${['PENDING','CONFIRMED','IN_PROGRESS','COMPLETED','CANCELLED'].map(s => `
                        <button class="btn btn-sm ${o.status === s ? 'btn-primary' : 'btn-outline'}" onclick="setOrderStatus('${o.orderId}','${s}')">${s}</button>
                    `).join("")}
                </div>
            </div>
        `).join("");
    } catch (e) {
        list.innerHTML = `<div class="empty-state">Couldn't load orders: ${e.message}</div>`;
    }
}

async function setOrderStatus(orderId, status) {
    try {
        await Api.updateOrderStatus(orderId, status);
        loadOrders();
    } catch (e) {
        alert("Couldn't update order: " + e.message);
    }
}

// ---------- Deliveries ----------
async function loadDeliveries() {
    const tbody = document.querySelector("#deliveryTable tbody");
    tbody.innerHTML = `<tr><td colspan="6">Loading…</td></tr>`;
    try {
        const deliveries = await Api.getDeliveries();
        tbody.innerHTML = deliveries.map(d => `
            <tr>
                <td>${d.orderId}</td><td>${d.customerName}</td><td>${d.address}</td>
                <td><span class="status-badge status-${d.status}">${d.status}</span></td>
                <td>${d.deliveryDate || '—'}</td>
                <td>
                    <select onchange="updateDelivery('${d.deliveryId}', this.value)">
                        <option value="">Update status…</option>
                        <option value="PREPARING" ${d.status==='PREPARING'?'selected':''}>Preparing</option>
                        <option value="DISPATCHED" ${d.status==='DISPATCHED'?'selected':''}>Dispatched</option>
                        <option value="DELIVERED" ${d.status==='DELIVERED'?'selected':''}>Delivered</option>
                        <option value="RETURNED" ${d.status==='RETURNED'?'selected':''}>Returned</option>
                    </select>
                </td>
            </tr>
        `).join("") || `<tr><td colspan="6">No deliveries yet.</td></tr>`;
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="6">Couldn't load deliveries: ${e.message}</td></tr>`;
    }
}

async function updateDelivery(deliveryId, status) {
    if (!status) return;
    const deliveryDate = status === 'DELIVERED' ? new Date().toISOString().slice(0,10) : null;
    try {
        await Api.updateDelivery(deliveryId, status, deliveryDate);
        loadDeliveries();
    } catch (e) {
        alert("Couldn't update delivery: " + e.message);
    }
}

// ---------- Inventory ----------
async function loadInventory() {
    const tbody = document.querySelector("#inventoryTable tbody");
    tbody.innerHTML = `<tr><td colspan="5">Loading…</td></tr>`;
    try {
        const items = await Api.getInventory();
        tbody.innerHTML = items.map(i => `
            <tr>
                <td>${i.itemType}</td><td>${i.itemName}</td>
                <td><input type="number" value="${i.quantity}" style="width:80px;" onchange="updateStock('${i.inventoryId}', this.value)"></td>
                <td>Rs. ${Number(i.unitPrice).toLocaleString()}</td>
                <td><button class="btn btn-ghost btn-sm" onclick="deleteInventoryItem('${i.inventoryId}')">Delete</button></td>
            </tr>
        `).join("") || `<tr><td colspan="5">No inventory yet.</td></tr>`;
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="5">Couldn't load inventory: ${e.message}</td></tr>`;
    }
}

async function updateStock(inventoryId, quantity) {
    try {
        await Api.updateInventoryStock(inventoryId, parseInt(quantity) || 0);
    } catch (e) {
        alert("Couldn't update stock: " + e.message);
    }
}

async function deleteInventoryItem(inventoryId) {
    if (!confirm("Delete this inventory item?")) return;
    try {
        await Api.deleteInventory(inventoryId);
        loadInventory();
    } catch (e) {
        alert("Couldn't delete item: " + e.message);
    }
}

document.getElementById("newInventoryBtn").addEventListener("click", () => {
    document.getElementById("modalContent").innerHTML = `
        <h2>Add inventory item</h2>
        <div class="field"><label>Type</label><input type="text" id="inv_type" placeholder="Fabric, Dye, Trim..."></div>
        <div class="field"><label>Item name</label><input type="text" id="inv_name"></div>
        <div class="field"><label>Quantity</label><input type="number" id="inv_qty" value="0"></div>
        <div class="field"><label>Unit price (Rs.)</label><input type="number" id="inv_price" value="0"></div>
        <button class="btn btn-accent btn-block" id="saveInvBtn">Add item</button>
    `;
    document.getElementById("saveInvBtn").onclick = async () => {
        try {
            await Api.addInventory({
                itemType: document.getElementById("inv_type").value.trim(),
                itemName: document.getElementById("inv_name").value.trim(),
                quantity: parseInt(document.getElementById("inv_qty").value) || 0,
                unitPrice: parseFloat(document.getElementById("inv_price").value) || 0,
            });
            closeModal();
            loadInventory();
        } catch (e) {
            alert("Couldn't add item: " + e.message);
        }
    };
    document.getElementById("modal").classList.add("show");
});

// ---------- Users ----------
async function loadUsers() {
    const tbody = document.querySelector("#usersTable tbody");
    tbody.innerHTML = `<tr><td colspan="5">Loading…</td></tr>`;
    try {
        const users = await Api.getAllUsers();
        tbody.innerHTML = users.map(u => `
            <tr>
                <td>${u.name}</td><td>${u.email}</td><td>${u.phone || '—'}</td>
                <td><span class="status-badge status-CONFIRMED">${u.role}</span></td>
                <td>${u.createdAt}</td>
            </tr>
        `).join("") || `<tr><td colspan="5">No users yet.</td></tr>`;
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="5">Couldn't load users: ${e.message}</td></tr>`;
    }
}

if (user) loadReport();
