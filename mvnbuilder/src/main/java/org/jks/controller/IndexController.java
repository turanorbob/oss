package org.jks.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Create
 * d by Administrator on 2017/5/31.
 */
@Controller
public class IndexController {

    @RequestMapping("/test")
    public void index(HttpServletResponse response) throws IOException{
        response.getWriter().print("helloworld");
        response.getWriter().flush();
    }
}
