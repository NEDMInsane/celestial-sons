
package celestialsons;

public class Player {

    public static PlayerCharacter[] addNewPlayerCharacter(PlayerCharacter[] playerCharacters, PlayerCharacter newPlayer){
        PlayerCharacter[] newPlayerCharacterList = new PlayerCharacter[playerCharacters.length + 1];
        System.out.println(playerCharacters.length);
        System.out.println(newPlayerCharacterList.length);
        int i = 0;
        for(PlayerCharacter player : playerCharacters){
            newPlayerCharacterList[i] = player;
            i++;
        }
        newPlayerCharacterList[playerCharacters.length] = newPlayer;
        return newPlayerCharacterList;
    }
}
