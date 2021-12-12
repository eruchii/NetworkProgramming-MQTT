package Subscriber;


/*
    Ho va ten: Hoang Minh Duc Anh
    MSSV: 18020003
    Tao 1 luong de doc message tu server, tao 1 cua so de xuat ra message vua doc cung nhu nhan text tu ban phim
*/

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

import javax.swing.AbstractAction;
import javax.swing.Action;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Subscriber extends Application {
    private static TextArea textArea;
    private TextField textField;
    private static InputFromKeyBoard inputFromKeyBoard = new InputFromKeyBoard();

    Action action = new AbstractAction()
    {
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        System.out.println("some action");
    }
    };

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setAlwaysOnTop(true);
        VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setSpacing(5);
        textField = new TextField();
        textArea = new TextArea();
        textArea.setEditable(false);
        textField.setPrefHeight(10);
        textField.setPromptText("Command to Server");
        textArea.setPrefHeight(490);
        textArea.positionCaret(4);
        textArea.setText("Nhap IP: ");
        root.getChildren().add(textField);
        root.getChildren().add(textArea);
        
        Scene scene = new Scene(root, 600, 500);


        scene.setOnKeyPressed(event -> {
            KeyCode k = event.getCode();
            if(k == KeyCode.CONTROL || k == KeyCode.ALT || k == KeyCode.SHIFT || k == KeyCode.WINDOWS) {
                // do nothing
            }
            else if(k == KeyCode.ENTER) {
                textArea.appendText(textField.getText() + "\n");
                inputFromKeyBoard.setInputFromKeyBoard(textField.getText());
                inputFromKeyBoard.setIsEndText(1);
                textField.clear();
            }

        });

        primaryStage.setTitle("Output");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args) {
        new Thread(() -> Application.launch(Subscriber.class, args)).start();
        String serverHost = inputFromKeyBoard.getStringFromKeyBoard();
        Socket socketOfClient = null;
        OutputStream os = null;
        InputStream is = null;
        byte[] buff = new byte[4096];
        Thread iThread = null;

        try {
            socketOfClient = new Socket(serverHost, 9999);
            os = socketOfClient.getOutputStream();
            is = socketOfClient.getInputStream();

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + serverHost);
            return;
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + serverHost);
            return;
        }
        System.err.println("Connected to " + serverHost);
        try {
            os.write("CONNECT".getBytes());
            os.flush();
            int recv_bytes = is.read(buff);
            if(recv_bytes == 0) {
                return;
            }
            String resp = new String(buff, StandardCharsets.UTF_8).substring(0, recv_bytes);
            if (resp.startsWith("CONNACK")) {
                textArea.appendText("FROM SERVER: " + resp + "\n");
            }
            InputHandler ih = new InputHandler(is, textArea);
            iThread = new Thread(ih);
            iThread.start();
            while (true) {
                String inp = inputFromKeyBoard.getStringFromKeyBoard();
                os.write(inp.getBytes());
            }
            
        } catch (UnknownHostException e) {
            System.err.println("Trying to connect to unknown host: " + e);
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
        }
        finally{
            try {
                os.close();
                is.close();
                socketOfClient.close();
                if(iThread != null){
                    iThread.interrupt();
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            } 
        }

    }

}