package cn.tannn.lychnos.controller;

import cn.tannn.jdevelops.annotations.web.authentication.ApiMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 前端路由控制器-合并打包要用
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/12 09:31
 */
@Controller
public class IndexPageController {

    /**
     * 处理已知的前端路由
     * 将这些路由请求直接转发到 index.html，避免 404 流程
     */
    @ApiMapping(value = {
            "/"
    }, method = RequestMethod.GET, checkToken = false)
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
