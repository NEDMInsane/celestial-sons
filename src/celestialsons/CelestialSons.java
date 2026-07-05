
package celestialsons;

import javax.swing.SwingUtilities;

public class CelestialSons {
    
    static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameServer server = new GameServer();
            GameClient client = new GameClient(server);
            client.show();
        });
    }


    public static void gameLoop(){
        GameServer server = new GameServer();
        GameClient client = new GameClient(server);
        client.show();
    }
}
