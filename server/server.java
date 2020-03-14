import java.net.*;
import java.io.*;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Scanner;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class server {

  private static String encrypt(String str, String keyDir){
    try{
      String key = "";
      try{key = (new Scanner(new File(keyDir))).next();}
      catch (FileNotFoundException f) {System.out.println("key file not found");}
      // Create key and cipher
      Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
      Cipher cipher = Cipher.getInstance("AES");
      // encrypt the text
      cipher.init(Cipher.ENCRYPT_MODE, aesKey);
      byte[] encrypted = cipher.doFinal(str.getBytes());
      String out = new String(encrypted);
      return URLEncoder.encode(out,"UTF-8");
    } catch (Exception e){
      e.printStackTrace();
      System.out.println("encryption error");
      return "ERROR";
    }
  }

  public static String encryptedTime(String keyDir) {
    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    String time = formatter.format(date);
    return encrypt(time, keyDir);
  }



  public static void main(String[] args) {
    String keyDir = "tKey.txt";
    ServerSocket socket = null;
    try {socket = new ServerSocket(8080);}
    catch (IOException e) {
      System.err.println("issue with port 8080");
      System.exit(-6);
    }
    Socket client = null;
    System.out.println("Server Online\n\n");
    String httpThing = "HTTP/1.1 200 OK \r\n";

    ArrayList<Client> clients = new ArrayList<Client>();

    //server loop
    while(true){
      try{
        client = socket.accept();
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        String received = in.readLine();
        while((received = in.readLine()) != null) {
          if (received.length() > 3 && received.substring(0,5).equals("RTREQ")){
            break;
          }
        }
        String sub;
        int index;
        received = received.substring(5);
        //if valid
        ArrayList<String> clientData = new ArrayList<String>();
        while(received.contains("&/")){
          index = received.indexOf("&/");
          sub = received.substring(0,index);
          clientData.add(new String(sub));
          try{received = received.substring(index+2);}
          //catch because kinda too lazy to think about how to do this without it
          //probably change this, current way is just use the catch to stop it from substringing over length
          //doesn't matter tho because after this actually is needed the string is never used
          catch (Exception e){received = received.substring(index+1);}
        }
        String timeKey = clientData.get(0);
        String address = clientData.get(1);
        String netMAC = clientData.get(2);
        boolean sharing = false;
        if(clientData.get(3).equals("true")) sharing = true;
        String id = clientData.get(4);
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm, dd-MM-yyyy");
        String time = formatter.format(date);
        OutputStreamWriter out = new OutputStreamWriter(client.getOutputStream());
        if(timeKey.equals(encryptedTime(keyDir))){
          //add client to list
          index = 0;
          while(index < clients.size() && clients.get(index).getID().compareTo(id) <= 0) {
            index++;
          }
          if(index == 0 || clients.get(index-1).getID().compareTo(id) != 0)
            clients.add(index,new Client(address, id, sharing, netMAC));
          System.out.println(clients.size());
          String response = "RTPOST\n";
          for(int i = 0; i < clients.size(); i++) response += clients.get(i).encodeClient() + "\n";
          out.write(httpThing + response.length() + "\r\n\r\n" + response);
          System.out.println("user " + id + " connected at " + time);

        }
        else{
          System.out.println("Unauthorized Attempt to Connect (Bad Key) from user " + id + " at time " + time);
          out.write("BAD CON\n");
        }
        out.close();
      }
      catch (IOException e){
        System.out.println("Unable to Accept Request");
        e.printStackTrace();
      } catch(Exception e){
        System.out.println("Internal Error");
        e.printStackTrace();
      }
    }
  }
}
