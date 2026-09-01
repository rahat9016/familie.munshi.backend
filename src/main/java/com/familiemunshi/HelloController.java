package com.familiemunshi;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String sayHello() {
        return "Welcome to Familie Munshi API! The server is running perfectly.";
    }
}