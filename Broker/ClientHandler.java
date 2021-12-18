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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private Map<String, ClientHandler> clients; 
    private Map<String, Set<String>> subscribers;
    private final BlockingQueue<Message> messageQueue;
    private Map<String, Set<String>> patterns;
    private Set<String> subTopics;
    private Set<String> subPatterns;
    private String clientId;
    public ClientHandler(Socket socket, Map<String, ClientHandler> clients, 
            Map<String,Set<String>> subscribers, 
            Map<String, Set<String>> patterns,
            BlockingQueue<Message> messageQueue) 
    {
        this.clientSocket = socket;
        this.subscribers = subscribers;
        this.messageQueue = messageQueue;
        this.patterns = patterns;
        this.clients = clients;
        this.subTopics =  new HashSet<>();
        this.subPatterns = new HashSet<>();
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
        synchronized(subTopics){
            try{
                subTopics.remove(topic);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }  
	}

    private void psub(String pattern)
	{
		Set<String> list;
		list = patterns.get(pattern);
		if (list == null)
		{
			synchronized (patterns)
			{
				if ((list = patterns.get(pattern)) == null)
				{
					list = new CopyOnWriteArraySet<>();
					patterns.put(pattern, list);
				}
			}
		}
		list.add(this.clientId);
        subPatterns.add(pattern);
	}

    private void punsub(String pattern)
	{
		Set<String> list;
		list = patterns.get(pattern);
		if (list != null)
		{
            list.remove(this.clientId);
		}
        try{
            subPatterns.remove(pattern);
        }
        catch (Exception e) {
        }
	}

    private void cleanOnDisconnect(){
        if(clientId == null){
            return;
        }
        synchronized(clients){
            clients.remove(clientId);
        }
        for (Iterator<String> iterator = subTopics.iterator(); iterator.hasNext();) {
            String topic = iterator.next();
            Set<String> list;
            list = subscribers.get(topic);
            if (list != null)
            {
                list.remove(this.clientId);
            }
            iterator.remove();
        }
        for (Iterator<String> iterator = subPatterns.iterator(); iterator.hasNext();) {
            String pattern = iterator.next();
            Set<String> list;
            list = patterns.get(pattern);
            if (list != null)
            {
                list.remove(this.clientId);
            }
            iterator.remove();
        } 
    }

    private boolean pub(String topic, String message){
        if(messageQueue.remainingCapacity() <= 0){
            return false;
        }
        Message m = new Message(topic, message);
        messageQueue.add(m);
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
                    sendData(HELLO+ " " + this.clientId);
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
                if(command[0].equals("PSUBSCRIBE")){
                    String pattern = command[1];
                    // pattern = pattern.replace("*", ".*");
                    psub(pattern);
                    sendData("PSUBACK " + pattern);
                    continue;
                }
                if(command[0].equals("PUNSUBSCRIBE")){
                    String pattern = command[1];
                    // pattern = pattern.replace("*", ".*");
                    punsub(pattern);
                    sendData("PUNSUBACK " + pattern);
                    continue;
                }
                os.write(COMMAND_NOT_FOUND.getBytes());
                os.flush();
            }
        }
        catch (IOException e) {
        }
        finally{
            cleanOnDisconnect();
            System.out.println(clientId + " disconnected");
        }

        
    }
}