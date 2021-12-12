package Subscriber;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import javafx.scene.control.TextArea;

class InputHandler implements Runnable {
    private InputStream is;
    private TextArea textArea;
    public InputHandler(InputStream is, TextArea ta) {
        this.is = is;
        this.textArea = ta;
    }   

    @Override
    public void run() {
        byte[] buff = new byte[4096];
        while (true) {
            int recv_bytes = 0;
            try {
                recv_bytes = is.read(buff);
            } catch (IOException e) {
                break;
            }
            if (recv_bytes == 0) {
                break;
            }
            String resp = new String(buff, StandardCharsets.UTF_8).substring(0, recv_bytes);
            textArea.appendText("FROM SERVER: " + resp + "\n");
        }
    }
}