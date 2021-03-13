import Model.Card;
import Model.InfoMultiCastConnection;
import Model.Project;
import Remote.Exception.UserAlreadyExistsException;
import Model.User;
import com.google.gson.Gson;

import java.io.DataInputStream;
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
    private DataInputStream dis;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Login<UserAndStatus> resultLogin;
    boolean alreadyLogged = false;

    private ArrayList<UserAndStatus> listUsersStatus;
    private ArrayList<InfoMultiCastConnection> multiCastAddresses;

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
                        //TCPClient client = new TCPClient(new User(command[1],command[2]));

                        boolean resultLogin = login(command,client);
                        if(resultLogin){
                            alreadyLogged = true;
                            System.out.println("Registrazione di " + command[1] + " alla callback");
                            registerRMI.registerForCallback(expCallback,command[1]);
                        }
                        break;
                    case "logout":
                        System.out.println("ciao");
                        boolean resultLogout = false;
                        if(alreadyLogged)
                            resultLogout = logout(command);
                        else System.err.println("Non c'è nessun utente collegato, impossibile effettuare il logout");
                        if(resultLogout) {
                            alreadyLogged = false;
                            System.out.println("Utente: " + command[1] + " scollegato");
                        }
                        ok = false;
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
                }

            }
        } catch (IOException | NotBoundException | UserAlreadyExistsException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }


    public void register(String[] command, RMI_register_Interface registerRMI) throws UserAlreadyExistsException, RemoteException {
        String result = "";
        if(command.length<3) registerRMI.register("","");
        else if(command.length>3) System.out.println("Hai inserito troppi argomenti");
        else result = registerRMI.register(command[1],command[2]);
        System.out.println(result);
    }

    public boolean login(String[] command, SocketChannel client) throws IOException, ClassNotFoundException {
        //invio al server
        oos.writeObject(new User(command[1],command[2]));
        //ricezione dal server
        this.resultLogin = (Login) ois.readObject();
        if(resultLogin.getMessage().equals("OK")){
            System.out.println("Utente: " + command[1] + " correttamente loggato");
            listUsersStatus = resultLogin.getList();
                if(resultLogin.getMulticast()!=null){
                for(InfoMultiCastConnection info: resultLogin.getMulticast()){
                    //aggiungo per ogni utente che logga informazioni sul
                    MulticastSocket mLogin;
                    try {
                        mLogin = new MulticastSocket(info.getPort());
                        mLogin.joinGroup(InetAddress.getByName(info.getmAddress()));
                        mLogin.setSoTimeout(2000);
                        multiCastAddresses.add(new InfoMultiCastConnection(mLogin, info.getPort(), info.getmAddress()));
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

        //invio al server la richiesta di logout
        oos.writeObject(command);
        //ricezione dal server del logout
        String result = (String) ois.readObject();
        System.out.println("result: " + result);
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
        Gson gson = new Gson();
        String projectsJson = gson.toJson(list);
        System.out.println(projectsJson);
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
            for(InfoMultiCastConnection info: multiCastAddresses){
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
                for(InfoMultiCastConnection info: multiCastAddresses){
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
    public void notifyEventChat(String mAddress, int port) throws RemoteException {
        MulticastSocket msClient;
        try {
            msClient = new MulticastSocket(port);
            msClient.joinGroup(InetAddress.getByName(mAddress));
            msClient.setSoTimeout(3000);
            multiCastAddresses.add(new InfoMultiCastConnection(msClient, port, mAddress));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
