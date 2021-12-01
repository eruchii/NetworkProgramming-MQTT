package Broker;

/*
    Ho va ten: Nguyen Chi Thanh
    MSSV: 18020053
    Server (multithread)
*/

public class Server {
    public static void main(String args[]) {

        Broker broker = new Broker(); 
        new Thread(broker).start();
        System.out.println("Bye");
    }

}