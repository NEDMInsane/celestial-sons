
package celestialsons;


public class DiceRoller {
    private int previousRoll;
    private int size = 4;
    
    public DiceRoller(int size){
        if(size < 4){
            System.out.printf("A %d side die cannot be rolled. I cannot be held responsible if it breaks.", size);
        }
        this.previousRoll = 0;
        this.size = size;
    }
    
    public int rollDice(){
        this.previousRoll = (int)(Math.random() * this.size) + 1;
        return this.previousRoll;
    }
}
