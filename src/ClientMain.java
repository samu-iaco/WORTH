import Remote.Exception.UserAlreadyExistsException;
import Model.User;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
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
    private static final String ServerAddress = "127.0.0.1";
    private DataInputStream dis;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Login<UserAndStatus> resultLogin;
    boolean alreadyLogged = false;

    private ArrayList<UserAndStatus> listUsersStatus;

    public static void main(String[] args){
        ClientMain clientMain = new ClientMain();
        clientMain.start();
    }

    public void start(){
        boolean ok = true;

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

                String command = in.nextLine();
                String[] splittedCommand = command.split(" ");
                switch (splittedCommand[0].toLowerCase()){

                    case "register":
                        register(splittedCommand,registerRMI);
                        break;
                    case "login":

                        if(alreadyLogged){
                            System.err.println("Un utente è gia loggato, prima si deve scollegare");
                            break;
                        }
                        //TCPClient client = new TCPClient(new User(splittedCommand[1],splittedCommand[2]));

                        boolean resultLogin = login(splittedCommand,client);
                        if(resultLogin){
                            alreadyLogged = true;
                            System.out.println("Registrazione di " + splittedCommand[1] + " alla callback");
                            registerRMI.registerForCallback(expCallback,splittedCommand[1]);
                        }
                        break;
                    case "logout":
                        boolean resultLogout = false;
                        if(alreadyLogged)
                            resultLogout = logout(splittedCommand[1],splittedCommand);
                        else System.err.println("Non c'è nessun utente collegato, impossibile effettuare il logout");
                        if(resultLogout) {
                            alreadyLogged = false;
                            System.out.println("Utente: " + splittedCommand[1] + " scollegato");
                        }
                        break;
                    case "listusers":
                        if(!alreadyLogged) {
                            System.err.println("Prima devi effettuare il login");
                            break;
                        }
                        listusers(splittedCommand);
                        break;
                    case "listonlineusers":
                        if(!alreadyLogged){
                            System.err.println("Prima devi effettuare il login");
                            break;
                        }
                        listonlineusers(splittedCommand);
                        break;
                    case "createproject":
                        if(!alreadyLogged){
                            System.err.println("Prima devi effettuare il login");
                            break;
                        }
                        createProject(splittedCommand);
                        break;
                }
            }
        } catch (IOException | NotBoundException | UserAlreadyExistsException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void register(String[] splittedCommand, RMI_register_Interface registerRMI) throws UserAlreadyExistsException, RemoteException {
        String result = "";
        if(splittedCommand.length<3) registerRMI.register("","");
        else if(splittedCommand.length>3) System.out.println("Hai inserito troppi argomenti");
        else result = registerRMI.register(splittedCommand[1],splittedCommand[2]);
        System.out.println(result);
    }

    public boolean login(String[] splittedCommand, SocketChannel client) throws IOException, ClassNotFoundException {
        //invio al server
        oos.writeObject(new User(splittedCommand[1],splittedCommand[2]));
        //ricezione dal server
        this.resultLogin = (Login) ois.readObject();
        if(resultLogin.getMessage().equals("OK")){
            System.out.println("Utente: " + splittedCommand[1] + " correttamente loggato");
            listUsersStatus = resultLogin.getList();
            return true;
        }
        else {
            System.err.println(resultLogin.getMessage());
            return false;
        }
    }

    public boolean logout(String userName, String[] splittedCommand) throws IOException, ClassNotFoundException {

        //invio al server la richiesta di logout
        oos.writeObject(splittedCommand);
        //ricezione dal server del logout
        String result = (String) ois.readObject();
        System.out.println("result: " + result);
        if(result.equals("OK")){
            return true;
        }
        else return false;
    }

    public void listusers(String[] splittedCommand) throws IOException, ClassNotFoundException {
        System.out.println("richiesta di vedere la lista degli utenti registrati");
        //invio la richiesta del comando al server
        oos.writeObject(splittedCommand);

        ArrayList<UserAndStatus> list= (ArrayList<UserAndStatus>) ois.readObject();

        for(UserAndStatus currUser: list){
            System.out.println(currUser);
        }

    }

    public void listonlineusers(String[] splittedCommand) throws IOException, ClassNotFoundException {
        oos.writeObject(splittedCommand);

        ArrayList<UserAndStatus> list= (ArrayList<UserAndStatus>) ois.readObject();

        for(UserAndStatus currUser: list){
            System.out.println(currUser);
        }
    }

    public void createProject(String[] splittedCommand) throws IOException, ClassNotFoundException {
        oos.writeObject(splittedCommand);

        String result = (String) ois.readObject();

        if(result.equals("OK")){
            System.out.println("Progetto: " + splittedCommand[1] + " creato");
        }else
            System.err.println(result);
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
