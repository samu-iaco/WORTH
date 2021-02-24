import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class TCPServer implements ServerInterface{
    private static final int PORT_TCP = 9999;

    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    ServerSocket serverSocket; //serverSocket per TCP
    SignedUpUsers userList;
    RMI_register_Class register;
    Login<UserAndStatus> resultLogin;


    public TCPServer(SignedUpUsers userList) throws IOException, ClassNotFoundException {
        serverSocket = new ServerSocket(PORT_TCP);
        System.out.println("server TCP in ascolto su: " + PORT_TCP);
        this.userList = userList;
        while(true){
            // Aspetto una connessione
            Socket sock = serverSocket.accept();
            System.out.println("connessione accettata da: " + sock.getInetAddress().getHostAddress());

            // Apro gli stream di Input e Output verso il socket
            ois = new ObjectInputStream(sock.getInputStream());
            //DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
            oos = new ObjectOutputStream(sock.getOutputStream());
            // Ottengo le informazioni di login dal socket
            User clientUser = (User) ois.readObject();
            register = new RMI_register_Class(userList);
            resultLogin = login(clientUser);
            oos.writeObject(resultLogin);

            start();
        }
    }

    public void start() {
        String[] splittedCommand;
        try{
            splittedCommand = (String[]) ois.readObject();
            System.out.println("command: " + splittedCommand[0]);
            switch (splittedCommand[0]){
                case "logout":
                    String resultLogout = logout(splittedCommand[1]);
                    oos.writeObject(resultLogout);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public synchronized Login<UserAndStatus> login(User u) throws RemoteException {
        String result = null;
        ArrayList<UserAndStatus> list = new ArrayList<>();
        Login<UserAndStatus> login;

        if(u.getName().isEmpty() || u.getPassword().isEmpty()){
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
                if(u.getPassword().equals(currUser.getPassword())){
                    if(u.getStatus().equals("offline")){
                        tmp = true;
                        u.setStatus("online");
                        register.update(u.getName(),"online");
                    }
                    else result = "Utente già loggato";

                }else result = "password errata";

            System.out.println("sono qui te sei qua loro sono la e questo progetto non mi riesce tralala");
            list.add(new UserAndStatus(currUser.getName(), currUser.getStatus()));
        }

        if(!tmp && result == null){
            result = "Utente non registrato nel sistema";
        }

        if(tmp){
            login = new Login<>("OK", list);
        }
        else {
            login = new Login<>(result,null);
        }

        return login;
    }

    @Override
    public String logout(String nickName) throws RemoteException {
        String result = null;

        if(nickName.isEmpty()){
            result = "Nome utente o password vuoti";
        }


        ArrayList<User> data = new ArrayList<>();
        userList.getList().forEach((s, user) -> {
            synchronized (user){
                data.add(user);
            }
        });

        if(data.isEmpty()) result = "Nessun utente registrato";
        for(User currUser: data){
            if(currUser.getName().equals(nickName))
                if(currUser.getStatus().equals("online")){
                    register.update(nickName,"offline");
                    currUser.setStatus("offline");
                }else result = "l'utente non è online";
            else result = "L'utente non è registrato nel sistema";
        }
        result = "OK";
        return result;
    }
}
