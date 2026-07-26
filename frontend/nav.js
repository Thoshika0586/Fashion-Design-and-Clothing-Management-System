

function renderNavbar(activePage) {
    const container = document.getElementById("navbarContainer");
    if (!container) return;
    const user = typeof getSession === "function" ? getSession() : null;

    let dashLink = "login.html";
    if (user) {
        if (user.role === "CUSTOMER") dashLink = "customer-dashboard.html";
        else if (user.role === "DESIGNER") dashLink = "designer-dashboard.html";
        else if (user.role === "ADMIN") dashLink = "admin-dashboard.html";
    }

    const rightLinks = user
        ? `<a href="${dashLink}" class="${activePage === 'dashboard' ? 'active-link' : ''}">My Dashboard</a>
           <span class="nav-hello">Hi, ${user.name.split(' ')[0]}</span>
           <button id="navLogoutBtn">Log out</button>`
        : `<a href="login.html" class="${activePage === 'login' ? 'active-link' : ''}">Log in</a>
           <a href="register.html" class="pill">Register</a>`;

    container.innerHTML = `
        <div class="topbar">
            <a href="index.html" class="brand"></span> LUMINA  LABLE</a>
            <nav>
                <a href="index.html" class="${activePage === 'home' ? 'active-link' : ''}">Home</a>
                <div class="nav-dropdown">
                    <button type="button" class="nav-dropdown-toggle">Clothes ▾</button>
                    <div class="nav-dropdown-menu">
                        <a href="index.html?category=FROCK#gallery">Frocks</a>
                        <a href="index.html?category=SAREE#gallery">Sarees</a>
                    </div>
                </div>
                <a href="about.html" class="${activePage === 'about' ? 'active-link' : ''}">About Us</a>
                <a href="contact.html" class="${activePage === 'contact' ? 'active-link' : ''}">Contact Us</a>
                ${rightLinks}
            </nav>
        </div>
    `;

    const logoutBtn = document.getElementById("navLogoutBtn");
    if (logoutBtn) logoutBtn.addEventListener("click", () => { clearSession(); window.location.href = "login.html"; });

    const dropdownToggle = container.querySelector(".nav-dropdown-toggle");
    const dropdown = container.querySelector(".nav-dropdown");
    if (dropdownToggle && dropdown) {
        dropdownToggle.addEventListener("click", (e) => {
            e.stopPropagation();
            dropdown.classList.toggle("open");
        });
        document.addEventListener("click", () => dropdown.classList.remove("open"));
    }
}
