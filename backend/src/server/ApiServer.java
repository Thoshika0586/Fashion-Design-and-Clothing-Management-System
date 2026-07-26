package com.fashiondesign.server;

import com.fashiondesign.dao.*;
import com.fashiondesign.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;

/**
 * Lightweight REST API server for the Fashion Design & Clothing Management System.
 * Built on the JDK's built-in HttpServer so no external web framework is required.
 *
 * Run with: java -cp "bin:lib/mysql-connector-j.jar" com.fashiondesign.server.ApiServer
 */
public class ApiServer {

    private static final UserDAO userDAO = new UserDAO();
    private static final DesignDAO designDAO = new DesignDAO();
    private static final FabricDAO fabricDAO = new FabricDAO();
    private static final CartDAO cartDAO = new CartDAO();
    private static final OrderDAO orderDAO = new OrderDAO();
    private static final PaymentDAO paymentDAO = new PaymentDAO();
    private static final DeliveryDAO deliveryDAO = new DeliveryDAO();
    private static final InventoryDAO inventoryDAO = new InventoryDAO();

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/login", ApiServer::handleLogin);
        server.createContext("/api/register", ApiServer::handleRegister);
        server.createContext("/api/profile", ApiServer::handleProfile);

        server.createContext("/api/designs", ApiServer::handleDesigns);
        server.createContext("/api/fabrics", ApiServer::handleFabrics);

        server.createContext("/api/cart", ApiServer::handleCart);
        server.createContext("/api/orders", ApiServer::handleOrders);
        server.createContext("/api/bill", ApiServer::handleBill);
        server.createContext("/api/payments", ApiServer::handlePayments);
        server.createContext("/api/deliveries", ApiServer::handleDeliveries);
        server.createContext("/api/inventory", ApiServer::handleInventory);
        server.createContext("/api/admin/users", ApiServer::handleAdminUsers);
        server.createContext("/api/admin/report", ApiServer::handleAdminReport);

        server.setExecutor(null);
        server.start();
        System.out.println("Fashion Design System API running at http://localhost:" + port);
    }

    // ---------------------------------------------------------------
    // AUTH
    // ---------------------------------------------------------------
    private static void handleLogin(HttpExchange ex) throws IOException {
        if (!method(ex, "POST")) return;
        Map<String, Object> body = readJsonBody(ex);
        try {
            Map<String, Object> user = userDAO.login((String) body.get("email"), (String) body.get("password"));
            if (user == null) {
                sendJson(ex, 401, err("Invalid email or password."));
            } else {
                sendJson(ex, 200, user);
            }
        } catch (SQLException e) {
            sendJson(ex, 500, err("Database error: " + e.getMessage()));
        }
    }

    private static void handleRegister(HttpExchange ex) throws IOException {
        if (!method(ex, "POST")) return;
        Map<String, Object> body = readJsonBody(ex);
        try {
            Map<String, Object> user = userDAO.registerCustomer(
                    (String) body.get("name"), (String) body.get("email"), (String) body.get("password"),
                    (String) body.get("phone"), (String) body.get("address"));
            sendJson(ex, 201, user);
        } catch (SQLException e) {
            sendJson(ex, 500, err("Could not register: " + e.getMessage()));
        }
    }

    private static void handleProfile(HttpExchange ex) throws IOException {
        if (!method(ex, "PUT")) return;
        Map<String, Object> body = readJsonBody(ex);
        try {
            userDAO.updateProfile((String) body.get("userId"), (String) body.get("name"),
                    (String) body.get("phone"), (String) body.get("address"));
            sendJson(ex, 200, ok("Profile updated."));
        } catch (SQLException e) {
            sendJson(ex, 500, err(e.getMessage()));
        }
    }

    // ---------------------------------------------------------------
    // DESIGNS (designer's sketches - frocks & sarees)
    // ---------------------------------------------------------------
    private static void handleDesigns(HttpExchange ex) throws IOException {
        String query = ex.getRequestURI().getRawQuery();
        Map<String, String> q = parseQuery(query);
        try {
            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                if (q.containsKey("designId")) {
                    sendJson(ex, 200, designDAO.getDesignById(q.get("designId")));
                } else {
                    sendJson(ex, 200, designDAO.getAllDesigns(q.get("category")));
                }
            } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String, Object> body = readJsonBody(ex);
                Map<String, Object> design = designDAO.createDesign(
                        (String) body.get("designerId"), (String) body.get("designName"),
                        (String) body.get("category"), (String) body.get("pattern"),
                        (String) body.get("fabricId"), toDouble(body.get("price")),
                        (String) body.get("sketchImage"), (String) body.get("description"));
                sendJson(ex, 201, design);
            } else if ("PUT".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String, Object> body = readJsonBody(ex);
                designDAO.updateDesign((String) body.get("designId"), (String) body.get("designName"),
                        (String) body.get("category"), (String) body.get("pattern"), (String) body.get("fabricId"),
                        toDouble(body.get("price")), (String) body.get("sketchImage"),
                        (String) body.get("description"), (String) body.getOrDefault("status", "AVAILABLE"));
                sendJson(ex, 200, ok("Design updated."));
            } else if ("DELETE".equalsIgnoreCase(ex.getRequestMethod())) {
                designDAO.deleteDesign(q.get("designId"));
                sendJson(ex, 200, ok("Design archived."));
            } else {
                sendJson(ex, 405, err("Method not allowed."));
            }
        } catch (SQLException e) {
            sendJson(ex, 500, err(e.getMessage()));
        }
    }

    private static void handleFabrics(HttpExchange ex) throws IOException {
        try {
            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                sendJson(ex, 200, fabricDAO.getAllFabrics());
            } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String, Object> body = readJsonBody(ex);
                sendJson(ex, 201, fabricDAO.addFabric((String) body.get("fabricName"),
                        (String) body.get("color"), toDouble(body.get("pricePerUnit"))));
            } else {
                sendJson(ex, 405, err("Method not allowed."));
            }
        } catch (SQLException e) {
            sendJson(ex, 500, err(e.getMessage()));
        }
    }

    // ---------------------------------------------------------------
    // CART
    // ---------------------------------------------------------------
    private static void handleCart(HttpExchange ex) throws IOException {
        Map<String, String> q = parseQuery(ex.getRequestURI().getRawQuery());
        try {
            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                sendJson(ex, 200, cartDAO.getCartItems(q.get("customerId")));
            } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String, Object> body = readJsonBody(ex);
                cartDAO.addItem((String) body.get("customerId"), (String) body.get("designId"),
                        (String) body.get("size"), (int) toDouble(body.get("quantity")),
                        (String) body.get("customNote"));
                sendJson(ex, 201, ok("Added to cart."));
            } else if ("DELETE".equalsIgnoreCase(ex.getRequestMethod())) {
                cartDAO.removeItem(q.get("cartItemId"));
                sendJson(ex, 200, ok("Removed from cart."));
            } else {
                sendJson(ex, 405, err("Method not allowed."));
            }
        } catch (SQLException e) {
            sendJson(ex, 500, err(e.getMessage()));
        }
    }

    // ---------------------------------------------------------------
    // ORDERS
    // ---------------------------------------------------------------
    private static void handleOrders(HttpExchange ex) throws IOException {
        Map<String, String> q = parseQuery(ex.getRequestURI().getRawQuery());
        try {
            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                if (q.containsKey("customerId")) {
                    sendJson(ex, 200, orderDAO.getOrdersForCustomer(q.get("customerId")));
                } else {
                    sendJson(ex, 200, orderDAO.getAllOrders());
                }
            } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String, Object> body = readJsonBody(ex);
                Map<String, Object> order = orderDAO.placeOrderFromCart((String) body.get("customerId"));
                if (order == null) {
                    sendJson(ex, 400, err("Your cart is empty."));
                } else {
                    sendJson(ex, 201, order);
                }
            } else if ("PUT".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String, Object> body = readJsonBody(ex);
                orderDAO.updateStatus((String) body.get("orderId"), (String) body.get("status"));
                sendJson(ex, 200, ok("Order status updated."));
            } else if ("DELETE".equalsIgnoreCase(ex.getRequestMethod())) {
                orderDAO.cancelOrder(q.get("orderId"));
                sendJson(ex, 200, ok("Order cancelled."));
            } else {
                sendJson(ex, 405, err("Method not allowed."));
            }
        } catch (SQLException e) {
            sendJson(ex, 500, err(e.getMessage()));
        }
    }

    // ---------------------------------------------------------------
    // BILL / INVOICE
    // ---------------------------------------------------------------
    private static void handleBill(HttpExchange ex) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) { sendJson(ex, 204, null); return; }
            sendJson(ex, 405, err("Method not allowed."));
            return;
        }
        Map<String, String> q = parseQuery(ex.getRequestURI().getRawQuery());
        try {
            Map<String, Object> bill = orderDAO.getBill(q.get("orderId"));
            if (bill == null) {
                sendJson(ex, 404, err("Order not found."));
            } else {
                sendJson(ex, 200, bill);
            }
        } catch (SQLException e) {
            sendJson(ex, 500, err(e.getMessage()));
        }
    }

    // ---------------------------------------------------------------
    // PAYMENTS
    // ---------------------------------------------------------------
    private static void handlePayments(HttpExchange ex) throws IOException {
        Map<String, String> q = parseQuery(ex.getRequestURI().getRawQuery());
        try {
            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                sendJson(ex, 200, paymentDAO.getPaymentsForOrder(q.get("orderId")));
            } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String, Object> body = readJsonBody(ex);
                Map<String, Object> payment = paymentDAO.makePayment((String) body.get("orderId"),
                        (String) body.get("method"), toDouble(body.get("amount")));
                sendJson(ex, 201, payment);
            } else {
                sendJson(ex, 405, err("Method not allowed."));
            }
        } catch (SQLException e) {
            sendJson(ex, 500, err(e.getMessage()));
        }
    }

    // ---------------------------------------------------------------
    // DELIVERIES
    // ---------------------------------------------------------------
    private static void handleDeliveries(HttpExchange ex) throws IOException {
        try {
            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                sendJson(ex, 200, deliveryDAO.getAllDeliveries());
            } else if ("PUT".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String, Object> body = readJsonBody(ex);
                deliveryDAO.updateStatus((String) body.get("deliveryId"), (String) body.get("status"),
                        (String) body.get("deliveryDate"));
                sendJson(ex, 200, ok("Delivery updated."));
            } else {
                sendJson(ex, 405, err("Method not allowed."));
            }
        } catch (SQLException e) {
            sendJson(ex, 500, err(e.getMessage()));
        }
    }

    // ---------------------------------------------------------------
    // INVENTORY (admin)
    // ---------------------------------------------------------------
    private static void handleInventory(HttpExchange ex) throws IOException {
        Map<String, String> q = parseQuery(ex.getRequestURI().getRawQuery());
        try {
            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                sendJson(ex, 200, inventoryDAO.getAllInventory());
            } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String, Object> body = readJsonBody(ex);
                sendJson(ex, 201, inventoryDAO.addItem((String) body.get("itemType"), (String) body.get("itemName"),
                        (int) toDouble(body.get("quantity")), toDouble(body.get("unitPrice"))));
            } else if ("PUT".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String, Object> body = readJsonBody(ex);
                inventoryDAO.updateStock((String) body.get("inventoryId"), (int) toDouble(body.get("quantity")));
                sendJson(ex, 200, ok("Stock updated."));
            } else if ("DELETE".equalsIgnoreCase(ex.getRequestMethod())) {
                inventoryDAO.deleteItem(q.get("inventoryId"));
                sendJson(ex, 200, ok("Item deleted."));
            } else {
                sendJson(ex, 405, err("Method not allowed."));
            }
        } catch (SQLException e) {
            sendJson(ex, 500, err(e.getMessage()));
        }
    }

    // ---------------------------------------------------------------
    // ADMIN
    // ---------------------------------------------------------------
    private static void handleAdminUsers(HttpExchange ex) throws IOException {
        try {
            sendJson(ex, 200, userDAO.getAllUsers());
        } catch (SQLException e) {
            sendJson(ex, 500, err(e.getMessage()));
        }
    }

    private static void handleAdminReport(HttpExchange ex) throws IOException {
        try {
            List<Map<String, Object>> orders = orderDAO.getAllOrders();
            double totalRevenue = 0;
            int completed = 0, pending = 0, cancelled = 0;
            for (Map<String, Object> o : orders) {
                String status = (String) o.get("status");
                if ("COMPLETED".equals(status)) { completed++; totalRevenue += (double) o.get("totalAmount"); }
                else if ("CANCELLED".equals(status)) cancelled++;
                else pending++;
            }
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("totalOrders", orders.size());
            report.put("completedOrders", completed);
            report.put("pendingOrders", pending);
            report.put("cancelledOrders", cancelled);
            report.put("totalRevenue", totalRevenue);
            report.put("inventory", inventoryDAO.getAllInventory());
            sendJson(ex, 200, report);
        } catch (SQLException e) {
            sendJson(ex, 500, err(e.getMessage()));
        }
    }

    // ---------------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------------
    private static boolean method(HttpExchange ex, String m) throws IOException {
        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            sendJson(ex, 204, null);
            return false;
        }
        if (!ex.getRequestMethod().equalsIgnoreCase(m)) {
            sendJson(ex, 405, err("Method not allowed."));
            return false;
        }
        return true;
    }

    private static Map<String, Object> readJsonBody(HttpExchange ex) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        InputStream is = ex.getRequestBody();
        while ((n = is.read(buf)) != -1) out.write(buf, 0, n);
        String body = out.toString(StandardCharsets.UTF_8);
        return JsonUtil.parseObject(body);
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new LinkedHashMap<>();
        if (query == null) return map;
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                try {
                    String key = java.net.URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                    String value = java.net.URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                    map.put(key, value);
                } catch (UnsupportedEncodingException ignored) {}
            }
        }
        return map;
    }

    private static double toDouble(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).doubleValue();
        try { return Double.parseDouble(o.toString()); } catch (NumberFormatException e) { return 0; }
    }

    private static Map<String, Object> ok(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", true);
        m.put("message", message);
        return m;
    }

    private static Map<String, Object> err(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", false);
        m.put("error", message);
        return m;
    }

    private static void sendJson(HttpExchange ex, int status, Object body) throws IOException {
        String json = JsonUtil.toJson(body);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
