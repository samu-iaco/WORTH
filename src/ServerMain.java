
import Model.SignedUpProjects;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;

public class ServerMain extends RemoteObject {
    private static final int PORT_RMI = 5001;

    /**
     *  Queste 2 classi astraggono i file json contenenti gli utenti registrati e i progetti
     */
    private static SignedUpUsers usersList = new SignedUpUsers();

    private static SignedUpProjects projectsList = new SignedUpProjects();



    public static void main(String[] args) {

        try{
            Registry registry = LocateRegistry.createRegistry(PORT_RMI);
            System.out.println("Server ready...");
            //avvio del server RMI
            //RMI_register_Class server = new RMI_register_Class(usersList); //devo creare il file con la lista degli utenti
            TCPServer server = new TCPServer(usersList,projectsList); //avvio della connessione TCP del server
            RMI_register_Interface stub = (RMI_register_Interface) UnicastRemoteObject.exportObject(server,0);
            //Interfaccia remota

            registry.rebind("SignUp" , stub);
            //parte TCP
            server.TCPStart();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

}
