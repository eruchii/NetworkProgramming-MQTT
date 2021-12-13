

/*
    Ho va ten: Nguyen Chi Thanh
    MSSV: 18020053
    Broker (multithread)
*/
package Broker;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import Models.Message;


public class Broker implements Runnable{
    private final BlockingQueue<Message> messageQueue;
    private Map<String, ClientHandler> clients;
    private Map<String, Set<String>> subscribers;
    public Broker(){
        this.subscribers = new ConcurrentHashMap<>();
        this.clients = new ConcurrentHashMap<>();
        this.messageQueue = new LinkedBlockingQueue<>();
    }
    @Override
    public void run() {
        
        ServerSocket listener = null;
        PublishHandler pub = new PublishHandler(clients, subscribers, messageQueue);
        new Thread(pub).start();
        try {
            listener = new ServerSocket(8888);
            listener.setReuseAddress(true);
            System.out.println("Server is waiting to accept user...");
            while(true){
                Socket clientSocket = listener.accept();
                System.out.println("New connection: "
                                   + clientSocket.getInetAddress()
                                         .getHostAddress());
                ClientHandler clientSock = new ClientHandler(clientSocket, clients, subscribers, messageQueue);
                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
        finally {
            if (listener != null) {
                try {
                    listener.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}