package cherrycosmetics.customer;

import cherrycosmetics.data.Models.Product;
import cherrycosmetics.data.Store;
import javax.swing.*;
import java.awt.*;
import java.util.Comparator;

public class Products extends JPanel {
    JPanel list = new JPanel(new GridLayout(0, 3, 15, 15));
    JTextField search = new JTextField();
    JComboBox<String> category = new JComboBox<>();
    JComboBox<String> sort = new JComboBox<>(new String[]{"Sort: Default", "Name A-Z", "Price Low-High", "Price High-Low", "Stock High-Low"});

    public Products() {
        setLayout(new BorderLayout(15,15));
        setBackground(new Color(0xF7F7F9));
        setBorder(BorderFactory.createEmptyBorder(25,25,25,25));
        JPanel top = new JPanel(new BorderLayout(10,0));
        top.setOpaque(false);
        JLabel t = new JLabel("Products");
        t.setFont(new Font("Segoe UI",Font.BOLD,27));
        top.add(t,BorderLayout.WEST);
        JPanel filter = new JPanel(new GridLayout(1,3,8,0));
        filter.setOpaque(false);
        filter.add(search);
        category.addItem("All Categories");
        for (String name : cherrycosmetics.data.Database.loadCategories()) category.addItem(name);
        filter.add(category);
        filter.add(sort);
        top.add(filter,BorderLayout.CENTER);
        add(top,BorderLayout.NORTH);
        list.setOpaque(false);
        JScrollPane productScroll = new JScrollPane(list);
        productScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        productScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        productScroll.getVerticalScrollBar().setUnitIncrement(18);
        add(productScroll,BorderLayout.CENTER);
        search.addActionListener(e->load());
        category.addActionListener(e->load());
        sort.addActionListener(e->load());
        load();
    }

    private void load() {
        list.removeAll();
        String q=search.getText().trim().toLowerCase();
        String cat=String.valueOf(category.getSelectedItem());
        java.util.List<Product> shown = new java.util.ArrayList<>();
        for(Product p:Store.products) if((q.isEmpty()||p.name.toLowerCase().contains(q)||p.description.toLowerCase().contains(q))&&(cat.equals("All Categories")||p.category.equals(cat))) shown.add(p);
        switch(String.valueOf(sort.getSelectedItem())) {
            case "Name A-Z" -> shown.sort(Comparator.comparing(p->p.name.toLowerCase()));
            case "Price Low-High" -> shown.sort(Comparator.comparingDouble(Product::discountedPrice));
            case "Price High-Low" -> shown.sort(Comparator.comparingDouble(Product::discountedPrice).reversed());
            case "Stock High-Low" -> shown.sort(Comparator.comparingInt((Product p)->p.stock).reversed());
        }
        for(Product p:shown) list.add(card(p));
        list.revalidate();
        list.repaint();
    }

    private JPanel card(Product p) {
        JPanel c=new JPanel(new BorderLayout(8,8));
        c.setBackground(Color.WHITE);
        c.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JLabel pic=new JLabel();
        pic.setHorizontalAlignment(SwingConstants.CENTER);
        pic.setPreferredSize(new Dimension(190,155));
        image(pic,p.image,180,145);
        c.add(pic,BorderLayout.CENTER);
        JLabel n=new JLabel("<html><b>"+p.name+"</b><br>"+p.category+"<br>"+Store.peso(p.discountedPrice())+"<br>"+p.stockText()+"</html>");
        n.setFont(new Font("Segoe UI",Font.BOLD,14));
        c.add(n,BorderLayout.NORTH);
        JPanel actions=new JPanel(new GridLayout(1,2,6,0));
        actions.setOpaque(false);
        JButton details=button("Details",new Color(0xF2DCE4),new Color(0x9D3159));
        details.addActionListener(e->details(p));
        actions.add(details);
        JButton add=button(p.stock>0?"Add to Cart":"Out of Stock",new Color(0xC83F70),Color.WHITE);
        add.setEnabled(p.stock>0);
        add.addActionListener(e->add(p,1));
        actions.add(add);
        c.add(actions,BorderLayout.SOUTH);
        return c;
    }

    private void add(Product p,int qty){ if(Store.addToCart(p,qty)) JOptionPane.showMessageDialog(this,p.name+" added to cart.");
    }

    private void details(Product p) {
        JPanel root=new JPanel(new BorderLayout(14,0));
        root.setPreferredSize(new Dimension(650,400));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        JPanel photo=new JPanel(new BorderLayout());
        photo.setBackground(new Color(0xFAF5F7));
        photo.setBorder(BorderFactory.createLineBorder(new Color(0xE7DCE1)));
        photo.setPreferredSize(new Dimension(235,360));
        JLabel pic=new JLabel();
        pic.setHorizontalAlignment(SwingConstants.CENTER);
        image(pic,p.image,210,320);
        photo.add(pic,BorderLayout.CENTER);
        root.add(photo,BorderLayout.WEST);
        JPanel info=new JPanel();
        info.setBackground(Color.WHITE);
        info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));
        info.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        JLabel name=new JLabel(p.name);
        name.setFont(new Font("Segoe UI",Font.BOLD,24));
        name.setForeground(new Color(0x333039));
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(name);
        JLabel cat=new JLabel("Category: "+p.category);
        cat.setFont(new Font("Segoe UI",Font.PLAIN,13));
        cat.setForeground(new Color(0x9D3159));
        cat.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(cat);
        info.add(Box.createVerticalStrut(10));
        JPanel prices=new JPanel(new FlowLayout(FlowLayout.LEFT,7,0));
        prices.setOpaque(false);
        prices.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sell=new JLabel(Store.peso(p.discountedPrice()));
        sell.setFont(new Font("Segoe UI",Font.BOLD,22));
        sell.setForeground(new Color(0xC83F70));
        prices.add(sell);
        if(p.discount>0){JLabel old=new JLabel("<html><strike>"+Store.peso(p.price)+"</strike></html>");
        old.setForeground(new Color(0x88818A));
        prices.add(old);
        JLabel off=new JLabel((int)p.discount+"% OFF");
        off.setForeground(new Color(0xC83F70));
        prices.add(off);
        } info.add(prices);
        info.add(Box.createVerticalStrut(10));
        JLabel stock=new JLabel("Stock: "+p.stockText());
        stock.setFont(new Font("Segoe UI",Font.BOLD,13));
        stock.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(stock);
        info.add(Box.createVerticalStrut(6));
        JLabel dt=new JLabel("Description");
        dt.setFont(new Font("Segoe UI",Font.BOLD,13));
        dt.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(dt);
        info.add(Box.createVerticalStrut(4));
        JTextArea desc=new JTextArea(p.description);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setEditable(false);
        desc.setBackground(Color.WHITE);
        desc.setFont(new Font("Segoe UI",Font.PLAIN,13));
        JScrollPane descScroll=new JScrollPane(desc);
        descScroll.setBorder(BorderFactory.createLineBorder(new Color(0xEEE7EA)));
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(descScroll);
        info.add(Box.createVerticalStrut(10));
        JPanel action=new JPanel(new FlowLayout(FlowLayout.LEFT,7,0));
        action.setOpaque(false);
        action.setAlignmentX(Component.LEFT_ALIGNMENT);
        action.add(new JLabel("Quantity"));
        JSpinner qty=new JSpinner(new SpinnerNumberModel(1,1,Math.max(1,p.stock),1));
        qty.setPreferredSize(new Dimension(55,30));
        action.add(qty);
        JButton add=button("Add to Cart",new Color(0xC83F70),Color.WHITE);
        add.setEnabled(p.stock>0);
        add.addActionListener(e->{if(Store.addToCart(p,(int)qty.getValue())){JOptionPane.showMessageDialog(this,"Added to cart.");Window w=SwingUtilities.getWindowAncestor(root);if(w!=null)w.dispose();}});
        action.add(add);
        info.add(action);
        root.add(info,BorderLayout.CENTER);
        // Use a plain dialog with no extra bottom OK button; users can close it with the window X.
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Product Details", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(650, 400));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }


    private JButton button(String text,Color bg,Color fg){ JButton b=new JButton(text);
    b.setBackground(bg);
    b.setForeground(fg);
    b.setOpaque(true);
    b.setFocusPainted(false);
    return b;
    }
    private void image(JLabel label,byte[] bytes,int w,int h){ if(bytes==null||bytes.length==0){label.setText("No image");
    return;
    } ImageIcon s=new ImageIcon(bytes);
    if(s.getIconWidth()<=0||s.getIconHeight()<=0){label.setText("No image");
    return;
    } double scale=Math.min((double)w/s.getIconWidth(),(double)h/s.getIconHeight());
    int nw=Math.max(1,(int)(s.getIconWidth()*scale)),nh=Math.max(1,(int)(s.getIconHeight()*scale));
    label.setIcon(new ImageIcon(s.getImage().getScaledInstance(nw,nh,Image.SCALE_SMOOTH)));
    label.setText("");
    }
}
