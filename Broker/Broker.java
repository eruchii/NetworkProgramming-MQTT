

/*
    Ho va ten: Nguyen Chi Thanh
    MSSV: 18020053
    Broker (multithread)
*/
package Broker;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class Broker implements Runnable{
    private Map<String, Set<ClientHandler>> publishers;
    private Map<String, Set<ClientHandler>> subscribers;
    public Broker(){
        publishers = new ConcurrentHashMap<String, Set<ClientHandler>>();
        subscribers = new ConcurrentHashMap<String, Set<ClientHandler>>();
    }
    @Override
    public void run() {
        
        ServerSocket listener = null;
        
        try {
            listener = new ServerSocket(9999);
            listener.setReuseAddress(true);
            System.out.println("Server is waiting to accept user...");
            while(true){
                Socket clientSocket = listener.accept();
                System.out.println("New connection: "
                                   + clientSocket.getInetAddress()
                                         .getHostAddress());
                ClientHandler clientSock = new ClientHandler(clientSocket, publishers, subscribers);
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
        System.out.println("Bye");
    }
}