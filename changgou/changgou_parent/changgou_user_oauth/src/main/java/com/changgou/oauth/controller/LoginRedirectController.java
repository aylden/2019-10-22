package com.changgou.oauth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author Ayden
 * @Date 2019/9/2 23:25
 * @Description
 * @Version
 **/
@Controller
@RequestMapping("/oauth")
public class LoginRedirectController {
    @RequestMapping("/login")
    public String login(String from, Model model){
        model.addAttribute("from", from);
        return "login";
    }
}
