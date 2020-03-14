import java.net.URLConnection;
import java.net.URL;
import java.net.NetworkInterface;
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
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URLEncoder;

public class GroupHost {
  private String urlString;
  private String passKeyDir;
  private URLConnection con;
  private boolean sharing;
  private String id;
  private ArrayList<User> users;
  public void setUrl(String url){this.urlString = url;}
  public void setID(String id){this.id = id;}
  public void setPassKeyDir(String pass){this.passKeyDir = pass;}
  public void setSharing(boolean sharing){this.sharing = sharing;}
  public String getUrl(){return this.urlString;}
  public String getID(){return this.id;}
  public String getPassKeyDir(){return this.passKeyDir;}
  public boolean getSharing(){return this.sharing;}
  public ArrayList<User> getUsers(){return this.users;}

  public GroupHost(String url, String passKeyDir, String id, boolean sharing){
    this.setPassKeyDir(passKeyDir);
    this.setUrl(url);
    this.setID(id);
    this.setSharing(sharing);
  }

  private String encrypt(String str){
    try{
      String key = "";
      try{key = (new Scanner(new File(this.passKeyDir))).next();}
      catch (FileNotFoundException f) {System.out.println("key file not found");}
      // Create key and cipher
      Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
      Cipher cipher = Cipher.getInstance("AES");
      // encrypt the text
      cipher.init(Cipher.ENCRYPT_MODE, aesKey);
      byte[] encrypted = cipher.doFinal(str.getBytes());
      String out = new String(encrypted);
      return URLEncoder.encode(out, "UTF-8");
    } catch (Exception e){
      e.printStackTrace();
      System.out.println("encryption error");
      return "ERROR";
    }
  }

  private String encryptedTime() {
    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    String time = formatter.format(date);
    return encrypt(time);
  }

  private String getMacAddressByUseArp(String ip) throws IOException {
      String cmd = "arp -a " + ip;
      Scanner s = new Scanner(Runtime.getRuntime().exec(cmd).getInputStream());
      String str = null;
      Pattern pattern = Pattern.compile("(([0-9A-Fa-f]{2}[-:]){5}[0-9A-Fa-f]{2})|(([0-9A-Fa-f]{4}\\.){2}[0-9A-Fa-f]{4})");
      try {
          while (s.hasNext()) {
              str = s.next();
              Matcher matcher = pattern.matcher(str);
              if (matcher.matches()){
                  break;
              }
              else{
                  str = null;
              }
          }
      }
      finally {
          s.close();
      }
      return (str != null) ? str.toUpperCase(): null;
  }
  private String getLocalNetworkMAC(){
    Runtime runtime = Runtime.getRuntime();
    //if statements to determine os type
    // really stupid function because java can't get default gateway and DOS is actual dogshit
    String osName = System.getProperty("os.name");
    String dGateway = "";
    if(osName.startsWith("Windows")) {
      try{
        Process p = runtime.exec("cmd /c winIP.bat");
        InputStream is = p.getInputStream();
        int i = 0;
        while((i = is.read()) != -1) dGateway += (char)i;
      } catch (Exception e) {e.printStackTrace();}
    }
    else {
      try{
        Process p = runtime.exec("netstat -rn | grep 'default'");
        InputStream is = p.getInputStream();
        int i = 0;
        while((i = is.read()) != -1) dGateway += (char)i;
      } catch (Exception e) {e.printStackTrace();}
    }
    try{
      return getMacAddressByUseArp(dGateway);
    } catch (IOException e){
      System.out.println("Error in finding network identification");
      return "Error";
    }
  }

  public ArrayList<String> decodeUser(String encoded){
    int index;
    String sub;
    ArrayList<String> data = new ArrayList<String>();
    while(encoded.contains("&/")){
      index = encoded.indexOf("&/");
      sub = encoded.substring(0,index);
      data.add(new String(sub));
      try{encoded = encoded.substring(index+2);}
      //catch because kinda too lazy to think about how to do this without it
      //probably change this, current way is just use the catch to stop it from substringing over length
      //doesn't matter tho because after this actually is needed the string is never used
      catch (Exception e){encoded = encoded.substring(index+1);}
    }
    return data;
  }

  public boolean pollUsers(){
    ArrayList<User> data = new ArrayList<User>();
     try{
      this.con = new URL(this.urlString).openConnection();
      this.con.setDoOutput(true);
      OutputStreamWriter out = new OutputStreamWriter(this.con.getOutputStream());
      InetAddress addr = InetAddress.getLocalHost();
      String enc = encryptedTime();
      if (enc.equals("ERROR")) throw new Exception("encryption error");
      String mac = getLocalNetworkMAC();
      out.write("RTREQ" + enc + "&/" + addr.getHostAddress() + "&/" + mac + "&/" + this.sharing + "&/" + this.id + "&/\n");
      out.close();
      BufferedReader in = new BufferedReader(new InputStreamReader(this.con.getInputStream()));
      String line;
      boolean adding = false;
      while((line = in.readLine()) != null) {
        if (line.equals("RTEND")) adding = false;

        if (adding){
          ArrayList<String> vals = decodeUser(line);
          boolean sharing = false;
          if(vals.get(2).equals("true")) sharing = true;
          if(vals.get(1) != this.getID())
            data.add(new User(vals.get(0), vals.get(1), sharing ,vals.get(3)));
        }
        if (line.equals("RTPOST")) adding = true;
      }
      in.close();
      this.users = data;
      return true;
    }
    catch (Exception e){
      System.out.println("connection error");
      e.printStackTrace();
      return false;

    }
  }
}
