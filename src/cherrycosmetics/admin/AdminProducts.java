package cherrycosmetics.admin;

import cherrycosmetics.data.Models.Product;
import cherrycosmetics.data.Store;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;

public class AdminProducts extends JPanel {
    JPanel list=new JPanel();
    JComboBox<String> category=new JComboBox<>();
    JTextField search=new JTextField();
    JComboBox<String> sort=new JComboBox<>(new String[]{"Sort: Default","Name A-Z","Price Low-High","Price High-Low","Stock Low-High","Stock High-Low"});
    
    public AdminProducts(){
        setLayout(new BorderLayout(15,15));
        setBackground(new Color(0xF7F7F9));
        setBorder(BorderFactory.createEmptyBorder(25,25,25,25));
        JPanel top=new JPanel(new BorderLayout(10,10));
        top.setOpaque(false);
        JLabel t=new JLabel("Products");
        t.setFont(new Font("Segoe UI",Font.BOLD,27));
        top.add(t,BorderLayout.WEST);
        JPanel filters=new JPanel(new GridLayout(1,4,8,0));
        filters.setOpaque(false);
        filters.add(search);
        category.addItem("All Categories");
        for(String c:cherrycosmetics.data.Database.loadCategories())category.addItem(c);
        filters.add(category);
        filters.add(sort);
        JButton add=button("Add Product",new Color(0xC83F70),Color.WHITE);
        add.addActionListener(e->form(null));
        filters.add(add);
        top.add(filters,BorderLayout.EAST);
        add(top,BorderLayout.NORTH);
        list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));
        list.setOpaque(false);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        add(scroll,BorderLayout.CENTER);
        search.addActionListener(e->load());
        category.addActionListener(e->load());
        sort.addActionListener(e->load());
        load();
    }
    private JButton button(String text,Color bg,Color fg){JButton b=new JButton(text);
    b.setBackground(bg);
    b.setForeground(fg);
    b.setFocusPainted(false);
    b.setOpaque(true);
    return b;
    }
    private void load(){
        list.removeAll();
        String q=search.getText().trim().toLowerCase(),cat=String.valueOf(category.getSelectedItem());
        java.util.List<Product> shown=new ArrayList<>();
        for(Product p:Store.products){boolean text=q.isEmpty()||p.name.toLowerCase().contains(q)||p.description.toLowerCase().contains(q);
        boolean group=cat.equals("All Categories")||p.category.equals(cat);
        if(text&&group)shown.add(p);
        }
        switch(String.valueOf(sort.getSelectedItem())){case "Name A-Z"->shown.sort(Comparator.comparing((Product p)->p.name.toLowerCase()));
        case "Price Low-High"->shown.sort(Comparator.comparingDouble(Product::discountedPrice));
        case "Price High-Low"->shown.sort(Comparator.comparingDouble(Product::discountedPrice).reversed());
        case "Stock Low-High"->shown.sort(Comparator.comparingInt((Product p)->p.stock));
        case "Stock High-Low"->shown.sort(Comparator.comparingInt((Product p)->p.stock).reversed());
        }
        for(Product p:shown)list.add(row(p));
        list.revalidate();
        list.repaint();
    }
    private JPanel row(Product p){
        JPanel r=new JPanel(new BorderLayout(12,0));
        r.setBackground(Color.WHITE);
        r.setBorder(BorderFactory.createEmptyBorder(12,15,12,15));
        JLabel pic=new JLabel();
        pic.setHorizontalAlignment(SwingConstants.CENTER);
        pic.setPreferredSize(new Dimension(70,55));
        setImage(pic,p.image,65,50);
        r.add(pic,BorderLayout.WEST);
        JLabel n=new JLabel("<html><b>"+p.name+"</b><br>"+p.category+" · "+Store.peso(p.discountedPrice())+" · "+p.stockText()+"</html>");
        r.add(n,BorderLayout.CENTER);
        JPanel a=new JPanel(new FlowLayout(FlowLayout.RIGHT,5,5));
        a.setOpaque(false);
        JButton edit=button("Edit",new Color(0xF2DCE4),new Color(0x9D3159));
        edit.addActionListener(e->form(p));
        a.add(edit);
        JButton restock=button("Restock",new Color(0xF2DCE4),new Color(0x9D3159));
        restock.addActionListener(e->restock(p));
        a.add(restock);
        JButton archive=button("Archive",new Color(0xF8E4E4),new Color(0xB33434));
        archive.addActionListener(e->{if(JOptionPane.showConfirmDialog(this,"Archive "+p.name+"? The record will remain in the database.","Confirm Archive",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){if(Store.deleteProduct(p))load();else JOptionPane.showMessageDialog(this,"Unable to archive the product.");}});
        a.add(archive);
        r.add(a,BorderLayout.EAST);
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE,82));
        return r;
    }
    private void details(Product p){JPanel box=new JPanel(new BorderLayout(12,10));
    JLabel pic=new JLabel();
    pic.setPreferredSize(new Dimension(190,160));
    pic.setHorizontalAlignment(SwingConstants.CENTER);
    setImage(pic,p.image,180,150);
    box.add(pic,BorderLayout.WEST);
    JTextArea info=new JTextArea("Name: "+p.name+"\nCategory: "+p.category+"\nPrice: "+Store.peso(p.price)+"\nSelling Price: "+Store.peso(p.discountedPrice())+"\nStock: "+p.stockText()+"\nDiscount: "+p.discount+"%\n\n"+p.description);
    info.setEditable(false);
    info.setLineWrap(true);
    info.setWrapStyleWord(true);
    info.setOpaque(false);
    box.add(info,BorderLayout.CENTER);
    JOptionPane.showMessageDialog(this,box,"Product Details",JOptionPane.INFORMATION_MESSAGE);
    }
    private void form(Product product){boolean edit=product!=null;
    JTextField name=new JTextField(edit?product.name:"");
    JComboBox<String> cat=new JComboBox<>(cherrycosmetics.data.Database.loadCategories().toArray(new String[0]));
    if(edit)cat.setSelectedItem(product.category);
    JTextField price=new JTextField(edit?String.valueOf(product.price):"");
    JTextField stock=new JTextField(edit?String.valueOf(product.stock):"");
    if(edit){stock.setEditable(false);
    stock.setBackground(new Color(0xF1EEF0));
    stock.setToolTipText("Stock is managed using the Restock button.");
    }JTextField discount=new JTextField(edit?String.valueOf(product.discount):"0");
    JTextArea desc=new JTextArea(edit?product.description:"",3,20);
    JLabel preview=new JLabel("No image selected",SwingConstants.CENTER);
    preview.setPreferredSize(new Dimension(180,130));
    preview.setBorder(BorderFactory.createLineBorder(new Color(0xD0C8CD)));
    byte[][] image={edit?product.image:null};
    setImage(preview,image[0],160,110);
        JButton choose=button("Choose Photo",new Color(0xF2DCE4),new Color(0x9D3159));
        choose.addActionListener(e->{JFileChooser fc=new JFileChooser();if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION)try{image[0]=Files.readAllBytes(fc.getSelectedFile().toPath());setImage(preview,image[0],160,110);}catch(Exception ex){JOptionPane.showMessageDialog(this,"Unable to read image.");}});
        JPanel p=new JPanel(new GridBagLayout());
        GridBagConstraints g=new GridBagConstraints();
        g.insets=new Insets(5,5,5,5);
        g.fill=GridBagConstraints.HORIZONTAL;
        g.weightx=1;
        addField(p,g,0,"Name",name);
        addField(p,g,1,"Category",cat);
        addField(p,g,2,"Price",price);
        addField(p,g,3,"Stock",stock);
        addField(p,g,4,"Discount %",discount);
        addField(p,g,5,"Description",new JScrollPane(desc));
        g.gridx=0;
        g.gridy=6;
        g.gridwidth=2;
        p.add(preview,g);
        g.gridy=7;
        p.add(choose,g);
        int ok=JOptionPane.showConfirmDialog(this,p,edit?"Edit Product":"Add Product",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
        if(ok!=JOptionPane.OK_OPTION)return;
        try{String n=name.getText().trim(),c=String.valueOf(cat.getSelectedItem());
        double pr=Double.parseDouble(price.getText().trim());
        int st=Integer.parseInt(stock.getText().trim());
        double dis=Double.parseDouble(discount.getText().trim());
        if(n.isEmpty()||pr<0||st<0||dis<0||dis>100){JOptionPane.showMessageDialog(this,"Please enter valid product values.");
        return;
        }if(edit)Store.editProduct(product,n,c,pr,product.stock,desc.getText().trim(),image[0],dis);
        else Store.addProduct(n,c,pr,st,desc.getText().trim(),image[0],dis);
        load();
        }catch(Exception ex){JOptionPane.showMessageDialog(this,"Please check the product values.");
        }}
    private void addField(JPanel p,GridBagConstraints g,int y,String label,Component c){g.gridx=0;
    g.gridy=y;
    g.gridwidth=1;
    p.add(new JLabel(label),g);
    g.gridx=1;
    g.gridwidth=2;
    p.add(c,g);
    }
    private void restock(Product p){String s=JOptionPane.showInputDialog(this,"Add stock for "+p.name+":","Restock",JOptionPane.PLAIN_MESSAGE);
    if(s==null)return;
    try{int q=Integer.parseInt(s.trim());
    if(!Store.restockProduct(p,q))JOptionPane.showMessageDialog(this,"Enter a quantity greater than zero.");
    load();
    }catch(Exception e){JOptionPane.showMessageDialog(this,"Enter a valid number.");
    }}
    private void setImage(JLabel label,byte[] bytes,int w,int h){if(bytes==null||bytes.length==0){label.setIcon(null);
    if(label.getText().isEmpty())label.setText("No image selected");
    return;
    }ImageIcon source=new ImageIcon(bytes);
    if(source.getIconWidth()<=0||source.getIconHeight()<=0){label.setText("No image");
    return;
    }double scale=Math.min((double)w/source.getIconWidth(),(double)h/source.getIconHeight());
    int nw=Math.max(1,(int)(source.getIconWidth()*scale)),nh=Math.max(1,(int)(source.getIconHeight()*scale));
    label.setText("");
    label.setIcon(new ImageIcon(source.getImage().getScaledInstance(nw,nh,Image.SCALE_SMOOTH)));
    }
}
