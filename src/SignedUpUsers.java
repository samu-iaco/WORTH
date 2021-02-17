import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;

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
            User[] dataArray = new Gson().fromJson(in,(Type) User[].class);
            ArrayList<User> data = new ArrayList<>();
            //Aggiungo i valori dentro il nuovo arraylist
            Collections.addAll(data,dataArray);
            //inserisco i valori dentro la concurrent Hashmap controllando che nessuno cerchi di modificare
            //i valori che vanno inseriti
            data.forEach(userModel -> {
                // Controllo che nessuno cerchi di modificare il valore da inserire
                synchronized (User){
                    users.put(User.getUser(),User);
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
