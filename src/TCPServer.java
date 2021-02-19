import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
    private static final int PORT_TCP = 9999;

    ServerSocket serverSocket; //serverSocket per TCP

    public TCPServer(SignedUpUsers userList) throws IOException, ClassNotFoundException {
        serverSocket = new ServerSocket(PORT_TCP);
        System.out.println("server TCP in ascolto su: " + PORT_TCP);
        while(true){
            // Aspetto una connessione
            Socket sock = serverSocket.accept();
            System.out.println("connessione accettata da: " + sock.getInetAddress().getHostAddress());
            // Apro gli stream di Input e Output verso il socket
            ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
            DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
            // Ottengo le informazioni di login dal socket
            User clientUser = (User) ois.readObject();
            System.out.println("Utente: " + clientUser.getName());
            System.out.println("password: " + clientUser.getPassword());
        }
    }
}
