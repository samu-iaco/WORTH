
import Model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class TCPServer implements ServerInterface{
    private static final int PORT_TCP = 9999;
    private String NAME_FILE;

    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private ArrayList<User> dataUsers;
    private ArrayList<Project> dataProjects;
    private ArrayList<String> multicastGens;
    private ArrayList<infoMultiCastConnection> multicastAddresses;
    private File addresses;
    //hashmap in cui salvo le coppie multicastaddress-multicast
    private final ConcurrentHashMap<String,MulticastGen> address = new ConcurrentHashMap<>();

    ServerSocket serverSocket; //serverSocket per TCP
    SignedUpUsers userList;
    SignedUpProjects projectList;
    RMI_register_Class register;
    Login<UserAndStatus> resultLogin;
    MulticastGen multicastGen;

    private String currUsername;


    public TCPServer(SignedUpUsers userList, SignedUpProjects projectList) throws IOException, ClassNotFoundException {
        serverSocket = new ServerSocket(PORT_TCP);
        this.projectList = projectList;
        System.out.println("server TCP in ascolto su: " + PORT_TCP);
        this.userList = userList;
        this.multicastGens = new ArrayList<>();
        this.multicastAddresses = new ArrayList<>();
        this.NAME_FILE = "MulticastIP.json";
        File addresses = new File(NAME_FILE);
        this.dataUsers = new ArrayList<>();
        userList.getList().forEach((s, user) -> {
            synchronized (user){
                dataUsers.add(user);
            }
        });

        this.dataProjects = new ArrayList<>();
        projectList.getList().forEach((s,Project) ->{
            synchronized (Project){
                dataProjects.add(Project);
            }
        });

        //Per ogni progetto controllo le informazioni per il multicast
        int projectPort;
        String mProject;
        MulticastSocket msServer;
        for(Project currProject: dataProjects){
            projectPort = currProject.getPort();
            mProject = currProject.getMulticast();
            msServer = new MulticastSocket(projectPort); // creo il multicast socket sulla porta del progetto
            InetAddress group = InetAddress.getByName(mProject);
            msServer.joinGroup(group); //aggiungo il server alla porta del server
            msServer.setSoTimeout(3000);
            multicastAddresses.add(new infoMultiCastConnection(projectPort,mProject));
        }

        //creo il file con gli indirizzi multicast
        try{
            if(addresses.exists()){
                if(!(projectList.getList().isEmpty())) {
                    searchFile();
                }
                else this.multicastGen = new MulticastGen(224,0,0,0);
            }else{
                if(!addresses.createNewFile()){
                    System.err.println("Problemi durante la creazione del file degli indirizzi multicast");
                }
                this.multicastGen = new MulticastGen(224,0,0,0);
                this.storeMulticast();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



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
            this.currUsername = clientUser.getName();
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
                    oos.writeObject(resultLogout); //invio verso il client
                    break;
                case "listusers":
                    ArrayList<UserAndStatus> listToClient;
                    listToClient = listUsers();
                    oos.writeObject(listToClient); //invio al client
                    break;
                case "listonlineusers":
                    ArrayList<UserAndStatus> listOnlineUsers;
                    listOnlineUsers = listOnlineusers();
                    oos.writeObject(listOnlineUsers);
                    break;
                case "listprojects":
                    ArrayList<Project> userProjects;
                    userProjects = listProjects(currUsername);
                    oos.writeObject(userProjects);
                    break;
                case "createproject":
                    String resultCreateProject = createProject(splittedCommand[1],currUsername);
                    oos.writeObject(resultCreateProject);
                    break;
                case "addmember":
                    String resultCreateMember = addMember(splittedCommand[1],splittedCommand[2]);
                    oos.writeObject(resultCreateMember);
                    break;
                case "showmembers":
                    ToClient<String> resultShowMembers = showMembers(splittedCommand[1]);
                    oos.writeObject(resultShowMembers);
                    break;
                case "showcards":
                    ToClient<Card> resultShowCards = showCards(splittedCommand[1]);
                    oos.writeObject(resultShowCards);
                    break;
                case "addcard":
                    String resultAddCard = addCard(splittedCommand[1],splittedCommand[2],splittedCommand[3]);
                    oos.writeObject(resultAddCard);
                    break;
                case "movecard":
                    String resultMoveCard = moveCard(splittedCommand[1],splittedCommand[2],splittedCommand[3],splittedCommand[4]);
                    oos.writeObject(resultMoveCard);
                    break;
                case "getcardhistory":
                    ToClient<String> resultCardHistory = getCardHistory(splittedCommand[1],splittedCommand[2]);
                    oos.writeObject(resultCardHistory);
                    break;
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


        /*ArrayList<User> data = new ArrayList<>();
        userList.getList().forEach((s, user) -> {
            synchronized (user){
                data.add(user);
            }
        });

         */
        boolean tmp = false;
        if(dataUsers.isEmpty()) result = "Nessun utente registrato";
        for(User currUser: dataUsers){
            if(u.getName().equals(currUser.getName()))
                if(u.getPassword().equals(currUser.getPassword())){
                    if(u.getStatus().equals("offline")){
                        tmp = true;
                        currUser.setStatus("online");
                        register.update(currUser.getName(),"online");
                    }
                    else result = "Utente già loggato";

                }else result = "password errata";

            System.out.println("io sono qui te sei qua loro sono la e questo progetto non mi riesce tralala");
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

        if(nickName.isEmpty()){
            return  "Nome utente vuoto";
        }


        /*ArrayList<User> data = new ArrayList<>();
        userList.getList().forEach((s, user) -> {
            synchronized (user){
                data.add(user);
            }
        });

         */

        String result = null;
        boolean finish = false;

        if(dataUsers.isEmpty()) result = "Nessun utente registrato";
        for(User currUser: dataUsers){
            System.out.println("curruser: " + currUser.getName() + " stato: " + currUser.getStatus());
            if(currUser.getName().equals(nickName))
                if(currUser.getStatus().equals("online")){
                    register.update(nickName,"offline");
                    currUser.setStatus("offline");
                    finish = true;
                    result = "OK";
                }else result = "l'utente non è online";
        }
        if(finish) return result;
        else{
            result = "L'utente non è registrato nel sistema";
            return result;
        }
    }

    @Override
    public ArrayList<UserAndStatus> listUsers(){
        ArrayList<UserAndStatus> list = new ArrayList<>();
        /*ArrayList<User> data = new ArrayList<>();
        userList.getList().forEach((s, user) -> {
            synchronized (user){
                data.add(user);
            }
        });

         */

        for(User currUser: dataUsers){
            list.add(new UserAndStatus(currUser.getName(), currUser.getStatus()));
        }
        return list;
    }

    @Override
    public ArrayList<UserAndStatus> listOnlineusers() {
        ArrayList<UserAndStatus> list = new ArrayList<>();
        /*ArrayList<User> data = new ArrayList<>();
        userList.getList().forEach((s, user) -> {
            synchronized (user){
                data.add(user);
            }
        });

         */

        for(User currUser: dataUsers){
            if(currUser.getStatus().equals("online"))
                list.add(new UserAndStatus(currUser.getName(), currUser.getStatus()));
        }

        return list;
    }


    @Override
    public ArrayList<Project> listProjects(String username) {
        ArrayList<Project> list = new ArrayList<>();
        /*ArrayList<Project> data = new ArrayList<>();
        projectList.getList().forEach((s,Project) ->{
            synchronized (Project){
                data.add(Project);
            }
        });

         */

        if(dataProjects.isEmpty()) return null;
        for(Project currProject: dataProjects){
            if(currProject.isInProject(username))
                list.add(currProject);
        }
        if(list.isEmpty()) return null; //L'utente non collabora a nessun progetto
        return list;


    }

    @Override
    public String createProject(String projectName, String username) {
        if(projectName.isEmpty()) {
            System.err.println("Il nome del progetto non puo essere vuoto");
            return "Nome progetto vuoto";
        }

        /*
        ArrayList<Project> data = new ArrayList<>();
        projectList.getList().forEach((s,Project) ->{
            synchronized (Project){
                data.add(Project);
            }
        });

         */


        for(Project currProject: dataProjects){
            if(currProject.getName().equals(projectName)){
                System.err.println("Il progetto " + projectName + " esiste gia");
                return "Progetto gia esistente";
            }
        }
        Random rand = new Random();
        int port = rand.nextInt((65535-1024))+1025; //escludo da tutte le 65535 le 1024 porte conosciute
                                                    //per generare un intero nelle porte disponibili
        String mip = this.multicastGen.randomIP();
        addAddress(this.multicastGen);  //salvo l'indirizzo multicast nel file json
        Project project = new Project(projectName,username,port,mip);
        projectList.addProject(project);

        return "OK";
    }

    @Override
    public String addMember(String projectName, String username) {
        /*ArrayList<Project> data = new ArrayList<>();
        projectList.getList().forEach((s,Project) ->{
            synchronized (Project){
                data.add(Project);
            }
        });

         */

        String result = null;

        /*ArrayList<User> dataUser = new ArrayList<>();
        userList.getList().forEach((s, user) -> {
            synchronized (user){
                dataUser.add(user);
            }
        });

         */
        boolean userExist = false;
        for(User currUser: dataUsers){
            if(currUser.getName().equals(username)){
                userExist = true;
            }
        }
        if(!userExist) return "L'utente che vuoi aggiungere non esiste";

        for(Project currProject: dataProjects){
            if(currProject.getName().equals(projectName))
                if(currProject.getProjectMembers().contains(currUsername)){
                    if(!(currProject.isInProject(username))){
                        currProject.addMember(username);
                        projectList.store(); //aggiorno il file
                        result = "OK";
                    }else result = "L'utente è gia presente nel progetto";
                }else result = "L'utente non è un membro del progetto";
            else result = "Non esiste un progetto con questo nome";
        }

        return result;

    }

    @Override
    public ToClient<String> showMembers(String projectName) {
        ArrayList<String> list = null;
        String result = null;

        /*ArrayList<Project> data = new ArrayList<>();
        projectList.getList().forEach((s,Project) ->{
            synchronized (Project){
                data.add(Project);
            }
        });

         */

        for(Project currProject: dataProjects){
            if(currProject.getName().equals(projectName))
                if(currProject.getProjectMembers().contains(currUsername)){
                    list = currProject.getProjectMembers();
                    result = "OK";
                }else result = "L'utente non è un membro del progetto";
            else result = "Non esiste un progetto con questo nome";
        }

        return new ToClient<>(result,list);
    }

    @Override
    public ToClient<Card> showCards(String projectName) {
        String result = null;
        ArrayList<Card> list = null;

        /*ArrayList<Project> data = new ArrayList<>();
        projectList.getList().forEach((s,Project) ->{
            synchronized (Project){
                data.add(Project);
            }
        });

         */

        for(Project currProject: dataProjects){
            if(currProject.getName().equals(projectName))
                if(currProject.getProjectMembers().contains(currUsername)){
                    list = currProject.getCards();
                    result = "OK";
                }else result = "L'utente non è un membro del progetto";
            else result = "Non esiste un progetto con questo nome";
        }

        return new ToClient<>(result, list);
    }

    @Override
    public String addCard(String projectName, String cardName, String description) {
        String result = null;

        /*ArrayList<Project> data = new ArrayList<>();
        projectList.getList().forEach((s,Project) ->{
            synchronized (Project){
                data.add(Project);
            }
        });

         */

        for(Project currProject: dataProjects){
            if(currProject.getName().equals(projectName))
                if(currProject.getProjectMembers().contains(currUsername)){
                    result = currProject.addCard(cardName,description);
                    projectList.store();
                }else result = "L'utente non è un membro del progetto";
            else result = "non esiste un progetto con questo nome";
        }

        return result;
    }

    @Override
    public String moveCard(String projectName, String cardName, String partenza, String arrivo) {
        String result = null;

        /*ArrayList<Project> data = new ArrayList<>();
        projectList.getList().forEach((s,Project) ->{
            synchronized (Project){
                data.add(Project);
            }
        });

         */

        for(Project currProject: dataProjects){
            if(currProject.getName().equals(projectName))
                if(currProject.getProjectMembers().contains(currUsername)){
                    result = currProject.moveCard(cardName,partenza,arrivo);
                    projectList.store();
                }else result = "L'utente non è un membro del progetto";
            else result = "non esiste un progetto con questo nome";
        }

        return result;
    }

    @Override
    public ToClient<String> getCardHistory(String projectName, String cardName) {
        ArrayList<String> list = null;
        String result = null;

        /*ArrayList<Project> data = new ArrayList<>();
        projectList.getList().forEach((s,Project) ->{
            synchronized (Project){
                data.add(Project);
            }
        });

         */

        for(Project currProject: dataProjects){
            if(currProject.getName().equals(projectName))
                if(currProject.getProjectMembers().contains(currUsername)){
                    list = currProject.cardHistory(cardName);
                    result = "OK";
                }else result = "L'utente non è un membro del progetto";
            else result = "non esiste un progetto con questo nome";
        }

        return new ToClient<>(result,list);
    }



    public synchronized void searchFile(){
        try{
            //apro il file e lo leggo
            FileInputStream fis = new FileInputStream(NAME_FILE);
            InputStreamReader in = new InputStreamReader(fis);
            MulticastGen[] dataArray = new Gson().fromJson(in,MulticastGen[].class);
            ArrayList<MulticastGen> data = new ArrayList<>();
            Collections.addAll(data,dataArray);

            data.forEach(MulticastGen ->{
                synchronized (MulticastGen){
                    address.put(MulticastGen.toString(), MulticastGen);

                }
            });
            fis.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Boolean addAddress(MulticastGen multicastGen){
        if(address.putIfAbsent(multicastGen.toString(),multicastGen) == null){
            this.storeMulticast(); //aggiungo l'utente e salvo il file
            return true;
        }
        else return false;
    }

    public synchronized void storeMulticast(){
        try {
            FileOutputStream fos = new FileOutputStream(NAME_FILE);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            ArrayList<MulticastGen> data = new ArrayList<>();
            address.forEach((s,MulticastGen) ->{
                synchronized (MulticastGen){
                    data.add(MulticastGen);
                }
            });

            //scrivo sul file
            String s = gson.toJson(data);
            byte[] b = s.getBytes();
            fos.write(b);

            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
