package cn.cvzhanshi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author cVzhanshi
 * @create 2021-08-14 17:05
 */
@Controller
public class IndexController {

    @GetMapping({"/","/index"})
    public String index(){
        return "index";
    }
}
