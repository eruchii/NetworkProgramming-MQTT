package Models;

public class Message{
    public final String topic;
    public final Object data;

    public Message(String topic, Object data){
        this.topic = topic;
        this.data = data;
    }
}