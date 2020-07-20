package cn.webdai.test;

/**
 * bcrypt密码加密测试
 * @author 戴金华
 * @date 2019-12-02 9:32
 */
public class BcryptTest {
    public static void main(String[] args) {
        String gensalt = BCrypt.gensalt();//盐 随机加到密码 随机生成 29个字符
        String password = BCrypt.hashpw("admin", gensalt);//根据盐对密码进行加密
        System.out.println("password:"+password);
    }
}
