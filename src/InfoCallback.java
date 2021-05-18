import java.io.Serializable;

public class InfoCallback implements Serializable {
    private Notify_Interface client;
    private String nickUtente;

    /**
     *
     * @param client interfaccia
     * @param nickUtente username
     * classe usata per salvare le informazioni per la callback
     */
    public InfoCallback(Notify_Interface client, String nickUtente) {
        this.client = client;
        this.nickUtente = nickUtente;
    }

    public Notify_Interface getClient() {
        return client;
    }

    public void setClient(Notify_Interface client) {
        this.client = client;
    }

    public String getNickUtente() {
        return nickUtente;
    }

    public void setNickUtente(String nickUtente) {
        this.nickUtente = nickUtente;
    }
}
