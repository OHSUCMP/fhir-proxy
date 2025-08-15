package edu.ohsu.cmp.fhirproxy.controller;

import edu.ohsu.cmp.fhirproxy.model.ClientInfo;
import edu.ohsu.cmp.fhirproxy.model.Registration;
import edu.ohsu.cmp.fhirproxy.service.RegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class RegistrationController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RegistrationService registrationService;

    @CrossOrigin
    @PostMapping("register")
    public ResponseEntity<Registration> registerClient(@RequestBody ClientInfo clientInfo) {
        logger.info("registering client " + clientInfo.getClientId() + " for " + clientInfo.getServerUrl());
        Registration registration = registrationService.put(clientInfo);
        return new ResponseEntity<>(registration, null, HttpStatus.OK);
    }
}
