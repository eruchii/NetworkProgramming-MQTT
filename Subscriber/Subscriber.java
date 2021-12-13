package Subscriber;

/*
    Ho va ten: Hoang Minh Duc Anh
    MSSV: 18020003
    Tao 1 luong de doc message tu server, tao 1 cua so de xuat ra message vua doc cung nhu nhan text tu ban phim
*/

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.Action;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.event.ActionEvent;

public class Subscriber extends Application {
    private TextArea textArea;
    private TextField textField;
    private OutputStream os = null;
    private InputStream is = null;
    private String serverHost = "localhost";
    private int port = 9999;
    private Thread serverThread;
    private boolean isConnected = false;

    private Socket socketOfClient;
    private Thread iThread;
    public void sendToServer(String cmd) {
        if (os != null) {
            try {
                os.write(cmd.getBytes());
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setAlwaysOnTop(true);
        VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setSpacing(5);

        textField = new TextField();
        textField.setPrefHeight(10);
        textField.setPromptText("Command to Server");
        root.getChildren().add(textField);

        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setPrefHeight(490);
        textArea.positionCaret(4);
        root.getChildren().add(textArea);

        Button buttonSubmit = new Button("Connect to server");
        buttonSubmit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(primaryStage);
                VBox dialogVbox = new VBox();
                dialogVbox.setSpacing(5);
                dialogVbox.setPadding(new Insets(10));
                Scene dialogScene = new Scene(dialogVbox, 300, 200);

                TextField ipAdd = new TextField();
                ipAdd.setText(serverHost);
                ipAdd.setPrefHeight(10);
                ipAdd.setPromptText("IP");
                dialogVbox.getChildren().add(ipAdd);
                TextField portText = new TextField();
                portText.setText(String.valueOf(port));
                portText.setPrefHeight(10);
                portText.setPromptText("Port");
                dialogVbox.getChildren().add(portText);
                Button connectButton = new Button("Connect");
                connectButton.setOnAction(new EventHandler<ActionEvent>(){
                    @Override
                    public void handle(ActionEvent event){
                        serverHost = ipAdd.getText();
                        System.out.println(serverHost);
                        port = Integer.parseInt(portText.getText());
                        if(serverThread != null){
                            serverThread.interrupt();
                        }
                        if(iThread != null){
                            iThread.interrupt();
                        }
                        serverThread = new Thread(){
                            public void run(){
                                startConnect();
                            }
                        };
                        serverThread.start();
                        try {
                            serverThread.join();
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        if(isConnected){
                            buttonSubmit.setText("Connected");
                            buttonSubmit.setDisable(true);
                            dialog.close();
                        }                        
                    }
                });
                dialogVbox.getChildren().add(connectButton);
                dialog.setScene(dialogScene);
                dialog.show();

            }
        });
        root.getChildren().add(buttonSubmit);


        Scene scene = new Scene(root, 600, 500);

        scene.setOnKeyPressed(event -> {
            KeyCode k = event.getCode();
            if (k == KeyCode.CONTROL || k == KeyCode.ALT || k == KeyCode.SHIFT || k == KeyCode.WINDOWS) {
                // do nothing
            } else if (k == KeyCode.ENTER) {
                String cmd = textField.getText();
                textField.clear();
                textArea.appendText(cmd + "\n");
                sendToServer(cmd);
            }

        });

        primaryStage.setOnHiding(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Closed");
                        System.exit(0);
                    }
                });
            }
        });

        primaryStage.setTitle("Output");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public void startConnect(){
        try {
            socketOfClient = new Socket(serverHost, port);
            os = socketOfClient.getOutputStream();
            is = socketOfClient.getInputStream();
            System.err.println("Connected to " + serverHost);
            InputHandler ih = new InputHandler(is, textArea);
            iThread = new Thread(ih);
            iThread.start();
            isConnected = true;

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + serverHost);
            isConnected = false;
            // System.exit(0);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + serverHost);
            isConnected = false;
            // System.exit(0);
        }
    }

    public static void main(String[] args) {

        Application.launch(args);
    }

}