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
  public static String decrypt(String received,String keyDir) {
    try{
      //decryption
      String key = "";
      try{key = (new Scanner(new File(keyDir))).next();}
      catch (FileNotFoundException f){System.out.println("key file not found");throw new Exception("no file");}
      Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.DECRYPT_MODE,aesKey);
      String decrypted = new String(cipher.doFinal(received.getBytes()));
      return decrypted;
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("decryption error");
      return "error";
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
    try{
      client = socket.accept();

      PrintWriter out = new PrintWriter(client.getOutputStream(), true);
      BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

      String received = in.readLine();
      while((received = in.readLine()) != null) System.out.println(received);

      Date date = new Date();
      SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
      String time = formatter.format(date);
      //if valid
      if(decrypt(received,keyDir).equals(time)){
        //give ips
        System.out.println("Valid");

      }
      out.close();
      in.close();
      client.close();
      socket.close();
    }
    catch (IOException e){
      System.err.print("Unable to Accept");
      System.exit(-6);
    }
  }
}
