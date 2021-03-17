package Model;

import java.io.Serializable;

/**
 * classe per generare l'indirizzo multicast da associare al progetto
 */
public class MulticastGen implements Serializable {
    private int p1,p2,p3,p4;
    private String address;

    public MulticastGen(int p1, int p2, int p3, int p4) {
        super();
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.p4 = p4;
    }

    public String randomIP(){
        if(p4<255){
            this.p4++;
            return (p1+"." + p2 + "." +p3 + "." +p4);
        }else{
            if(p3<255){
                this.p3++;
                this.p4=0;
                return (p1+"." + p2 + "." +p3 + "." +p4);
            }else{
                if(p2<255){
                    this.p2++;
                    this.p3=0;
                    this.p4=0;
                    return (p1+"." + p2 + "." +p3 + "." +p4);
                }else{
                    if(p1<239){
                        this.p1++;
                        this.p2=0;
                        this.p3=0;
                        this.p4=0;
                        return (p1+"." + p2 + "." +p3 + "." +p4);
                    }
                }
            }
        }

        return "Errore nella generazione dell'indirizzo IP";
    }

    public int getP1() {
        return p1;
    }

    public void setP1(int p1) {
        this.p1 = p1;
    }

    public int getP2() {
        return p2;
    }

    public void setP2(int p2) {
        this.p2 = p2;
    }

    public int getP3() {
        return p3;
    }

    public void setP3(int p3) {
        this.p3 = p3;
    }

    public int getP4() {
        return p4;
    }

    public void setP4(int p4) {
        this.p4 = p4;
    }

    @Override
    public String toString() {
        return "MulticastGen{" +
                "p1=" + p1 +
                ", p2=" + p2 +
                ", p3=" + p3 +
                ", p4=" + p4 +
                '}';
    }
}
