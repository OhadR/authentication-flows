package com.watchdox.security.oauth.resource_server.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/welcome")
public class HelloController
{

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public String printWelcome() {

//        model.addAttribute("message", "Spring Security Hello World");
        return "hello from resource server";

    }

}
