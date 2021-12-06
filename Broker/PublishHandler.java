

/*
    Ho va ten: Nguyen Chi Thanh
    MSSV: 18020053
    Broker (multithread)
*/
package Broker;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import Models.Message;


public class PublishHandler implements Runnable{
    private Map<String, Set<String>> subscribers;
    private Map<String, ClientHandler> clients;
    private final BlockingQueue<Message> messageQueue;
    public PublishHandler(Map<String, ClientHandler> clients, Map<String, Set<String>> subscribers, BlockingQueue<Message> messageQueue) {
        this.subscribers = subscribers;
        this.messageQueue = messageQueue;
        this.clients = clients;
    }
    @Override
    public void run() {
        Message msg;
        while(true){
            try
			{
				msg = messageQueue.take();
			}
			catch (InterruptedException e)
			{
				continue;
			}
            Set<String> subs = subscribers.get(msg.topic);
            if (subs == null || subs.isEmpty())
			{
				continue;
			}
            for(String clientId: subs){
                ClientHandler client = clients.get(clientId);
                if(client != null){
                    client.onRecvMsg(msg);
                }
            }
        }
    }
}