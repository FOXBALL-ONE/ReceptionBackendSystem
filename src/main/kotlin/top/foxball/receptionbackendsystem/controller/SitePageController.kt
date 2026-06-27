package top.foxball.receptionbackendsystem.controller

import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

/**
 * 前台展示页入口。
 *
 * 将 /s 前缀下的所有路径统一回写到 classpath 下的静态页面
 * static/s/index-red.html，交由前端路由接管；后端不做模板渲染。
 */
@Controller
class SitePageController {

    /** 返回前台展示页静态 HTML。 */
    @GetMapping("/s", "/s/", "/s/**")
    fun indexRed(): ResponseEntity<Resource> =
        ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(ClassPathResource("static/s/index-red.html"))
}
