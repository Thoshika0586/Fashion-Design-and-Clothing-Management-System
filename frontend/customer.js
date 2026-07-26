const user = requireRole("CUSTOMER");
let activeCategory = "";
let allDesigns = [];

renderNavbar('dashboard');

if (user) {
    document.getElementById("profileName").value = user.name || "";
    document.getElementById("profilePhone").value = user.phone || "";
    document.getElementById("profileAddress").value = user.address || "";
}

// ---------- View switching ----------
document.querySelectorAll(".nav-btn").forEach(btn => {
    btn.addEventListener("click", () => {
        document.querySelectorAll(".nav-btn").forEach(b => b.classList.remove("active"));
        btn.classList.add("active");
        document.querySelectorAll(".dash-main section").forEach(s => s.style.display = "none");
        const view = btn.dataset.view;
        document.getElementById(`view-${view}`).style.display = "block";
        if (view === "cart") loadCart();
        if (view === "orders") loadOrders();
    });
});

// ---------- Browse / gallery ----------
async function loadGallery() {
    const grid = document.getElementById("galleryGrid");
    grid.innerHTML = `<div class="empty-state"><div class="icon">🧵</div>Loading sketches…</div>`;
    try {
        allDesigns = await Api.getDesigns(activeCategory);
        if (!allDesigns.length) {
            grid.innerHTML = `<div class="empty-state"><div class="icon">🧵</div>No sketches available yet.</div>`;
            return;
        }
        grid.innerHTML = allDesigns.map(d => `
            <div class="design-card">
                <div class="sketch"><img src="${d.sketchImage || 'assets/image2.png'}" alt="${d.designName}"></div>
                <div class="body">
                    <span class="tag ${d.category === 'FROCK' ? 'tag-frock' : 'tag-saree'}">${d.category}</span>
                    <h3 class="name">${d.designName}</h3>
                    <p class="desc">${d.description || ''} ${d.fabricName ? `· ${d.fabricName}, ${d.fabricColor}` : ''}</p>
                    <div class="price-row">
                        <span class="price">Rs. ${Number(d.price).toLocaleString()}</span>
                        <button class="btn btn-primary btn-sm" onclick="openDesign('${d.designId}')">View &amp; order</button>
                    </div>
                </div>
            </div>
        `).join("");
    } catch (e) {
        grid.innerHTML = `<div class="empty-state">Couldn't reach the server. Is the backend running on port 8080?</div>`;
    }
}

document.getElementById("filterTabs").addEventListener("click", (e) => {
    if (e.target.tagName !== "BUTTON") return;
    document.querySelectorAll("#filterTabs button").forEach(b => b.classList.remove("active"));
    e.target.classList.add("active");
    activeCategory = e.target.dataset.cat;
    loadGallery();
});

// ---------- Design detail modal ----------
function openDesign(designId) {
    const d = allDesigns.find(x => x.designId === designId);
    if (!d) return;
    document.getElementById("modalContent").innerHTML = `
        <div style="display:grid; grid-template-columns: 1fr 1.2fr; gap:1.5rem;">
            <div class="sketch" style="border-radius:8px; overflow:hidden;"><img src="${d.sketchImage}" style="width:100%;height:100%;object-fit:cover;" alt="${d.designName}"></div>
            <div>
                <span class="tag ${d.category === 'FROCK' ? 'tag-frock' : 'tag-saree'}">${d.category} · ${d.pattern || 'Tie & Dye'}</span>
                <h2 style="margin-top:0.5rem;">${d.designName}</h2>
                <p style="color:var(--ink-soft); font-size:0.9rem;">${d.description || ''}</p>
                <p style="font-size:0.85rem; color:var(--ink-soft);">Fabric: ${d.fabricName || '—'} (${d.fabricColor || '—'})</p>
                <p class="price" style="font-size:1.3rem;">Rs. ${Number(d.price).toLocaleString()}</p>
                <div class="field">
                    <label>Size</label>
                    <select id="sizeSelect">
                        <option value="XS">XS</option><option value="S">S</option>
                        <option value="M" selected>M</option><option value="L">L</option>
                        <option value="XL">XL</option><option value="XXL">XXL</option><option value="NONE">NONE</option>
                    </select>
                </div>
                <div class="field">
                    <label>Quantity</label>
                    <input type="number" id="qtyInput" value="1" min="1">
                </div>
                <div class="field">
                    <label>Customization note (optional)</label>
                    <textarea id="noteInput" rows="2" placeholder="e.g. add 2 inches to length, prefer deeper madder red"></textarea>
                </div>
                <button class="btn btn-accent btn-block" id="addToCartBtn">Add to cart</button>
            </div>
        </div>
    `;
    document.getElementById("addToCartBtn").onclick = () => addToCart(designId);
    document.getElementById("designModal").classList.add("show");
}

document.getElementById("closeModal").addEventListener("click", () => {
    document.getElementById("designModal").classList.remove("show");
});
document.getElementById("designModal").addEventListener("click", (e) => {
    if (e.target.id === "designModal") document.getElementById("designModal").classList.remove("show");
});

async function addToCart(designId) {
    const size = document.getElementById("sizeSelect").value;
    const quantity = parseInt(document.getElementById("qtyInput").value) || 1;
    const customNote = document.getElementById("noteInput").value.trim();
    try {
        await Api.addToCart({ customerId: user.customerId, designId, size, quantity, customNote });
        document.getElementById("designModal").classList.remove("show");
        refreshCartCount();
        alert("Added to your cart.");
    } catch (e) {
        alert("Couldn't add to cart: " + e.message);
    }
}

// ---------- Cart ----------
async function loadCart() {
    const list = document.getElementById("cartList");
    const summary = document.getElementById("cartSummary");
    list.innerHTML = `<div class="empty-state">Loading cart…</div>`;
    try {
        const items = await Api.getCart(user.customerId);
        if (!items.length) {
            list.innerHTML = `<div class="empty-state"><div class="icon">🧺</div>Your cart is empty. Browse sketches to add a design.</div>`;
            summary.style.display = "none";
            return;
        }
        let total = 0;
        list.innerHTML = items.map(i => {
            const lineTotal = i.unitPrice * i.quantity;
            total += lineTotal;
            return `
            <div class="cart-row">
                <div class="thumb"><img src="${i.sketchImage || 'assets/image1.png'}" alt="${i.designName}"></div>
                <div class="info">
                    <div class="name">${i.designName}</div>
                    <div class="meta">${i.category} · Size ${i.size} · Qty ${i.quantity} ${i.customNote ? `· "${i.customNote}"` : ''}</div>
                </div>
                <div style="font-weight:700;">Rs. ${lineTotal.toLocaleString()}</div>
                <button class="btn btn-ghost btn-sm" onclick="removeCartItem('${i.cartItemId}')">Remove</button>
            </div>`;
        }).join("");
        document.getElementById("cartTotal").textContent = `Rs. ${total.toLocaleString()}`;
        summary.style.display = "block";
    } catch (e) {
        list.innerHTML = `<div class="empty-state">Couldn't load cart: ${e.message}</div>`;
    }
    refreshCartCount();
}

async function removeCartItem(cartItemId) {
    try {
        await Api.removeFromCart(cartItemId);
        loadCart();
    } catch (e) {
        alert("Couldn't remove item: " + e.message);
    }
}

async function refreshCartCount() {
    try {
        const items = await Api.getCart(user.customerId);
        const el = document.getElementById("cartCount");
        el.textContent = items.length ? `(${items.length})` : "";
    } catch (e) { /* ignore */ }
}

document.getElementById("checkoutBtn").addEventListener("click", async () => {
    try {
        const order = await Api.placeOrder(user.customerId);
        showPaymentModal(order);
    } catch (e) {
        alert("Couldn't place order: " + e.message);
    }
});

function showPaymentModal(order) {
    document.getElementById("modalContent").innerHTML = `
        <h2>Pay for your order</h2>
        <p style="color:var(--ink-soft); font-size:0.9rem;">Order ${order.orderId}</p>
        <div class="cart-summary" style="margin-bottom:1.25rem;">
            <div class="total-row"><span>Amount due</span><span>Rs. ${Number(order.totalAmount).toLocaleString()}</span></div>
        </div>
        <div class="field">
            <label>Payment method</label>
            <select id="paymentMethod">
                <option value="CASH">Cash</option>

                <option value="ONLINE">Online</option>
                
            </select>
        </div>
        <button class="btn btn-accent btn-block" id="payNowBtn">Place now</button>
    `;
    document.getElementById("payNowBtn").onclick = async () => {
        const method = document.getElementById("paymentMethod").value;
        const btn = document.getElementById("payNowBtn");
        btn.disabled = true;
        btn.textContent = "Processing…";
        try {
            await Api.makePayment(order.orderId, method, order.totalAmount);
            showBillModal(order.orderId);
            loadCart();
        } catch (e) {
            alert("Payment failed: " + e.message);
            btn.disabled = false;
            btn.textContent = "Pay now";
        }
    };
    document.getElementById("designModal").classList.add("show");
}

async function showBillModal(orderId) {
    document.getElementById("modalContent").innerHTML = `<div class="empty-state">Loading your bill…</div>`;
    try {
        const bill = await Api.getBill(orderId);
        const itemsHtml = bill.items.map(i => `
            <tr>
                <td>${i.designName} <span style="color:var(--ink-soft); font-size:0.8rem;">(${i.category}, ${i.size})</span></td>
                <td>${i.quantity}</td>
                <td>Rs. ${(i.unitPrice * i.quantity).toLocaleString()}</td>
            </tr>
        `).join("");
        document.getElementById("modalContent").innerHTML = `
            <div style="text-align:center; margin-bottom:1rem;">
                <div style="font-size:2rem;">✅</div>
                <h2 style="margin-bottom:0.2rem;">Order Successful</h2>
                <p style="color:var(--ink-soft); font-size:0.9rem;">Order ${bill.orderId} · ${bill.orderDate}</p>
            </div>
            <table class="bill-table" style="width:100%; margin-bottom:1rem;">
                <thead><tr><th>Item</th><th>Qty</th><th>Total</th></tr></thead>
                <tbody>${itemsHtml}</tbody>
            </table>
            <div class="total-row" style="border-top:1px solid var(--line); padding-top:0.75rem; display:flex; justify-content:space-between; font-weight:700; font-family:'Fraunces',serif; font-size:1.15rem;">
                <span>Total paid</span><span>Rs. ${Number(bill.totalAmount).toLocaleString()}</span>
            </div>
            <div style="display:flex; gap:0.75rem; margin-top:1.5rem;">
                <a class="btn btn-accent btn-block" href="bill.html?orderId=${bill.orderId}" target="_blank">🧾 View full invoice</a>
                <button class="btn btn-outline" id="doneBtn">Done</button>
            </div>
        `;
        document.getElementById("doneBtn").onclick = () => {
            closeCustomerModal();
            document.querySelector('[data-view="orders"]').click();
        };
    } catch (e) {
        document.getElementById("modalContent").innerHTML = `<div class="empty-state">Payment went through, but the bill couldn't be loaded: ${e.message}. You can view it later from My Orders.</div>`;
    }
}

function closeCustomerModal() {
    document.getElementById("designModal").classList.remove("show");
}

// ---------- Orders ----------
async function loadOrders() {
    const list = document.getElementById("ordersList");
    list.innerHTML = `<div class="empty-state">Loading orders…</div>`;
    try {
        const orders = await Api.getOrders(user.customerId);
        if (!orders.length) {
            list.innerHTML = `<div class="empty-state"><div class="icon">📦</div>You haven't placed any orders yet.</div>`;
            return;
        }
        list.innerHTML = orders.map(o => `
            <div class="card" style="margin-bottom:1rem;">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:0.75rem;">
                    <div>
                        <strong>${o.orderId}</strong>
                        <span class="status-badge status-${o.status}">${o.status}</span>
                    </div>
                    <div style="font-weight:700;">Rs. ${Number(o.totalAmount).toLocaleString()}</div>
                </div>
                <div style="font-size:0.85rem; color:var(--ink-soft); margin-bottom:0.5rem;">${o.orderDate}</div>
                ${o.items.map(i => `<div style="font-size:0.9rem; padding:0.3rem 0; border-top:1px solid var(--line);">
                    ${i.designName} · ${i.category} · Size ${i.size} · Qty ${i.quantity} — Rs. ${(i.unitPrice*i.quantity).toLocaleString()}
                </div>`).join("")}
                <div style="margin-top:0.75rem; display:flex; gap:0.5rem;">
                    <a class="btn btn-outline btn-sm" href="bill.html?orderId=${o.orderId}" target="_blank">🧾 View invoice</a>
                    ${o.status === 'PENDING' ? `<button class="btn btn-danger btn-sm" onclick="cancelOrder('${o.orderId}')">Cancel order</button>` : ''}
                </div>
            </div>
        `).join("");
    } catch (e) {
        list.innerHTML = `<div class="empty-state">Couldn't load orders: ${e.message}</div>`;
    }
}

async function cancelOrder(orderId) {
    if (!confirm("Cancel this order?")) return;
    try {
        await Api.cancelOrder(orderId);
        loadOrders();
    } catch (e) {
        alert("Couldn't cancel order: " + e.message);
    }
}

// ---------- Profile ----------
document.getElementById("profileForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const name = document.getElementById("profileName").value.trim();
    const phone = document.getElementById("profilePhone").value.trim();
    const address = document.getElementById("profileAddress").value.trim();
    try {
        await Api.updateProfile(user.userId, name, phone, address);
        user.name = name; user.phone = phone; user.address = address;
        saveSession(user);
        renderNavbar('dashboard');
        const successBox = document.getElementById("profileSuccess");
        successBox.textContent = "Profile updated.";
        successBox.classList.add("show");
        setTimeout(() => successBox.classList.remove("show"), 2500);
    } catch (e) {
        alert("Couldn't update profile: " + e.message);
    }
});

if (user) {
    loadGallery();
    refreshCartCount();
}
