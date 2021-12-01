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

import Models.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private Map<String, Set<ClientHandler>> subscribers;
    private final BlockingQueue<Message> messageQueue;
    private List<String> subTopics;
    public ClientHandler(Socket socket, Map<String, Set<ClientHandler>> subscribers, BlockingQueue<Message> messageQueue) {
        this.clientSocket = socket;
        this.subscribers = subscribers;
        this.messageQueue = messageQueue;
        this.subTopics = new ArrayList<>();
    }

    final String FILE_NOT_FOUND = "410 File Not Found";
    final String DOWNLOAD_OK = "210 Download Mode OK";
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
		Set<ClientHandler> list;
		list = subscribers.get(topic);
		if (list == null)
		{
			synchronized (this)
			{
				if ((list = subscribers.get(topic)) == null)
				{
					list = new CopyOnWriteArraySet<>();
					subscribers.put(topic, list);
				}
			}
		}
		list.add(this);
        subTopics.add(topic);
	}

    private void unsub(String topic)
	{
		Set<ClientHandler> list;
		list = subscribers.get(topic);
		if (list != null)
		{
            list.remove(this);
		}
        subTopics.remove(topic);
	}

    private void cleanOnDisconnect(){

        for (String s : subTopics) {
            unsub(s);
        }
    }

    private boolean pub(String topic, String message){
        Set<ClientHandler> l = subscribers.get(topic);
		if (l != null && !l.isEmpty())
		{
            Message m = new Message(topic, message);
			messageQueue.add(m);
			return true;
		}
		return false;
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
        try{
            is = clientSocket.getInputStream();
            os = clientSocket.getOutputStream();
            sendData(HELLO);
            while (true) {
                byte[] buff = new byte[4096];
                int cc = is.read(buff);
                if(cc < 0){
                    break;
                }
                line = new String(buff, StandardCharsets.UTF_8).substring(0, cc);
                System.out.println(line);
                String[] command = line.split(" ");
                if (command[0].equals("SUBSCRIBE")) {
                    String topic  = command[1];
                    sub(topic);
                    System.out.println(subscribers.get(topic).size());
                    sendData("SUBACK " + topic);
                    continue;
                }
                if (command[0].equals("UNSUBSCRIBE")) {
                    String topic  = command[1];
                    unsub(topic);
                    System.out.println(subscribers.get(topic).size());
                    sendData("UNSUBACK " + topic);
                    continue;
                }
                if (command[0].equals("PUBLISH")) {
                    String topic  = command[1];
                    String message = command[2];
                    boolean x = pub(topic, message);
                    if(x){
                        sendData("PUBACK " + topic + " " + message);
                    }
                    System.out.println(messageQueue.size());
                    continue;
                }
                os.write(COMMAND_NOT_FOUND.getBytes());
                os.flush();
            }
        }
        catch (IOException e) {
            System.out.println(e);
            // e.printStackTrace();   
        }
        cleanOnDisconnect();
    }
}