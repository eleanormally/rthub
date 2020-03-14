public class User{
  private String id;
  private String ip;
  private String network;
  private boolean sharing;

  public String getID(){return this.id;}
  public String getIP(){return this.ip;}
  public String getNet(){return this.network;}
  public boolean isSharing(){return this.sharing;}

  public void setIP(String ip){this.ip = ip;}
  public void setID(String id){this.id = id;}
  public void setNet(String net){this.network = net;}
  public void setSharing(boolean s){this.sharing = s;}

  public User(String ip, String id, boolean sharing, String network){
    this.ip = ip;
    this.id = id;
    this.sharing = sharing;
    this.network = network;
  }

}
