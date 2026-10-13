package cherrycosmetics.customer;

import cherrycosmetics.MainFrame;
import cherrycosmetics.data.Models.*;
import cherrycosmetics.data.Store;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Cart extends JPanel {
    JPanel list=new JPanel();
    JLabel total=new JLabel();
    JTextField address=new JTextField();
    JTextField contact=new JTextField();
    JComboBox<String> payment=new JComboBox<>(new String[]{"Cash on Delivery","GCash"});
    JTextField reference=new JTextField();
    JLabel qr=new JLabel();
    Color pink=new Color(0xC83F70), light=new Color(0xF7F7F9), border=new Color(0xE7DCE1);

    public Cart(){
        setLayout(new BorderLayout(15,15));
        setBackground(light);
        setBorder(new EmptyBorder(25,30,25,30));
        JLabel t=new JLabel("Cart");
        t.setFont(new Font("Segoe UI",Font.BOLD,27));
        add(t,BorderLayout.NORTH);
        list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));
        list.setOpaque(false);
        add(new JScrollPane(list),BorderLayout.CENTER);
        JPanel bottom=new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        total.setFont(new Font("Segoe UI",Font.BOLD,18));
        bottom.add(total,BorderLayout.WEST);
        JButton checkout=button("Checkout",pink,Color.WHITE);
        checkout.addActionListener(e->checkout());
        bottom.add(checkout,BorderLayout.EAST);
        add(bottom,BorderLayout.SOUTH);
        load();
    }
    private void updateCartTotal() {
        total.setText("Total: " + Store.peso(Store.cartTotal()));
        total.revalidate();
        total.repaint();
    }

    private void load(){
        list.removeAll();
        for(CartItem c:Store.cart) list.add(row(c));
        updateCartTotal();
        list.revalidate();
        list.repaint();
    }
    private JPanel row(CartItem c){
        JPanel p=new JPanel(new BorderLayout(10,0));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(14,16,14,16));
        Product product=Store.findProduct(c.productId);
        JLabel l=new JLabel();
        Runnable refreshAmounts = () -> {
            l.setText("<html><b>"+c.name+"</b><br>"
                    + Store.peso(c.price) + " each<br>Subtotal: "
                    + Store.peso(c.price * c.qty) + "</html>");
            updateCartTotal();
            l.revalidate();
            l.repaint();
        };
        refreshAmounts.run();
        p.add(l,BorderLayout.CENTER);
        JPanel a=new JPanel(new FlowLayout(FlowLayout.RIGHT,5,5));
        a.setOpaque(false);
        int max=product==null?Math.max(1,c.qty):Math.max(1,product.stock);
        JSpinner qty=new JSpinner(new SpinnerNumberModel(c.qty,1,max,1));
        qty.addChangeListener(e -> {
            // Keep the model in sync with the visible spinner before recalculating.
            Object value = qty.getValue();
            c.qty = (value instanceof Number) ? ((Number) value).intValue() : c.qty;
            refreshAmounts.run();
        });
        JComponent editor = qty.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().addActionListener(e -> {
                try { qty.commitEdit();
                } catch (java.text.ParseException ignored) {}
                Object value = qty.getValue();
                c.qty = (value instanceof Number) ? ((Number) value).intValue() : c.qty;
                refreshAmounts.run();
            });
        }
        JButton cancel=button("Remove",new Color(0xF8E4E4),new Color(0xB33434));
        cancel.addActionListener(e->{Store.cart.remove(c);load();});
        a.add(new JLabel("Qty"));
        a.add(qty);
        a.add(cancel);
        p.add(a,BorderLayout.EAST);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE,90));
        return p;
    }
    private void checkout(){
        if(Store.cart.isEmpty()){JOptionPane.showMessageDialog(this,"Your cart is empty.");
        return;
        }
        for(CartItem c:Store.cart){
            Product p=Store.findProduct(c.productId);
            if(p==null||c.qty>p.stock){
                JOptionPane.showMessageDialog(this,"Please update your cart because some stock changed.");
                load();
                return;
            }
        }

        // Main checkout area: fixed, balanced columns so the form never stretches
        // beyond the dialog or creates an awkward horizontal/vertical scrollbar.
        JPanel main=new JPanel(new BorderLayout(16,0));
        main.setBackground(new Color(0xF8F5F7));
        main.setBorder(new EmptyBorder(10,10,8,10));
        main.setPreferredSize(new Dimension(760,500));

        // LEFT: order summary
        JPanel left=new JPanel(new BorderLayout(0,8));
        left.setBackground(Color.WHITE);
        left.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                new EmptyBorder(16,16,14,16)));
        left.setPreferredSize(new Dimension(250,480));

        JLabel summaryTitle=new JLabel("Order Summary");
        summaryTitle.setFont(new Font("Segoe UI",Font.BOLD,19));
        summaryTitle.setForeground(new Color(0x332D33));
        left.add(summaryTitle,BorderLayout.NORTH);

        JPanel items=new JPanel();
        items.setBackground(Color.WHITE);
        items.setLayout(new BoxLayout(items,BoxLayout.Y_AXIS));
        for(int i=0;i<Store.cart.size();i++){
            CartItem c=Store.cart.get(i);
            JPanel item=new JPanel(new BorderLayout(8,0));
            item.setOpaque(false);
            item.setBorder(new EmptyBorder(12,0,12,0));

            JLabel name=new JLabel("<html><b>"+c.name+"</b><br><span style='color:#77717A'>Qty: "+c.qty+"</span></html>");
            JLabel amount=new JLabel(Store.peso(c.subtotal()));
            amount.setFont(new Font("Segoe UI",Font.BOLD,12));
            amount.setHorizontalAlignment(SwingConstants.RIGHT);
            item.add(name,BorderLayout.CENTER);
            item.add(amount,BorderLayout.EAST);
            items.add(item);
            if(i<Store.cart.size()-1){
                JSeparator sep=new JSeparator();
                sep.setForeground(border);
                items.add(sep);
            }
        }
        JScrollPane summaryScroll=new JScrollPane(items);
        summaryScroll.setBorder(BorderFactory.createEmptyBorder());
        summaryScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        summaryScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        summaryScroll.getVerticalScrollBar().setUnitIncrement(14);
        left.add(summaryScroll,BorderLayout.CENTER);

        JPanel totalPanel=new JPanel(new BorderLayout());
        totalPanel.setOpaque(false);
        totalPanel.setBorder(new EmptyBorder(10,0,0,0));
        JLabel totalLabel=new JLabel("Total");
        totalLabel.setFont(new Font("Segoe UI",Font.BOLD,14));
        JLabel totalValue=new JLabel(Store.peso(Store.cartTotal()));
        totalValue.setFont(new Font("Segoe UI",Font.BOLD,20));
        totalValue.setForeground(pink);
        totalPanel.add(totalLabel,BorderLayout.WEST);
        totalPanel.add(totalValue,BorderLayout.EAST);
        left.add(totalPanel,BorderLayout.SOUTH);
        main.add(left,BorderLayout.WEST);

        // RIGHT: one clean vertical form. GridBagLayout keeps every field aligned.
        JPanel right=new JPanel(new GridBagLayout());
        right.setBackground(Color.WHITE);
        right.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                new EmptyBorder(18,20,16,20)));
        right.setPreferredSize(new Dimension(470,560));

        GridBagConstraints g=new GridBagConstraints();
        g.gridx=0;
        g.weightx=1;
        g.fill=GridBagConstraints.HORIZONTAL;
        g.anchor=GridBagConstraints.WEST;
        g.insets=new Insets(0,0,0,0);

        JLabel title=new JLabel("Checkout");
        title.setFont(new Font("Segoe UI",Font.BOLD,22));
        title.setForeground(new Color(0x332D33));
        g.gridy=0;
        g.gridwidth=1;
        g.insets=new Insets(0,0,2,0);
        right.add(title,g);

        JLabel subtitle=new JLabel("Enter your delivery details and choose a payment method.");
        subtitle.setFont(new Font("Segoe UI",Font.PLAIN,12));
        subtitle.setForeground(new Color(0x77717A));
        g.gridy=1;
        g.insets=new Insets(0,0,14,0);
        right.add(subtitle,g);

        JLabel delivery=section("1  Delivery Information");
        delivery.setBorder(new EmptyBorder(0,0,8,0));
        g.gridy=2;
        g.insets=new Insets(0,0,6,0);
        right.add(delivery,g);

        JLabel addressLabel=label("Delivery Address");
        g.gridy=3;
        g.insets=new Insets(0,0,4,0);
        right.add(addressLabel,g);
        style(address);
        address.setPreferredSize(new Dimension(440,36));
        address.setMinimumSize(new Dimension(0,36));
        address.setMaximumSize(new Dimension(440,36));
        g.gridy=4;
        g.insets=new Insets(0,0,10,0);
        right.add(address,g);

        JLabel contactLabel=label("Contact Number");
        g.gridy=5;
        g.insets=new Insets(0,0,4,0);
        right.add(contactLabel,g);
        style(contact);
        contact.setPreferredSize(new Dimension(440,36));
        contact.setMinimumSize(new Dimension(0,36));
        contact.setMaximumSize(new Dimension(440,36));
        g.gridy=6;
        g.insets=new Insets(0,0,14,0);
        right.add(contact,g);

        JLabel paymentTitle=section("2  Payment");
        paymentTitle.setBorder(new EmptyBorder(0,0,8,0));
        g.gridy=7;
        g.insets=new Insets(0,0,6,0);
        right.add(paymentTitle,g);

        JLabel paymentLabel=label("Payment Method");
        g.gridy=8;
        g.insets=new Insets(0,0,4,0);
        right.add(paymentLabel,g);
        payment.setPreferredSize(new Dimension(440,36));
        payment.setMinimumSize(new Dimension(0,36));
        payment.setMaximumSize(new Dimension(440,36));
        g.gridy=9;
        g.insets=new Insets(0,0,8,0);
        right.add(payment,g);

        JPanel gcashPanel=new JPanel(new GridBagLayout());
        gcashPanel.setOpaque(false);
        gcashPanel.setBorder(new EmptyBorder(0,0,0,0));
        GridBagConstraints cg=new GridBagConstraints();
        cg.gridx=0;
        cg.weightx=1;
        cg.fill=GridBagConstraints.HORIZONTAL;
        cg.anchor=GridBagConstraints.WEST;
        JLabel hint=new JLabel("For GCash, enter the payment reference number:");
        hint.setFont(new Font("Segoe UI",Font.PLAIN,11));
        hint.setForeground(new Color(0x77717A));
        cg.gridy=0;
        cg.insets=new Insets(0,0,4,0);
        gcashPanel.add(hint,cg);

        style(reference);
        reference.setPreferredSize(new Dimension(440,36));
        reference.setMinimumSize(new Dimension(0,36));
        reference.setMaximumSize(new Dimension(440,36));
        cg.gridy=1;
        cg.insets=new Insets(0,0,8,0);
        gcashPanel.add(reference,cg);

        qr.setHorizontalAlignment(SwingConstants.CENTER);
        qr.setPreferredSize(new Dimension(150,150));
        qr.setMaximumSize(new Dimension(150,150));
        loadQr();
        JPanel qrWrap=new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
        qrWrap.setOpaque(false);
        qrWrap.add(qr);
        cg.gridy=2;
        cg.fill=GridBagConstraints.HORIZONTAL;
        cg.insets=new Insets(0,0,0,0);
        gcashPanel.add(qrWrap,cg);
        gcashPanel.setVisible(false);

        g.gridy=10;
        g.insets=new Insets(0,0,4,0);
        right.add(gcashPanel,g);

        JLabel noteText=new JLabel("Your order will be submitted for confirmation after checkout.");
        noteText.setFont(new Font("Segoe UI",Font.PLAIN,11));
        noteText.setForeground(new Color(0x77717A));
        g.gridy=11;
        g.insets=new Insets(8,0,0,0);
        right.add(noteText,g);

        JScrollPane checkoutScroll=new JScrollPane(right);
        checkoutScroll.setBorder(BorderFactory.createEmptyBorder());
        checkoutScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        checkoutScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        checkoutScroll.getVerticalScrollBar().setUnitIncrement(16);
        checkoutScroll.getVerticalScrollBar().setPreferredSize(new Dimension(12,0));
        checkoutScroll.setPreferredSize(new Dimension(494,480));
        main.add(checkoutScroll,BorderLayout.CENTER);

        // Reset the shared payment selector and reveal only the controls that apply.
        for(java.awt.event.ActionListener al:payment.getActionListeners()) payment.removeActionListener(al);
        payment.setSelectedItem("Cash on Delivery");
        reference.setText("");
        gcashPanel.setVisible(false);
        payment.addActionListener(e->{
            boolean gCash="GCash".equals(payment.getSelectedItem());
            gcashPanel.setVisible(gCash);
            if(!gCash) reference.setText("");
            right.revalidate();
            right.repaint();
            checkoutScroll.revalidate();
            checkoutScroll.getVerticalScrollBar().setValue(0);
        });

        int ok=JOptionPane.showConfirmDialog(this,main,"Checkout",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
        if(ok!=JOptionPane.OK_OPTION)return;

        String loc=address.getText().trim(),con=contact.getText().trim(),method=String.valueOf(payment.getSelectedItem()),ref=reference.getText().trim();
        if(loc.isEmpty()||con.isEmpty()){
            JOptionPane.showMessageDialog(this,"Please enter your address and contact number.");
            return;
        }
        if(method.equals("GCash")&&ref.isEmpty()){
            JOptionPane.showMessageDialog(this,"Please enter the GCash reference number.");
            return;
        }
        String status=method.equals("GCash")?"For Approval":"Unpaid";
        Order o=Store.createOrder(loc,con,method,status,ref);
        if(o==null){
            JOptionPane.showMessageDialog(this,"Unable to place the order. Please check the stock.");
            return;
        }
        JOptionPane.showMessageDialog(this,method.equals("GCash")
                ?"Order #"+o.id+" placed. Your GCash payment is waiting for admin approval."
                :"Order #"+o.id+" placed successfully. It is waiting for admin approval.");
        MainFrame.getInstance().showPage("MyOrders");
    }
    private JLabel section(String s){JLabel l=new JLabel(s);
    l.setFont(new Font("Segoe UI",Font.BOLD,17));
    l.setForeground(new Color(0x333039));
    l.setBorder(new EmptyBorder(0,0,10,0));
    return l;
    }
    private JLabel label(String s){JLabel l=new JLabel(s);
    l.setFont(new Font("Segoe UI",Font.BOLD,12));
    l.setBorder(new EmptyBorder(4,0,5,0));
    return l;
    }
    private void style(JTextField f){f.setMaximumSize(new Dimension(Integer.MAX_VALUE,38));
    f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0xD0C8CD)),new EmptyBorder(7,9,7,9)));
    }
    private void loadQr(){try(java.io.InputStream in=getClass().getResourceAsStream("/cherrycosmetics/images/gcash_qr.png")){if(in!=null){byte[] b=in.readAllBytes();
    ImageIcon x=new ImageIcon(b);
    qr.setIcon(new ImageIcon(x.getImage().getScaledInstance(150,150,Image.SCALE_SMOOTH)));
    }}catch(Exception ignored){}}
    private JButton button(String text,Color bg,Color fg){JButton b=new JButton(text);
    b.setBackground(bg);
    b.setForeground(fg);
    b.setOpaque(true);
    b.setFocusPainted(false);
    return b;
    }
}
