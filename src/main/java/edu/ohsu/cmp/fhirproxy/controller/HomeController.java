package edu.ohsu.cmp.fhirproxy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @CrossOrigin
    @GetMapping(value = "status")
    public ResponseEntity<String> status() {
        return new ResponseEntity<>("Service is running.", HttpStatus.OK);
    }
}
