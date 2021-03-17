import Model.Card;
import Model.infoMultiCastConnection;
import Model.Project;
import Model.User;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;

public class ClientMain extends RemoteObject implements Notify_Interface{
    private static final int PORT_RMI = 5001;
    private static final int PORT_TCP = 9999;

    private SocketChannel client;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Login<UserAndStatus> resultLogin;
    boolean alreadyLogged = false;

    private ArrayList<UserAndStatus> listUsersStatus;
    private ArrayList<infoMultiCastConnection> multiCastAddresses;

    public static void main(String[] args){
        ClientMain clientMain = new ClientMain();
        clientMain.start();
    }

    public void start(){
        boolean ok = true;
        this.multiCastAddresses = new ArrayList<>();
        SocketChannel socketChannel;
        try{
            //preparo la connessione RMI
            Registry r = LocateRegistry.getRegistry(PORT_RMI);
            RMI_register_Interface registerRMI = (RMI_register_Interface) r.lookup("SignUp");

            //preparo la connessione TCP
            InetSocketAddress hA = new InetSocketAddress("localhost", PORT_TCP);
            client = SocketChannel.open(hA);
            oos = new ObjectOutputStream(client.socket().getOutputStream());
            ois = new ObjectInputStream(client.socket().getInputStream());


            Scanner in = new Scanner(System.in);
            //Inizializzo le callbacks
            Notify_Interface obj = this;
            Notify_Interface expCallback = (Notify_Interface) UnicastRemoteObject.exportObject(obj, 0);
            System.out.println("DIGITARE help PER RICEVERE ISTRUZIONI SUI COMANDI");
            while(ok){

                String s = in.nextLine();
                String[] command = s.split(" ");
                switch (command[0].toLowerCase()){
                    case "register":
                        register(command,registerRMI);
                        break;
                    case "login":
                        if(alreadyLogged){
                            System.err.println("Un utente è gia loggato, prima si deve scollegare");
                            break;
                        }
                        boolean resultLogin = login(command,client);
                        if(resultLogin){
                            alreadyLogged = true;
                            System.out.println("Registrazione di " + command[1] + " alla callback");
                            registerRMI.registerForCallback(expCallback,command[1]);
                        }
                        break;
                    case "logout":
                        boolean resultLogout = false;
                        if(alreadyLogged)
                            resultLogout = logout(command);
                        else System.err.println("Non c'è nessun utente collegato, impossibile effettuare il logout");
                        if(resultLogout) {
                            alreadyLogged = false;
                            System.out.println("Utente: " + command[1] + " scollegato");
                            ok = false;
                        }
                        break;
                    case "listusers":
                        if(!alreadyLogged) {
                            System.err.println("Prima devi effettuare il login");
                            break;
                        }
                        listusers(command);
                        break;
                    case "listonlineusers":
                        if(!alreadyLogged){
                            System.err.println("Prima devi effettuare il login");
                            break;
                        }
                        listonlineusers(command);
                        break;
                    case "listprojects":
                        if(!alreadyLogged){
                            System.err.println("Prima devi effettuare il login");
                            break;
                        }
                        listProjects(command);
                        break;
                    case "createproject":
                        if(!alreadyLogged){
                            System.err.println("Prima devi effettuare il login");
                            break;
                        }
                        createProject(command);
                        break;
                    case "addmember":
                        if(!alreadyLogged){
                            System.err.println("Prima devi effettuare il login");
                            break;
                        }
                        addmember(command);
                        break;
                    case "showmembers":
                        if(!alreadyLogged){
                            System.err.println("Prima devi effettuare il login");
                            break;
                        }
                        showmembers(command);
                        break;
                    case "showcards":
                        if(!alreadyLogged){
                            System.err.println("Prima devi effettuare il login");
                            break;
                        }
                        showcards(command);
                        break;
                    case "addcard":
                        if(!alreadyLogged){
                            System.err.println("Prima devi effettuare il login");
                            break;
                        }
                        addCard(command);
                        break;
                    case "movecard":
                        if(!alreadyLogged){
                            System.err.println("Prima devi effettuare il login");
                            break;
                        }
                        movecard(command);
                        break;
                    case "getcardhistory":
                        if(!alreadyLogged){
                            System.err.println("Prima devi effettuare il login");
                            break;
                        }
                        getCardHistory(command);
                        break;
                    case "readchat":
                        if(!alreadyLogged){
                            System.err.println("Prima devi effettuare il login");
                            break;
                        }
                        readChat(command);
                        break;
                    case "sendchatmsg":
                        if(!alreadyLogged){
                            System.err.println("Prima devi effettuare il login");
                            break;
                        }
                        sendChatMsg(command,in);
                        break;
                    case "cancelproject":
                        if(!alreadyLogged){
                            System.err.println("Prima devi effettuare il login");
                            break;
                        }
                        cancelProject(command);
                        break;
                    case "help":
                        help();
                        break;
                    default:
                        oos.writeObject(command);
                        String otherCommands = (String) ois.readObject();
                        System.out.println(otherCommands);
                        break;

                }

            }
        } catch (IOException | NotBoundException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }


    public void register(String[] command, RMI_register_Interface registerRMI) throws RemoteException {
        String result = "";
        if(command.length<3) registerRMI.register("","");
        else if(command.length>3) System.out.println("Hai inserito troppi argomenti");
        else result = registerRMI.register(command[1],command[2]);
        if(result == null) System.err.println("Non è stato possibile registrare l'utente");
        else System.out.println(result);
    }

    public boolean login(String[] command, SocketChannel client) throws IOException, ClassNotFoundException {
        //invio al server
        oos.writeObject(new String[]{"login"});
        oos.writeObject(new User(command[1],command[2]));
        //ricezione dal server
        this.resultLogin = (Login) ois.readObject();
        if(resultLogin.getMessage().equals("OK")){
            System.out.println("Utente: " + command[1] + " correttamente loggato");
            listUsersStatus = resultLogin.getList();
                if(resultLogin.getMulticast()!=null){
                for(infoMultiCastConnection info: resultLogin.getMulticast()){
                    //aggiungo per ogni utente che logga informazioni sul
                    MulticastSocket mLogin;
                    try {
                        mLogin = new MulticastSocket(info.getPort());
                        mLogin.joinGroup(InetAddress.getByName(info.getmAddress()));
                        mLogin.setSoTimeout(2000);
                        multiCastAddresses.add(new infoMultiCastConnection(mLogin, info.getPort(), info.getmAddress()));
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


            return true;
        }
        else {
            System.err.println(resultLogin.getMessage());
            return false;
        }
    }

    public boolean logout(String[] command) throws IOException, ClassNotFoundException {
        if(command.length<2){
            System.err.println("Serve almeno un argomento");
            return false;
        }
        //invio al server la richiesta di logout
        oos.writeObject(command);
        //ricezione dal server del logout
        String result = (String) ois.readObject();
        if(result.equals("OK")){
            return true;
        }
        else return false;
    }

    public void listusers(String[] command) throws IOException, ClassNotFoundException {
        System.out.println("richiesta di vedere la lista degli utenti registrati");
        //invio la richiesta del comando al server
        oos.writeObject(command);

        ArrayList<UserAndStatus> list= (ArrayList<UserAndStatus>) ois.readObject();

        for(UserAndStatus currUser: list){
            System.out.println(currUser);
        }

    }

    public void listonlineusers(String[] command) throws IOException, ClassNotFoundException {
        oos.writeObject(command);

        ArrayList<UserAndStatus> list= (ArrayList<UserAndStatus>) ois.readObject();

        for(UserAndStatus currUser: list){
            System.out.println(currUser);
        }
    }

    public void listProjects(String[] command) throws IOException, ClassNotFoundException {
        oos.writeObject(command);
        ArrayList<Project> list= (ArrayList<Project>) ois.readObject();
        if(list == null){
            System.err.println("L'utente non è membro di nessun progetto");
        }else{
            Gson gson = new Gson();
            String projectsJson = gson.toJson(list);
            System.out.println(projectsJson);
        }

    }

    public void createProject(String[] command) throws IOException, ClassNotFoundException {
        oos.writeObject(command);

        String result = (String) ois.readObject();

        if(result.equals("OK")){
            System.out.println("Progetto: " + command[1] + " creato");
        }else
            System.err.println(result);
    }

    public void addmember(String[] command) throws IOException, ClassNotFoundException {
        oos.writeObject(command);
        String result = (String) ois.readObject();

        if(result.equals("OK")){
            System.out.println("Utente: " + command[2] + " correttamente inserito nel progetto "
            + command[1]);
        }
        else System.err.println(result);
    }

    public void showmembers(String[] command) throws IOException, ClassNotFoundException {
        oos.writeObject(command);
        ToClient<String> result = (ToClient<String>) ois.readObject();
        if(result.getMessage().equals("OK")){
            Gson gson = new Gson();
            System.out.print("Utenti: ");
            String membersGson = gson.toJson(result.getList());
            System.out.println(membersGson);

        }else System.err.println(result.getMessage());
    }

    public void showcards(String[] command) throws IOException, ClassNotFoundException {
        oos.writeObject(command);
        ToClient<Card> result = (ToClient<Card>) ois.readObject();

        if(result.getMessage().equals("OK")){
            Gson gson = new Gson();
            System.out.print("Cards: ");
            String cardsGson = gson.toJson(result.getList());
            System.out.println(cardsGson);
        }else System.err.println(result.getMessage());
    }

    public void addCard(String[] command) throws IOException, ClassNotFoundException {
        oos.writeObject(command);
        String result = (String) ois.readObject();
        if(result.equals("OK")){
            System.out.println("card: " + command[2] + " aggiunta a: " + command[1]);
        }else System.err.println(result);
    }

    public void movecard(String[] command) throws IOException, ClassNotFoundException {
        oos.writeObject(command);
        String result = (String) ois.readObject();
        if(result.equals("OK")){
            System.out.println("card: " + command[2] + " spostata a: " + command[4]);
        }else System.err.println(result);
    }

    public void getCardHistory(String[] command) throws IOException, ClassNotFoundException {
        oos.writeObject(command);
        ToClient<String> result = (ToClient<String>) ois.readObject();
        if(result.getMessage().equals("OK")){
            Gson gson = new Gson();
            System.out.print("Card history: ");
            String historyGson = gson.toJson(result.getList());
            System.out.println(historyGson);

        }else System.err.println(result.getMessage());
    }

    public void readChat(String[] command) throws IOException, ClassNotFoundException {
        oos.writeObject(command);
        String result;
        String byteToString = null;
        boolean ok = true;
        byte[] receiveBuffer = new byte[1024]; //alloco il buffer per la ricezione dei messaggi
        result = (String) ois.readObject();
        if(result.substring(0,2).equals("OK")){
            DatagramPacket receivedPacket;
            for(infoMultiCastConnection info: multiCastAddresses){
                if(info.getmAddress().equals(result.substring(2,11))){
                    while(ok){
                        receivedPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        try{
                            info.getMulticastsocket().receive(receivedPacket);
                            byteToString = new String(receivedPacket.getData(), 0, receivedPacket.getLength(), StandardCharsets.US_ASCII);
                            System.out.println(byteToString);
                        } catch (SocketTimeoutException e) {
                            System.out.println("messaggi finiti");
                            ok = false;
                        }

                    }
                }
            }

        }else System.err.println(result);
    }

    public void sendChatMsg(String[] command, Scanner in) throws IOException, ClassNotFoundException {
        oos.writeObject(command);
        String message;
        String result;
        System.out.println("Inserisci il messaggio da inviare: ");
        message = in.nextLine();
        byte[] sendBuffer = message.getBytes();
        //System.out.println("dimenione del messaggio: " + sendBuffer.);
        result = (String) ois.readObject();
        System.out.println(result.substring(0,2));  //risposta dal server
        System.out.println(result.substring(2,11)); //indirizzo multicast
        if(result.substring(0,2).equals("OK")){
            try (DatagramSocket clientSocket = new DatagramSocket()) {
                clientSocket.setSoTimeout(2000);
                DatagramPacket packetToSend;
                for(infoMultiCastConnection info: multiCastAddresses){
                    if(info.getmAddress().equals(result.substring(2,11))){
                        packetToSend = new DatagramPacket(sendBuffer, sendBuffer.length,
                                                          InetAddress.getByName(info.getmAddress()),info.getPort());
                        info.getMulticastsocket().send(packetToSend);
                    }
                }

            } catch (SocketException e) {
                e.printStackTrace();
            }
        }else System.err.println(result);


    }

    public void cancelProject(String[] command) throws IOException, ClassNotFoundException {
        oos.writeObject(command);
        String result = null;
        result = (String) ois.readObject();

        if(result.equals("OK")){
            System.out.println("Progetto " + command[1] + " cancellato");
        }else System.err.print(result);
    }


    @Override
    public void notifyEvent(String userName, String status) throws RemoteException {
        boolean found = false;
        System.out.println("Richiesta di aggiornamento di stato " + userName + " " + status);
        for(UserAndStatus curr: listUsersStatus)
            if(curr.getUserName().equals(userName)) {
                found = true;
                if(!curr.getStatus().equals(status)) curr.setStatus(status);
            }
        if(!found) listUsersStatus.add(new UserAndStatus(userName,status));
    }

    private void help(){
        System.out.println("---------------WORTH HELP COMMANDS---------------");
        System.out.println("TUTTI I COMANDI DEVONO ESSERE SCRITTI IN MINUSCOLO!");
        System.out.println("register user pass: per registrare un nuovo utente");
        System.out.println("login user pass: per loggare un utente");
        System.out.println("logout user: per sloggare un utente e terminare la sessione");
        System.out.println("listusers: per consultare la lista degli utenti con il loro stato");
        System.out.println("listonlineusers: per consultare la lista degli utenti online");
        System.out.println("listprojects: per recuperare la lista dei progetti di cui l'utente è membro");
        System.out.println("createproject projectName: per creare un nuovo progetto");
        System.out.println("addmember projectName user: aggiunge al progetto un utente user");
        System.out.println("showmembers projectName: recupera la lista degli utenti del progetto");
        System.out.println("showcards projectName: recupera la lista delle cards del progetto");
        System.out.println("addcard projectName cardName description: aggiunge una card con relativa" +
                           "descrizione al progetto ");
        System.out.println("movecard projectName cardName startList finishList: sposta la card da una " +
                           "lista di partenza ad una lista di arrivo mantenendo i vincoli di spostamento");
        System.out.println("getcardhistory projectName cardName: per recuperare la lista degli spostamenti " +
                           "della card");
        System.out.println("readchat projectName: visualizza i messaggi della chat del progetto");
        System.out.println("sendchatmsg projectName-> messagge: per inviare un messaggio nella chat del progetto");
        System.out.println("cancelproject projectName: cancella il progetto a patto che tutte la card " +
                           "siano finite");

    }
}
