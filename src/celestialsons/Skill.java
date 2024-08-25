
package celestialsons;


public class Skill {
    private String name;
    private String skillType;
    private String attribute;
    private int level;
    private boolean mastery = false;
    private String description;
    
    public Skill(String name, String skillType, String attribute){
        this.name = name;
        this.skillType = skillType;
        this.attribute = attribute;
        this.level = 0;
        this.description = "";
    }
    
    public Skill(String name, String skillType, String attribute, int level){
        this.name = name;
        this.skillType = skillType;
        this.attribute = attribute;
        this.level = level;
        this.description = "";
    }
    
    public void setName(String name){this.name = name;}
    public String getName(){return this.name;}
    public void setSkillType(String skillType){this.skillType = skillType;}
    public String getSkillType(){return this.skillType;}
    public void setAttribute(String attribute){this.attribute = attribute;}
    public String getAttribute(){return this.attribute;}
    public void setLevel(int level){this.level = level;}
    public int getLevel(){return this.level;}
    public void incrementLevel(){this.level++;}
    public void decrementLevel(){this.level--;}
    public void checkMastery(){this.mastery = this.level == 5;}
    public void setMastery(boolean mastery){this.mastery = mastery;}
    public boolean getMastery(){return this.mastery;}
    public void setDescription(String description){this.description = description;}
    public String getDescription(){return this.description;}
    
}
