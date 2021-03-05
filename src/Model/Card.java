package Model;

import java.io.Serializable;
import java.util.ArrayList;

public class Card implements Serializable {

    private Card card;
    private String description;
    private String name;
    private ArrayList<String> cardHistory;

    public Card(String description, String name) {
        super();
        this.description = description;
        this.name = name;
        this.cardHistory = new ArrayList<>();
        this.cardHistory.add("TODO");
    }


    public void updateHistory(String arrivo){
        cardHistory.add(arrivo);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getCardHistory() {
        return cardHistory;
    }

    public void setCardHistory(ArrayList<String> cardHistory) {
        this.cardHistory = cardHistory;
    }
}
