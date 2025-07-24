package edu.ohsu.cmp.fhirproxy.controller;

import edu.ohsu.cmp.fhirproxy.model.ClientInfo;
import edu.ohsu.cmp.fhirproxy.service.CacheService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrationController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CacheService cacheService;

    @PostMapping("register")
    public ResponseEntity<String> registerClient(HttpSession session,
                                                 @RequestParam String clientId,
                                                 @RequestParam String serverUrl,
                                                 @RequestParam String bearerToken,
                                                 @RequestParam String patientId,
                                                 @RequestParam String userId) {

        ClientInfo clientInfo = new ClientInfo(clientId, serverUrl, bearerToken, patientId, userId);
        String key = cacheService.putClientInfo(clientInfo);
        return new ResponseEntity<>(key, null, HttpStatus.OK);
    }
}
