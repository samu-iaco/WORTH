
import Model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class TCPServer extends UnicastRemoteObject implements ServerInterface, RMI_register_Interface{
    private static final int PORT_TCP = 9999;
    private String NAME_FILE; //file per gli indirizzi multicast

    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private ArrayList<Project> dataProjects;

    private ArrayList<MulticastGen> multicastGens;
    private ArrayList<infoMultiCastConnection> multicastAddresses;
    private final List<InfoCallback> clients;


    ServerSocket serverSocket; //serverSocket per TCP
    SignedUpUsers userList;
    SignedUpProjects projectList;

    MulticastGen multicastGen;

    /**
     * Il pool dei thread in esecuzione
     */
    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();


    public TCPServer(SignedUpUsers userList, SignedUpProjects projectList) throws IOException {
        super();
        serverSocket = new ServerSocket(PORT_TCP);  //creo il socket TCP

        this.projectList = projectList;
        clients = new ArrayList<>();
        System.out.println("server TCP in ascolto su: " + PORT_TCP);
        this.userList = userList;
        this.multicastGens = new ArrayList<>();
        this.multicastAddresses = new ArrayList<>();
        this.NAME_FILE = "MulticastIP.json";
        File addresses = new File(NAME_FILE);

        ConcurrentHashMap<String,Project> map = projectList.getList();
        Collection<Project> values = map.values();
        this.dataProjects = new ArrayList<>(values);

        //Per ogni progetto controllo le informazioni per il multicast
        int projectPort;
        String mProject;
        MulticastSocket msServer;
        for(Project currProject: dataProjects){ //ottento le informazioni dei progetti per il server
            projectPort = currProject.getPort();
            mProject = currProject.getMulticast();
            msServer = new MulticastSocket(projectPort); // creo il multicast socket sulla porta del progetto
            InetAddress group = InetAddress.getByName(mProject);
            msServer.joinGroup(group); //aggiungo il server alla porta del progetto
            msServer.setSoTimeout(3000);
            multicastAddresses.add(new infoMultiCastConnection(msServer,projectPort,mProject));
        }

        //creo il file con gli indirizzi multicast
        try{
            if(addresses.exists()){
                if(!(projectList.getList().isEmpty())) {
                    this.multicastGen = new MulticastGen(224,0,0,0);
                    searchFile();
                }
                else this.multicastGen = new MulticastGen(224,0,0,0);
            }else{
                if(!addresses.createNewFile()){
                    System.err.println("Problemi durante la creazione del file degli indirizzi multicast");
                }
                else this.multicastGen = new MulticastGen(224,0,0,0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(Map.Entry<String, User> user : userList.getList().entrySet()){
            User currUser = user.getValue();
            if(currUser.getStatus().equals("online"))
                currUser.setStatus("offline");
        }

    }

    public void TCPStart() throws IOException {
        while(true){
            // Aspetto una connessione
            Socket sock = serverSocket.accept();

            System.out.println("connessione accettata da: " + sock.getInetAddress().getHostAddress());

            // Apro gli stream di Input e Output verso il socket
            ois = new ObjectInputStream(sock.getInputStream());

            oos = new ObjectOutputStream(sock.getOutputStream());

            //creo l'oggetto che descrive le informazioni sulla connessione del client
            ClientInfo clientInfo = new ClientInfo();

            clientInfo.setObjectInputStream(ois);
            clientInfo.setObjectOutputStream(oos);
            clientInfo.setSocket(sock);

            LoggedInHandler client = new LoggedInHandler(clientInfo, this, this.userList);
            executor.execute(client); //avvio il thread

        }
    }


    public synchronized Login<UserAndStatus> login(User u, String currUsername) throws RemoteException {
        String result = null;

        ArrayList<UserAndStatus> list = new ArrayList<>();
        Login<UserAndStatus> login;
        ArrayList<infoMultiCastConnection> multicast = new ArrayList<>();
        if(u.getName().isEmpty() || u.getPassword().isEmpty()){
            result = "Nome utente o password vuoti";
        }


        boolean tmp = false;
        for(Map.Entry<String, User> user : userList.getList().entrySet()) {
            User currUser = user.getValue();
            if(currUser.getName().equals(u.getName()))
                if(currUser.getPassword().equals(u.getPassword())){
                    if(currUser.getStatus().equals("offline")){
                        tmp = true;
                        currUser.setStatus("online");
                        update(currUser.getName(), "online");

                        for(Map.Entry<String, Project> project : projectList.getList().entrySet()) {
                            Project currProject = project.getValue();   //informazioni multicast di cui l'utente è membro
                            if(currProject.isInProject(currUsername))
                                multicast.add(new infoMultiCastConnection(null, currProject.getPort(), currProject.getMulticast()));
                        }
                    } else result = "Utente gia loggato";
                } else result = "Password errata";

            list.add(new UserAndStatus(currUser.getName(), currUser.getStatus()));  //lista degli utenti
        }
        if(!tmp && result == null){
            result = "Utente non registrato nel sistema";
        }

        if(tmp){
            login = new Login<>("OK", list,multicast);
        }
        else {
            login = new Login<>(result,null,multicast);
        }

        return login;
    }

    @Override
    public synchronized String logout(String nickName , String currUsername) throws RemoteException {

        if(nickName.isEmpty()){
            return  "Nome utente vuoto";
        }

        for(Map.Entry<String, User> u : userList.getList().entrySet()) {
            User currUser = u.getValue();
            System.out.println("currUsername: " + currUsername + " currUser: " + currUser.getName() + " status: " + currUser.getStatus());
            if(currUser.getName().equals(currUsername)){
                if(currUser.getName().equals(nickName))
                    if(currUser.getStatus().equals("online")){
                        update(nickName,"offline");
                        currUser.setStatus("offline");
                        return "OK";
                    }
                    else return "L'utente non è online";
                if(currUser.getStatus().equals("online"))
                    return "Stai cercando di disconnettere un altro utente";
            }
        }

        return "L'utente non è registrato";

    }

    @Override
    public  ArrayList<UserAndStatus> listUsers(){
        ArrayList<UserAndStatus> list = new ArrayList<>();

        Collection<User> values = userList.getList().values();
        ArrayList<User> currData = new ArrayList<>(values);

        for(User currUser: currData){
            list.add(new UserAndStatus(currUser.getName(), currUser.getStatus()));
        }
        return list;
    }

    @Override
    public ArrayList<UserAndStatus> listOnlineusers() {
        ArrayList<UserAndStatus> list = new ArrayList<>();

        Collection<User> values = userList.getList().values();
        ArrayList<User> currData = new ArrayList<>(values);

        for(User currUser: currData){
            if(currUser.getStatus().equals("online"))
                list.add(new UserAndStatus(currUser.getName(), currUser.getStatus()));
        }

        return list;
    }


    @Override
    public ArrayList<Project> listProjects(String username) {
        ArrayList<Project> list = new ArrayList<>();

        Collection<Project> values = projectList.getList().values();
        ArrayList<Project> currData  = new ArrayList<>(values);

        if(currData.isEmpty()) return null;
        for(Project currProject: currData){
            if(currProject.isInProject(username))
                list.add(currProject);
        }
        if(list.isEmpty()) return null; //L'utente non collabora a nessun progetto
        return list;


    }

    @Override
    public ToClientProject createProject(String projectName, String username) {
        if(projectName.isEmpty()) {
            return new ToClientProject("Nome progetto vuoto", null);
        }

        Collection<Project> values = projectList.getList().values();
        ArrayList<Project> currData  = new ArrayList<>(values);

        for(Project currProject: currData){
            if(currProject.getName().equals(projectName)){
                return new ToClientProject("Progetto gia esistente", null);
            }
        }
        Random rand = new Random();
        int port = rand.nextInt((65535-1024))+1025; //escludo da tutte le 65535 le 1024 porte conosciute
                                                    //per generare un intero nelle porte disponibili
        String mip = this.multicastGen.randomIP();

        storeMulticast(multicastGen);    //salvo l'indirizzo multicast nel file json
        Project project = new Project(projectName,username,port,mip);
        if(projectList.addProject(project).equals("OK")){
            try { //Callback per l'utente per aggiornare le informazioni di multicast
                updateMulticast(project,username);
            } catch (RemoteException e) { e.printStackTrace(); }

            MulticastSocket mServer; //aggiungo il server al multicast
            try {
                mServer = new MulticastSocket(port);
                mServer.joinGroup(InetAddress.getByName(mip));
                mServer.setSoTimeout(3000);
                multicastAddresses.add(new infoMultiCastConnection(mServer,port,mip));

            } catch (IOException e) {
                e.printStackTrace();


            }

            return new ToClientProject("OK" , project);
        }
        return new ToClientProject("Non è stato possibile aggiungere il progetto", null);
    }

    @Override
    public synchronized String addMember(String projectName, String username , String currUsername) {


        boolean userExist = false;

        Collection<User> values = userList.getList().values();
        ArrayList<User> currData = new ArrayList<>(values);

        for(User currUser: currData){   //controllo se l'utente da aggiungere al progetto esiste
            if(currUser.getName().equals(username)){
                userExist = true;
                break;
            }
        }


        if(!userExist) return "L'utente che vuoi aggiungere non esiste";
        for(Map.Entry<String, Project> project : projectList.getList().entrySet()) {
            Project currProject = project.getValue();
            if(currProject.getName().equals(projectName)){
                if(currProject.getProjectMembers().contains(currUsername)){
                    if(!(currProject.isInProject(username))){   //se l'utente non è già presente nel progetto
                        currProject.addMember(username);
                        projectList.store();    //aggiorno il file dei progetti con il nuovo utente
                        try { //Callback per informare l'utente delle info multicast
                            updateMulticast(currProject,username);
                        } catch (RemoteException e) { e.printStackTrace(); }

                        return "OK";
                    }else return "L'utente è gia presente nel progetto";
                }else return "L'utente non è un membro del progetto";
            }
        }

        return "Non esiste un progetto con questo nome";

    }

    @Override
    public ToClient<String> showMembers(String projectName, String currUsername) {
        ArrayList<String> list = new ArrayList<>();

        Collection<Project> values = projectList.getList().values();

        ArrayList<Project> currData  = new ArrayList<>(values);
        if(currData.isEmpty())
            return new ToClient<>("Non esiste nessun progetto",null);
        for(Project currProject: currData){
            if(currProject.getName().equals(projectName))
                if(currProject.getProjectMembers().contains(currUsername)){
                    if(currProject.getProjectMembers()!=null)
                        list.addAll(currProject.getProjectMembers());
                    return new ToClient<>("OK",list);
                }else {
                    return new ToClient<>("L'utente non è un membro del progetto",null);
                }
        }

        return new ToClient<>("Non esiste un progetto con questo nome",null);
    }

    @Override
    public ToClient<Card> showCards(String projectName,String currUsername) {
        String result;
        ArrayList<Card> list = new ArrayList<>();

        Collection<Project> values = projectList.getList().values();
        ArrayList<Project> currData  = new ArrayList<>(values);

        for(Project currProject: currData){
            if(currProject.getName().equals(projectName))
                if(currProject.getProjectMembers().contains(currUsername)){
                    if(currProject.getCards()!=null){
                        list.addAll(currProject.getCards());
                    }
                    result = "OK";
                    return new ToClient<>(result, list);
                }else {
                    result = "L'utente non è un membro del progetto";
                    return new ToClient<>(result, list);
                }
        }

        return new ToClient<>("Non esiste un progetto con questo nome", null);
    }

    @Override
    public ToClient<Card> showCard(String projectName, String cardName, String currUsername) {
        String result;
        ArrayList<Card> list = new ArrayList<>();

        Collection<Project> values = projectList.getList().values();
        ArrayList<Project> currData  = new ArrayList<>(values);

        for(Project currProject: currData){
            if(currProject.getName().equals(projectName))
                if(currProject.getProjectMembers().contains(currUsername)){
                    if(currProject.cardHistory(cardName) == null){
                        result = "Questa card non esiste";
                        return new ToClient<>(result,null);
                    }else
                        list = new ArrayList<>(currProject.getCards());

                    result = "OK";
                    return new ToClient<>(result,list);
                }else{
                    result = "L'utente non è un membro del progetto";
                    return new ToClient<>(result,null);
                }
        }
        return new ToClient<>("non esiste un progetto con questo nome",null);
    }

    @Override
    public String addCard(String projectName, String cardName, String description,String currUsername) {
        String result;

        for(Map.Entry<String, Project> project : projectList.getList().entrySet()) {
            Project currProject = project.getValue();
            if(currProject.getName().equals(projectName))
                if(currProject.getProjectMembers().contains(currUsername)){
                    result = currProject.addCard(cardName,description);
                    projectList.store();    //aggiorno il file dei progetti con la card aggiunta
                    return result;
                }else return "L'utente non è un membro del progetto";
        }


        return "non esiste un progetto con questo nome";
    }

    @Override
    public String moveCard(String projectName, String cardName, String partenza, String arrivo,String currUsername) {
        String result;


        for(Map.Entry<String, Project> project : projectList.getList().entrySet()) {
            Project currProject = project.getValue();
            if(currProject.getName().equals(projectName))
                if(currProject.getProjectMembers().contains(currUsername)){
                    result = currProject.moveCard(cardName,partenza,arrivo);
                    projectList.store();    //aggiorno il file dei progetti con la card spostata
                    return result;
                }else return "L'utente non è un membro del progetto";
        }
        
        return "non esiste un progetto con questo nome";
    }

    @Override
    public ToClient<String> getCardHistory(String projectName, String cardName,String currUsername) {
        ArrayList<String> list;
        String result;

        Collection<Project> values = projectList.getList().values();
        ArrayList<Project> currData  = new ArrayList<>(values);

        for(Project currProject: currData){
            if(currProject.getName().equals(projectName))
                if(currProject.getProjectMembers().contains(currUsername)){
                    if(currProject.cardHistory(cardName) == null){
                        result = "Questa card non esiste";
                        return new ToClient<>(result,null);
                    }
                    else
                        list = new ArrayList<>(currProject.cardHistory(cardName));

                    result = "OK";
                    return new ToClient<>(result,list);
                }else {
                    result = "L'utente non è un membro del progetto";
                    return new ToClient<>(result,null);
                }
        }

        return new ToClient<>("non esiste un progetto con questo nome",null);
    }

    @Override
    public ToClientChat sendChatMsg(String projectName,String currUsername) {

        Collection<Project> values = projectList.getList().values();
        ArrayList<Project> currData  = new ArrayList<>(values);

        for(Project currProject: currData){
            if(currProject.getName().equals(projectName))
                if((currProject.getProjectMembers().contains(currUsername))){
                    return new ToClientChat("OK",currProject.getMulticast());
                }else return new ToClientChat("L'utente non è un membro del progetto", null);
        }

        return new ToClientChat("non esiste un progetto con questo nome",null);
    }

    @Override
    public ToClientChat readChat(String projectName, String currUsername) {

        Collection<Project> values = projectList.getList().values();
        ArrayList<Project> currData  = new ArrayList<>(values);

        for(Project currProject: currData){
            if(currProject.getName().equals(projectName))
                if(currProject.getProjectMembers().contains(currUsername)){

                    return new ToClientChat("OK", currProject.getMulticast());
                }else return new ToClientChat("L'utente non è un membro del progetto" , null);

        }

        return new ToClientChat("non esiste un progetto con questo nome", null);
    }

    @Override
    public String cancelProject(String projectName, String currUsername){
        int countCards = 0;

        ConcurrentHashMap<String,Project> projectMap = projectList.getList();
        for(Map.Entry<String, Project> project : projectMap.entrySet()) {
            Project currProject = project.getValue();
            if(currProject.getName().equals(projectName))
                if(currProject.getProjectMembers().contains(currUsername)){
                    countCards = currProject.countCards();
                    if(currProject.getDONE().size() == countCards){
                        //rimuovo il progetto
                        if(projectList.getList().remove(currProject.getName())==null){
                            return "Impossibile rimuovere il progetto";
                        }
                        if(projectList.getList().isEmpty()) {
                            this.multicastGen.reset();
                            System.out.println(this.multicastGen);
                        }
                        projectList.store();    //aggiorno il file dei progetti
                        currProject.deleteDirectory(new File("./" + currProject.getName()));
                        try{
                            updateCancelProject(currProject); //callback per rimozione progetto
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        //aggiorno il multicast
                        infoMultiCastConnection mserverLeave = null;
                        for(infoMultiCastConnection info: multicastAddresses){
                            if(info.getmAddress().equals(currProject.getMulticast())){
                                try {
                                    info.getMulticastsocket().leaveGroup(InetAddress.getByName(currProject.getMulticast()));
                                    mserverLeave = info;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if(mserverLeave!=null) multicastAddresses.remove(mserverLeave);

                        return "OK";
                    }else return  "Ci sono delle card che non sono terminate";
                }else return "L'utente non è un membro del progetto";
        }



        return "non esiste un progetto con questo nome";
    }

    //aggiorna le informazioni multicast dei client
    public void updateMulticast(Project projectName,String nickName) throws RemoteException {
        doMulticastCallBacks(projectName,nickName);
    }

    private synchronized void doMulticastCallBacks(Project project,String nickName) throws RemoteException {
        System.out.println("Callback multicast iniziate");
        for (InfoCallback callbackinfoUser : clients) {
            Notify_Interface client = callbackinfoUser.getClient();
            if (callbackinfoUser.getNickUtente().equals(nickName))
                client.notifyMulticastEvent(project.getMulticast(), project.getPort());
        }
        System.out.println("Callback multicast finite");
    }

    public void updateCancelProject(Project currProject) throws RemoteException{
        doCancelProjectCallbacks(currProject);
    }

    private synchronized void doCancelProjectCallbacks(Project currProject){
        System.out.println("Callback cancellazione progetto iniziate");
        for(InfoCallback callbackinfoUser: clients){
            Notify_Interface client = callbackinfoUser.getClient();
            try{
                if(currProject.isInProject(callbackinfoUser.getNickUtente())){
                    client.notifyCancelProject(currProject.getMulticast(), currProject.getPort());
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
        System.out.println("Callback cancellazione progetto finite");
    }

    //RMI
    @Override
    public String register(String nickUtente, String password) throws RemoteException {

        if(nickUtente.isEmpty() || password.isEmpty()) {
            System.err.println("Il nome utente o la password non possono essere vuoti");
            throw new IllegalArgumentException("Nome utente o password vuoti");
        }

        User user = new User(nickUtente,password);
        if(userList.addUser(user)) {
            update(nickUtente,"offline");
            return "OK"; //OK se l'utente viene registrato
        }

        return null;    //null se l'utente non viene registrato
    }

    @Override
    public synchronized void registerForCallback (Notify_Interface ClientInterface, String nickUtente) throws RemoteException {
        boolean contains = clients.stream()
                .anyMatch(client -> ClientInterface.equals(client.getClient()));
        if (!contains){
            clients.add(new InfoCallback(ClientInterface,nickUtente));
            System.out.println("Aggiunto un nuovo utente alla callback: " + nickUtente);
        }
    }

    public void update(String nickName, String status) throws RemoteException {
        doCallbacks(nickName,status);
    }

    private synchronized void doCallbacks(String nickName, String status) throws RemoteException {
        LinkedList<Notify_Interface> errors = new LinkedList<>();
        System.out.println("callback iniziate");
        for (InfoCallback callbackinfoUser : clients) {
            Notify_Interface client = callbackinfoUser.getClient();
            try {
                client.notifyEvent(nickName, status);
            } catch (RemoteException e) {
                errors.add(client);
            }
        }
        if(!errors.isEmpty()) { //se c'è un errore
            System.out.println("errore nella registrazione di un client alla callback");
            for(Notify_Interface Ne : errors) unregisterForCallback(Ne);
        }
        System.out.println("callbacks completate");
    }

    @Override
    public synchronized void unregisterForCallback(Notify_Interface Client) throws RemoteException {
        InfoCallback user = clients.stream()
                .filter(client -> Client.equals(client.getClient()))
                .findAny()
                .orElse(null);
        if (user!=null) {
            clients.remove(user);
            System.out.println("client rimosso dalla callback");
        }
        else System.out.println("errore durante la rimozione del client dalla callback");
    }

    public void searchFile(){
        try{
            //apro il file degli indirizzi multicast e lo leggo
            FileInputStream fis = new FileInputStream(NAME_FILE);
            InputStreamReader in = new InputStreamReader(fis);
            MulticastGen[] dataArray = new Gson().fromJson(in,MulticastGen[].class);
            this.multicastGen = dataArray[0];
            fis.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //metodo usato per salvare sul file multicast
    public synchronized void storeMulticast(MulticastGen mip){
        try {
            FileOutputStream fos = new FileOutputStream(NAME_FILE);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            multicastGens.add(mip);

            //scrivo sul file
            String s = gson.toJson(multicastGens);
            byte[] b = s.getBytes();
            fos.write(b);

            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
