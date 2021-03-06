import Model.Card;
import Model.infoMultiCastConnection;
import Model.Project;
import Model.User;

import java.io.*;
import java.net.*;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class ClientMain extends RemoteObject implements Notify_Interface{
    private static final int PORT_RMI = 5001;
    private static final int PORT_TCP = 9999;

    private SocketChannel client;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    boolean alreadyLogged = false;

    private ArrayList<UserAndStatus> listUsersStatus;
    private ArrayList<infoMultiCastConnection> multiCastAddresses;

    public ClientMain(){
        super();    //creo l'oggetto remoto
    }

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
            //preparo la connessione TCP
            InetSocketAddress hA = new InetSocketAddress("localhost", PORT_TCP);
            client = SocketChannel.open(hA);
            oos = new ObjectOutputStream(client.socket().getOutputStream());
            ois = new ObjectInputStream(client.socket().getInputStream());
            RMI_register_Interface registerRMI = (RMI_register_Interface) r.lookup("SignUp");


            Scanner in = new Scanner(System.in);
            //Inizializzo le callbacks
            Notify_Interface obj = this;
            //definisce l'oggetto remoto i cui riferimenti sono validi solo mentre il server è attivo
            Notify_Interface expCallback = (Notify_Interface) UnicastRemoteObject.exportObject(obj, 0);

            System.out.println("DIGITARE help PER RICEVERE ISTRUZIONI SUI COMANDI");
            while(ok){

                String s = in.nextLine();
                String[] command = s.split(" ");
                switch (command[0].toLowerCase()){
                    case "register":
                        if(command.length > 3){
                            System.err.println("Hai inserito troppi argomenti per questo comando");
                            break;
                        }
                        else if(command.length < 3){
                            System.err.println("Hai inserito pochi argomenti per questo comando");
                            break;
                        }
                        else {
                            register(command, registerRMI);
                        }
                        break;
                    case "login":
                        if(command.length > 3){
                            System.err.println("Hai inserito troppi argomenti per questo comando");
                            break;
                        }
                        else if(command.length < 3){
                            System.err.println("Hai inserito pochi argomenti per questo comando");
                            break;
                        }
                        else{
                            if(alreadyLogged){
                                System.err.println("Un utente è gia loggato, prima si deve scollegare");
                                break;
                            }
                            boolean resultLogin = login(command);
                            if(resultLogin){
                                alreadyLogged = true;
                                registerRMI.registerForCallback(expCallback,command[1]);
                            }
                        }
                        break;
                    case "logout":
                        if(command.length > 2){
                            System.err.println("Hai inserito troppi argomenti per questo comando");
                            break;
                        }
                        else {
                            boolean resultLogout = false;
                            if (alreadyLogged)
                                resultLogout = logout(command);
                            else
                                System.err.println("Non c'è nessun utente collegato, impossibile effettuare il logout");
                            if (resultLogout) {
                                alreadyLogged = false;
                                System.out.println("Utente: " + command[1] + " scollegato");
                                ok = false;
                            }
                        }
                        break;
                    case "listusers":
                        if(command.length > 1){
                            System.err.println("Hai inserito troppi argomenti per questo comando");
                            break;
                        }
                        else {
                            if (!alreadyLogged) {
                                System.err.println("Prima devi effettuare il login");
                                break;
                            }
                            listusers(command);
                        }
                        break;
                    case "listonlineusers":
                        if(command.length > 1){
                            System.err.println("Hai inserito troppi argomenti per questo comando");
                            break;
                        }
                        else {
                            if (!alreadyLogged) {
                                System.err.println("Prima devi effettuare il login");
                                break;
                            }
                            listonlineusers(command);
                        }
                        break;
                    case "listprojects":
                        if(command.length > 1){
                            System.err.println("Hai inserito troppi argomenti per questo comando");
                            break;
                        }
                        else {
                            if (!alreadyLogged) {
                                System.err.println("Prima devi effettuare il login");
                                break;
                            }
                            listProjects(command);
                        }
                        break;
                    case "createproject":
                        if(command.length > 2){
                            System.err.println("Hai inserito troppi argomenti per questo comando");
                            break;
                        }
                        else if(command.length < 2){
                            System.err.println("Hai inserito pochi argomenti per questo comando");
                            break;
                        }
                        else{
                            if(!alreadyLogged){
                                System.err.println("Prima devi effettuare il login");
                                break;
                            }
                            createProject(command);
                        }
                        break;
                    case "addmember":
                        if(command.length > 3){
                            System.err.println("Hai inserito troppi argomenti per questo comando");
                            break;
                        }
                        else if(command.length < 3){
                            System.err.println("Hai inserito pochi argomenti per questo comando");
                            break;
                        }
                        else {
                            if (!alreadyLogged) {
                                System.err.println("Prima devi effettuare il login");
                                break;
                            }
                            addmember(command);
                        }
                        break;
                    case "showmembers":
                        if(command.length > 2){
                            System.err.println("Hai inserito troppi argomenti per questo comando");
                            break;
                        }
                        else if(command.length < 2){
                            System.err.println("Hai inserito pochi argomenti per questo comando");
                            break;
                        }
                        else {
                            if (!alreadyLogged) {
                                System.err.println("Prima devi effettuare il login");
                                break;
                            }
                            showmembers(command);
                        }
                        break;
                    case "showcards":
                        if(command.length > 2){
                            System.err.println("Hai inserito troppi argomenti per questo comando");
                            break;
                        }
                        else if(command.length < 2){
                            System.err.println("Hai inserito pochi argomenti per questo comando");
                            break;
                        }
                        else {
                            if (!alreadyLogged) {
                                System.err.println("Prima devi effettuare il login");
                                break;
                            }
                            showcards(command);
                        }
                        break;
                    case "showcard":
                        if(command.length>3){
                            System.err.println("hai inserito troppi argomenti per questo comando");
                            break;
                        }
                        else if(command.length<3) {
                            System.err.println("hai inserito pochi argomenti per questo comando");
                            break;
                        }
                        else {
                            if (!alreadyLogged) {
                                System.err.println("Prima devi effettuare il login");
                                break;
                            }
                            showcard(command);
                        }
                        break;
                    case "addcard":
                        if(command.length > 4){
                            System.err.println("Hai inserito troppi argomenti per questo comando");
                            break;
                        }
                        else if(command.length < 4){
                            System.err.println("Hai inserito pochi argomenti per questo comando");
                            break;
                        }
                        else {
                            if (!alreadyLogged) {
                                System.err.println("Prima devi effettuare il login");
                                break;
                            }
                            addCard(command);
                        }
                        break;
                    case "movecard":
                        if(command.length > 5){
                            System.err.println("Hai inserito troppi argomenti per questo comando");
                            break;
                        }
                        else if(command.length < 5){
                            System.err.println("Hai inserito pochi argomenti per questo comando");
                            break;
                        }
                        else {
                            if (!alreadyLogged) {
                                System.err.println("Prima devi effettuare il login");
                                break;
                            }
                            movecard(command);
                        }
                        break;
                    case "getcardhistory":
                        if(command.length > 3){
                            System.err.println("Hai inserito troppi argomenti per questo comando");
                            break;
                        }
                        else if(command.length < 3){
                            System.err.println("Hai inserito pochi argomenti per questo comando");
                            break;
                        }
                        else {
                            if (!alreadyLogged) {
                                System.err.println("Prima devi effettuare il login");
                                break;
                            }
                            getCardHistory(command);
                        }
                        break;
                    case "readchat":
                        if(command.length > 2){
                            System.err.println("Hai inserito troppi argomenti per questo comando");
                            break;
                        }
                        else if(command.length < 2){
                            System.err.println("Hai inserito pochi argomenti per questo comando");
                            break;
                        }
                        else {
                            if (!alreadyLogged) {
                                System.err.println("Prima devi effettuare il login");
                                break;
                            }
                            readChat(command);
                        }
                        break;
                    case "sendchatmsg":
                        if(command.length > 2){
                            System.err.println("Hai inserito troppi argomenti per questo comando");
                            break;
                        }
                        else if(command.length < 2){
                            System.err.println("Hai inserito pochi argomenti per questo comando");
                            break;
                        }
                        else {
                            if (!alreadyLogged) {
                                System.err.println("Prima devi effettuare il login");
                                break;
                            }
                            sendChatMsg(command, in);
                        }
                        break;
                    case "cancelproject":
                        if(command.length > 2){
                            System.err.println("Hai inserito troppi argomenti per questo comando");
                            break;
                        }
                        else if(command.length < 2){
                            System.err.println("Hai inserito pochi argomenti per questo comando");
                            break;
                        }
                        else {
                            if(!alreadyLogged){
                                System.err.println("Prima devi effettuare il login");
                                break;
                            }
                            cancelProject(command);
                        }
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
        } catch (NullPointerException np){
            System.err.println("Eccezione rilevata");
            alreadyLogged = false;
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

    public boolean login(String[] command) throws IOException, ClassNotFoundException {
        //invio al server
        oos.writeObject(new String[]{"login"});
        oos.writeObject(new User(command[1],command[2]));
        //ricezione dal server
        Login<UserAndStatus> resultLogin = (Login<UserAndStatus>) ois.readObject();

        if(resultLogin.getMessage().equals("OK")){
            System.out.println("Utente: " + command[1] + " correttamente loggato");
            listUsersStatus = resultLogin.getList();
                if(resultLogin.getMulticast()!=null){
                for(infoMultiCastConnection info: resultLogin.getMulticast()){
                    //aggiungo per ogni utente che logga informazioni sul multicast
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
        if(command.length > 2){
            System.err.println("Hai inserito troppi argomenti");
            return false;
        }
        //invio al server la richiesta di logout
        oos.writeObject(command);
        //ricezione dal server del logout
        String result = (String) ois.readObject();
        if(result.equals("OK")){
            return true;
        }
        else {
            System.err.println(result);
            return false;
        }
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
        ArrayList<Project> list = (ArrayList<Project>) ois.readObject();
        if(list == null){
            System.err.println("L'utente non è membro di nessun progetto");
        }else{
            System.out.println("Progetti di cui l'utente fa parte: ");
            for(Project currProject: list)
                System.out.println(currProject.getName());
        }
    }

    public void createProject(String[] command) throws IOException, ClassNotFoundException {
        oos.writeObject(command);

        ToClientProject result = (ToClientProject) ois.readObject();

        if(result.getMessage().equals("OK")){
            System.out.println("Progetto " + result.getProject().getName() + " creato");
        }else System.err.println(result.getMessage());

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
        if(result == null)
            System.err.println("Impossibile recuperare la lista degli utenti al momento");
        else{

            if(result.getMessage().equals("OK")){
                System.out.println("Utenti del progetto " + command[1] + ": ");
                for(String currUser: result.getList()){
                    System.out.println(currUser);
                }
            }else System.err.println(result.getMessage());
        }
    }

    public void showcards(String[] command) throws IOException, ClassNotFoundException {
        oos.writeObject(command);
        ToClient<Card> result = (ToClient<Card>) ois.readObject();
        if(result == null)
            System.err.println("Impossibile mostrare card al momento");
        else{
            if(result.getMessage().equals("OK")){
                if(result.getList().size() == 0){
                    System.out.println("Il progetto non contiene nessuna card");
                }
                for(Card currCard: result.getList()){

                    System.out.println("Card: " +currCard.getName());
                    System.out.println("Descrizione: " + currCard.getDescription());
                }

            }else System.err.println(result.getMessage());
        }
    }

    public void showcard(String[] command) throws IOException, ClassNotFoundException {
        oos.writeObject(command);
        ToClient<Card> result = (ToClient<Card>) ois.readObject();
        if(result == null)
            System.err.println("Impossibile mostrare card al momento");
        else{
            if(result.getMessage().equals("OK")){
                for(Card currCard : result.getList()){
                    if(currCard.getName().equals(command[2])){
                        System.out.println("Card: " +currCard.getName());
                        System.out.println("Descrizione: " + currCard.getDescription());
                        int tmp = currCard.getCardHistory().size();
                        System.out.println("Lista: " + currCard.getCardHistory().get(tmp-1));
                    }
                }
            }else System.err.println(result.getMessage());
        }
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
            System.out.println("Card history: ");

            for(String currCardHistory: result.getList()){
                System.out.println(currCardHistory);
            }

        }else System.err.println(result.getMessage());
    }

    public void readChat(String[] command) throws IOException, ClassNotFoundException {
        oos.writeObject(command);
        ToClientChat result;

        boolean ok = true;
        result = (ToClientChat) ois.readObject();
        if(result == null){
            System.err.println("Impossibile leggere la chat al momento");
        }
        else{
            if(result.getMessage().equals("OK")){
                DatagramPacket receivedPacket;
                for(infoMultiCastConnection info: multiCastAddresses){
                    if(info.getmAddress().equals(result.getMulticastChat())){
                        while(ok){
                            byte[] receiveBuffer = new byte[8192]; //alloco il buffer per la ricezione dei messaggi
                            receivedPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                            try{
                                info.getMulticastsocket().setSoTimeout(2000);
                                info.getMulticastsocket().receive(receivedPacket);
                                String byteToString = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
                                System.out.println(byteToString);
                            } catch (SocketTimeoutException e) {
                                System.out.println("messaggi finiti");
                                ok = false;
                            }

                        }
                    }
                }

            }else System.err.println(result.getMessage());
        }

    }

    public void sendChatMsg(String[] command, Scanner in) throws IOException, ClassNotFoundException {
        oos.writeObject(command);
        String message;
        ToClientChat result;

        result = (ToClientChat) ois.readObject();
        if(result == null){
            System.err.println("Impossibile leggere la chat al momento");
        }
        else{
            if(result.getMessage().equals("OK")){
                System.out.println("Inserisci il messaggio da inviare: ");
                message = in.nextLine();
                byte[] sendBuffer = message.getBytes();
                DatagramPacket packetToSend;
                for(infoMultiCastConnection info: multiCastAddresses){
                    if(info.getmAddress().equals(result.getMulticastChat())){
                        packetToSend = new DatagramPacket(sendBuffer, sendBuffer.length,
                                InetAddress.getByName(info.getmAddress()),info.getPort());
                        info.getMulticastsocket().send(packetToSend);
                        System.out.println("messaggio inviato");
                    }
                }


            }else System.err.println(result.getMessage());
        }

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
    public void notifyEvent(String userName, String status) {
        boolean found = false;
        System.out.println(userName + " passa: " + status);
        for(UserAndStatus curr: listUsersStatus)
            if(curr.getUserName().equals(userName)) {
                found = true;
                if(!curr.getStatus().equals(status)) curr.setStatus(status);
            }
        if(!found) listUsersStatus.add(new UserAndStatus(userName,status));
    }

    @Override
    public void notifyMulticastEvent(String projectMulticast, int projectPort) {
        MulticastSocket msClient;
        try {
            msClient = new MulticastSocket(projectPort);
            msClient.joinGroup(InetAddress.getByName(projectMulticast));
            msClient.setSoTimeout(2000);
            multiCastAddresses.add(new infoMultiCastConnection(msClient, projectPort, projectMulticast));
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void notifyCancelProject(String projectMulticast, int projectPort) {
        infoMultiCastConnection msClient = null;
        for(infoMultiCastConnection infoMs: multiCastAddresses){
            if(infoMs.getmAddress().equals(projectMulticast)){
                try {
                    infoMs.getMulticastsocket().leaveGroup(InetAddress.getByName(projectMulticast));
                    msClient = infoMs;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(msClient !=null) multiCastAddresses.remove(msClient);
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
        System.out.println("showcard projectName cardName: recupera le informazioni sulla specifica card del progetto");
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
