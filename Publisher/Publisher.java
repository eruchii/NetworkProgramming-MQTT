package Publisher;

/*
    Ho va ten: Nguyen Hai Long
    MSSV: 18020037
    Start thread khoi tao tin nhan va 1 thread tuong tac vs client
*/

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Publisher {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Nhap IP: ");
        String serverHost = sc.nextLine();
        Boolean ready = false;
        Socket socketOfClient = null;
        OutputStream os = null;
        InputStream is = null;
        byte[] buff = new byte[4096];

        final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

        // chay thread sinh thong bao
        MessageGenerator messageGenerator = new MessageGenerator(messageQueue, "Out of Message");
        new Thread(messageGenerator).start();
        ClientInteractHandler clientInteractHandler = new ClientInteractHandler(messageQueue,"Out of Message", serverHost);
        new Thread(clientInteractHandler).start();
    }

}