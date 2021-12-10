package Publisher;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class MessageGenerator implements Runnable{
    // Queue shared between thread to read Message
    private final BlockingQueue<String> messageQueue;
    private final String outOfMessageFlag;

    public MessageGenerator(BlockingQueue <String> queue,String _outOfMessageFlag) {
        this.messageQueue = queue;
        this.outOfMessageFlag = _outOfMessageFlag;   
    }

    public void run(){
        try{
            // try create message
            generateMessage();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    // Generate random message and put to queue
    private void generateMessage() throws IOException{
        try{
            while (true){
                Thread.sleep(ThreadLocalRandom.current().nextInt(1000));
                Integer temp = ThreadLocalRandom.current().nextInt(1000);
                String message = "The Temperature is " + String.valueOf(temp);
                synchronized (this){
                    messageQueue.put(message);
                }
            }
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }

    }
}