package Broker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.UUID;

import Models.Message;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private Map<String, ClientHandler> clients; 
    private Map<String, Set<String>> subscribers;
    private final BlockingQueue<Message> messageQueue;
    private Set<String> subTopics;
    private String clientId;
    public ClientHandler(Socket socket, Map<String, ClientHandler> clients, Map<String, Set<String>> subscribers, BlockingQueue<Message> messageQueue) {
        this.clientSocket = socket;
        this.subscribers = subscribers;
        this.messageQueue = messageQueue;
        this.clients = clients;
        this.subTopics =  new HashSet<>();
    }
    final String QUIT = "500 bye";
    final String HELLO = "CONNACK";
    final String COMMAND_NOT_FOUND = "400 Command not found";
    private InputStream is = null;
    private OutputStream os = null;

    public void sendData(String data) throws IOException{
        if(os != null){
            os.write(data.getBytes());
            os.flush();
        }
    }

    private void sub(String topic)
	{
		Set<String> list;
		list = subscribers.get(topic);
		if (list == null)
		{
			synchronized (subscribers)
			{
				if ((list = subscribers.get(topic)) == null)
				{
					list = new CopyOnWriteArraySet<>();
					subscribers.put(topic, list);
				}
			}
		}
		list.add(this.clientId);
        subTopics.add(topic);
	}

    private void unsub(String topic)
	{
		Set<String> list;
		list = subscribers.get(topic);
		if (list != null)
		{
            list.remove(this.clientId);
		}
        try{
            subTopics.remove(topic);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
	}

    private void cleanOnDisconnect(){
        if(clientId == null){
            return;
        }
        clients.remove(clientId);
        for (String s : subTopics) {
            unsub(s);
        }
    }

    private boolean pub(String topic, String message){
        Set<String> l = subscribers.get(topic);
		if (l != null && !l.isEmpty())
		{
            Message m = new Message(topic, message);
			messageQueue.add(m);
			return true;
		}
		return true;
    }

    public void onRecvMsg(Message msg){
        try{
            sendData("PUBLISH " + msg.topic + " " + msg.data.toString());
        }
        catch(IOException e){
        }
        
    }

    @Override
    public void run()
    {
        String line;
        boolean ready = false;
        try{
            is = clientSocket.getInputStream();
            os = clientSocket.getOutputStream();
            while (true) {
                byte[] buff = new byte[4096];
                int cc = is.read(buff);
                if(cc < 0){
                    break;
                }
                line = new String(buff, StandardCharsets.UTF_8).substring(0, cc);
                System.out.println(line);
                if(line.equals("CONNECT")){
                    ready = true;
                    this.clientId = UUID.randomUUID().toString();
                    System.out.println(this.clientId);
                    synchronized(clients){
                        clients.put(clientId, this);
                    }
                    sendData(HELLO);
                    continue;
                }
                if(!ready){
                    continue;
                }
                String[] command = line.split(" ", 3);
                if (command[0].equals("SUBSCRIBE")) {
                    String topic  = command[1];
                    sub(topic);
                    sendData("SUBACK " + topic);
                    continue;
                }
                if (command[0].equals("UNSUBSCRIBE")) {
                    String topic  = command[1];
                    unsub(topic);
                    sendData("UNSUBACK " + topic);
                    continue;
                }
                if (command[0].equals("PUBLISH")) {
                    String topic  = command[1];
                    String message = command[2];
                    boolean x = pub(topic, message);
                    if(x){
                        sendData("PUBACK");
                    }
                    continue;
                }
                os.write(COMMAND_NOT_FOUND.getBytes());
                os.flush();
            }
        }
        catch (IOException e) {
            System.out.println(clientId + " disconnected");
        }
        cleanOnDisconnect();
    }
}