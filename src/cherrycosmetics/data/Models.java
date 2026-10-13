package cherrycosmetics.data;

import java.util.ArrayList;
import java.util.List;

public class Models {

    public static class User {
        public int id;
        public String username, email, password, role;

        public User(int id, String username, String email, String password, String role) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.password = password;
            this.role = role;
        }
    }

    public static class Product {
        public int id, stock;
        public String name, category, description;
        public double price, discount;
        public byte[] image;

        public Product(int id, String name, String category, double price, int stock, String emoji, String description) {
            this(id, name, category, price, stock, emoji, description, null);
        }

        public Product(int id, String name, String category, double price, int stock, String emoji, String description, byte[] image) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
            this.stock = stock;
            this.description = description;
            this.image = image;
        }

        public double discountedPrice() {
            return price * (1 - discount / 100);
        }

        public boolean hasImage() {
            return image != null && image.length > 0;
        }

        public String stockText() {
            return stock == 0 ? "Out of Stock" : stock <= 5 ? "Low Stock (" + stock + ")" : "In Stock (" + stock + ")";
        }
    }

    public static class CartItem {
        public int productId, qty;
        public String name;
        public double price;

        public CartItem(int productId, String name, double price, int qty) {
            this.productId = productId;
            this.name = name;
            this.price = price;
            this.qty = qty;
        }

        public double subtotal() {
            return price * qty;
        }
    }

    public static class OrderItem {
        public int productId, qty;
        public String name;
        public double price;

        public OrderItem(int productId, String name, int qty, double price) {
            this.productId = productId;
            this.name = name;
            this.qty = qty;
            this.price = price;
        }

        public OrderItem(String name, int qty, double price) { this(0, name, qty, price);
        }

        public double subtotal() {
            return price * qty;
        }
    }

    public static class Order {
        public int id;
        public String customer, email, date, location, contact, paymentMethod, paymentStatus, status, paymentReference;
        public String returnReason = "", receivedDate = "";
        public double total;
        public List<OrderItem> items = new ArrayList<>();

        public Order(int id, String customer, String email, String date, String location, String contact, String method, String payment, String status) {
            this.id = id;
            this.customer = customer;
            this.email = email;
            this.date = date;
            this.location = location;
            this.contact = contact;
            this.paymentMethod = method;
            this.paymentStatus = payment;
            this.status = status;
            this.paymentReference = "";
        }
    }


}
