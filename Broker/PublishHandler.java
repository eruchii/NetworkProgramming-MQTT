
/*
    Ho va ten: Nguyen Chi Thanh
    MSSV: 18020053
    Broker (multithread)
*/
package Broker;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import Models.Message;

public class PublishHandler implements Runnable {
    private Map<String, Set<String>> subscribers;
    private Map<String, ClientHandler> clients;
    private Map<String, Set<String>> patterns;
    private final BlockingQueue<Message> messageQueue;

    public PublishHandler(Map<String, ClientHandler> clients, Map<String, Set<String>> subscribers,
            Map<String, Set<String>> patterns,
            BlockingQueue<Message> messageQueue) {
        this.subscribers = subscribers;
        this.messageQueue = messageQueue;
        this.clients = clients;
        this.patterns = patterns;
    }

    private String convertGlobToRegex(String pattern) {
        StringBuilder sb = new StringBuilder(pattern.length());
        int inGroup = 0;
        int inClass = 0;
        int firstIndexInClass = -1;
        char[] arr = pattern.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            char ch = arr[i];
            switch (ch) {
                case '\\':
                    if (++i >= arr.length) {
                        sb.append('\\');
                    } else {
                        char next = arr[i];
                        switch (next) {
                            case ',':
                                // escape not needed
                                break;
                            case 'Q':
                            case 'E':
                                // extra escape needed
                                sb.append('\\');
                            default:
                                sb.append('\\');
                        }
                        sb.append(next);
                    }
                    break;
                case '*':
                    if (inClass == 0)
                        sb.append(".*");
                    else
                        sb.append('*');
                    break;
                case '?':
                    if (inClass == 0)
                        sb.append('.');
                    else
                        sb.append('?');
                    break;
                case '[':
                    inClass++;
                    firstIndexInClass = i+1;
                    sb.append('[');
                    break;
                case ']':
                    inClass--;
                    sb.append(']');
                    break;
                case '.':
                case '(':
                case ')':
                case '+':
                case '|':
                case '^':
                case '$':
                case '@':
                case '%':
                    if (inClass == 0 || (firstIndexInClass == i && ch == '^'))
                        sb.append('\\');
                    sb.append(ch);
                    break;
                case '!':
                    if (firstIndexInClass == i)
                        sb.append('^');
                    else
                        sb.append('!');
                    break;
                case '{':
                    inGroup++;
                    sb.append('(');
                    break;
                case '}':
                    inGroup--;
                    sb.append(')');
                    break;
                case ',':
                    if (inGroup > 0)
                        sb.append('|');
                    else
                        sb.append(',');
                    break;
                default:
                    sb.append(ch);
            }
        }
        return sb.toString();
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

            Set<String> subs = new HashSet<>();
            Set<String> s = subscribers.get(msg.topic);
            if(s != null && !s.isEmpty()){
                subs.addAll(s);
            }
            for(Map.Entry<String, Set<String>> entry : patterns.entrySet()){
                String pattern = convertGlobToRegex(entry.getKey());
                if(msg.topic.matches(pattern)){
                    // System.out.println(pattern + " matches " + msg.topic);
                    subs.addAll(entry.getValue());
                }
            }
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