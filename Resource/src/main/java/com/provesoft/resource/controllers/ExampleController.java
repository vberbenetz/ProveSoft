package com.provesoft.resource.controllers;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@RestController
public class ExampleController {

    @RequestMapping(value="/", method= RequestMethod.GET)
    public Map<String, String> home() {

        String message = "Hello World";

        Map<String, String> retMap = new HashMap<>();
        retMap.put("id", UUID.randomUUID().toString());
        retMap.put("content", message);
        return retMap;
    }
}
