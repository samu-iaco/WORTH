

public interface RMI_login_Interface {

    /**
     *
     * @param nickUtente nome utente
     * @param password password dell'utente
     * @return "ok" se l'operazione ha aggiunto l'utente
     */
    String register(String nickUtente, String password);
}
