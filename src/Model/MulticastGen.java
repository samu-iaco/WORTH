package Model;

public class MulticastGen {
    private int p1,p2,p3,p4;

    public MulticastGen(int p1, int p2, int p3, int p4) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.p4 = p4;
    }   //224.0.0.0
        //239.255.1
    public String randomIP(){
        if(p4<255){
            p4++;
            return (p1+"." + p2 + "." +p3 + "." +p4);
        }else if(p3<255){
            p3++;
            p4=0;
            return (p1+"." + p2 + "." +p3 + "." +p4);
        }else if(p2<255){
            p2++;
            p3=0;
            p4=0;
            return (p1+"." + p2 + "." +p3 + "." +p4);
        }else if(p1<239){
            p1++;
            p2=0;
            p3=0;
            p4=0;
            return (p1+"." + p2 + "." +p3 + "." +p4);
        }

        return "Errore nella generazione dell'indirizzo IP";
    }
}
