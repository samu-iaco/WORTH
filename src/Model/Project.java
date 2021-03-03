package Model;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * classe che gestisce i progetti
 */
public class Project implements Serializable {
    private String name;
    private ArrayList<Card> cards;
    private ArrayList<String> projectMembers;
    private ArrayList<String> TODO;
    private ArrayList<String> INPROGRESS;
    private ArrayList<String> TOBEREVISITED;
    private ArrayList<String> DONE;
    private transient File dir; //transient per evitare la serializzazione di gson


    public Project(String name, String username) {
        super();
        this.name = name;
        this.TODO = new ArrayList<>();
        this.INPROGRESS = new ArrayList<>();
        this.TOBEREVISITED = new ArrayList<>();
        this.DONE = new ArrayList<>();
        this.projectMembers = new ArrayList<>();
        this.cards = new ArrayList<>();
        projectMembers.add(username);
        this.dir = new File("./" + name);
        if(!dir.exists()) dir.mkdir();
    }

    public boolean isInProject(String username){
        for(String currUser: projectMembers){
            if(currUser.equals(username)){
                return true;
            }
        }
        return false;
    }

    public boolean addMember(String username){
        return projectMembers.add(username);
    }

    public String addCard(String cardName, String description){
        if(name.isEmpty() || description.isEmpty()){
            return "Nome o descrizione della carta vuoti";
        }
        System.out.println("cards size: " + cards.size());
        for(Card currCard: cards) {
            System.out.println("ciao");
            if (currCard.getName().equals(cardName)) {
                return ("Card " + currCard.getName() + " gia esistente");
            }
        }

        Card card = new Card(description, cardName);
        cards.add(card);
        TODO.add(cardName);
        File file = new File(name + "/" +cardName+".json");
        try{
            if(!file.exists()){
                file.createNewFile();
                System.out.println("ciao nel nuovo file");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "OK";
    }

    public String moveCard(String cardName, String partenza, String arrivo){
        if(partenza.equals(arrivo)) return "La lista di partenza e arrivo coincidono";
        if(!partenza.equalsIgnoreCase("TODO") && !partenza.equalsIgnoreCase("TOBEREVISITED")
                && !partenza.equalsIgnoreCase("INPROGRESS") && !partenza.equalsIgnoreCase("DONE"))
            return "Lista di partenza non valida";


        for(Card currCard: cards){
            //Caso in cui la card si trova in TODO
            if(partenza.equalsIgnoreCase("TODO")){
                if(arrivo.equalsIgnoreCase("INPROGRESS")){
                    TODO.remove(currCard.getName());
                    INPROGRESS.add(currCard.getName());

                }
            }
        }
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getProjectMembers() {
        return projectMembers;
    }

    public void setProjectMembers(ArrayList<String> projectMembers) {
        this.projectMembers = projectMembers;
    }

    public ArrayList<String> getTODO() {
        return TODO;
    }

    public void setTODO(ArrayList<String> TODO) {
        this.TODO = TODO;
    }

    public ArrayList<String> getINPROGRESS() {
        return INPROGRESS;
    }

    public void setINPROGRESS(ArrayList<String> INPROGRESS) {
        this.INPROGRESS = INPROGRESS;
    }

    public ArrayList<String> getTOBEREVISITED() {
        return TOBEREVISITED;
    }

    public void setTOBEREVISITED(ArrayList<String> TOBEREVISITED) {
        this.TOBEREVISITED = TOBEREVISITED;
    }

    public ArrayList<String> getDONE() {
        return DONE;
    }

    public void setDONE(ArrayList<String> DONE) {
        this.DONE = DONE;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }

    @Override
    public String toString() {
        return "Progetto{ " +
                "Nome: " + name + '\'' +
                 + '\'' +
                '}';
    }
}
