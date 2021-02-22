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
import java.util.Scanner;

public class ClientMain extends RemoteObject {
    private static final int PORT_RMI = 5001;

    private static final String ServerAddress = "127.0.0.1";
    private DataInputStream dis;
    private ObjectOutputStream oos;

    public static void main(String[] args){
        ClientMain clientMain = new ClientMain();
        clientMain.start();
    }

    public void start(){
        boolean ok = true;
        SocketChannel socketChannel;
        try{
            Registry r = LocateRegistry.getRegistry(PORT_RMI);
            RMI_register_Interface registerRMI = (RMI_register_Interface) r.lookup("SignUp");
            //socketChannel = SocketChannel.open(); //Apertura socket
            //socketChannel.connect(new InetSocketAddress(ServerAddress, PORT_TCP));
            Scanner in = new Scanner(System.in);


            while(ok){
                String command = in.nextLine();
                String[] splittedCommand = command.split(" ");
                switch (splittedCommand[0].toLowerCase()){
                    case "register":
                        register(splittedCommand,registerRMI);
                    case "login":
                        login(splittedCommand);
                }
            }
        } catch (IOException | NotBoundException | UserAlreadyExistsException e) {
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

    public void login(String[] splittedCommand) throws IOException {
        System.out.println("Tentativo di login da parte di: " + splittedCommand[1]);
        TCPClient client = new TCPClient(new User(splittedCommand[1],splittedCommand[2]));
        if(client.getResultLogin()) System.out.println("Utente " + client.getUser().getName() + " loggato correttamente");
        else System.err.println("C'è gia un utente loggato, prima si deve scollegare");
    }
}
