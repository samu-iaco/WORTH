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
        this.cards = new ArrayList<Card>();
        projectMembers.add(username);
        dir = new File("./" + name);
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

    public String addCard(String name, String description){
        if(name.isEmpty() || description.isEmpty()){
            return "Nome o descrizione della carta vuoti";
        }

        for(Card currCard: cards){
            if(currCard.getName().equals(name)){
                return ("Card " + currCard.getName() + " gia esistente");
            }
            Card card = new Card(description, name);
            TODO.add(name);

            File file = new File(dir + "/" + name + ".json");
            try{
                if(!file.exists()){
                    file.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return null;
    }

    public String getName() {
        return name;
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

    @Override
    public String toString() {
        return "Progetto{ " +
                "Nome: " + name + '\'' +
                 + '\'' +
                '}';
    }
}
