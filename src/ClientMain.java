import Remote.Exception.UserAlreadyExistsException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteObject;
import java.util.Scanner;

public class ClientMain extends RemoteObject {
    private static final int PORT_RMI = 5000;
    private static final int PORT_TCP = 9999;
    private static final String ServerAddress = "127.0.0.1";


    public static void main(String[] args){
        ClientMain clientMain = new ClientMain();
        clientMain.start();
    }

    public void start(){
        boolean ok = true;
        SocketChannel socketChannel;
        try{
            Registry r = LocateRegistry.createRegistry(PORT_RMI);
            RMI_register_Interface registerRMI = (RMI_register_Interface) r.lookup("SignUp");
            socketChannel = SocketChannel.open(); //Apertura socket
            socketChannel.connect(new InetSocketAddress(ServerAddress, PORT_TCP));
            Scanner in = new Scanner(System.in);


            while(ok){
                String command = in.nextLine();
                String[] splittedCommand = command.split(" ");
                switch (splittedCommand[0].toLowerCase()){
                    case "register":
                        register(splittedCommand,registerRMI);
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
}
