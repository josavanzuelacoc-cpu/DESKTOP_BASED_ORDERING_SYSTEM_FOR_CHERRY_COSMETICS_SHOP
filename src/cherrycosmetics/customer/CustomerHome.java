package cherrycosmetics.customer;

import cherrycosmetics.MainFrame;
import cherrycosmetics.data.Models.Order;
import cherrycosmetics.data.Store;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CustomerHome extends JPanel {
    private static final Color PINK = new Color(0xC83F70);
    private static final Color LIGHT = new Color(0xF7F7F9);
    private static final Color TEXT = new Color(0x333039);
    private static final Color BORDER = new Color(0xE7DCE1);

    public CustomerHome() {
        setupPage();
    }

    private void setupPage() {
        setLayout(new BorderLayout(18, 18));
        setBackground(LIGHT);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 30, 30));

        add(createTitle(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
    }

    private JLabel createTitle() {
        JLabel title = new JLabel("Welcome, " + Store.currentUser);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT);
        return title;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(18, 22));
        content.setOpaque(false);
        content.add(createQuickActions(), BorderLayout.NORTH);
        content.add(createOrderSummary(), BorderLayout.CENTER);
        return content;
    }

    private JPanel createQuickActions() {
        JPanel quick = new JPanel(new GridLayout(1, 2, 18, 0));
        quick.setOpaque(false);
        quick.setPreferredSize(new Dimension(0, 145));

        quick.add(createActionCard(
                "Browse Products",
                "Find cosmetics and add them to your cart.",
                "Products",
                "Shop Now"
        ));

        quick.add(createActionCard(
                "My Orders",
                "Check your orders and delivery progress.",
                "MyOrders",
                "View Orders"
        ));

        return quick;
    }

    private JPanel createOrderSummary() {
        JPanel area = new JPanel();
        area.setOpaque(false);
        area.setLayout(new BoxLayout(area, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Order Summary");
        title.setFont(new Font("Segoe UI", Font.BOLD, 19));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        area.add(title);
        area.add(Box.createVerticalStrut(12));

        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(900, 125));

        cards.add(createSummaryCard("To Approve", count("Pending")));
        cards.add(createSummaryCard("To Ship", count("Confirmed") + count("Preparing")));
        cards.add(createSummaryCard("To Receive", count("Shipped")));
        cards.add(createSummaryCard("Completed", count("Delivered")));

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapper.setOpaque(false);
        wrapper.add(cards);
        area.add(wrapper);

        return area;
    }

    private JPanel createActionCard(String title, String description, String page, String buttonText) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(300, 125));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(20, 22, 20, 22)
        ));

        JLabel name = new JLabel(title);
        name.setFont(new Font("Segoe UI", Font.BOLD, 20));
        name.setForeground(PINK);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(name);

        card.add(Box.createVerticalStrut(7));

        JLabel text = new JLabel("<html>" + description + "</html>");
        text.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        text.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(text);
        card.add(Box.createVerticalGlue());

        JButton button = new JButton(buttonText);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setBackground(PINK);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.addActionListener(e -> MainFrame.getInstance().showPage(page));
        card.add(button);

        return card;
    }

    private JPanel createSummaryCard(String title, int number) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(190, 105));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel numberLabel = new JLabel(String.valueOf(number));
        numberLabel.setFont(new Font("Segoe UI", Font.BOLD, 23));
        numberLabel.setForeground(TEXT);
        numberLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(numberLabel);

        card.add(Box.createVerticalStrut(4));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(0x66616A));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);

        return card;
    }

    private int count(String status) {
        int total = 0;

        for (Order order : Store.orders) {
            if (order.customer.equals(Store.currentUser)
                    && order.status.equals(status)) {
                total++;
            }
        }

        return total;
    }
}
