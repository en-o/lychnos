package cn.tannn.lychnos.common.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/20 22:45
 */
@Getter
@Setter
@ToString
public class UserRequestInfo {
    String ip;
    String username;
    String nickname;
    String loginName;
    String userId;


    public UserRequestInfo() {
    }

    public UserRequestInfo(String ip, String username, String nickname, String loginName, String userId) {
        this.ip = ip;
        this.username = username;
        this.nickname = nickname;
        this.loginName = loginName;
        this.userId = userId;
    }

    public UserRequestInfo(String ip, String nickname, String loginName, String userId) {
        this.ip = ip;
        this.username = nickname==null?loginName:nickname;
        this.nickname = nickname;
        this.loginName = loginName;
        this.userId = userId;
    }

}
