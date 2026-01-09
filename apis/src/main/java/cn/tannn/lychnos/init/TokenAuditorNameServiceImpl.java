package cn.tannn.lychnos.init;

import cn.tannn.jdevelops.jpa.auditor.AuditorNameService;
import cn.tannn.jdevelops.jwt.standalone.util.JwtWebUtil;
import cn.tannn.jdevelops.utils.jwt.module.SignEntity;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
/**
 * @author tan
 */
@Component
@Slf4j
public class TokenAuditorNameServiceImpl implements AuditorNameService {

    @Resource
    private HttpServletRequest request;

    @Override
    public Optional<String> settingAuditorName() {
        // 自己重新构建
        try {
            SignEntity<String> signEntity = JwtWebUtil.getTokenBySignEntity(request);
            return Optional.of(signEntity.getSubject());
        } catch (Exception e) {
            log.error("自动填充数据创建者时获取当前登录用户的loginName失败");
        }
        return Optional.of("administrator");
    }
}
