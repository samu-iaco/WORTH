package Model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class SignedUpProjects {

    //nome file in cui andranno salvati gli utenti
    private final String nameFile = "Projects.json";

    File file = new File(nameFile);

    //creo un hashmap in cui salvo le coppie nomeprogetto-progetto
    private final ConcurrentHashMap<String,Project> projects = new ConcurrentHashMap<>();

    public SignedUpProjects() {
        try{
            if(file.exists()){
                this.searchFile();
            }
            else{
                if(!file.createNewFile()){
                    System.err.println("Problemi durante la creazione del file dei progetti");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Recupero i progetti dal file
     */
    private void searchFile(){
        try{
            //apro il file e lo leggo
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader in = new InputStreamReader(fis);
            Project[] dataArray = new Gson().fromJson(in,Project[].class);
            ArrayList<Project> data = new ArrayList<>();
            //Aggiungo i valori dentro il nuovo arraylist
            Collections.addAll(data,dataArray);
            //inserisco i progetti dentro la concurrent Hashmap
            for(Project currProject: data){
                projects.putIfAbsent(currProject.getName(), currProject);
            }
            fis.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public String addProject(Project project){
        System.out.println("Thread in signedUpProjects ADDPROJECT: " + Thread.currentThread().getName());
        if(projects.putIfAbsent(project.getName(),project) == null){
            this.store(); //aggiungo il progetto e salvo il file
            return "OK";
        }
        return null;
    }

    /**
     *
     * @return la lista di tutti i progetti
     */
    public ConcurrentHashMap<String,Project> getList(){
        return projects;
    }

    public synchronized void store(){
        System.out.println("Thread in signedUpProjects STORE: " + Thread.currentThread().getName());

        try {
            FileOutputStream fos = new FileOutputStream(file);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            ArrayList<Project> data;
            Collection<Project> values = projects.values();
            data = new ArrayList<>(values);

            //scrivo sul file
            String s = gson.toJson(data);
            byte[] b = s.getBytes();
            fos.write(b);

            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
