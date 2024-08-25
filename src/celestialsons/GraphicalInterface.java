
package celestialsons;

import java.io.File;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.JOptionPane;


public class GraphicalInterface {
    private JFrame frame;
    private String name = "";
    private int width = 800;
    private int height = 600;
    private Universe universe = null;

    private NonPlayerCharacter[] nonPlayerCharacters;
    private PlayerCharacter[] playerCharacters;
    
    public GraphicalInterface(){
        this.frame = new JFrame();
        
        this.frame.setSize(this.width, this.height);
        this.frame.setLayout(null);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public void mainMenu(Universe universe,NonPlayerCharacter[] nonPlayerCharacters, PlayerCharacter[] playerCharacters){
        this.name = "Celestial Sons";
        this.height = 400;
        this.width = 300;
        
        JLabel titleLabel = new JLabel("Celestial Sons");
        JButton debugMenu = new JButton("Debug Menu");
        JButton newGame = new JButton("New Game");
        JButton loadGame = new JButton("Load Game");
        JButton quitGame = new JButton("Quit");
        
        titleLabel.setBounds((this.width / 2) - 50, 25, 83,25);

        debugMenu.addActionListener(event -> createDebugMenu(universe, nonPlayerCharacters, playerCharacters));
        debugMenu.setBounds((this.width / 2) - 83, 55, 150, 25);
        
        newGame.addActionListener(event -> startNewGame());
        newGame.setBounds((this.width / 2) - 83, 85, 150, 25);
        
        loadGame.addActionListener(event -> {
            try {
                loadGameMenu();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        loadGame.setBounds((this.width / 2) - 83, 125, 150, 25);
        
        //This is a lambda expression
        quitGame.addActionListener(event -> System.exit(0));
        quitGame.setBounds((this.width / 2) - 83, 155, 150, 25);
        
        this.frame.setTitle(this.name);
        this.frame.setSize(this.width, this.height);
        this.frame.add(titleLabel);
        this.frame.add(newGame);
        this.frame.add(debugMenu);
        this.frame.add(loadGame);
        this.frame.add(quitGame);
        this.frame.setVisible(true);
    }

    private void createDebugMenu(Universe universe,NonPlayerCharacter[] nonPlayerCharacters, PlayerCharacter[] playerCharacters) {
        this.universe = universe;
        this.nonPlayerCharacters = nonPlayerCharacters;
        this.playerCharacters = playerCharacters;
        JFrame menu = new JFrame("Debug Menu");
        menu.setSize(this.width, this.height);
        menu.setLayout(new FlowLayout());
        menu.setVisible(true);
        frame.setVisible(false);
        menu.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                frame.setVisible(true);
                super.windowClosed(e);
            }
        });
        JLabel titleLabel = new JLabel("!!!DEBUG MENU!!!");
        menu.add(titleLabel);

        JButton characterMenu = new JButton("Character Menu");
        characterMenu.addActionListener(event -> characterMenu());

        JButton universeMenu = new JButton("Universe Menu");
        universeMenu.addActionListener(event -> universeMenu());

        menu.add(characterMenu);
        menu.add(universeMenu);
        menu.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private void universeMenu() {
        JFrame universeFrame = new JFrame("Universe Menu");
        universeFrame.setSize(this.width, this.height);
        universeFrame.setLayout(new FlowLayout());
        universeFrame.setVisible(true);
        universeFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private void characterMenu() {
        JFrame charFrame = new JFrame("Character Menu");
        final PlayerCharacter[] selectedPlayerCharacter = new PlayerCharacter[1];
        charFrame.setSize(this.width, this.height);
        charFrame.setLayout(new FlowLayout());
        JComboBox<PlayerCharacter> charSelector = new JComboBox<>(this.playerCharacters);
        charFrame.add(charSelector);

        JLabel nameLabel = new JLabel("Name: Select Character\t");
        JLabel levelLabel = new JLabel("Level:\t");
        JLabel charLocation = new JLabel("Location:\t");
        JLabel charSkills = new JLabel("Skills:\t");
        charSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(e.getActionCommand().equals("comboBoxChanged")){
                    selectedPlayerCharacter[0] = (PlayerCharacter) charSelector.getSelectedItem();
                    String labelText = String.format("Name:\t%s", selectedPlayerCharacter[0].getName());
                    nameLabel.setText(labelText);
                    labelText = String.format("Level:\t%d", selectedPlayerCharacter[0].getLevel());
                    levelLabel.setText(labelText);
                    labelText = String.format("Location:\t%s", selectedPlayerCharacter[0].getCurrentSystem());
                    charLocation.setText(labelText);
                    labelText = String.format("Skills:\t%s", selectedPlayerCharacter[0].getSkills());
                    charSkills.setText(labelText);
                }
            }
        });

        JButton changeName = new JButton("Change Name");
        JTextField changeNameField = new JTextField("Enter new name.");
        changeName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedPlayerCharacter[0].setName(changeNameField.getText());
                String labelText = String.format("Name:\t%s", selectedPlayerCharacter[0].getName());
                nameLabel.setText(labelText);
            }
        });

        JButton incrementLevel = new JButton("Increment Level");
        incrementLevel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedPlayerCharacter[0].increaseLevel();
                String labelText = String.format("Level:\t%d", selectedPlayerCharacter[0].getLevel());
                levelLabel.setText(labelText);
            }
        });

        JButton decrementLevel = new JButton("Decrement Level");
        decrementLevel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedPlayerCharacter[0].decreaseLevel();
                String labelText = String.format("Level:\t%d", selectedPlayerCharacter[0].getLevel());
                levelLabel.setText(labelText);
            }
        });

        JButton createNewCharacter = new JButton("Create New Character");
        JTextField newNameField = new JTextField("Enter name for new Character");
        createNewCharacter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PlayerCharacter newPlayer = new PlayerCharacter(newNameField.getText(), 0);
                loadNewPlayerCharacterList(newPlayer);
                charFrame.dispose();
            }
        });


        charFrame.add(nameLabel);
        charFrame.add(levelLabel);
        charFrame.add(charLocation);
        charFrame.add(charSkills);
        charFrame.add(changeName);
        charFrame.add(changeNameField);
        charFrame.add(incrementLevel);
        charFrame.add(decrementLevel);
        charFrame.add(createNewCharacter);
        charFrame.add(newNameField);
        charFrame.setVisible(true);
        charFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    public void loadNewPlayerCharacterList(PlayerCharacter newPlayer){
        this.playerCharacters = Player.addNewPlayerCharacter(this.playerCharacters, newPlayer);
        characterMenu();
    }

//    public void createWorld(Universe uni1){
//        JLabel comingsoon = new JLabel("This feature is coming soon!!! (TM)");
//        comingsoon.setBounds(280,15,250,25);
//        
//        JButton exit = new JButton("Exit!");
//        exit.setBounds(15,525,250,25);
//        exit.addActionListener(event -> System.exit(0));
//        
//        JButton nextMenu = new JButton("Create Character");
//        nextMenu.setBounds(500,525,250,25);
//        nextMenu.addActionListener(event -> closeWindow());
//        
//        this.frame.add(exit);
//        this.frame.add(nextMenu);
//        this.frame.add(comingsoon);
//        this.frame.setTitle("Create New World");
//        this.frame.setVisible(true);
//    }
    
    public void createCharacter(PlayerCharacter ply){
        
    }
    
    public void loadGameMenu() throws IOException {
        JFrame loadGameFrame = new JFrame("Load Game");
        loadGameFrame.setSize(800, 800);
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(".").getCanonicalFile());
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        loadGameFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        fileChooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //System.out.println(e);
                if(e.getActionCommand().equals("CancelSelection")){
                    loadGameFrame.dispose();
                }
                if(e.getActionCommand().equals("ApproveSelection")){
                    String directory = String.valueOf(fileChooser.getCurrentDirectory());
                    try {
                        loadUniverseData(directory);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    loadGameFrame.dispose();
                }
            }
        });

        loadGameFrame.add(fileChooser);
        loadGameFrame.setVisible(true);
    }

    private void loadUniverseData(String directory) throws IOException{
        this.universe = FileHandling.loadAlmanacData(directory);
    }

    public void startNewGame(){
        this.frame.setVisible(false);
        //CelestialSons.createNewGame();
    }
    
    public void loadGameFile(String fileName){
        JOptionPane.showConfirmDialog(this.frame, "Loading game");
    }
    
    public void closeWindow(){
        this.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.frame.dispose();
    }
    public void restoreWindow(){this.frame.setVisible(true);}
    public void setName(String name){this.name = name;}
    public int getHeight(){ return this.height;}
    public int getWidth(){return this.width;}
}
