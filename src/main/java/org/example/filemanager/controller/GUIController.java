package org.example.filemanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GUIController {

    @GetMapping("/")
    public String index() {
        return "file-manager";
    }

    @GetMapping("/file-manager")
    public String fileManager() {
        return "file-manager";
    }
}
