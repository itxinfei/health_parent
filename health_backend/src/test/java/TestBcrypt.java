/**
 * @author 戴金华
 * @date 2019-11-24 9:38
 */
public class TestBcrypt {
    public static void main(String[] args) {
        String gensalt = BCrypt.gensalt();//盐
        System.out.println(gensalt);
        String password = BCrypt.hashpw("123456", gensalt);
        System.out.println(password);
    }
}
