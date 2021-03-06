import Model.User;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Descrive il file contenente gli utenti registrati
 */
public class SignedUpUsers {


    //nome file in cui andranno salvati gli utenti
    private final String nameFile = "Users.json";

    File file = new File(nameFile);

    //hashmap in cui salvo le coppie nickUtente-utente
    private final ConcurrentHashMap<String,User> users = new ConcurrentHashMap<>();

    public SignedUpUsers() {
        try{
            if(file.exists()){
                this.searchFile();
            }
            else{
                if(!file.createNewFile())
                    throw new IOException("Errore nella creazione del file Users.json");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ricerca e recupera gli utenti registrati nel file
     */
    private void searchFile(){
        try{
            //apro lo stream del file
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader in = new InputStreamReader(fis);
            User[] dataArray = new Gson().fromJson(in,User[].class);
            ArrayList<User> data = new ArrayList<>();
            //Aggiungo i valori dentro il nuovo arraylist
            Collections.addAll(data,dataArray);

            //inserisco gli utenti dentro la concurrent Hashmap
            for(User currUser: data){
                users.putIfAbsent(currUser.getName(), currUser);
            }

            fis.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @return la dimensione di users
     */
    public int size(){
        return users.size();
    }

    public Boolean addUser(User user){
        if(users.putIfAbsent(user.getName(),user) == null){
            this.store(); //aggiungo l'utente e salvo il file
            return true;
        }
        else return false;
    }

    /**
     * Controllo se l'utente esiste
     * @param u l'utente da controllare
     * @return se è un utente valido o meno
     */
    public boolean isValid(User u){
        return users.containsKey(u.getName()) && users.get(u.getName()).getPassword().equals(u.getPassword());
    }

    /**
     * salva su file
     */
    public void store(){
        try{
            FileOutputStream fos = new FileOutputStream(file);
            //creo un GSON per formattare il testo
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            //porto i dati dall' hashmap all'arraylist
            ArrayList<User> data;
            Collection<User> values = users.values();
            data = new ArrayList<>(values);

            String s = gson.toJson(data);
            byte[] b = s.getBytes();
            fos.write(b);

            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @return restituisce la lista degli utenti registrati
     */
    public ConcurrentHashMap<String,User> getList(){
        return users;
    }
}
