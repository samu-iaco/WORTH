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
    private int port;
    private String multicast;
    private ArrayList<Card> cards;
    private ArrayList<String> projectMembers;
    private ArrayList<String> TODO;
    private ArrayList<String> INPROGRESS;
    private ArrayList<String> TOBEREVISITED;
    private ArrayList<String> DONE;
    private transient File dir; //transient per evitare la serializzazione di gson


    public Project(String name, String username, int port, String multicast) {
        super();
        this.name = name;
        this.TODO = new ArrayList<>();
        this.INPROGRESS = new ArrayList<>();
        this.TOBEREVISITED = new ArrayList<>();
        this.DONE = new ArrayList<>();
        this.projectMembers = new ArrayList<>();
        this.cards = new ArrayList<>();
        this.port = port;
        this.multicast = multicast;
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
            if(currCard.getName().equals(cardName)){
                //Caso in cui la card si trova in TODO
                if(partenza.equalsIgnoreCase("TODO")){
                    System.out.println("TODO size: " + TODO.size());
                    if(TODO.contains(currCard.getName())){
                        if(arrivo.equalsIgnoreCase("INPROGRESS")){
                            currCard.updateHistory(arrivo);
                            TODO.remove(currCard.getName());
                            INPROGRESS.add(currCard.getName());
                        }else return ("Non si può muovere una card da " + partenza + " ad " + arrivo);
                    }else return "La card non si trova in questa lista";

                }

                //caso in cui la card si trova in INPROGRESS
                if(partenza.equalsIgnoreCase("INPROGRESS")){
                    if(INPROGRESS.contains(currCard.getName())){
                        if(arrivo.equalsIgnoreCase("TOBEREVISITED")){
                            INPROGRESS.remove(currCard.getName());
                            TOBEREVISITED.add(currCard.getName());
                            currCard.updateHistory(arrivo);
                        } else if(arrivo.equalsIgnoreCase("DONE")){
                            INPROGRESS.remove(currCard.getName());
                            DONE.add(currCard.getName());
                            currCard.updateHistory(arrivo);
                        }else return "Non è stato possibile muovere la card";
                    }else return "La card non si trova in questa lista";

                }

                //caso in cui la card si trova in TOBEREVISITED
                if(partenza.equalsIgnoreCase("TOBEREVISITED")){
                    if(TOBEREVISITED.contains(currCard.getName())){
                        if(arrivo.equalsIgnoreCase("INPROGRESS")){
                            TOBEREVISITED.remove(currCard.getName());
                            INPROGRESS.add(currCard.getName());
                            currCard.updateHistory(arrivo);
                        }else if(arrivo.equalsIgnoreCase("DONE")){
                            TOBEREVISITED.remove(currCard.getName());
                            DONE.add(currCard.getName());
                            currCard.updateHistory(arrivo);
                        }else return "Non è stato possibile muovere la card";
                    }else return "La card non si trova in questa lista";
                }

                //caso in cui la card si trova in DONE
                if(partenza.equalsIgnoreCase("DONE")){
                    return "La card è finita e non si può spostare";
                }
            }
        }
        return "OK";
    }

    public ArrayList<String> cardHistory(String cardName){
        ArrayList<String> list = null;
        for(Card currCard: cards){
            if(currCard.getName().equals(cardName)){
                System.out.println("history size: " + currCard.getCardHistory().size());
                list = currCard.getCardHistory();
            }
        }
        return list;
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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getMulticast() {
        return multicast;
    }

    public void setMulticast(String multicast) {
        this.multicast = multicast;
    }

    @Override
    public String toString() {
        return "Progetto{ " +
                "Nome: " + name + '\'' +
                 + '\'' +
                '}';
    }
}
