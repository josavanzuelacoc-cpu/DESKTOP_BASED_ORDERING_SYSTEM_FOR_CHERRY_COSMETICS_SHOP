package cherrycosmetics.admin;

import cherrycosmetics.data.Models.Order;
import cherrycosmetics.data.Store;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AdminReports extends JPanel {
    private JTable table;
    private JTextField search;
    private JLabel totalSales;
    private JComboBox<String> statusFilter;
    private JComboBox<String> periodFilter;

    public AdminReports() {
        setLayout(new BorderLayout(12,12));
        setBackground(new Color(0xF7F7F9));
        setBorder(BorderFactory.createEmptyBorder(25,25,25,25));

        JLabel title = new JLabel("Sales Report");
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
        statusFilter = new JComboBox<>(new String[]{"All Sales", "Delivered", "Return Rejected"});
        statusFilter.addActionListener(e -> load());
        searchPanel.add(statusFilter);
        periodFilter = new JComboBox<>(new String[]{"Weekly", "Monthly", "Yearly"});
        periodFilter.setSelectedItem("Monthly");
        periodFilter.setToolTipText("Show sales for the selected time period");
        periodFilter.addActionListener(e -> load());
        searchPanel.add(periodFilter);
        JButton exportButton = new JButton("Export CSV");
        exportButton.setBackground(new Color(0x5B6573));
        exportButton.setForeground(Color.WHITE);
        exportButton.setFocusPainted(false);
        exportButton.setOpaque(true);
        exportButton.addActionListener(e -> exportCsv());
        searchPanel.add(exportButton);
        top.add(searchPanel,BorderLayout.EAST);
        add(top,BorderLayout.NORTH);

        table = new JTable(new DefaultTableModel(new String[]{"Order ID","Customer","Date","Payment","Status","Total"},0) {
            public boolean isCellEditable(int row,int column) { return false;
            }
        });
        table.setRowHeight(32);
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        tableScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScroll.getVerticalScrollBar().setUnitIncrement(18);
        add(tableScroll,BorderLayout.CENTER);
        totalSales = new JLabel();
        totalSales.setFont(new Font("Segoe UI",Font.BOLD,16));
        totalSales.setForeground(new Color(0xC83F70));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(totalSales);
        add(bottom,BorderLayout.SOUTH);

        load();
    }

    private void load() {
        DefaultTableModel m = (DefaultTableModel)table.getModel();
        m.setRowCount(0);
        String q = search.getText().trim().toLowerCase();
        double total = 0;
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate from;
        java.time.LocalDate to = today;
        String period = String.valueOf(periodFilter.getSelectedItem());
        if ("Weekly".equals(period)) {
            from = today.with(java.time.DayOfWeek.MONDAY);
        } else if ("Yearly".equals(period)) {
            from = today.withDayOfYear(1);
        } else {
            from = today.withDayOfMonth(1);
        }
        for(Order o:Store.orders) {
            if(!isSale(o)) continue;
            String selectedStatus = String.valueOf(statusFilter.getSelectedItem());
            if(!selectedStatus.equals("All Sales") && !o.status.equals(selectedStatus)) continue;
            java.time.LocalDate orderDate = parseOrderDate(o.date);
            if (orderDate == null || orderDate.isBefore(from) || orderDate.isAfter(to)) continue;
            String all = o.id + " " + o.customer + " " + o.date + " " + o.paymentMethod + " " + o.status;
            if(q.isEmpty() || all.toLowerCase().contains(q)) {
                m.addRow(new Object[]{o.id,o.customer,o.date,o.paymentMethod,o.status,Store.peso(o.total)});
                total += o.total;
            }
        }
        totalSales.setText("Total Sales: " + Store.peso(total));
    }

    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Sales Report as CSV");
        chooser.setSelectedFile(new java.io.File("sales_report_" + String.valueOf(periodFilter.getSelectedItem()).toLowerCase() + ".csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        java.io.File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new java.io.File(file.getParentFile(), file.getName() + ".csv");
        }

        try (java.io.Writer writer = new java.io.BufferedWriter(
                new java.io.OutputStreamWriter(new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8))) {
            writer.write("\uFEFF");
            for (int column = 0; column < table.getColumnCount(); column++) {
                if (column > 0) writer.write(",");
                writer.write(csvEscape(table.getColumnName(column)));
            }
            writer.write("\r\n");

            for (int row = 0; row < table.getRowCount(); row++) {
                for (int column = 0; column < table.getColumnCount(); column++) {
                    if (column > 0) writer.write(",");
                    Object value = table.getValueAt(row, column);
                    writer.write(csvEscape(value == null ? "" : value.toString()));
                }
                writer.write("\r\n");
            }
            writer.write("\r\n");
            writer.write(csvEscape(totalSales.getText()));
            writer.write("\r\n");
            JOptionPane.showMessageDialog(this, "Sales report exported successfully.", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(this, "Unable to export the sales report: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String csvEscape(String value) {
        String safe = value == null ? "" : value;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    private java.time.LocalDate parseOrderDate(String value) {
        if (value == null) return null;
        String[] patterns = {"MMM d, yyyy h:mm a", "MMM dd, yyyy h:mm a", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd"};
        for (String pattern : patterns) {
            try {
                java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(pattern, java.util.Locale.ENGLISH);
                format.setLenient(false);
                return format.parse(value.trim()).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            } catch (Exception ignored) { }
        }
        return null;
    }

    private boolean isSale(Order o) {
        return o.paymentStatus.equals("Paid") && (o.status.equals("Delivered") || o.status.equals("Return Rejected"));
    }
}
