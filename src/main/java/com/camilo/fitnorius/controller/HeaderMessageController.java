package com.camilo.fitnorius.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.ClassPathResource;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.Map;

@RestController
@RequestMapping("/header-messages")
@CrossOrigin(origins = "*")
public class HeaderMessageController {

    private final ObjectMapper mapper = new ObjectMapper();
    private final File jsonFile;

    public HeaderMessageController() throws Exception {
        jsonFile = new ClassPathResource("headerMessages.json").getFile();
    }

    @GetMapping
    public Map<String, Object> getMessages() throws Exception {
        return mapper.readValue(jsonFile, Map.class);
    }

    @PutMapping
    public Map<String, Object> updateMessages(@RequestBody Map<String, Object> newData) throws Exception {
        mapper.writeValue(jsonFile, newData);
        return newData;
    }
}
