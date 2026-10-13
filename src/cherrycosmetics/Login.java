package cherrycosmetics;

import cherrycosmetics.data.Models.User;
import cherrycosmetics.data.Store;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Login extends JFrame {
    private static final Color PINK = new Color(0xC83F70);
    private static final Color LIGHT_PINK = new Color(0xF3B5C8);
    private static final Color TEXT = new Color(0x333039);
    private static final Color MUTED = new Color(0x777177);
    private static final Color BORDER = new Color(0xD8CDD2);

    private final JTextField username = new JTextField();
    private final JPasswordField password = new JPasswordField();

    public Login() {
        setTitle("Cherry Cosmetics - Login");
        setSize(850, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 2));
        add(createBrandPanel());
        add(createLoginPanel());
    }

    private JPanel createBrandPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(LIGHT_PINK);

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));

        JLabel name = new JLabel("Cherry Cosmetics");
        name.setFont(new Font("Segoe UI", Font.BOLD, 30));
        name.setForeground(Color.WHITE);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel line = new JLabel("Simple. Easy. Beautiful.");
        line.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        line.setForeground(Color.WHITE);
        line.setAlignmentX(Component.CENTER_ALIGNMENT);

        brand.add(name);
        brand.add(Box.createVerticalStrut(8));
        brand.add(line);
        panel.add(brand);
        return panel;
    }

    private JPanel createLoginPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Color.WHITE);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(315, 360));

        JLabel title = new JLabel("Login");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in to your Cherry Cosmetics account");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(title);
        form.add(Box.createVerticalStrut(5));
        form.add(subtitle);
        form.add(Box.createVerticalStrut(26));

        addField(form, "Username", username);
        form.add(Box.createVerticalStrut(14));
        addField(form, "Password", password);
        form.add(Box.createVerticalStrut(21));

        JButton login = createButton("Login", PINK, Color.WHITE);
        login.addActionListener(e -> login());
        form.add(login);
        form.add(Box.createVerticalStrut(10));

        JButton signup = createButton("Create Account", Color.WHITE, PINK);
        signup.setBorder(BorderFactory.createLineBorder(new Color(0xB8AEB4)));
        signup.addActionListener(e -> openSignup());
        form.add(signup);
        form.add(Box.createVerticalStrut(8));

        JButton forgot = new JButton("Forgot Password?");
        forgot.setAlignmentX(Component.LEFT_ALIGNMENT);
        forgot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgot.setForeground(PINK);
        forgot.setBorderPainted(false);
        forgot.setContentAreaFilled(false);
        forgot.setFocusPainted(false);
        forgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgot.addActionListener(e -> showRecoveryDialog());
        form.add(forgot);

        outer.add(form);
        return outer;
    }

    private void addField(JPanel panel, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(6));

        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(7, 10, 7, 10)));
        panel.add(field);
    }

    private JButton createButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(
                background.equals(Color.WHITE) ? new Color(0xB8AEB4) : PINK));
        return button;
    }

    private void openSignup() {
        dispose();
        new Signup().setVisible(true);
    }

    private void login() {
        String user = username.getText().trim();
        String pass = new String(password.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            showError(this, "Please enter your username and password.");
            return;
        }
        if (user.length() < 3) {
            showError(this, "Username must have at least 3 characters.");
            return;
        }

        User found = findUser(user, pass);
        if (found == null) {
            showError(this, "Invalid username or password.");
            return;
        }

        Store.currentUser = found.username;
        Store.currentEmail = found.email;
        Store.isAdmin = found.role.equals("Admin");
        dispose();
        MainFrame.open(Store.isAdmin);
    }

    private User findUser(String username, String password) {
        for (User u : Store.users) {
            if (u.username.equalsIgnoreCase(username) && cherrycosmetics.data.PasswordUtil.verify(password, u.password)) {
                return u;
            }
        }
        return null;
    }

    private void showRecoveryDialog() {
        JDialog dialog = new JDialog(this, "Forgot Password", true);
        dialog.setSize(410, 460);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Color.WHITE);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(LIGHT_PINK);
        header.setBorder(new EmptyBorder(18, 22, 16, 22));

        JLabel title = new JLabel("Account Recovery");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel note = new JLabel("Verify your account and create a new password.");
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        note.setForeground(new Color(0x5F555B));
        note.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(note);
        main.add(header, BorderLayout.NORTH);

        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setBackground(Color.WHITE);
        fields.setBorder(new EmptyBorder(18, 24, 8, 24));

        JTextField recoveryUser = new JTextField();
        JTextField email = new JTextField();
        JPasswordField newPassword = new JPasswordField();
        JPasswordField confirm = new JPasswordField();

        addRecoveryField(fields, "Username", recoveryUser);
        addRecoveryField(fields, "Registered Email", email);
        addRecoveryField(fields, "New Password", newPassword);
        addRecoveryField(fields, "Confirm Password", confirm);

        JLabel rule = new JLabel("Use 8+ characters with at least one letter and number.");
        rule.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rule.setForeground(MUTED);
        fields.add(rule);
        main.add(fields, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttons.setBackground(Color.WHITE);
        buttons.setBorder(new EmptyBorder(0, 15, 10, 15));

        JButton cancel = new JButton("Cancel");
        styleDialogButton(cancel, Color.WHITE, PINK);
        cancel.addActionListener(e -> dialog.dispose());

        JButton reset = new JButton("Reset Password");
        styleDialogButton(reset, PINK, Color.WHITE);
        reset.addActionListener(e -> resetPassword(dialog, recoveryUser, email, newPassword, confirm));

        buttons.add(cancel);
        buttons.add(reset);
        main.add(buttons, BorderLayout.SOUTH);

        dialog.setContentPane(main);
        dialog.setVisible(true);
    }

    private void addRecoveryField(JPanel panel, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(5));

        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(5, 8, 5, 8)));
        panel.add(field);
        panel.add(Box.createVerticalStrut(9));
    }

    private void styleDialogButton(JButton button, Color background, Color foreground) {
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(
                background.equals(Color.WHITE) ? new Color(0xB8AEB4) : PINK));
    }

    private void resetPassword(JDialog dialog, JTextField userField, JTextField emailField,
                               JPasswordField newPassword, JPasswordField confirm) {
        String user = userField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(newPassword.getPassword());
        String confirmPassword = new String(confirm.getPassword());

        if (user.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError(dialog, "Please complete all fields.");
            return;
        }
        if (user.length() < 3) {
            showError(dialog, "Username must have at least 3 characters.");
            return;
        }
        if (!Store.validEmail(email)) {
            showError(dialog, "Please enter a valid email address.");
            return;
        }
        if (!Store.validPassword(password)) {
            showError(dialog, "Password must be at least 12 characters and include uppercase and lowercase letters, a number, and a symbol.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError(dialog, "Passwords do not match.");
            return;
        }
        if (password.equalsIgnoreCase(user) || password.equalsIgnoreCase(email)) {
            showError(dialog, "For security, your password cannot be the same as your username or email.");
            return;
        }
        if (!Store.resetPassword(user, email, password)) {
            showError(dialog, "The username and email do not match an active customer account.");
            return;
        }

        JOptionPane.showMessageDialog(dialog,
                "Password changed successfully. You can now log in.",
                "Account Recovery", JOptionPane.INFORMATION_MESSAGE);
        dialog.dispose();
    }

    private void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Validation", JOptionPane.WARNING_MESSAGE);
    }
}
