package cherrycosmetics.customer;

import cherrycosmetics.data.Models.Order;
import cherrycosmetics.data.Store;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class MyOrders extends JPanel {
    private final JPanel list = new JPanel();
    private String filter = "To Approve";
    private JScrollPane orderScroll;

    public MyOrders() {
        setLayout(new BorderLayout(10, 15));
        setBackground(new Color(0xF7F7F9));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("My Orders");
        title.setFont(new Font("Segoe UI", Font.BOLD, 27));
        header.add(title, BorderLayout.NORTH);

        JPanel tabs = new JPanel(new GridLayout(1, 5));
        tabs.setBackground(Color.WHITE);
        for (String name : new String[]{"To Approve", "To Ship", "To Receive", "Completed", "Cancelled"}) {
            JButton b = new JButton(name);
            b.setFocusPainted(false);
            b.addActionListener(e -> { filter = name; load(); });
            tabs.add(b);
        }
        header.add(tabs, BorderLayout.SOUTH);

        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);
        add(header, BorderLayout.NORTH);
        orderScroll = new JScrollPane(list);
        orderScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        orderScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        orderScroll.getVerticalScrollBar().setUnitIncrement(18);
        add(orderScroll, BorderLayout.CENTER);
        load();
    }

    private boolean showOrder(Order o) {
        return switch (filter) {
            case "To Approve" -> o.status.equals("Pending");
            case "To Ship" -> o.status.equals("Confirmed") || o.status.equals("Preparing");
            case "To Receive" -> o.status.equals("Shipped");
            case "Completed" -> o.status.equals("Delivered") || o.status.equals("Return Rejected");
            case "Cancelled" -> o.status.equals("Cancelled");
            default -> true;
        };
    }

    private void load() {
        list.removeAll();
        for (Order o : Store.orders) {
            if (o.customer.equals(Store.currentUser) && showOrder(o)) list.add(card(o));
        }
        list.revalidate();
        list.repaint();
    }

    private JPanel card(Order o) {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel head = new JLabel("Order #" + o.id + "  ·  " + o.date + "     " + o.status);
        head.setFont(new Font("Segoe UI", Font.BOLD, 14));
        p.add(head, BorderLayout.NORTH);

        StringBuilder items = new StringBuilder("Items: ");
        for (var i : o.items) items.append(i.name).append(" x").append(i.qty).append("   ");
        String info = "<html>" + items + "<br>Total: " + Store.peso(o.total)
                + " · Payment: " + o.paymentMethod + " (" + o.paymentStatus + ")"
                + "<br>Delivery: " + o.location + " · " + o.contact + "</html>";
        p.add(new JLabel(info), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton view = button("View Details", new Color(0xF2DCE4), new Color(0x9D3159));
        view.addActionListener(e -> details(o));
        actions.add(view);

        if (!o.status.equals("Pending") && !o.status.equals("Cancelled")) {
            JButton receipt = button("View Receipt", new Color(0xC83F70), Color.WHITE);
            receipt.addActionListener(e -> showReceipt(o));
            actions.add(receipt);
        }

        if (o.status.equals("Shipped")) {
            JButton received = button("Yes, I Received It", new Color(0x2E7D5B), Color.WHITE);
            received.addActionListener(e -> { if (Store.markReceived(o)) load(); });
            actions.add(received);
        }
        if (o.status.equals("Delivered")) {
            JButton ret = button("Return Item", new Color(0xF2DCE4), new Color(0x9D3159));
            ret.addActionListener(e -> {
                String reason = JOptionPane.showInputDialog(this, "Reason for return:");
                if (reason != null && !reason.isBlank() && Store.submitReturn(o, reason)) load();
            });
            actions.add(ret);
        }
        p.add(actions, BorderLayout.SOUTH);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        return p;
    }

    private void details(Order o) {
        StringBuilder s = new StringBuilder();
        s.append("Order #").append(o.id).append("\nDate: ").append(o.date)
                .append("\nStatus: ").append(o.status)
                .append("\nPayment: ").append(o.paymentMethod).append(" (").append(o.paymentStatus).append(")")
                .append("\nDelivery: ").append(o.location).append("\nContact: ").append(o.contact).append("\n\n");
        for (var i : o.items) s.append(i.name).append(" x").append(i.qty).append(" — ").append(Store.peso(i.price)).append("\n");
        s.append("\nTotal: ").append(Store.peso(o.total));
        if (!o.returnReason.isBlank()) s.append("\nReturn reason: ").append(o.returnReason);
        JOptionPane.showMessageDialog(this, s.toString(), "Order Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showReceipt(Order o) {
        try {
            BufferedImage image = createReceipt(o);
            JLabel preview = new JLabel(new ImageIcon(image));
            JScrollPane scroll = new JScrollPane(preview);
            scroll.setPreferredSize(new Dimension(430, 500));

            JButton save = button("Save Receipt", new Color(0xC83F70), Color.WHITE);
            save.addActionListener(e -> saveReceipt(o));

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.add(scroll, BorderLayout.CENTER);
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(save);
            panel.add(bottom, BorderLayout.SOUTH);

            JOptionPane.showMessageDialog(this, panel, "Order Receipt #" + o.id, JOptionPane.PLAIN_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Unable to display the receipt.", "Receipt Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BufferedImage createReceipt(Order o) {
        int width = 380;
        int height = 220 + (o.items.size() * 28);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        g.drawString("CHERRY COSMETICS", 75, 30);
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g.drawString("ORDER RECEIPT", 145, 50);
        g.drawLine(20, 62, 360, 62);
        g.drawString("Order #: " + o.id, 20, 82);
        g.drawString("Date: " + o.date, 20, 100);
        g.drawString("Customer: " + o.customer, 20, 118);
        g.drawString("Payment: " + o.paymentMethod, 20, 136);
        g.drawLine(20, 148, 360, 148);
        g.setFont(new Font("Monospaced", Font.BOLD, 12));
        g.drawString("ITEM", 20, 166);
        g.drawString("QTY", 255, 166);
        g.drawString("AMOUNT", 300, 166);
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        int y = 188;
        for (var i : o.items) {
            String name = i.name.length() > 27 ? i.name.substring(0, 27) : i.name;
            g.drawString(name, 20, y);
            g.drawString(String.valueOf(i.qty), 260, y);
            g.drawString(Store.peso(i.subtotal()), 300, y);
            y += 28;
        }
        g.drawLine(20, y - 12, 360, y - 12);
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.drawString("TOTAL", 215, y + 10);
        g.drawString(Store.peso(o.total), 300, y + 10);
        g.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g.drawString("Status: " + o.status, 20, y + 38);
        g.drawString("Thank you!", 150, y + 58);
        g.dispose();
        return image;
    }

    private void saveReceipt(Order o) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("CherryCosmetics_Receipt_" + o.id + ".png"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".png")) file = new File(file.getAbsolutePath() + ".png");

        try {
            ImageIO.write(createReceipt(o), "png", file);
            JOptionPane.showMessageDialog(this, "Receipt saved as an image.", "Receipt", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Unable to save the receipt.", "Receipt Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton button(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setOpaque(true);
        b.setFocusPainted(false);
        return b;
    }
}
