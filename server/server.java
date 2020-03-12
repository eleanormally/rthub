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

public class server {
  public static String encrypted(String keyDir) {
    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    String time = formatter.format(date);
    try{
      String key = "";
      try{key = (new Scanner(new File(keyDir))).next();}
      catch (FileNotFoundException f) {System.out.println("key file not found");}
      // Create key and cipher
      Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
      Cipher cipher = Cipher.getInstance("AES");
      // encrypt the text
      cipher.init(Cipher.ENCRYPT_MODE, aesKey);
      byte[] encrypted = cipher.doFinal(time.getBytes());
      String out = new String(encrypted);
      //stupid thing to fix the fact that char 65533 gets converted to 63 when sent
      out = out.replace((char)65533, (char)63);
      return out;
    } catch (Exception e){
      e.printStackTrace();
      System.out.println("encryption error");
      return "ERROR";
    }
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
    while(true){
      try{
        client = socket.accept();
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        String received = in.readLine();
        while((received = in.readLine()) != null) {
          if (received.length() > 3 && received.substring(0,4).equals("key=")){
            in.close();
            break;
          }
        }
        received = received.substring(4).substring(0,received.length()-4);
        //if valid

        if(received.equals(encrypted(keyDir))){
          //give ips
          Date date = new Date();
          SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
          String time = formatter.format(date);
          System.out.println("user " + user + " connected at " + time);

        }
        else{
          System.out.println("Unauthorized Attempt to Connect (Bad Key)");
        }
      }
      catch (IOException e){
        System.out.println("Unable to Accept Request");
      }
    }
  }
}
