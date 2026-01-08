package cn.tannn.lychnos.service;

import cn.tannn.jdevelops.jpa.service.J2ServiceImpl;
import cn.tannn.lychnos.dao.UserInfoDao;
import cn.tannn.lychnos.entity.UserInfo;
import org.springframework.stereotype.Service;

/**
 * 用户
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/8 22:36
 */
@Service
public class UserInfoService extends J2ServiceImpl<UserInfoDao, UserInfo,Long> {
    public UserInfoService() {
        super(UserInfo.class);
    }


}
