import java.net.URLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;


public class ReferenceLink {
  private String urlString;
  private String passKeyDir;
  private URLConnection con;
  public void setUrl(String url){this.urlString = url;}
  public void setPassKeyDir(String pass){this.passKeyDir = pass;}
  public String getUrl(){return this.urlString;}
  public String getPassKeyDir(){return this.passKeyDir;}

  public ReferenceLink(String url, String passKeyDir){
    this.setPassKeyDir(passKeyDir);
    this.setUrl(url);
  }

  public byte[] encrypted() {
    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    String time = formatter.format(date);
    try{
      String key = "";
      try{key = (new Scanner(new File(this.passKeyDir))).next();}
      catch (FileNotFoundException f) {System.out.println("key file not found");}
      // Create key and cipher
      Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
      Cipher cipher = Cipher.getInstance("AES");
      // encrypt the text
      cipher.init(Cipher.ENCRYPT_MODE, aesKey);
      byte[] encrypted = cipher.doFinal(time.getBytes());
      return encrypted;
    } catch (Exception e){
      e.printStackTrace();
      System.out.println("decryption error");
      return "ERROR".getBytes();
    }
  }

  public ArrayList<String> pollUsers(){
    ArrayList<String> data = new ArrayList<String>();
    try{
      byte[] enc = encrypted();
      if (new String(enc).equals("ERROR")) throw new Exception("error");
      this.con = new URL(this.urlString).openConnection();
      this.con.setDoOutput(true);
      OutputStream out = this.con.getOutputStream();
      out.write(enc);
      BufferedReader reader = new BufferedReader(new InputStreamReader(this.con.getInputStream()));
      String line;
      while((line = reader.readLine()) != null) data.add(new String(line));
      return data;
    }
    catch (Exception e){
      data.add("none");
      e.printStackTrace();
      return data;
    }
  }
}
