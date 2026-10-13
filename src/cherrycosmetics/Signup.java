package cherrycosmetics;

import cherrycosmetics.data.Models.User;
import cherrycosmetics.data.Store;
import javax.swing.*;
import java.awt.*;

public class Signup extends JFrame {
    private final JTextField username = new JTextField();
    private final JTextField email = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final JPasswordField confirm = new JPasswordField();

    public Signup() {
        setTitle("Cherry Cosmetics - Sign Up");
        setSize(470, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(40, 55, 35, 55));

        JLabel title = new JLabel("Create Customer Account");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        p.add(title);
        p.add(Box.createVerticalStrut(8));
        JLabel note = new JLabel("Use a unique username and email.");
        note.setForeground(new Color(0x777177));
        p.add(note);
        p.add(Box.createVerticalStrut(20));

        field(p, "Username", username);
        field(p, "Email", email);
        field(p, "Password", password);
        field(p, "Confirm Password", confirm);

        JLabel rule = new JLabel("Password: 12+ characters, uppercase, lowercase, number, and symbol.");
        rule.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rule.setForeground(new Color(0x777177));
        p.add(rule);
        p.add(Box.createVerticalStrut(15));

        JButton create = button("Create Account");
        create.addActionListener(e -> createAccount());
        p.add(create);
        p.add(Box.createVerticalStrut(10));

        JButton back = button("Back to Login");
        back.setBackground(Color.WHITE);
        back.setForeground(new Color(0xC83F70));
        back.addActionListener(e -> { dispose(); new Login().setVisible(true); });
        p.add(back);
        add(p);
    }

    private void field(JPanel p, String name, JComponent f) {
        p.add(new JLabel(name));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD0C8CD)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        p.add(f);
        p.add(Box.createVerticalStrut(10));
    }

    private JButton button(String text) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        b.setBackground(new Color(0xC83F70));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }

    private void createAccount() {
        String u = username.getText().trim();
        String e = email.getText().trim();
        String pw = new String(password.getPassword());
        String cpw = new String(confirm.getPassword());

        if (u.isEmpty() || e.isEmpty() || pw.isEmpty() || cpw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please complete all fields.");
            return;
        }
        if (u.length() < 3) {
            JOptionPane.showMessageDialog(this, "Username must have at least 3 characters.");
            return;
        }
        if (!Store.validEmail(e)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.");
            return;
        }
        if (!Store.validPassword(pw)) {
            JOptionPane.showMessageDialog(this, "Password must be at least 12 characters and include uppercase and lowercase letters, a number, and a symbol.");
            return;
        }
        if (!pw.equals(cpw)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.");
            return;
        }
        if (Store.currentUser.equalsIgnoreCase(u)) {
            JOptionPane.showMessageDialog(this, "That username is already in use.");
            return;
        }

        User user = new User(0, u, e, pw, "Customer");
        if (!Store.addUser(user)) {
            JOptionPane.showMessageDialog(this, "Username or email is already registered.");
            return;
        }

        JOptionPane.showMessageDialog(this, "Account created successfully. You can now log in.");
        dispose();
        new Login().setVisible(true);
    }
}
