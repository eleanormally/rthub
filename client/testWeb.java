import java.util.ArrayList;

public class testWeb {
  public static void main(String[] args){
    GroupHost test = new GroupHost("http://192.168.86.32:8080","tKey.txt", "bestPC4", true);
    test.pollUsers();
    ArrayList<User> users = test.getUsers();
    for(int i = 0; i < users.size(); i++) System.out.println(users.get(i).getID());
  }
}
