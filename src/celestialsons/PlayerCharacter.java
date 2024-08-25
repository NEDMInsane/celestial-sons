
package celestialsons;

import java.util.HashMap;
import java.util.Map;
import orbitalbodies.*;
import ship.*;

public class PlayerCharacter {
    private String name;
    private int level;
    private double exp;
    private int health;
    
    private Vector3d currentLocation = new Vector3d(0, 0, 0);
    private Star currentSystem;
    
    private Ship currentHull = new Ship();
    
    
    /**Stats are:
     * Intelligence, Charisma, Constitution, Dexterity, Luck
     */
    private HashMap<String, Integer> attributes;
    private HashMap<String, Skill> knowledgeRepo;
    
    
    public PlayerCharacter(){
        this.name = "Player1";
        this.level = 0;
        this.exp = 0.0;
        this.health = 100;
        initializeCharacter();
    }
    
    public PlayerCharacter(String name){
        this.name = name;
        this.level = 0;
        this.exp = 0.0;
        this.health = 100;
        initializeCharacter();
    }
    
    public PlayerCharacter(String name, int level){
        this.name = name;
        this.level = level;
        this.exp = 0.0;
        this.health = 100;
        initializeCharacter();
    }
    
    private void initializeCharacter(){
        this.attributes = new HashMap<>();
        this.knowledgeRepo = new HashMap<>();
        
        this.attributes.put("Intelligence",0);
        this.attributes.put("Charisma",0);
        this.attributes.put("Constitution",0);
        this.attributes.put("Dexterity",0);
        this.attributes.put("Luck",0);
        this.attributes.put("Free", 15);

        Skill frigateOperation = new Skill("Frigate Operation", "Spaceship Operation", "Intelligence", 1);
        learnSkill( frigateOperation);
        
        Skill laserOperation = new Skill("Laser Operation", "Weapon Systems", "Dexterity", 1);
        learnSkill( laserOperation);
        
        Skill turretOperation = new Skill("Turret Operation", "Weapon Systems", "Dexterity", 1);
        learnSkill( turretOperation);
        
        Skill launcherOperation = new Skill("Launcher Operation", "Weapon Systems", "Dexterity", 1);
        learnSkill( launcherOperation);
        
        Skill miningLaserOperation = new Skill("Mining Laser Operation", "Harvesting", "Constitution", 1);
        learnSkill( miningLaserOperation);
        
        Skill shieldOperation = new Skill("Shield Operation", "Defensive Systems", "Dexterity", 1);
        learnSkill( shieldOperation);
        
        Skill trade = new Skill("Trade", "Social", "Charisma", 1);
        learnSkill(  trade);
    }

    public String getSkills(){
        String skillOutput = "";
        for(Map.Entry<String, Skill> entry: this.knowledgeRepo.entrySet()){
            skillOutput = String.format("%s\n, Skill: %s - %d", skillOutput, entry.getValue().getName(), entry.getValue().getLevel());
        }

        return skillOutput;
    }

    public void printSkills(){
        int index = 0;
        for(Map.Entry<String, Skill> entry : knowledgeRepo.entrySet()){
            if(index < 3){
                System.out.printf("%s :\t %d\t",entry.getValue().getName(), entry.getValue().getLevel());
            } else {
                System.out.printf("%s :\t %d\t\n",entry.getValue().getName(), entry.getValue().getLevel());
                index = 0;
            }
            
            index++;
        }
        System.out.println();
    }
    
    public boolean isDead(){
        return(this.health <= 0);
    }
    
    public void learnSkill(Skill skill){
        if(!this.knowledgeRepo.containsKey(skill.getName())){
            this.knowledgeRepo.put(skill.getName(), skill);
        } else {
            System.out.println("%s basics already learned. Try adding to skill queue.");
        }
    }


    public void setSystem(Star star){this.currentSystem = star;}
    public Star getCurrentSystem(){return this.currentSystem;}
    public void setLocation(Vector3d location){this.currentLocation = location;}
    public Vector3d getCurrentLocation(){return this.currentLocation;}
    public void increaseLevel(){this.level++;}
    public void increaseLevel(int levels){this.level += levels;}
    public void decreaseLevel(){this.level--;}
    public void decreaseLevel(int levels){this.level -= levels;}
    public void setLevel(int level){this.level = level;}
    public int getLevel(){return this.level;}
    public void setName(String name){this.name = name;}    
    public String getName(){return this.name;}
    public void setExp(double exp){this.exp = exp;}
    public double getExp(){return this.exp;}
    public int getHealth(){return this.health;}
    public void setHealth(int health){this.health = health;}

}
