package cherrycosmetics.data;

import cherrycosmetics.data.Models.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Store {
    public static String currentUser = "", currentEmail = "";
    public static boolean isAdmin;
    public static List<User> users = new ArrayList<>();
    public static List<Product> products = new ArrayList<>();
    public static List<CartItem> cart = new ArrayList<>();
    public static List<Order> orders = new ArrayList<>();

    public static void seed() {
        users = Database.loadUsers();
        products = Database.loadProducts();
        images();
        orders = Database.loadOrders();
    }

    private static void images() {
        for (int i = 0; i < products.size() && i < 8; i++)
            if (!products.get(i).hasImage()) products.get(i).image = readImage(i + 1);
    }

    private static byte[] readImage(int n) {
        try (InputStream in = Store.class.getResourceAsStream("/cherrycosmetics/images/products/product" + String.format("%02d", n) + ".jpg")) {
            if (in == null) return null;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] b = new byte[4096];
            int r;
            while ((r = in.read(b)) != -1) out.write(b, 0, r);
            return out.toByteArray();
        } catch (Exception e) { return null;
        }
    }

    public static int cartCount() {
        int n = 0;
        for (CartItem c : cart) n += c.qty;
        return n;
    }

    public static double cartTotal() {
        double n = 0;
        for (CartItem c : cart) n += c.subtotal();
        return n;
    }

    public static Product findProduct(int id) {
        for (Product p : products) if (p.id == id) return p;
        return null;
    }

    public static User currentCustomer() {
        for (User u : users) if (u.username.equals(currentUser)) return u;
        return null;
    }

    public static boolean updateCustomer(String username, String email) {
        User u = currentCustomer();
        if (u == null || u.role.equals("Admin") || username.isBlank() || email.isBlank()) return false;
        for (User x : users) if (x != u && x.username.equalsIgnoreCase(username)) return false;
        String oldUsername = u.username;
        u.username = username.trim();
        u.email = email.trim();
        if (!Database.updateCustomer(u)) { u.username = oldUsername;
        return false;
        }
        currentUser = u.username;
        currentEmail = u.email;
        return true;
    }

    public static boolean resetPassword(String username, String email, String password) {
        if (!validPassword(password)) return false;
        String hashed = PasswordUtil.hash(password);
        boolean updated = Database.resetPassword(username.trim(), email.trim(), hashed);
        if (updated) for (User u : users) if (u.username.equalsIgnoreCase(username.trim()) && u.email.equalsIgnoreCase(email.trim())) u.password = hashed;
        return updated;
    }

    public static boolean addUser(User u) {
        u.username = u.username.trim();
        u.email = u.email.trim();
        if (u.username.length() < 3 || !validEmail(u.email) || !validPassword(u.password)) return false;
        u.password = PasswordUtil.hash(u.password);
        if (Database.insertUser(u)) { users.add(u);
        return true;
        }
        return false;
    }

    public static boolean validEmail(String email) {
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public static boolean validPassword(String password) {
        return password != null && password.length() >= 12
                && password.matches(".*[a-z].*")
                && password.matches(".*[A-Z].*")
                && password.matches(".*\\d.*")
                && password.matches(".*[^A-Za-z0-9].*");
    }

    public static boolean addProduct(String n, String c, double price, int stock, String d, byte[] image, double discount) {
        Product p = new Product(0, n, c, price, stock, "", d, image);
        p.discount = discount;
        if (!Database.insertProduct(p)) return false;
        products.add(p);
        return true;
    }

    public static boolean editProduct(Product p, String n, String c, double price, int stock, String d, byte[] image, double discount) {
        p.name = n;
        p.category = c;
        p.price = price;
        p.stock = stock;
        p.description = d;
        p.image = image;
        p.discount = discount;
        boolean ok = Database.updateProduct(p);
        return ok;
    }

    public static boolean deleteProduct(Product p) {
        if (!Database.archiveProduct(p.id)) return false;
        products.remove(p);
        return true;
    }

    public static boolean restockProduct(Product p, int q) {
        if (q <= 0) return false;
        p.stock += q;
        if (!Database.updateProduct(p)) return false;
        return true;
    }

    public static boolean addToCart(Product p, int qty) {
        if (p == null || qty <= 0 || p.stock < qty) return false;
        for (CartItem c : cart) {
            if (c.productId == p.id) {
                if (c.qty + qty > p.stock) return false;
                c.qty += qty;
                return true;
            }
        }
        cart.add(new CartItem(p.id, p.name, p.discountedPrice(), qty));
        return true;
    }

    public static boolean changeCartQty(CartItem item, int qty) {
        if (qty <= 0) { cart.remove(item);
        return true;
        }
        Product p = findProduct(item.productId);
        if (p == null || qty > p.stock) return false;
        item.qty = qty;
        item.price = p.discountedPrice();
        return true;
    }

    public static Order createOrder(String location, String contact, String method, String payment, String reference) {
        if (location == null || location.isBlank() || contact == null || contact.isBlank() || cart.isEmpty()) return null;
        for (CartItem c : cart) {
            Product p = findProduct(c.productId);
            if (p == null || c.qty <= 0 || c.qty > p.stock) return null;
        }
        Order o = new Order(0, currentUser, currentEmail, today(), location.trim(), contact.trim(), method, payment, "Pending");
        o.paymentReference = reference == null ? "" : reference.trim();
        for (CartItem c : cart) {
            o.items.add(new OrderItem(c.productId, c.name, c.qty, c.price));
            o.total += c.subtotal();
        }
        if (!Database.insertOrder(o)) return null;
        // Database.insertOrder() saves the order and deducts stock in one transaction.
        // Update only the in-memory product list here to match the committed database change.
        for (OrderItem i : o.items) {
            Product p = findProduct(i.productId);
            if (p != null) p.stock -= i.qty;
        }
        orders.add(o);
        cart.clear();
        return o;
    }


    public static boolean approvePayment(Order o) {
        if (o == null || !o.paymentMethod.equals("GCash") || !o.paymentStatus.equals("For Approval") || o.paymentReference.isBlank()) return false;
        o.paymentStatus = "Paid";
        return Database.updatePayment(o);
    }

    public static boolean cancelOrder(Order o) {
        if (o == null || !(o.status.equals("Pending") || o.status.equals("Confirmed"))) return false;
        o.status = "Cancelled";
        o.paymentStatus = "Unpaid";
        restoreStock(o);
        return Database.updateOrder(o);
    }

    public static boolean markReceived(Order o) {
        if (o == null || !o.status.equals("Shipped")) return false;
        o.status = "Delivered";
        o.paymentStatus = "Paid";
        o.receivedDate = today();
        return Database.updateOrder(o);
    }

    public static boolean submitReturn(Order o, String reason) {
        if (o == null || !o.status.equals("Delivered") || reason == null || reason.isBlank()) return false;
        o.status = "Return Requested";
        o.returnReason = reason.trim();
        return Database.updateOrder(o);
    }

    public static boolean updateOrderStatus(Order o, String status) {
        if (o == null) return false;
        if (status.equals("Confirmed") && o.status.equals("Pending")) {
            if (o.paymentMethod.equals("GCash")) {
                if (o.paymentReference.isBlank()) return false;
                o.paymentStatus = "Paid";
            }
            o.status = status;
        }
        else if (status.equals("Preparing") && o.status.equals("Confirmed")) o.status = status;
        else if (status.equals("Shipped") && o.status.equals("Preparing")) o.status = status;
        else if (status.equals("Cancelled") && (o.status.equals("Pending") || o.status.equals("Confirmed"))) {
            o.status = status;
            restoreStock(o);
        } else if (status.equals("Return Approved") && o.status.equals("Return Requested")) {
            o.status = status;
            restoreStock(o);
        } else if (status.equals("Return Rejected") && o.status.equals("Return Requested")) o.status = status;
        else return false;
        return Database.updateOrder(o);
    }

    private static void restoreStock(Order o) {
        for (OrderItem i : o.items) {
            Product p = findProduct(i.productId);
            if (p != null) {
                p.stock += i.qty;
                Database.updateProduct(p);
            }
        }
    }

    public static double netSales() {
        double total = 0;
        for (Order o : orders)
            if (o.paymentStatus.equals("Paid") && (o.status.equals("Delivered") || o.status.equals("Return Rejected"))) total += o.total;
        return total;
    }

    public static String today() { return new SimpleDateFormat("MMM d, yyyy h:mm a").format(new Date());
    }
    public static String peso(double n) { return String.format("₱%,.2f", n);
    }
}
