package Model;

import java.io.File;
import java.util.ArrayList;

/**
 * classe che gestisce i progetti
 */
public class Project {
    private String name;
    private ArrayList<String> projectMembers;
    private ArrayList<String> TODO;
    private ArrayList<String> INPROGRESS;
    private ArrayList<String> TOBEREVISITED;
    private ArrayList<String> DONE;
    private File dir;


    public Project(String name, String username) {
        this.name = name;
        this.TODO = new ArrayList<>();
        this.INPROGRESS = new ArrayList<>();
        this.TOBEREVISITED = new ArrayList<>();
        this.DONE = new ArrayList<>();
        this.projectMembers = new ArrayList<>();
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
}
