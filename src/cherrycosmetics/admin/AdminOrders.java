package cherrycosmetics.admin;

import cherrycosmetics.data.Models.Order;
import cherrycosmetics.data.Store;
import javax.swing.*;
import java.awt.*;

public class AdminOrders extends JPanel {
    JPanel list=new JPanel();
    JTextField search=new JTextField();
    JComboBox<String> filter=new JComboBox<>(new String[]{"All Orders","Pending","Confirmed","Preparing","Shipped","Delivered","Cancelled","Return Requested","Return Approved","Return Rejected"});
    public AdminOrders(){setLayout(new BorderLayout(15,15));
    setBackground(new Color(0xF7F7F9));
    setBorder(BorderFactory.createEmptyBorder(25,25,25,25));
    JPanel top=new JPanel(new BorderLayout(10,0));
    top.setOpaque(false);
    JLabel t=new JLabel("Orders");
    t.setFont(new Font("Segoe UI",Font.BOLD,27));
    top.add(t,BorderLayout.WEST);
    JPanel f=new JPanel(new GridLayout(1,2,8,0));
    f.setOpaque(false);
    f.add(search);
    f.add(filter);
    top.add(f,BorderLayout.EAST);
    add(top,BorderLayout.NORTH);
    list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));
    list.setOpaque(false);
    JScrollPane scroll = new JScrollPane(list);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scroll.getVerticalScrollBar().setUnitIncrement(18);
    add(scroll,BorderLayout.CENTER);
    search.addActionListener(e->load());
    filter.addActionListener(e->load());
    load();
    }
    private void load(){list.removeAll();
    String q=search.getText().trim().toLowerCase(),st=String.valueOf(filter.getSelectedItem());
    for(Order o:Store.orders){boolean text=q.isEmpty()||o.customer.toLowerCase().contains(q)||String.valueOf(o.id).contains(q)||o.email.toLowerCase().contains(q);
    boolean status=st.equals("All Orders")||o.status.equals(st);
    if(text&&status)list.add(row(o));
    }list.revalidate();
    list.repaint();
    }
    private JPanel row(Order o){JPanel p=new JPanel(new BorderLayout(10,0));
    p.setBackground(Color.WHITE);
    p.setBorder(BorderFactory.createEmptyBorder(12,15,12,15));
    String pay=o.paymentMethod.equals("GCash")?"GCash · "+o.paymentStatus+(o.paymentReference.isBlank()?"":" · Ref: "+o.paymentReference):o.paymentMethod+" · "+o.paymentStatus;
    JLabel info=new JLabel("<html><b>Order #"+o.id+"</b> · "+o.customer+" · "+Store.peso(o.total)+"<br>"+o.status+" · "+pay+" · "+o.date+"</html>");
    p.add(info,BorderLayout.CENTER);
    JPanel a=new JPanel(new FlowLayout(FlowLayout.RIGHT,5,5));
    a.setOpaque(false);
    JButton view=button("View",new Color(0xF2DCE4),new Color(0x9D3159));
    view.addActionListener(e->details(o));
    a.add(view);
    String[] next=allowedNext(o.status);
    if(next.length>0){JComboBox<String> box=new JComboBox<>(next);
    JButton update=button("Update",new Color(0xC83F70),Color.WHITE);
    update.addActionListener(e->{if(Store.updateOrderStatus(o,String.valueOf(box.getSelectedItem())))load();else JOptionPane.showMessageDialog(this,"Unable to update the order status.");});
    a.add(box);
    a.add(update);
    }p.add(a,BorderLayout.EAST);
    p.setMaximumSize(new Dimension(Integer.MAX_VALUE,95));
    return p;
    }
    private String[] allowedNext(String status){return switch(status){case "Pending"->new String[]{"Confirmed","Cancelled"};
    case "Confirmed"->new String[]{"Preparing","Cancelled"};
    case "Preparing"->new String[]{"Shipped"};
    case "Return Requested"->new String[]{"Return Approved","Return Rejected"};
    default->new String[]{};
    };
    }
    private void details(Order o){StringBuilder s=new StringBuilder("Order #").append(o.id).append("\nCustomer: ").append(o.customer).append("\nEmail: ").append(o.email).append("\nLocation: ").append(o.location).append("\nContact: ").append(o.contact).append("\nDate: ").append(o.date).append("\nStatus: ").append(o.status).append("\nPayment: ").append(o.paymentMethod).append(" (").append(o.paymentStatus).append(")");
    if(!o.paymentReference.isBlank())s.append("\nReference Number: ").append(o.paymentReference);
    s.append("\n\n");
    for(var i:o.items)s.append(i.name).append(" x").append(i.qty).append(" - ").append(Store.peso(i.price)).append("\n");
    s.append("\nTotal: ").append(Store.peso(o.total));
    if(!o.returnReason.isBlank())s.append("\nReturn reason: ").append(o.returnReason);
    if(!o.receivedDate.isBlank())s.append("\nReceived: ").append(o.receivedDate);
    JOptionPane.showMessageDialog(this,s.toString(),"Order Details",JOptionPane.INFORMATION_MESSAGE);
    }
    private JButton button(String text,Color bg,Color fg){JButton b=new JButton(text);
    b.setBackground(bg);
    b.setForeground(fg);
    b.setOpaque(true);
    b.setFocusPainted(false);
    return b;
    }
}
