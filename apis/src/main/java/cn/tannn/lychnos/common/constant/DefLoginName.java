package cn.tannn.lychnos.common.constant;

import java.util.Arrays;
import java.util.List;

/**
 * 默认用户名（登录名
 *
 * @author tnnn
 * @version V1.0
 * @date 2026-01-08 23:16:48
 */
public interface DefLoginName {

    /**
     * administrator
     */
    String ADMINISTRATORS = "administrator";

    /**
     * admin
     */
    String ADMIN = "admin";

    /**
     * tan
     */
    String TAN = "tan";

    /**
     * 不允许注册的用户名（这是我的独有用户名）
     */
    List<String> SUPPER_USER = Arrays.asList(ADMINISTRATORS,ADMIN);
}
