package market;

import java.util.Scanner;

public class Contract {
    final String itemName;
    final String sellingParty; // Can be a PC/NPC/Corp
    final int quantity;
    final double pricePerUnit;
    public Contract(){
        //TODO: Add Functionality
        Scanner scnr = new Scanner(System.in);
        System.out.print("Whats the item you are selling? ");
        this.itemName = scnr.next();
        System.out.print("Whats the quantity you are selling? ");
        this.quantity = scnr.nextInt();
        System.out.print("Whats the price you are selling? ");
        this.pricePerUnit = scnr.nextDouble();
        this.sellingParty = null; // We need to get the player character that is posing this listing
    }

    public Contract(String itemName, String sellingParty, int quantity, double pricePerUnit){
        this.itemName = itemName;
        this.sellingParty = sellingParty;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
    }

    public String[] toStringArray(){
        String builder = itemName +
                quantity +
                pricePerUnit;
        return builder.split(",");
    }
}
