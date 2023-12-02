package com.example.centrocosto;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String index() {
        return "Home";
    }



    @GetMapping("/gastos")
    public String Gastos() {
        return "gastos";
    }

    @GetMapping("/index")
    public String Index() {
        return "index";
    }
}