package Subscriber;


/*
    Ho va ten: Nguyen Chi Thanh
    MSSV: 18020053
    Subscriber
*/

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Subscriber extends Application {
    private static TextArea textArea;
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setAlwaysOnTop(true);
        VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setSpacing(5);
        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setPrefHeight(500);
        root.getChildren().add(textArea);

        Scene scene = new Scene(root, 600, 500);

        primaryStage.setTitle("Output");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args) {
        new Thread(() -> Application.launch(Subscriber.class, args)).start();
        Scanner sc = new Scanner(System.in);
        System.out.print("Nhap IP: ");
        String serverHost = sc.nextLine();

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
            boolean ready = false;
            int recv_bytes = is.read(buff);
            if(recv_bytes == 0){
                return;
            }
            String resp = new String(buff, StandardCharsets.UTF_8).substring(0, recv_bytes);
            if (resp.startsWith("CONNACK")) {
                ready = true;
                textArea.appendText("FROM SERVER: " + resp + "\n");
            }
            InputHandler ih = new InputHandler(is, textArea);
            iThread = new Thread(ih);
            iThread.start();
            while (true) {
                String inp = sc.nextLine();
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
                sc.close();
                if(iThread != null){
                    iThread.interrupt();
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            } 
        }

    }

}