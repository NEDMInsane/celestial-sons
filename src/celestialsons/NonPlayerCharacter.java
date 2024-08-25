
package celestialsons;

public class NonPlayerCharacter {
    private String name;
    private int level;
    
    public NonPlayerCharacter(){
        this.name = "Player1";
        this.level = 0;
    }
    
    public NonPlayerCharacter(String name){
        this.name = name;
        this.level = 0;
    }
    
    public NonPlayerCharacter(String name, int level){
        this.name = name;
        this.level = level;
    }    
    
    public void setLevel(int level){this.level = level;}
    public int getLevel(){return this.level;}
    public void setName(String name){this.name = name;}   
    public String getName(){return this.name;}
}
