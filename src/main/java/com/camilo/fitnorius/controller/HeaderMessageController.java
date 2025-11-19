package com.camilo.fitnorius.controller;

import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.*;

@RestController
@RequestMapping("/header-messages")
@CrossOrigin(origins = "*")
public class HeaderMessageController {

    private final ObjectMapper mapper = new ObjectMapper();
    private final File jsonFile;

    public HeaderMessageController() throws Exception {

        // Ruta donde se guardará el archivo
        String folderPath = "src/main/resources/data";
        File folder = new File(folderPath);

        if (!folder.exists()) {
            folder.mkdirs(); // Crear carpeta si no existe
        }

        jsonFile = new File(folder, "headerMessages.json");

        // Crear JSON si no existe
        if (!jsonFile.exists()) {
            Map<String, Object> defaultData = new HashMap<>();
            defaultData.put("messages", new ArrayList<>());

            mapper.writeValue(jsonFile, defaultData);
        }
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
