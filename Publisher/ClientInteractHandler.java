package Publisher;
// Todo :Pop message from queue, Gui request den server, cho doi puback

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.text.StyledEditorKit.BoldAction;

import java.nio.charset.StandardCharsets;

public class ClientInteractHandler implements Runnable{
    // Queue shared between thread to read Message
    private final BlockingQueue<String> messageQueue;
    private final String outOfMessageFlag;
    private final String serverSocketAddress;
    private final int serverPort;

    final String FILE_NOT_FOUND = "410 File Not Found";
    final String DOWNLOAD_OK = "210 Download Mode OK";
    final String CONNECT ="CONNECT";
    final String QUIT = "500 bye";
    final String HELLO = "CONNACK";
    final String COMMAND_NOT_FOUND = "400 Command not found";
    final String PUBACK = "PUBACK";
    private InputStream is = null;
    private OutputStream os = null;
    private Socket serverSocket;
    private Boolean connectToServer = false;
    private Boolean lastSentSuccess = true;
    private String message ="";

    public ClientInteractHandler(BlockingQueue <String> queue, String _outOfMessageFlag, String _serverAddress, int _serverPort) {
        this.messageQueue = queue;
        this.outOfMessageFlag = _outOfMessageFlag;   
        this.serverSocketAddress = _serverAddress;
        this.serverPort = _serverPort;
    }

    @Override
    public void run() {
        try{
             serverSocket = new Socket(serverSocketAddress,serverPort);
             serverSocket.setSoTimeout(5000);
             is = serverSocket.getInputStream();
             os = serverSocket.getOutputStream();

        }
        catch(ConnectException e){
            System.err.println("Cant connect to server");
            System.exit(0);
        }
        catch(IOException e){
            e.printStackTrace();
        }
        while (true){
            try{
                byte[] buff = new byte[4096];
                // nếu chưa connect thành công -> connect đến server
                if(!connectToServer){
                    os.write(CONNECT.getBytes());
                    os.flush();
                    int byteReceived =is.read(buff);
                    if (byteReceived < 0){
                        break;
                    }
                    String line = new String(buff, StandardCharsets.UTF_8).substring(0, byteReceived);
                    if(line.startsWith(HELLO)){
                        connectToServer = true;
                        System.out.println("Done connect to server");
                    }
                }
                else{
                    if (lastSentSuccess){
                        message = messageQueue.take();
                    }
                    
                    if (!message.equals("")){
                        if (!lastSentSuccess){
                            Thread.sleep(1000);
                        }
                        System.out.println("Sending publish: "+ message);
                        sendData(message);
                        int byteReceived =is.read(buff);
                        if (byteReceived < 0){
                            break;
                        }
                        String line = new String(buff, StandardCharsets.UTF_8).substring(0, byteReceived);
                        if(line.equals(PUBACK)){
                            lastSentSuccess = true;
                        }
                        else{
                            lastSentSuccess = false;
                        }
                    }
                }
            }
            catch(SocketException e){
                System.err.println("Lost connection to server");
                System.exit(0);
            }
            catch(InterruptedIOException e){
                lastSentSuccess = false;
                System.out.println("Publish Fail. Receive nothing from server");
            }
            catch(IOException e){
                e.printStackTrace();
            }
            catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }

    }

    public void sendData(String data) throws IOException{
        if(os != null){
            os.write(data.getBytes());
            os.flush();
        }
    }
}