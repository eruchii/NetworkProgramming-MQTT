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
                if(count % 2 == 0){
                    os.write(("PUBLISH xyz jkasd hajkdhashjkd asdkaj sdak dask d"+String.valueOf(count)).getBytes());
                }
                else{
                    os.write(("PUBLISH abc asikdjlasdj asldj asdl jadlj a dal da"+String.valueOf(count)).getBytes());
                }
                os.flush();
                Thread.sleep(100);
                count++;
                if(count == 50){
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