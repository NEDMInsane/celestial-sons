package celestialsons;

public class DimensionalPosition {
    private double x;
    private double y;
    private double z;
    
    public DimensionalPosition(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public void set(int index, double value){
        if(index == 0){
            this.x = value;
        } else if(index == 1){
            this.y = value;
        } else if(index == 2){
            this.z = value;
        }
    }
    
    public double get(int index){
        if(index == 0){
            return this.x;
        } else if(index == 1){
            return this.y;
        } else if(index == 2){
            return this.z;
        } else {
            return -1.0;
        }
    }
    
    public void setX(double value){
        this.x = value;
    }
    
    public double getX(){
        return this.x;
    }
    
    public void setY(double value){
        this.y = value;
    }
    
    public double getY(){
        return this.y;
    }
    
    public void setZ(double value){
        this.z = value;
    }
    
    public double getZ(){
        return this.z;
    }
    
    @Override
    public String toString(){
        return String.format("DimensionalPosition{ x = %.2f, y = %.2f, z = %.2f}", this.x, this.y, this.z);
    }

    public String toLabel(){
        return String.format("%.2f, %.2f, %.2f", this.x, this.y, this.z);
    }
    
    public void add(DimensionalPosition value){
        this.x = this.x + value.getX();
        this.y = this.y + value.getY();
        this.z = this.z + value.getZ();
    }
}
