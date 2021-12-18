/**
 * 18020037 _ Nguyen Hai Long
 * Hàm sinh thông báo random
 */

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

    private List<String> sensorList = new ArrayList<String>();
    private List<String> locationList = new ArrayList<String>();

    public MessageGenerator(BlockingQueue <String> queue,String _outOfMessageFlag) {
        this.messageQueue = queue;
        this.outOfMessageFlag = _outOfMessageFlag;   
    }

    public void run(){
        try{
            sensorList = Arrays.asList("Pressure", "Temperature", "Humidity","AirQuality");
            locationList = Arrays.asList("LocationA", "LocationB", "LocationC","LocationD");
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
                int temp = ThreadLocalRandom.current().nextInt(100);
                int location = ThreadLocalRandom.current().nextInt(locationList.size());
                int sensor = ThreadLocalRandom.current().nextInt(sensorList.size());
                String topic = locationList.get(location)+ "/" + sensorList.get(sensor);
                String message = "PUBLISH " + topic + " The " + sensorList.get(sensor) + " is " +  String.valueOf(temp);
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