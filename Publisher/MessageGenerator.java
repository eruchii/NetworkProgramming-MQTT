package Publisher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class MessageGenerator implements Runnable{
    // Queue shared between thread to read Message
    private final BlockingQueue<String> messageQueue;
    private final String outOfMessageFlag;

    private List<String> topicList = new ArrayList<String>();

    public MessageGenerator(BlockingQueue <String> queue,String _outOfMessageFlag) {
        this.messageQueue = queue;
        this.outOfMessageFlag = _outOfMessageFlag;   
    }

    public void run(){
        try{
            topicList = Arrays.asList("Pressure", "Temperature", "Humidity","Air Quality");
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
                int randomNumber = ThreadLocalRandom.current().nextInt(4);
                Thread.sleep(randomNumber*1000);
                Integer temp = ThreadLocalRandom.current().nextInt(100);
                System.out.println(randomNumber);
                String message = "PUBLISH " + topicList.get(randomNumber) + " The " + topicList.get(randomNumber) + " is " +    String.valueOf(temp);
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