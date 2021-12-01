

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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import Models.Message;


public class PublishHandler implements Runnable{
    private Map<String, Set<ClientHandler>> subscribers;
    private final BlockingQueue<Message> messageQueue;
    public PublishHandler( Map<String, Set<ClientHandler>> subscribers, BlockingQueue<Message> messageQueue) {
        this.subscribers = subscribers;
        this.messageQueue = messageQueue;
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
            Set<ClientHandler> subs = subscribers.get(msg.topic);
            if (subs == null || subs.isEmpty())
			{
				continue;
			}
            for(ClientHandler client: subs){
                client.onRecvMsg(msg);
            }
        }
    }
}