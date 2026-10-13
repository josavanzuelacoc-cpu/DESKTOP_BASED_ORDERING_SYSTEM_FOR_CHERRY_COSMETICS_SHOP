package cherrycosmetics.data;

import cherrycosmetics.data.Models.Order;
import cherrycosmetics.data.Models.OrderItem;
import cherrycosmetics.data.Models.Product;
import cherrycosmetics.data.Models.User;
import java.sql.*;
import java.util.*;

public class Database {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/cherry_cosmetics?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Manila";

    public static Connection getConnection() throws SQLException {
        String url = System.getProperty("cherry.db.url", DEFAULT_URL);
        String username = System.getProperty("cherry.db.user", "root");
        String password = System.getProperty("cherry.db.password", "");
        return DriverManager.getConnection(url, username, password);
    }

    public static void initialize() {
        try (Connection c = getConnection()) {
            createSchema(c);
            migratePlainTextPasswords(c);
            restoreKnownAdminPassword(c);
            seedAdmin(c);
            seedCategories(c);
            seedProducts(c);
        } catch (Exception e) {
            System.err.println("Cherry Cosmetics could not connect to MySQL. Check that MySQL is running, the cherry_cosmetics database exists, and Database.java connection settings are correct.");
            e.printStackTrace();
        }
    }

    private static void createSchema(Connection c) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS users (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, username VARCHAR(100) NOT NULL UNIQUE, email VARCHAR(190) NOT NULL UNIQUE, password VARCHAR(255) NOT NULL, role VARCHAR(20) NOT NULL, is_archived TINYINT(1) NOT NULL DEFAULT 0, CHECK (role IN ('Admin','Customer')))");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS categories (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, name VARCHAR(120) NOT NULL UNIQUE, is_archived TINYINT(1) NOT NULL DEFAULT 0)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS products (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, name VARCHAR(180) NOT NULL, category_id INT NOT NULL, price DECIMAL(10,2) NOT NULL CHECK (price >= 0), stock INT NOT NULL DEFAULT 0 CHECK (stock >= 0), discount DECIMAL(5,2) NOT NULL DEFAULT 0 CHECK (discount >= 0 AND discount <= 100), description TEXT, image LONGBLOB, is_archived TINYINT(1) NOT NULL DEFAULT 0, CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id))");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS orders (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, customer_id INT NOT NULL, order_date VARCHAR(40) NOT NULL, location VARCHAR(255) DEFAULT '', contact VARCHAR(80) DEFAULT '', status VARCHAR(40) NOT NULL DEFAULT 'Pending', total DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (total >= 0), return_reason TEXT, received_date VARCHAR(40) DEFAULT '', CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES users(id))");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS order_items (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, order_id INT NOT NULL, product_id INT NOT NULL, qty INT NOT NULL CHECK (qty > 0), unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price >= 0), CONSTRAINT fk_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE, CONSTRAINT fk_items_product FOREIGN KEY (product_id) REFERENCES products(id))");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS payments (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, order_id INT NOT NULL UNIQUE, method VARCHAR(60) NOT NULL, status VARCHAR(40) NOT NULL, reference VARCHAR(190) DEFAULT '', payment_date VARCHAR(40) DEFAULT '', CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE)");
        }
    }
    private static String nvl(String s){return s==null?"":s;
    }
    private static int userId(Connection c,String u)throws SQLException{try(PreparedStatement p=c.prepareStatement("SELECT id FROM users WHERE username=?")){p.setString(1,u);
    try(ResultSet r=p.executeQuery()){return r.next()?r.getInt(1):0;
    }}}
    private static int productId(Connection c,String n)throws SQLException{try(PreparedStatement p=c.prepareStatement("SELECT id FROM products WHERE name=?")){p.setString(1,n);
    try(ResultSet r=p.executeQuery()){return r.next()?r.getInt(1):0;
    }}}
    private static int categoryId(Connection c,String n)throws SQLException{try(PreparedStatement p=c.prepareStatement("SELECT id FROM categories WHERE name=?")){p.setString(1,n);
    try(ResultSet r=p.executeQuery()){return r.next()?r.getInt(1):insertCategory(c,n);
    }}}
    private static int insertCategory(Connection c,String n)throws SQLException{try(PreparedStatement p=c.prepareStatement("INSERT INTO categories(name) VALUES(?) ON DUPLICATE KEY UPDATE name=VALUES(name)",Statement.RETURN_GENERATED_KEYS)){p.setString(1,n);
    p.executeUpdate();
    }return categoryId(c,n);
    }
    private static void migratePlainTextPasswords(Connection c) throws SQLException {
        try (PreparedStatement q = c.prepareStatement("SELECT id,username,password FROM users"); ResultSet r = q.executeQuery()) {
            while (r.next()) {
                String value = r.getString("password");
                if (!PasswordUtil.isHashed(value)) {
                    try (PreparedStatement u = c.prepareStatement("UPDATE users SET password=? WHERE id=?")) {
                        String username = r.getString("username");
                        String replacement = "admin".equalsIgnoreCase(username) ? "admin123" : (value == null ? "" : value);
                        u.setString(1, PasswordUtil.hash(replacement));
                        u.setInt(2, r.getInt("id"));
                        u.executeUpdate();
                    }
                }
            }
        }
    }
    private static void restoreKnownAdminPassword(Connection c) throws SQLException {
        try (PreparedStatement q = c.prepareStatement("SELECT id,password FROM users WHERE LOWER(username)='admin' AND role='Admin' LIMIT 1"); ResultSet r = q.executeQuery()) {
            if (r.next()) {
                String stored = r.getString("password");
                // Repair the previous version's documented default without resetting unrelated accounts.
                if (PasswordUtil.verify("Cherry@2026Admin!", stored)) {
                    try (PreparedStatement u = c.prepareStatement("UPDATE users SET password=? WHERE id=?")) {
                        u.setString(1, PasswordUtil.hash("admin123"));
                        u.setInt(2, r.getInt("id"));
                        u.executeUpdate();
                    }
                }
            }
        }
    }

    private static void seedAdmin(Connection c)throws SQLException{try(PreparedStatement p=c.prepareStatement("INSERT IGNORE INTO users(username,email,password,role) VALUES(?,?,?,?)")){p.setString(1,"admin");
    p.setString(2,"admin@cherrycosmetics.local");
    p.setString(3,PasswordUtil.hash("admin123"));
    p.setString(4,"Admin");
    p.executeUpdate();
    }}
    private static void seedCategories(Connection c)throws SQLException{for(String x:new String[]{"Lips","Face","Eyes","Cheeks","Skincare"})insertCategory(c,x);
    }
    private static void seedProducts(Connection c)throws SQLException{String[][] a={{"Lipstick","Lips","199","20","Classic red lipstick"},{"Foundation","Face","399","15","Lightweight liquid foundation"},{"Blush","Face","249","18","Soft pink blush"},{"Mascara","Eyes","299","20","Lengthening mascara"},{"Eyeliner","Eyes","179","25","Black eyeliner"},{"Powder","Face","329","15","Pressed face powder"},{"Concealer","Face","279","18","Cream concealer"},{"Lip Gloss","Lips","229","20","Glossy lip finish"}};
    String q="INSERT INTO products(name,category_id,price,stock,description) SELECT ?,?,?,?,? WHERE NOT EXISTS(SELECT 1 FROM products WHERE name=?)";
    for(String[] x:a)try(PreparedStatement p=c.prepareStatement(q)){p.setString(1,x[0]);
    p.setInt(2,categoryId(c,x[1]));
    p.setDouble(3,Double.parseDouble(x[2]));
    p.setInt(4,Integer.parseInt(x[3]));
    p.setString(5,x[4]);
    p.setString(6,x[0]);
    p.executeUpdate();
    }}


    public static List<String> loadCategories() {
        List<String> names = new ArrayList<>();
        String sql = "SELECT name FROM categories WHERE is_archived=0 ORDER BY name";
        try (Connection c=getConnection(); PreparedStatement p=c.prepareStatement(sql); ResultSet r=p.executeQuery()) {
            while (r.next()) names.add(r.getString("name"));
        } catch (Exception e) { e.printStackTrace(); }
        return names;
    }

    public static boolean addCategory(String name) {
        String value = name == null ? "" : name.trim();
        if (value.isEmpty()) return false;
        String sql = "INSERT INTO categories(name,is_archived) VALUES(?,0)";
        try (Connection c=getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1, value);
            return p.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    public static boolean renameCategory(String oldName, String newName) {
        String value = newName == null ? "" : newName.trim();
        if (value.isEmpty()) return false;
        String sql = "UPDATE categories SET name=? WHERE name=? AND is_archived=0";
        try (Connection c=getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1, value); p.setString(2, oldName);
            return p.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    public static boolean archiveCategory(String name) {
        String check = "SELECT COUNT(*) FROM products p JOIN categories c ON c.id=p.category_id WHERE c.name=? AND p.is_archived=0";
        try (Connection c=getConnection(); PreparedStatement q=c.prepareStatement(check)) {
            q.setString(1, name);
            try (ResultSet r=q.executeQuery()) { if (r.next() && r.getInt(1)>0) return false; }
            try (PreparedStatement p=c.prepareStatement("UPDATE categories SET is_archived=1 WHERE name=? AND is_archived=0")) {
                p.setString(1, name); return p.executeUpdate()>0;
            }
        } catch (Exception e) { return false; }
    }

    public static boolean testConnection(){try(Connection c=getConnection()){try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT COUNT(*) FROM products")){return r.next();
    }}catch(Exception e){return false;
    }}
    public static List<User> loadUsers(){List<User> a=new ArrayList<>();
    try(Connection c=getConnection();PreparedStatement p=c.prepareStatement("SELECT * FROM users WHERE is_archived=0 ORDER BY id");ResultSet r=p.executeQuery()){while(r.next())a.add(new User(r.getInt("id"),r.getString("username"),r.getString("email"),r.getString("password"),r.getString("role")));
    }catch(Exception e){e.printStackTrace();
    }return a;
    }
    public static List<Product> loadProducts(){List<Product>a=new ArrayList<>();
    String q="SELECT p.*,c.name category FROM products p JOIN categories c ON c.id=p.category_id WHERE p.is_archived=0 AND c.is_archived=0 ORDER BY p.id";
    try(Connection c=getConnection();PreparedStatement p=c.prepareStatement(q);ResultSet r=p.executeQuery()){while(r.next()){Product x=new Product(r.getInt("id"),r.getString("name"),r.getString("category"),r.getDouble("price"),r.getInt("stock"),"",nvl(r.getString("description")),r.getBytes("image"));
    x.discount=r.getDouble("discount");
    a.add(x);
    }}catch(Exception e){e.printStackTrace();
    }return a;
    }
    public static List<Order> loadOrders(){List<Order>a=new ArrayList<>();
    String q="SELECT o.*,u.username,u.email,p.method,p.status payment_status,p.reference FROM orders o JOIN users u ON u.id=o.customer_id LEFT JOIN payments p ON p.order_id=o.id ORDER BY o.id";
    try(Connection c=getConnection();PreparedStatement p=c.prepareStatement(q);ResultSet r=p.executeQuery()){while(r.next()){Order o=new Order(r.getInt("id"),r.getString("username"),r.getString("email"),r.getString("order_date"),r.getString("location"),r.getString("contact"),nvl(r.getString("method")),nvl(r.getString("payment_status")),r.getString("status"));
    o.total=r.getDouble("total");
    o.paymentReference=nvl(r.getString("reference"));
    o.returnReason=nvl(r.getString("return_reason"));
    o.receivedDate=nvl(r.getString("received_date"));
    a.add(o);
    }}catch(Exception e){e.printStackTrace();
    }String iq="SELECT i.order_id,i.product_id,p.name,i.qty,i.unit_price FROM order_items i JOIN products p ON p.id=i.product_id ORDER BY i.id";
    try(Connection c=getConnection();PreparedStatement p=c.prepareStatement(iq);ResultSet r=p.executeQuery()){while(r.next())for(Order o:a)if(o.id==r.getInt(1)){o.items.add(new OrderItem(r.getInt("product_id"),r.getString("name"),r.getInt("qty"),r.getDouble("unit_price")));
    break;
    }}catch(Exception e){e.printStackTrace();
    }return a;
    }

    public static boolean insertUser(User u){
        String check="SELECT 1 FROM users WHERE lower(username)=lower(?) OR lower(email)=lower(?) LIMIT 1";
        try(Connection c=getConnection();PreparedStatement checkUser=c.prepareStatement(check)){
            checkUser.setString(1,u.username);
            checkUser.setString(2,u.email);
            try(ResultSet r=checkUser.executeQuery()){if(r.next()) return false;
            }
            try(PreparedStatement p=c.prepareStatement("INSERT INTO users(username,email,password,role) VALUES(?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
                p.setString(1,u.username);
                p.setString(2,u.email);
                p.setString(3,PasswordUtil.isHashed(u.password) ? u.password : PasswordUtil.hash(u.password));
                p.setString(4,u.role);
                p.executeUpdate();
                try(ResultSet r=p.getGeneratedKeys()){if(r.next())u.id=r.getInt(1);
                }
                return true;
            }
        }catch(Exception e){return false;
        }
    }
    public static boolean insertProduct(Product p){try(Connection c=getConnection();PreparedStatement x=c.prepareStatement("INSERT INTO products(name,category_id,price,stock,description,image,discount) VALUES(?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){x.setString(1,p.name);
    x.setInt(2,categoryId(c,p.category));
    x.setDouble(3,p.price);
    x.setInt(4,p.stock);
    x.setString(5,p.description);
    if(p.image==null)x.setNull(6,Types.BLOB);
    else x.setBytes(6,p.image);
    x.setDouble(7,p.discount);
    x.executeUpdate();
    try(ResultSet r=x.getGeneratedKeys()){if(r.next())p.id=r.getInt(1);
    }return true;
    }catch(Exception e){e.printStackTrace();
    return false;
    }}
    public static boolean insertOrder(Order order) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                int customerId = userId(connection, order.customer);
                if (customerId == 0) {
                    throw new SQLException("Customer not found.");
                }

                String orderSql = "INSERT INTO orders(customer_id,order_date,location,contact,status,total,return_reason,received_date) VALUES(?,?,?,?,?,?,?,?)";
                try (PreparedStatement statement = connection.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, customerId);
                    statement.setString(2, order.date);
                    statement.setString(3, order.location);
                    statement.setString(4, order.contact);
                    statement.setString(5, order.status);
                    statement.setDouble(6, order.total);
                    statement.setString(7, order.returnReason);
                    statement.setString(8, order.receivedDate);
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Could not retrieve the new order ID.");
                        order.id = keys.getInt(1);
                    }
                }

                String itemSql = "INSERT INTO order_items(order_id,product_id,qty,unit_price) VALUES(?,?,?,?)";
                try (PreparedStatement statement = connection.prepareStatement(itemSql)) {
                    for (OrderItem item : order.items) {
                        if (item.productId <= 0) item.productId = productId(connection, item.name);
                        if (item.productId <= 0 || item.qty <= 0) {
                            throw new SQLException("Invalid product or quantity in the order.");
                        }
                        statement.setInt(1, order.id);
                        statement.setInt(2, item.productId);
                        statement.setInt(3, item.qty);
                        statement.setDouble(4, item.price);
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }

                String stockSql = "UPDATE products SET stock=stock-? WHERE id=? AND is_archived=0 AND stock>=?";
                try (PreparedStatement statement = connection.prepareStatement(stockSql)) {
                    for (OrderItem item : order.items) {
                        statement.setInt(1, item.qty);
                        statement.setInt(2, item.productId);
                        statement.setInt(3, item.qty);
                        if (statement.executeUpdate() != 1) {
                            throw new SQLException("Insufficient stock for product ID " + item.productId);
                        }
                    }
                }

                String paymentSql = "INSERT INTO payments(order_id,method,status,reference,payment_date) VALUES(?,?,?,?,?)";
                try (PreparedStatement statement = connection.prepareStatement(paymentSql)) {
                    statement.setInt(1, order.id);
                    statement.setString(2, order.paymentMethod);
                    statement.setString(3, order.paymentStatus);
                    statement.setString(4, order.paymentReference);
                    statement.setString(5, order.date);
                    statement.executeUpdate();
                }

                connection.commit();
                return true;
            } catch (Exception e) {
                connection.rollback();
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean updateProduct(Product p){try(Connection c=getConnection();PreparedStatement x=c.prepareStatement("UPDATE products SET name=?,category_id=?,price=?,stock=?,description=?,image=?,discount=? WHERE id=? AND is_archived=0")){x.setString(1,p.name);
    x.setInt(2,categoryId(c,p.category));
    x.setDouble(3,p.price);
    x.setInt(4,p.stock);
    x.setString(5,p.description);
    if(p.image==null)x.setNull(6,Types.BLOB);
    else x.setBytes(6,p.image);
    x.setDouble(7,p.discount);
    x.setInt(8,p.id);
    return x.executeUpdate()>0;
    }catch(Exception e){return false;
    }}
    public static boolean updateOrder(Order o){try(Connection c=getConnection();PreparedStatement p=c.prepareStatement("UPDATE orders SET location=?,contact=?,status=?,total=?,return_reason=?,received_date=? WHERE id=?")){p.setString(1,o.location);
    p.setString(2,o.contact);
    p.setString(3,o.status);
    p.setDouble(4,o.total);
    p.setString(5,o.returnReason);
    p.setString(6,o.receivedDate);
    p.setInt(7,o.id);
    int n=p.executeUpdate();
    updatePayment(c,o);
    return n>0;
    }catch(Exception e){return false;
    }}
    private static void updatePayment(Connection c,Order o)throws SQLException{try(PreparedStatement p=c.prepareStatement("UPDATE payments SET method=?,status=?,reference=? WHERE order_id=?")){p.setString(1,o.paymentMethod);
    p.setString(2,o.paymentStatus);
    p.setString(3,o.paymentReference);
    p.setInt(4,o.id);
    p.executeUpdate();
    }}
    public static boolean updatePayment(Order o){try(Connection c=getConnection()){updatePayment(c,o);
    return true;
    }catch(Exception e){return false;
    }}
    public static boolean updateCustomer(User u){
        String check="SELECT 1 FROM users WHERE (lower(username)=lower(?) OR lower(email)=lower(?)) AND id<>? AND is_archived=0 LIMIT 1";
        try(Connection c=getConnection();PreparedStatement q=c.prepareStatement(check)){
            q.setString(1,u.username);
            q.setString(2,u.email);
            q.setInt(3,u.id);
            try(ResultSet r=q.executeQuery()){if(r.next()) return false;
            }
            try(PreparedStatement p=c.prepareStatement("UPDATE users SET username=?,email=? WHERE id=? AND role='Customer' AND is_archived=0")){
                p.setString(1,u.username);
                p.setString(2,u.email);
                p.setInt(3,u.id);
                return p.executeUpdate()>0;
            }
        }catch(Exception e){return false;
        }
    }
    public static boolean resetPassword(String username,String email,String password){
        try(Connection c=getConnection();PreparedStatement p=c.prepareStatement("UPDATE users SET password=? WHERE lower(username)=lower(?) AND lower(email)=lower(?) AND role='Customer' AND is_archived=0")){
            p.setString(1,password);
            p.setString(2,username);
            p.setString(3,email);
            return p.executeUpdate()>0;
        }catch(Exception e){return false;
        }
    }
    public static boolean archiveProduct(int id){try(Connection c=getConnection();PreparedStatement p=c.prepareStatement("UPDATE products SET is_archived=1 WHERE id=?")){p.setInt(1,id);
    return p.executeUpdate()>0;
    }catch(Exception e){return false;
    }}
    public static boolean archiveUser(int id){try(Connection c=getConnection();PreparedStatement p=c.prepareStatement("UPDATE users SET is_archived=1 WHERE id=? AND role='Customer'")){p.setInt(1,id);
    return p.executeUpdate()>0;
    }catch(Exception e){return false;
    }}
    public static User findUserByUsername(String username){try(Connection c=getConnection();PreparedStatement p=c.prepareStatement("SELECT * FROM users WHERE username=? AND is_archived=0")){p.setString(1,username);
    try(ResultSet r=p.executeQuery()){if(r.next())return new User(r.getInt("id"),r.getString("username"),r.getString("email"),r.getString("password"),r.getString("role"));
    }}catch(Exception e){}return null;
    }
}
