import java.util.ArrayList;

public class testWeb {
  public static void main(String[] args){
    ReferenceLink test = new ReferenceLink("http://192.168.86.32:8080","tKey.txt");
    ArrayList<String> users = test.pollUsers();
    for(int i = 0; i < users.size(); i++) System.out.println(users.get(i));
    System.out.println(users.size());
  }
}
