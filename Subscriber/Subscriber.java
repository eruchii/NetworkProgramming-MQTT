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

public class Subscriber {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Nhap IP: ");
        String serverHost = sc.nextLine();

        Socket socketOfClient = null;
        OutputStream os = null;
        InputStream is = null;
        byte[] buff = new byte[4096];

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
            while (true) {
                int recv_bytes = is.read(buff);
                String resp = new String(buff, StandardCharsets.UTF_8).substring(0, recv_bytes);
                System.out.println("FROM SERVER: " + resp);
                if (resp.startsWith("CONNACK")) {
                    ready = true;
                    os.write("SUBSCRIBE temp".getBytes());
                    os.flush();
                }                   
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
            } catch (IOException e) {
                e.printStackTrace();
            } 
        }

    }

}