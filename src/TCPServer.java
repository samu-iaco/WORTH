import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class TCPServer{
    private static final int PORT_TCP = 9999;
    private boolean alreadyLogged = false;

    ServerSocket serverSocket; //serverSocket per TCP
    SignedUpUsers userList;
    RMI_register_Class register;


    public TCPServer(SignedUpUsers userList) throws IOException, ClassNotFoundException {
        serverSocket = new ServerSocket(PORT_TCP);
        System.out.println("server TCP in ascolto su: " + PORT_TCP);
        this.userList = userList;
        while(true){
            // Aspetto una connessione
            Socket sock = serverSocket.accept();
            System.out.println("connessione accettata da: " + sock.getInetAddress().getHostAddress());

            // Apro gli stream di Input e Output verso il socket
            ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
            DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
            // Ottengo le informazioni di login dal socket
            User clientUser = (User) ois.readObject();
            register = new RMI_register_Class(userList);
            if(login(clientUser) && !alreadyLogged) dos.writeBoolean(true);
            else dos.writeBoolean(false);
        }
    }

    public synchronized boolean login(User u) throws RemoteException {
        String result = null;

        if(u.getName().isEmpty() || u.getPassword().isEmpty()){
            System.err.println("Il nome utente e la password non possono essere vuoti");
            result = "Nome utente o password vuoti";
        }


        ArrayList<User> data = new ArrayList<>();
        userList.getList().forEach((s, user) -> {
            synchronized (user){
                data.add(user);
            }
        });
        boolean tmp = false;
        if(data.isEmpty()) result = "Nessun utente registrato";
        for(User currUser: data){
            if(u.getName().equals(currUser.getName()))
                if(currUser.getPassword().equals(u.getPassword())){
                    if(u.getStatus().equals("offline")){
                        tmp = true;
                        register.update(currUser.getName(),"online");
                        currUser.setStatus("online");
                    }
                    else result = "Utente già loggato";

                }else result = "password errata";


        }   //bisogno di rappresentare con una lista 
        if(!tmp) {
            System.err.println("Utente non registrato nel sistema!");
            System.out.println(result);
            return false;
        }
        else {
            alreadyLogged = true;
            return true;
        }
    }
}
