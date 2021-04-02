package Model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

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
        dir = new File("./" + name);
        if(!dir.exists()) dir.mkdir();
    }

    /**
     *
     * @param username utente
     * @return se l'utente è nel progetto oppure no
     */
    public boolean isInProject(String username){
        for(String currUser: projectMembers){
            if(currUser.equals(username)){
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param username utente
     * @return aggiunge il membro al progetto
     */
    public boolean addMember(String username){
        return projectMembers.add(username);
    }

    /**
     *
     * @param cardName nome della card
     * @param description descrizione
     * @return aggiunge la card al progetto
     */
    public String addCard(String cardName, String description){
        if(name.isEmpty() || description.isEmpty()){
            return "Nome o descrizione della carta vuoti";
        }

        synchronized (cards){
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
                    FileOutputStream fos = new FileOutputStream(file);
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    String dataCard = gson.toJson(description);
                    byte[] b = dataCard.getBytes();
                    fos.write(b);

                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return "OK";
    }

    /**
     *
     * @param cardName nome della carta
     * @param partenza lista di partenza
     * @param arrivo lista di arrivo
     * @return muove la card dalla lista di partenza a quella di arrivo
     */
    public String moveCard(String cardName, String partenza, String arrivo){
        if(partenza.equals(arrivo)) return "La lista di partenza e arrivo coincidono";
        if(!partenza.equalsIgnoreCase("TODO") && !partenza.equalsIgnoreCase("TOBEREVISITED")
                && !partenza.equalsIgnoreCase("INPROGRESS") && !partenza.equalsIgnoreCase("DONE"))
            return "Lista di partenza non valida";

        try{
            Thread.sleep(6000);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }

        synchronized (cards){
            for(Card currCard: cards){
                if(currCard.getName().equals(cardName)){
                    //Caso in cui la card si trova in TODO
                    if(partenza.equalsIgnoreCase("TODO")){
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
        }

        return "OK";
    }

    /**
     *
     * @param cardName nome della carta
     * @return resistuisce la storia della carta
     */
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

    /**
     *
     * @param currDir cartella da eliminare
     * rimuove la cartella del progetto
     */
    public void deleteDirectory(File currDir){
        String result;
        System.out.println(currDir);
        try{
            File[] allContents = currDir.listFiles();
            if (allContents != null) {
                for (File file : allContents) {
                    System.out.println(file);
                    deleteDirectory(file);
                }
            }
            currDir.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int countCards(){
        int count = 0;
        count = TODO.size() + INPROGRESS.size()+ TOBEREVISITED.size() + DONE.size();

        return count;
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

    public File getDir() {
        return dir;
    }

    public void setDir(File dir) {
        this.dir = dir;
    }

    @Override
    public String toString() {
        return "Progetto{" +
                name + '\'' +
                 + '\'' +
                '}';
    }
}
