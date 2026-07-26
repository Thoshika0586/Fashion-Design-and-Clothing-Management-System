const user = requireRole("DESIGNER");
let fabricsCache = [];

renderNavbar('dashboard');

document.querySelectorAll(".nav-btn").forEach(btn => {
    btn.addEventListener("click", () => {
        document.querySelectorAll(".nav-btn").forEach(b => b.classList.remove("active"));
        btn.classList.add("active");
        document.querySelectorAll(".dash-main section").forEach(s => s.style.display = "none");
        const view = btn.dataset.view;
        document.getElementById(`view-${view}`).style.display = "block";
        if (view === "designs") loadDesigns();
        if (view === "fabrics") loadFabrics();
        if (view === "orders") loadOrders();
        if (view === "inventory") loadInventory();
    });
});

function closeModal() { document.getElementById("modal").classList.remove("show"); }
document.getElementById("closeModal").addEventListener("click", closeModal);
document.getElementById("modal").addEventListener("click", (e) => { if (e.target.id === "modal") closeModal(); });

// ---------- Designs ----------
async function loadDesigns() {
    const grid = document.getElementById("designGrid");
    grid.innerHTML = `<div class="empty-state">Loading sketches…</div>`;
    try {
        const [designs, fabrics] = await Promise.all([Api.getDesigns(), Api.getFabrics()]);
        fabricsCache = fabrics;
        if (!designs.length) {
            grid.innerHTML = `<div class="empty-state"><div class="icon">✏️</div>No sketches yet. Create your first one.</div>`;
            return;
        }
        grid.innerHTML = designs.map(d => `
            <div class="design-card">
                <div class="sketch"><img src="${d.sketchImage || 'assets/sketch-frock-1.svg'}" alt="${d.designName}"></div>
                <div class="body">
                    <span class="tag ${d.category === 'FROCK' ? 'tag-frock' : 'tag-saree'}">${d.category}</span>
                    <h3 class="name">${d.designName}</h3>
                    <p class="desc">${d.description || ''}</p>
                    <div class="price-row">
                        <span class="price">Rs. ${Number(d.price).toLocaleString()}</span>
                        <div style="display:flex; gap:0.4rem;">
                            <button class="btn btn-outline btn-sm" onclick="editDesign('${d.designId}')">Edit</button>
                            <button class="btn btn-ghost btn-sm" onclick="archiveDesign('${d.designId}')">Archive</button>
                        </div>
                    </div>
                </div>
            </div>
        `).join("");
    } catch (e) {
        grid.innerHTML = `<div class="empty-state">Couldn't reach the server: ${e.message}</div>`;
    }
}

function fabricOptions(selectedId) {
    return fabricsCache.map(f => `<option value="${f.fabricId}" ${f.fabricId === selectedId ? 'selected' : ''}>${f.fabricName} — ${f.color}</option>`).join("");
}

function designFormHtml(d = {}) {
    return `
        <h2>${d.designId ? 'Edit sketch' : 'New sketch'}</h2>
        <div class="field"><label>Design name</label><input type="text" id="f_name" value="${d.designName || ''}"></div>
        <div class="field"><label>Category</label>
            <select id="f_category">
                <option value="FROCK" ${d.category === 'FROCK' ? 'selected' : ''}>Frock</option>
                <option value="SAREE" ${d.category === 'SAREE' ? 'selected' : ''}>Saree</option>
            </select>
        </div>
        <div class="field"><label>Tie &amp; dye pattern</label><input type="text" id="f_pattern" value="${d.pattern || 'Tie & Dye'}"></div>
        <div class="field"><label>Fabric</label><select id="f_fabric">${fabricOptions(d.fabricId)}</select></div>
        <div class="field"><label>Price (Rs.)</label><input type="number" id="f_price" value="${d.price || ''}"></div>
        <div class="field"><label>Sketch image path (SVG/PNG placed in assets/)</label><input type="text" id="f_sketch" value="${d.sketchImage || 'assets/sketch-frock-1.svg'}"></div>
        <div class="field"><label>Description</label><textarea id="f_desc" rows="3">${d.description || ''}</textarea></div>
        <button class="btn btn-accent btn-block" id="saveDesignBtn">${d.designId ? 'Save changes' : 'Create sketch'}</button>
    `;
}

document.getElementById("newDesignBtn").addEventListener("click", async () => {
    if (!fabricsCache.length) fabricsCache = await Api.getFabrics();
    document.getElementById("modalContent").innerHTML = designFormHtml();
    document.getElementById("saveDesignBtn").onclick = () => saveDesign(null);
    document.getElementById("modal").classList.add("show");
});

async function editDesign(designId) {
    const d = await Api.getDesign(designId);
    if (!fabricsCache.length) fabricsCache = await Api.getFabrics();
    document.getElementById("modalContent").innerHTML = designFormHtml(d);
    document.getElementById("saveDesignBtn").onclick = () => saveDesign(designId);
    document.getElementById("modal").classList.add("show");
}

async function saveDesign(designId) {
    const payload = {
        designerId: user.designerId,
        designName: document.getElementById("f_name").value.trim(),
        category: document.getElementById("f_category").value,
        pattern: document.getElementById("f_pattern").value.trim(),
        fabricId: document.getElementById("f_fabric").value,
        price: parseFloat(document.getElementById("f_price").value) || 0,
        sketchImage: document.getElementById("f_sketch").value.trim(),
        description: document.getElementById("f_desc").value.trim(),
    };
    try {
        if (designId) {
            payload.designId = designId;
            payload.status = "AVAILABLE";
            await Api.updateDesign(payload);
        } else {
            await Api.createDesign(payload);
        }
        closeModal();
        loadDesigns();
    } catch (e) {
        alert("Couldn't save sketch: " + e.message);
    }
}

async function archiveDesign(designId) {
    if (!confirm("Archive this sketch? Customers will no longer see it.")) return;
    try {
        await Api.deleteDesign(designId);
        loadDesigns();
    } catch (e) {
        alert("Couldn't archive: " + e.message);
    }
}

// ---------- Fabrics ----------
async function loadFabrics() {
    const tbody = document.querySelector("#fabricTable tbody");
    tbody.innerHTML = `<tr><td colspan="3">Loading…</td></tr>`;
    try {
        const fabrics = await Api.getFabrics();
        fabricsCache = fabrics;
        tbody.innerHTML = fabrics.map(f => `
            <tr><td>${f.fabricName}</td><td>${f.color}</td><td>Rs. ${Number(f.pricePerUnit).toLocaleString()}</td></tr>
        `).join("") || `<tr><td colspan="3">No fabrics yet.</td></tr>`;
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="3">Couldn't load fabrics: ${e.message}</td></tr>`;
    }
}

document.getElementById("newFabricBtn").addEventListener("click", () => {
    document.getElementById("modalContent").innerHTML = `
        <h2>Add fabric</h2>
        <div class="field"><label>Fabric name</label><input type="text" id="fab_name" placeholder="Cotton, Silk Blend, Rayon..."></div>
        <div class="field"><label>Color</label><input type="text" id="fab_color" placeholder="Indigo Blue"></div>
        <div class="field"><label>Price per unit (Rs.)</label><input type="number" id="fab_price"></div>
        <button class="btn btn-accent btn-block" id="saveFabricBtn">Add fabric</button>
    `;
    document.getElementById("saveFabricBtn").onclick = async () => {
        try {
            await Api.addFabric({
                fabricName: document.getElementById("fab_name").value.trim(),
                color: document.getElementById("fab_color").value.trim(),
                pricePerUnit: parseFloat(document.getElementById("fab_price").value) || 0,
            });
            closeModal();
            loadFabrics();
        } catch (e) {
            alert("Couldn't add fabric: " + e.message);
        }
    };
    document.getElementById("modal").classList.add("show");
});

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
                ${o.items.map(i => `<div style="font-size:0.9rem; padding:0.3rem 0; border-top:1px solid var(--line);">
                    ${i.designName} · ${i.category} · Size ${i.size} · Qty ${i.quantity} ${i.customNote ? `· note: "${i.customNote}"` : ''}
                </div>`).join("")}
                <div style="margin-top:0.75rem; display:flex; gap:0.5rem;">
                    <a class="btn btn-outline btn-sm" href="bill.html?orderId=${o.orderId}" target="_blank">🧾 View invoice</a>
                    ${o.status !== 'IN_PROGRESS' && o.status !== 'COMPLETED' && o.status !== 'CANCELLED' ? `<button class="btn btn-outline btn-sm" onclick="setOrderStatus('${o.orderId}','IN_PROGRESS')">Start making</button>` : ''}
                    ${o.status === 'IN_PROGRESS' ? `<button class="btn btn-primary btn-sm" onclick="setOrderStatus('${o.orderId}','COMPLETED')">Mark completed</button>` : ''}
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
                <td></td>
            </tr>
        `).join("") || `<tr><td colspan="5">No inventory items yet.</td></tr>`;
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

if (user) loadDesigns();
