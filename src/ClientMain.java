import Remote.Exception.UserAlreadyExistsException;

import java.io.DataInputStream;
import java.io.IOException;
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

    private static final String ServerAddress = "127.0.0.1";
    private DataInputStream dis;
    private ObjectOutputStream oos;

    private ArrayList<UserAndStatus> listUsersStatus;

    public static void main(String[] args){
        ClientMain clientMain = new ClientMain();
        clientMain.start();
    }

    public void start(){
        boolean ok = true;
        boolean alreadyLogged = false;
        SocketChannel socketChannel;
        try{
            Registry r = LocateRegistry.getRegistry(PORT_RMI);
            RMI_register_Interface registerRMI = (RMI_register_Interface) r.lookup("SignUp");

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
                    case "login":
                        if(alreadyLogged){
                            System.err.println("Un utente è gia loggato, prima si deve scollegare");
                            break;
                        }
                        boolean resultLogin = login(splittedCommand);
                        if(resultLogin){
                            alreadyLogged = true;
                            System.out.println("Registrazione di " + splittedCommand[1] + " alla callback");
                            registerRMI.registerForCallback(expCallback,splittedCommand[1]);
                        }
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

    public boolean login(String[] splittedCommand) throws IOException, ClassNotFoundException {
        TCPClient client = new TCPClient(new User(splittedCommand[1],splittedCommand[2]));
        //RISOLVERE IL NULL QUA, QUINDI CONTROLLARE IL GET LOGIN DENTRO TCP CLIENT
        if(client.getLogin().getMessage().equals("OK")){
            System.out.println("Utente: " + splittedCommand[1] + " correttamente loggato");
            listUsersStatus = client.getLogin().getList();
            return true;
        }
        else {
            System.err.println(client.getLogin().getMessage());
            return false;
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
