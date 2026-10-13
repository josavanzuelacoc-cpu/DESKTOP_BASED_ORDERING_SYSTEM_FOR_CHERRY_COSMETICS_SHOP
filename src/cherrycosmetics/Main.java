package cherrycosmetics;

import cherrycosmetics.data.Database;
import cherrycosmetics.data.Store;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        Database.initialize();
        Store.seed();
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}
