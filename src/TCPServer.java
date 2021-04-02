
import Model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class TCPServer implements ServerInterface{
    private static final int PORT_TCP = 9999;
    private String NAME_FILE; //file per gli indirizzi multicast

    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private ArrayList<User> dataUsers;
    private ArrayList<Project> dataProjects;

    private ArrayList<MulticastGen> multicastGens;
    private ArrayList<infoMultiCastConnection> multicastAddresses;


    ServerSocket serverSocket; //serverSocket per TCP
    SignedUpUsers userList;
    SignedUpProjects projectList;
    RMI_register_Class register;
    MulticastGen multicastGen;

    /**
     * Il pool dei thread in esecuzione
     */
    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();


    public TCPServer(SignedUpUsers userList, SignedUpProjects projectList) throws IOException, ClassNotFoundException {
        serverSocket = new ServerSocket(PORT_TCP);  //creo il socket TCP

        this.projectList = projectList;

        System.out.println("server TCP in ascolto su: " + PORT_TCP);
        this.userList = userList;
        this.multicastGens = new ArrayList<>();
        this.multicastAddresses = new ArrayList<>();
        this.NAME_FILE = "MulticastIP.json";
        File addresses = new File(NAME_FILE);
        //this.dataUsers = new ArrayList<>();
        /*userList.getList().forEach((s, user) -> {//copio il contenuto della hashmap degli utenti in un array
            synchronized (user){
                dataUsers.add(user);
            }
        });

         */

        /*
        projectList.getList().forEach((s,Project) ->{//copio il contenuto della hashmap dei progetti in un array
            synchronized (Project){
                dataProjects.add(Project);
            }
        });

         */
        ConcurrentHashMap<String,Project> map = projectList.getList();
        Collection<Project> values = map.values();
        this.dataProjects = new ArrayList<>(values);

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
            System.out.println("costruttore TCPServer " + Thread.currentThread().getName());


            register = new RMI_register_Class(userList);

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

        ConcurrentHashMap<String,Project> projectMap = projectList.getList();
        ConcurrentHashMap<String,User> userMap = userList.getList();
        /*ArrayList<Project> currDataProject = new ArrayList<>();
        projectList.getList().forEach((s,Project) ->{
            synchronized (Project){
                currDataProject.add(Project);
            }
        });


        ArrayList<User> currDataUser = new ArrayList<>();
        userList.getList().forEach((s,User) ->{
            synchronized (User){
                currDataUser.add(User);
            }
        });

         */

        boolean tmp = false;
        for(Map.Entry<String, User> user : userMap.entrySet()) {
            User currUser = user.getValue();
            if(currUser.getName().equals(u.getName()))
                if(currUser.getPassword().equals(u.getPassword())){
                    if(currUser.getStatus().equals("offline")){
                        tmp = true;
                        currUser.setStatus("online");
                        register.update(currUser.getName(), "online");

                        for(Map.Entry<String, Project> project : projectMap.entrySet()) {
                            Project currProject = project.getValue();
                            if(currProject.isInProject(currUsername))
                                multicast.add(new infoMultiCastConnection(null, currProject.getPort(), currProject.getMulticast()));
                        }
                    } else result = "Utente gia loggato";
                } else result = "Password errata";

            list.add(new UserAndStatus(currUser.getName(), currUser.getStatus()));
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

        /*
        if(currDataUser.isEmpty()) result = "Nessun utente registrato";
        for(User currUser: currDataUser){
            if(u.getName().equals(currUser.getName()))
                if(u.getPassword().equals(currUser.getPassword())){
                    if(currUser.getStatus().equals("offline")){
                        tmp = true;
                        currUser.setStatus("online");
                        register.update(currUser.getName(),"online");
                        for(Project currProject : currDataProject) {
                            if (currProject.isInProject(currUsername))
                                multicast.add(new infoMultiCastConnection(null, currProject.getPort(), currProject.getMulticast()));
                        }
                    }else result = "Utente già loggato";
                }else result = "password errata";
            list.add(new UserAndStatus(currUser.getName(), currUser.getStatus()));
        }

         */


        return login;
    }

    @Override
    public synchronized String logout(String nickName , String currUsername) throws RemoteException {

        if(nickName.isEmpty()){
            return  "Nome utente vuoto";
        }

        String result;
        ConcurrentHashMap<String,User> map = userList.getList();

        for(Map.Entry<String, User> u : map.entrySet()) {
            User currUser = u.getValue();
            System.out.println("currUser: " + currUser.getName());

            if(currUser.getName().equals(currUsername)){
                if(currUser.getName().equals(nickName))
                    if(currUser.getStatus().equals("online")){
                        register.update(nickName,"offline");
                        currUser.setStatus("offline");
                        return "OK";
                    }
                    else return "L'utente non è online";
                if(currUser.getStatus().equals("online"))
                    return "Stai cercando di disconnettere un altro utente";
            }
        }


        return "L'utente non è registrato";

        /*
        Collection<User> values = map.values();
        ArrayList<User> currData = new ArrayList<>(values);

        if(dataUsers.isEmpty()) result = "Nessun utente registrato";
        for(User currUser: currData){
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


         */

    }

    @Override
    public synchronized ArrayList<UserAndStatus> listUsers(){
        ArrayList<UserAndStatus> list = new ArrayList<>();

        ConcurrentHashMap<String,User> map = userList.getList();
        Collection<User> values = map.values();
        ArrayList<User> currData = new ArrayList<>(values);

        for(User currUser: currData){
            list.add(new UserAndStatus(currUser.getName(), currUser.getStatus()));
        }
        return list;
    }

    @Override
    public synchronized ArrayList<UserAndStatus> listOnlineusers() {
        ArrayList<UserAndStatus> list = new ArrayList<>();

        ConcurrentHashMap<String,User> map = userList.getList();
        Collection<User> values = map.values();
        ArrayList<User> currData = new ArrayList<>(values);

        for(User currUser: currData){
            if(currUser.getStatus().equals("online"))
                list.add(new UserAndStatus(currUser.getName(), currUser.getStatus()));
        }

        return list;
    }


    @Override
    public synchronized ArrayList<Project> listProjects(String username) {
        ArrayList<Project> list = new ArrayList<>();

        ConcurrentHashMap<String,Project> map = projectList.getList();
        Collection<Project> values = map.values();
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
    public synchronized ToClientProject createProject(String projectName, String username) {
        if(projectName.isEmpty()) {
            return new ToClientProject("Nome progetto vuoto", null);
        }

        ConcurrentHashMap<String,Project> map = projectList.getList();
        Collection<Project> values = map.values();
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
        projectList.addProject(project);
        projectList.store();
        try { //Callback per l'utente per aggiornare le informazioni di multicast
            updateMulticast(project,username);
        } catch (RemoteException e) { e.printStackTrace(); }

        MulticastSocket mServer; //aggiungo il server al multicast
        try {
            mServer = new MulticastSocket(port);
            mServer.joinGroup(InetAddress.getByName(mip));
            mServer.setSoTimeout(3000);
            multicastAddresses.add(new infoMultiCastConnection(mServer,port,mip));
            for(infoMultiCastConnection info: multicastAddresses)
                System.out.println("multicastaddresses info dentro createproject: " + info.getmAddress());
        } catch (IOException e) {
            e.printStackTrace();


        }

        return new ToClientProject("OK" , project);
    }

    @Override
    public synchronized String addMember(String projectName, String username , String currUsername) {
        String result;

        /*
        ArrayList<User> currDataUsers = new ArrayList<>();
        userList.getList().forEach((s,User) ->{
            synchronized (User){
                currDataUsers.add(User);
            }
        });

        ArrayList<Project> currDataProject = new ArrayList<>();
        projectList.getList().forEach((s,Project) ->{
            synchronized (Project){
                currDataProject.add(Project);
            }
        });

         */

        ConcurrentHashMap<String,User> userMap = userList.getList();
        ConcurrentHashMap<String,Project> projectMap = projectList.getList();

        boolean userExist = false;

        Collection<User> values = userMap.values();
        ArrayList<User> currData = new ArrayList<>(values);

        for(User currUser: currData){
            if(currUser.getName().equals(username)){
                userExist = true;
                break;
            }
        }


        if(!userExist) return "L'utente che vuoi aggiungere non esiste";
        for(Map.Entry<String, Project> project : projectMap.entrySet()) {
            Project currProject = project.getValue();
            if(currProject.getName().equals(projectName)){
                if(currProject.getProjectMembers().contains(currUsername)){
                    if(!(currProject.isInProject(username))){
                        currProject.addMember(username);
                        projectList.store();
                        try { //Callback per informare dell'aggiunta di un nuovo utente
                            updateMulticast(currProject,username);
                        } catch (RemoteException e) { e.printStackTrace(); }

                        return "OK";
                    }else return "L'utente è gia presente nel progetto";
                }else return "L'utente non è un membro del progetto";
            }
        }


        /*
        for(Project currProject: currDataProject){
            if(currProject.getName().equals(projectName)) {
                if (currProject.getProjectMembers().contains(currUsername)) {
                    if (!(currProject.isInProject(username))) {
                        currProject.addMember(username);
                        projectList.store(); //aggiorno il file
                        result = "OK";
                        return result;
                    } else {
                        result = "L'utente è gia presente nel progetto";
                        return result;
                    }
                } else {
                    result = "L'utente non è un membro del progetto";
                    return result;
                }
            }
        }
         */

        return "Non esiste un progetto con questo nome";

    }

    @Override
    public synchronized ToClient<String> showMembers(String projectName, String currUsername) {
        ArrayList<String> list = null;
        String result = null;

        ConcurrentHashMap<String,Project> map = projectList.getList();
        Collection<Project> values = map.values();
        ArrayList<Project> currData  = new ArrayList<>(values);

        for(Project currProject: currData){
            if(currProject.getName().equals(projectName))
                if(currProject.getProjectMembers().contains(currUsername)){
                    list = currProject.getProjectMembers();
                    result = "OK";
                    return new ToClient<>(result,list);
                }else {
                    result = "L'utente non è un membro del progetto";
                    return new ToClient<>(result,null);
                }
            else result = "Non esiste un progetto con questo nome";
        }

        return new ToClient<>(result,null);
    }

    @Override
    public synchronized ToClient<Card> showCards(String projectName,String currUsername) {
        String result = null;
        ArrayList<Card> list = new ArrayList<>();

        ConcurrentHashMap<String,Project> map = projectList.getList();

        Collection<Project> values = map.values();
        ArrayList<Project> currData  = new ArrayList<>(values);

        for(Project currProject: currData){
            if(currProject.getName().equals(projectName))
                if(currProject.getProjectMembers().contains(currUsername)){
                    //list = currProject.getCards();
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


        /*
        for(Map.Entry<String, Project> project : map.entrySet()) {
            Project currProject = project.getValue();
            if(currProject.getName().equals(projectName))
                if(currProject.getProjectMembers().contains(currUsername)){
                    list = currProject.getCards();
                    System.out.println("list size: " + list.size() + " Thread: " + Thread.currentThread().getName());
                    for(Card currcard: list)
                        System.out.println(currcard);
                    result = "OK";
                    return new ToClient<>(result, list);
                }else {
                    result = "L'utente non è un membro del progetto";
                    return new ToClient<>(result, null);
                }
            else result = "Non esiste un progetto con questo nome";
        }

         */
        return new ToClient<>("Non esiste un progetto con questo nome", null);
    }

    @Override
    public String addCard(String projectName, String cardName, String description,String currUsername) {
        String result;

        ConcurrentHashMap<String,Project> projectMap = projectList.getList();
        for(Map.Entry<String, Project> project : projectMap.entrySet()) {
            Project currProject = project.getValue();
            if(currProject.getName().equals(projectName))
                if(currProject.getProjectMembers().contains(currUsername)){
                    result = currProject.addCard(cardName,description);
                    projectList.store();
                    return result;
                }else return "L'utente non è un membro del progetto";
        }

        /*
        for(Project currProject: currData){
            if(currProject.getName().equals(projectName))
                if(currProject.getProjectMembers().contains(currUsername)){
                    result = currProject.addCard(cardName,description);
                    projectList.store();
                    return result;
                }else return "L'utente non è un membro del progetto";
        }

         */

        return "non esiste un progetto con questo nome";
    }

    @Override
    public String moveCard(String projectName, String cardName, String partenza, String arrivo,String currUsername) {
        String result;
        System.out.println("move " + Thread.currentThread().getName());

        ConcurrentHashMap<String,Project> projectMap = projectList.getList();
        for(Map.Entry<String, Project> project : projectMap.entrySet()) {
            Project currProject = project.getValue();
            if(currProject.getName().equals(projectName))
                if(currProject.getProjectMembers().contains(currUsername)){
                    result = currProject.moveCard(cardName,partenza,arrivo);
                    projectList.store();
                    return result;
                }else return "L'utente non è un membro del progetto";
        }

        /*
        for(Project currProject: currData){
            if(currProject.getName().equals(projectName))
                if(currProject.getProjectMembers().contains(currUsername)){
                    result = currProject.moveCard(cardName,partenza,arrivo);
                    projectList.store();
                    return result;
                }else return "L'utente non è un membro del progetto";
        }
         */

        return "non esiste un progetto con questo nome";
    }

    @Override
    public synchronized ToClient<String> getCardHistory(String projectName, String cardName,String currUsername) {
        ArrayList<String> list = null;
        String result = null;

        /*
        for(Map.Entry<String, Project> project : projectMap.entrySet()) {
            Project currProject = project.getValue();
            System.out.println("progetto: " + currProject.getName());
            if(currProject.getName().equals(projectName))
                if(currProject.getProjectMembers().contains(currUsername)){
                    list = currProject.cardHistory(cardName);
                    if(list == null){
                        result = "Questa card non esiste";
                        return new ToClient<>(result,null);
                    }
                    result = "OK";
                    return new ToClient<>(result,list);
                }else {
                    result = "L'utente non è un membro del progetto";
                    return new ToClient<>(result,null);
                }
        }

         */

        ConcurrentHashMap<String,Project> projectMap = projectList.getList();

        Collection<Project> values = projectMap.values();
        ArrayList<Project> currData  = new ArrayList<>(values);

        for(Project currProject: currData){
            if(currProject.getName().equals(projectName))

                if(currProject.getProjectMembers().contains(currUsername)){
                    list = currProject.cardHistory(cardName);
                    if(list == null){
                        result = "Questa card non esiste";
                        return new ToClient<>(result,null);
                    }
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
    public synchronized ToClientChat sendChatMsg(String projectName,String currUsername) {
        ConcurrentHashMap<String,Project> projectMap = projectList.getList();

        Collection<Project> values = projectMap.values();
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
    public synchronized ToClientChat readChat(String projectName, String currUsername) {
        ConcurrentHashMap<String,Project> projectMap = projectList.getList();

        Collection<Project> values = projectMap.values();
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
    public synchronized String cancelProject(String projectName, String currUsername) throws IOException {
        String result = null;
        int countCards = 0;

        ConcurrentHashMap<String,Project> projectMap = projectList.getList();
        for(Map.Entry<String, Project> project : projectMap.entrySet()) {
            Project currProject = project.getValue();
            if(currProject.getName().equals(projectName))
                if(currProject.getProjectMembers().contains(currUsername)){
                    countCards = currProject.countCards();
                    if(currProject.getDONE().size() == countCards){
                        //rimuovo il progetto
                        projectList.getList().remove(currProject.getName());
                        projectList.store();
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

        /*
        for(Project currProject: currData){
            if(currProject.getName().equals(projectName))
                if(currProject.getProjectMembers().contains(currUsername)){
                    countCards = currProject.countCards();
                    if(currProject.getDONE().size() == countCards){
                        //rimuovo il progetto
                        System.out.println(projectList.getList());
                        projectList.getList().remove(currProject.getName());
                        projectList.store();
                        currProject.deleteDirectory(new File("./" + currProject.getName()));

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

         */

        return "non esiste un progetto con questo nome";
    }

    //aggiorna le informazioni multicast dei client
    public void updateMulticast(Project projectName,String nickName) throws RemoteException {
        doMulticastCallBacks(projectName,nickName);
    }

    private synchronized void doMulticastCallBacks(Project project,String nickName) throws RemoteException {
        System.out.println("Callback multicast iniziate");
        for (InfoCallback callbackinfoUser : register.getClients()) {
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
        for(InfoCallback callbackinfoUser: register.getClients()){
            Notify_Interface client = callbackinfoUser.getClient();
            try{
                if(currProject.isInProject(callbackinfoUser.getNickUtente())){
                    client.notifyCancelProject(currProject.getMulticast(), currProject.getPort());
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    }

    public synchronized void searchFile(){
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
