package Model;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class MulticastAddress {

    //nome file in cui andranno salvati gli utenti
    private final String nameFile = "MulticastIP.json";
    File file = new File(nameFile);

    //hashmap in cui salvo le coppie multicastaddress-multicast
    private final ConcurrentHashMap<String,MulticastGen> address = new ConcurrentHashMap<>();

    public MulticastAddress() {
        try{
            if(file.exists()){
                this.searchFile();
            }
            else{
                if(!file.createNewFile()){
                    System.err.println("Problemi durante la creazione del file multicastIP");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Recupero gli indirizzi dal file
     */
    private void searchFile(){
        try{
            //apro il file e lo leggo
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader in = new InputStreamReader(fis);
            MulticastGen[] dataArray = new Gson().fromJson(in,MulticastGen[].class);
            ArrayList<MulticastGen> data = new ArrayList<>();
            //Aggiungo i valori dentro il nuovo arraylist
            Collections.addAll(data,dataArray);
            //inserisco i valori dentro la concurrent Hashmap controllando che nessuno cerchi di modificare
            //i valori che vanno inseriti
            data.forEach(MulticastGen ->{
                synchronized (MulticastGen){
                    address.put(MulticastGen.toString(), MulticastGen);
                }
            });
            fis.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }



}
