import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
    private static final int PORT_TCP = 9999;

    ServerSocket serverSocket; //serverSocket per TCP

    public TCPServer(SignedUpUsers userList) throws IOException {
        serverSocket = new ServerSocket(PORT_TCP);

        while(true){
            // Aspetto una connessione
            Socket sock = serverSocket.accept();
            // Apro gli stream di Input e Output verso il socket
            //ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
            //DataOutputStream dos = new DataOutputStream(sock.getOutputStream());

            System.out.println("connessione accettata da: " + sock.getInetAddress().getHostAddress());
        }
    }
}
