import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class TCPServer {
    private static final int PORT_TCP = 9999;

    ServerSocket serverSocket; //serverSocket per TCP
    SignedUpUsers userList;

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
            login(clientUser);
        }
    }

    public boolean login(User u){
        if(u.getName().isEmpty() || u.getPassword().isEmpty())
            System.err.println("Il nome utente e la paword non possono essere vuoti");

        ArrayList<User> data = new ArrayList<>();
        userList.getList().forEach((s, user) -> {
            synchronized (user){
                data.add(user);
            }
        });
        boolean tmp = false;
        for(User currUser: data){
            if(u.getName().equals(currUser.getName()))
                if(currUser.getPassword().equals(u.getPassword())){
                    if(u.getStatus().equals("offline")){
                        tmp = true;
                        System.out.println("login completato correttamente");
                    }
                    else System.err.println("utente gia loggato");
                }else System.err.println("Password errata");

        }
        if(!tmp) System.err.println("Utente non registrato nel sistema!");
        return false;
    }
}
