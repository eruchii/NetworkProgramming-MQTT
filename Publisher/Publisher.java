package Publisher;


/*
    Ho va ten: Nguyen Chi Thanh
    MSSV: 18020053
    Publisher
*/

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Publisher {

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
            int count = 0;
            while (true) {
                os.write("PUBLISH abc abc".getBytes());
                os.flush();
                Thread.sleep(1000);
                count++;
                if(count == 10){
                    break;
                }
                
            }
        } catch (UnknownHostException e) {
            System.err.println("Trying to connect to unknown host: " + e);
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
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