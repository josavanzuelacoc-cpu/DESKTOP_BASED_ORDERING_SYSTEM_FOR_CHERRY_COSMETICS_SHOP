package cherrycosmetics.customer;

import cherrycosmetics.MainFrame;
import cherrycosmetics.data.Models.User;
import cherrycosmetics.data.Store;
import javax.swing.*;
import java.awt.*;

public class Account extends JPanel {
    JTextField username = new JTextField();
    JTextField email = new JTextField();
    JLabel id = new JLabel();

    public Account() {
        setLayout(new BorderLayout());
        setBackground(new Color(0xF7F7F9));
        setBorder(BorderFactory.createEmptyBorder(28, 35, 28, 35));
        User u = Store.currentCustomer();

        JPanel card = new JPanel(new BorderLayout(0, 22));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE7DCE1)),
                BorderFactory.createEmptyBorder(26, 30, 28, 30)));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Account Information");
        t.setFont(new Font("Segoe UI", Font.BOLD, 26));
        t.setForeground(new Color(0x333039));
        heading.add(t);
        heading.add(Box.createVerticalStrut(7));
        id.setText("Customer ID  ·  " + (u == null ? "—" : u.id));
        id.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        id.setForeground(new Color(0x77717A));
        heading.add(id);
        card.add(heading, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 15, 0);
        g.gridy = 0;
        fields.add(label("Username"), g);
        g.gridy = 1;
        username.setText(Store.currentUser);
        style(username);
        fields.add(username, g);
        g.gridy = 2;
        g.insets = new Insets(4, 0, 15, 0);
        fields.add(label("Email Address"), g);
        g.gridy = 3;
        g.insets = new Insets(0, 0, 4, 0);
        email.setText(Store.currentEmail);
        style(email);
        fields.add(email, g);
        card.add(fields, BorderLayout.CENTER);

        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        JButton save = button("Save Changes");
        save.addActionListener(e -> save());
        actions.add(save, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        add(card, BorderLayout.NORTH);
    }

    private JLabel label(String s) { JLabel l=new JLabel(s);
    l.setFont(new Font("Segoe UI",Font.BOLD,12));
    return l;
    }
    private void style(JTextField f) { f.setPreferredSize(new Dimension(420,42));
    f.setMaximumSize(new Dimension(Integer.MAX_VALUE,42));
    f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0xD0C8CD)),BorderFactory.createEmptyBorder(8,10,8,10)));
    }
    private JButton button(String s) { JButton b=new JButton(s);
    b.setMaximumSize(new Dimension(Integer.MAX_VALUE,42));
    b.setBackground(new Color(0xC83F70));
    b.setForeground(Color.WHITE);
    b.setFocusPainted(false);
    return b;
    }
    private void save() { if (Store.updateCustomer(username.getText().trim(), email.getText().trim())) { JOptionPane.showMessageDialog(this,"Account updated.");
    MainFrame.getInstance().showPage("Account");
    } else JOptionPane.showMessageDialog(this,"Username may already exist or the fields are invalid.");
    }
}
