package cherrycosmetics.admin;

import cherrycosmetics.data.Store;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AdminHome extends JPanel {
    private static final Color PINK = new Color(0xC83F70);
    private static final Color BG = new Color(0xF7F7F9);
    private static final Color TEXT = new Color(0x333039);
    private static final Color BORDER = new Color(0xE7DCE1);

    public AdminHome() {
        setupPage();
    }

    private void setupPage() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        add(createTopArea(), BorderLayout.NORTH);
    }

    private JPanel createTopArea() {
        JPanel top = new JPanel(new BorderLayout(15, 12));
        top.setOpaque(false);
        top.add(createTitle(), BorderLayout.NORTH);
        top.add(createSummaryCards(), BorderLayout.CENTER);
        return top;
    }

    private JLabel createTitle() {
        JLabel title = new JLabel("Admin");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT);
        return title;
    }

    private JPanel createSummaryCards() {
        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 0));
        cards.setOpaque(false);
        cards.add(createCard("Products", Store.products.size()));
        cards.add(createCard("Orders", Store.orders.size()));
        cards.add(createCard("Customers", customerCount()));
        cards.add(createCard("Net Sales", Store.peso(Store.netSales())));
        return cards;
    }

    private JPanel createCard(String name, Object value) {
        JPanel card = new JPanel(new BorderLayout(8, 5));
        card.setPreferredSize(new Dimension(245, 125));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(PINK);

        JLabel valueLabel = new JLabel(String.valueOf(value));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(new Color(0x2C2C34));

        card.add(nameLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private int customerCount() {
        int total = 0;

        for (var user : Store.users) {
            if (!user.role.equals("Admin")) {
                total++;
            }
        }

        return total;
    }
}
