
package celestialsons;

import javax.swing.SwingUtilities;

public class CelestialSons {
    
    public static void main(String[] args) {
        gameLoop();
    }


    public static void gameLoop(){
        GameServer server = new GameServer();
        GameClient client = new GameClient(server);
        client.show();
    }
}
