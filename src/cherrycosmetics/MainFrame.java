package cherrycosmetics;

import cherrycosmetics.admin.*;
import cherrycosmetics.customer.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.*;

public class MainFrame extends JFrame {
    private static MainFrame instance;
    private final JPanel content = new JPanel(new BorderLayout());
    private final boolean admin;

    public MainFrame(boolean admin) {
        instance = this;
        this.admin = admin;
        setTitle("Cherry Cosmetics");
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        content.setBackground(new Color(0xF7F7F9));
        setLayout(new BorderLayout());
        add(content, BorderLayout.CENTER);
        showPage(admin ? "AdminHome" : "CustomerHome");
    }

    public static MainFrame getInstance() { return instance;
    }

    public static void open(boolean admin) {
        if (instance != null) instance.dispose();
        new MainFrame(admin).setVisible(true);
    }

    public void showPage(String page) {
        JPanel p;
        switch (page) {
            case "AdminHome" -> p = new AdminHome();
            case "AdminProducts" -> p = new AdminProducts();
            case "AdminOrders" -> p = new AdminOrders();
            case "AdminCustomers" -> p = new AdminCustomers();
            case "AdminReports" -> p = new AdminReports();
            case "CustomerHome" -> p = new CustomerHome();
            case "Products" -> p = new Products();
            case "MyOrders" -> p = new MyOrders();
            case "Cart" -> p = new Cart();
            case "Account" -> p = new Account();
            default -> { return;
            }
        }
        content.removeAll();
        content.add(sidebar(page), BorderLayout.WEST);
        content.add(p, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }

    private JPanel sidebar(String active) {
        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(230, 0));
        side.setBackground(new Color(0xFFF7FA));
        side.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0xE5D7DD)));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        JLabel name = new JLabel("Cherry Cosmetics");
        name.setFont(new Font("Segoe UI", Font.BOLD, 19));
        name.setForeground(new Color(0xC83F70));
        name.setBorder(BorderFactory.createEmptyBorder(28, 22, 22, 10));
        side.add(name);
        if (admin) {
            addNav(side, "Dashboard", "AdminHome", active, "dashboard");
            addNav(side, "Products", "AdminProducts", active, "products");
            addNav(side, "Orders", "AdminOrders", active, "orders");
            addNav(side, "Sales Report", "AdminReports", active, "reports");
            addNav(side, "Customers", "AdminCustomers", active, "customers");
        } else {
            addNav(side, "Home", "CustomerHome", active, "home");
            addNav(side, "Products", "Products", active, "products");
            addNav(side, "Orders", "MyOrders", active, "orders");
            addNav(side, "Cart (" + cherrycosmetics.data.Store.cartCount() + ")", "Cart", active, "cart");
            addNav(side, "Account", "Account", active, "account");
        }
        side.add(Box.createVerticalGlue());
        JButton logout = new JButton("Logout");
        logout.setIcon(icon("logout", 19));
        logout.setHorizontalAlignment(SwingConstants.LEFT);
        logout.setIconTextGap(10);
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.setMaximumSize(new Dimension(210, 42));
        logout.setBackground(new Color(0xF2DCE4));
        logout.setForeground(new Color(0x9D3159));
        logout.setFocusPainted(false);
        logout.setOpaque(true);
        logout.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        logout.addActionListener(e -> { dispose(); new Login().setVisible(true); });
        side.add(logout);
        side.add(Box.createVerticalStrut(20));
        return side;
    }

    private void addNav(JPanel side, String text, String page, String active, String iconName) {
        JButton b = new JButton(text, icon(iconName, 18));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(210, 42));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setIconTextGap(12);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        b.setBackground(page.equals(active) ? new Color(0xF3D7E1) : new Color(0xFFF7FA));
        b.setForeground(page.equals(active) ? new Color(0xB52F61) : new Color(0x55505A));
        b.addActionListener(e -> showPage(page));
        side.add(b);
    }

    private ImageIcon icon(String name, int size) {
        try (InputStream in = MainFrame.class.getResourceAsStream("/cherrycosmetics/images/icons/" + name + ".png")) {
            if (in == null) return null;
            BufferedImage img = ImageIO.read(in);
            return new ImageIcon(img.getScaledInstance(size, size, Image.SCALE_SMOOTH));
        } catch (Exception e) { return null;
        }
    }
}
