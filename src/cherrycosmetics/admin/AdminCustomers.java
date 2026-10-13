package cherrycosmetics.admin;

import cherrycosmetics.data.Models.User;
import cherrycosmetics.data.Store;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AdminCustomers extends JPanel {
    private JTable table;
    private JTextField search;
    private JComboBox<String> sortFilter;

    public AdminCustomers() {
        setLayout(new BorderLayout(12,12));
        setBackground(new Color(0xF7F7F9));
        setBorder(BorderFactory.createEmptyBorder(25,25,25,25));

        JLabel title = new JLabel("Customers");
        title.setFont(new Font("Segoe UI",Font.BOLD,27));

        search = new JTextField(20);
        JButton searchButton = new JButton("Search");
        searchButton.setBackground(new Color(0xC83F70));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setOpaque(true);
        searchButton.addActionListener(e -> load());
        search.addActionListener(e -> load());

        JPanel top = new JPanel(new BorderLayout(10,0));
        top.setOpaque(false);
        top.add(title,BorderLayout.WEST);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0));
        searchPanel.setOpaque(false);
        searchPanel.add(search);
        searchPanel.add(searchButton);
        searchPanel.add(new JLabel("Sort:"));
        sortFilter = new JComboBox<>(new String[]{"Username A-Z", "Customer ID: Low to High", "Customer ID: High to Low"});
        sortFilter.addActionListener(e -> load());
        searchPanel.add(sortFilter);
        JButton archiveButton = new JButton("Archive Selected");
        archiveButton.setBackground(new Color(0xF8E4E4));
        archiveButton.setForeground(new Color(0xB33434));
        archiveButton.setFocusPainted(false);
        archiveButton.addActionListener(e -> archiveSelected());
        searchPanel.add(archiveButton);
        top.add(searchPanel,BorderLayout.EAST);
        add(top,BorderLayout.NORTH);

        table = new JTable(new DefaultTableModel(new String[]{"Customer ID","Username","Email","Action"},0) {
            public boolean isCellEditable(int row,int column) { return false;
            }
        });
        table.setRowHeight(32);
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.getVerticalScrollBar().setUnitIncrement(18);
        add(tableScroll,BorderLayout.CENTER);
        table.addMouseListener(new java.awt.event.MouseAdapter(){ public void mouseClicked(java.awt.event.MouseEvent e){ if(e.getClickCount()==2){int row=table.rowAtPoint(e.getPoint()); if(row>=0){int id=(int)table.getValueAt(row,0); if(JOptionPane.showConfirmDialog(AdminCustomers.this,"Archive this customer? The record will remain in the database.","Confirm Archive",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){if(cherrycosmetics.data.Database.archiveUser(id)) load(); else JOptionPane.showMessageDialog(AdminCustomers.this,"Unable to archive customer.");}}}}});
        load();
    }

    private void archiveSelected() {
        int row=table.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Select a customer first.");
        return;
        }
        int id=(int)table.getValueAt(row,0);
        if(JOptionPane.showConfirmDialog(this,"Archive this customer? The database record will be kept.","Confirm Archive",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
            if(cherrycosmetics.data.Database.archiveUser(id)) load();
            else JOptionPane.showMessageDialog(this,"Unable to archive customer.");
        }
    }

    private void load() {
        DefaultTableModel m = (DefaultTableModel)table.getModel();
        m.setRowCount(0);
        String q = search.getText().trim().toLowerCase();
        List<User> customers = new ArrayList<>();
        for (User user : Store.users) {
            if (!user.role.equals("Admin")) {
                String all = user.id + " " + user.username + " " + user.email;
                if (q.isEmpty() || all.toLowerCase().contains(q)) customers.add(user);
            }
        }

        String sort = String.valueOf(sortFilter.getSelectedItem());
        if ("Customer ID: Low to High".equals(sort)) {
            customers.sort(Comparator.comparingInt(user -> user.id));
        } else if ("Customer ID: High to Low".equals(sort)) {
            customers.sort(Comparator.comparingInt((User user) -> user.id).reversed());
        } else {
            customers.sort(Comparator.comparing(user -> user.username, String.CASE_INSENSITIVE_ORDER));
        }

        for (User user : customers) {
            m.addRow(new Object[]{user.id, user.username, user.email, "Archive"});
        }
    }
}
