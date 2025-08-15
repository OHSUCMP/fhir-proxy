package edu.ohsu.cmp.fhirproxy.controller;

import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import edu.ohsu.cmp.fhirproxy.exception.ClientInfoNotFoundException;
import edu.ohsu.cmp.fhirproxy.model.ClientInfo;
import edu.ohsu.cmp.fhirproxy.service.RegistrationService;
import edu.ohsu.cmp.fhirproxy.service.ProxyService;
import edu.ohsu.cmp.fhirproxy.util.FhirUtil;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * This class implements FHIR RESTful APIs as described here: https://www.hl7.org/fhir/R4/http.html
 */
@Controller
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/proxy")
public class ProxyController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String REQUEST_HEADER_PAGE_LIMIT = "X-Page-Limit";
    private static final String PARAM_FORMAT = "_format";
    private static final String PARAM_PRETTY = "_pretty";

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private ProxyService proxyService;

    /**
     * Read a resource
     * Implements https://www.hl7.org/fhir/R4/http.html#read
     * @param authorization
     * @param resourceType
     * @param id
     * @param params
     * @return
     */
    @GetMapping("/{resourceType}/{id}")
    public ResponseEntity<String> read(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                       @PathVariable String resourceType,
                                       @PathVariable String id,
                                       @RequestParam Map<String,String> params) {
        HttpHeaders responseHeaders = new HttpHeaders();
        appendContentTypeResponseHeader(responseHeaders, params.get(PARAM_FORMAT));

        try {
            ClientInfo clientInfo = registrationService.get(extractBearerToken(authorization));

            IBaseResource resource = proxyService.read(clientInfo, resourceType, id, params);

            responseHeaders.add("ETag", resource.getMeta().getVersionId());
            responseHeaders.add("Last-Modified", resource.getMeta().getLastUpdated().toString());

            return new ResponseEntity<>(encodeResponse(resource, params), responseHeaders, HttpStatus.OK);

        } catch (ClientInfoNotFoundException cinfe) {
            logger.warn("client info not found for authorization=" + authorization);
            OperationOutcome outcome = new OperationOutcome();
            outcome.addIssue()
                    .setCode(OperationOutcome.IssueType.FORBIDDEN)
                    .setDiagnostics("invalid authorization");

            return new ResponseEntity<>(encodeResponse(outcome, params), responseHeaders, HttpStatus.UNAUTHORIZED);

        } catch (BaseServerResponseException bsre) {
            logger.error(bsre.getMessage());
            return new ResponseEntity<>(encodeResponse(bsre.getOperationOutcome(), params), responseHeaders, bsre.getStatusCode());

        } catch (Exception e) {
            logger.error("caught " + e.getClass().getSimpleName() + " while processing request - " + e.getMessage(), e);

            OperationOutcome outcome = new OperationOutcome();
            outcome.addIssue()
                    .setCode(OperationOutcome.IssueType.EXCEPTION)
                    .setDiagnostics(e.getMessage());

            return new ResponseEntity<>(encodeResponse(outcome, params), responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Read a specific version of a resource
     * Implements https://www.hl7.org/fhir/R4/http.html#vread
     * @param authorization
     * @param resourceType
     * @param id
     * @param vid
     * @param params
     * @return
     */
    @GetMapping("/{resourceType}/{id}/_history/{vid}")
    public ResponseEntity<String> vread(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                       @PathVariable String resourceType,
                                       @PathVariable String id,
                                       @PathVariable String vid,
                                       @RequestParam Map<String,String> params) {
        HttpHeaders responseHeaders = new HttpHeaders();
        appendContentTypeResponseHeader(responseHeaders, params.get(PARAM_FORMAT));

        try {
            ClientInfo clientInfo = registrationService.get(extractBearerToken(authorization));

            IBaseResource resource = proxyService.vread(clientInfo, resourceType, id, vid, params);

            responseHeaders.add("ETag", resource.getMeta().getVersionId());
            responseHeaders.add("Last-Modified", resource.getMeta().getLastUpdated().toString());

            return new ResponseEntity<>(encodeResponse(resource, params), responseHeaders, HttpStatus.OK);

        } catch (ClientInfoNotFoundException cinfe) {
            logger.warn("client info not found for authorization=" + authorization);
            OperationOutcome outcome = new OperationOutcome();
            outcome.addIssue()
                    .setCode(OperationOutcome.IssueType.FORBIDDEN)
                    .setDiagnostics("invalid authorization");

            return new ResponseEntity<>(encodeResponse(outcome, params), responseHeaders, HttpStatus.UNAUTHORIZED);

        } catch (BaseServerResponseException bsre) {
            logger.error(bsre.getMessage());
            return new ResponseEntity<>(encodeResponse(bsre.getOperationOutcome(), params), responseHeaders, bsre.getStatusCode());

        } catch (Exception e) {
            logger.error("caught " + e.getClass().getSimpleName() + " while processing request - " + e.getMessage());
            logger.debug("stack trace: ", e);

            OperationOutcome outcome = new OperationOutcome();
            outcome.addIssue()
                    .setCode(OperationOutcome.IssueType.EXCEPTION)
                    .setDiagnostics(e.getMessage());

            return new ResponseEntity<>(encodeResponse(outcome, params), responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update a resource
     * Implements https://www.hl7.org/fhir/R4/http.html#update
     * @param authorization
     * @param resourceType
     * @param id
     * @param params
     * @param body
     * @return
     */
    @PutMapping("/{resourceType}/{id}")
    public ResponseEntity<String> update(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                         @PathVariable String resourceType,
                                         @PathVariable String id,
                                         @RequestParam Map<String,String> params,
                                         @RequestBody(required = false) String body) { // todo : make body required when implementing
        HttpHeaders responseHeaders = new HttpHeaders();
        appendContentTypeResponseHeader(responseHeaders, params.get(PARAM_FORMAT));

        try {
            ClientInfo clientInfo = registrationService.get(extractBearerToken(authorization));

            // todo : implement this

            OperationOutcome outcome = new OperationOutcome();
            outcome.addIssue()
                    .setCode(OperationOutcome.IssueType.NOTSUPPORTED)
                    .setDiagnostics("update not supported");

            return new ResponseEntity<>(encodeResponse(outcome, params), responseHeaders, HttpStatus.METHOD_NOT_ALLOWED);

        } catch (ClientInfoNotFoundException cinfe) {
            logger.warn("client info not found for authorization=" + authorization);
            OperationOutcome outcome = new OperationOutcome();
            outcome.addIssue()
                    .setCode(OperationOutcome.IssueType.FORBIDDEN)
                    .setDiagnostics("invalid authorization");

            return new ResponseEntity<>(encodeResponse(outcome, params), responseHeaders, HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            logger.error("caught " + e.getClass().getSimpleName() + " while processing request - " + e.getMessage());
            logger.debug("stack trace: ", e);

            OperationOutcome outcome = new OperationOutcome();
            outcome.addIssue()
                    .setCode(OperationOutcome.IssueType.EXCEPTION)
                    .setDiagnostics(e.getMessage());

            return new ResponseEntity<>(encodeResponse(outcome, params), responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Patch a resource
     * Implements https://www.hl7.org/fhir/R4/http.html#patch
     * @param authorization
     * @param resourceType
     * @param id
     * @param params
     * @param body
     * @return
     */
    @PatchMapping("/{resourceType}/{id}")
    public ResponseEntity<String> patch(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                        @PathVariable String resourceType,
                                        @PathVariable String id,
                                        @RequestParam Map<String,String> params,
                                        @RequestBody(required = false) String body) { // todo : make body required when implementing
        HttpHeaders responseHeaders = new HttpHeaders();
        appendContentTypeResponseHeader(responseHeaders, params.get(PARAM_FORMAT));

        try {
            ClientInfo clientInfo = registrationService.get(extractBearerToken(authorization));

            // todo : implement this

            OperationOutcome outcome = new OperationOutcome();
            outcome.addIssue()
                    .setCode(OperationOutcome.IssueType.NOTSUPPORTED)
                    .setDiagnostics("patch not supported");

            return new ResponseEntity<>(encodeResponse(outcome, params), responseHeaders, HttpStatus.METHOD_NOT_ALLOWED);

        } catch (ClientInfoNotFoundException cinfe) {
            logger.warn("client info not found for authorization=" + authorization);
            OperationOutcome outcome = new OperationOutcome();
            outcome.addIssue()
                    .setCode(OperationOutcome.IssueType.FORBIDDEN)
                    .setDiagnostics("invalid authorization");

            return new ResponseEntity<>(encodeResponse(outcome, params), responseHeaders, HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            logger.error("caught " + e.getClass().getSimpleName() + " while processing request - " + e.getMessage());
            logger.debug("stack trace: ", e);

            OperationOutcome outcome = new OperationOutcome();
            outcome.addIssue()
                    .setCode(OperationOutcome.IssueType.EXCEPTION)
                    .setDiagnostics(e.getMessage());

            return new ResponseEntity<>(encodeResponse(outcome, params), responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete a resource
     * Implements https://www.hl7.org/fhir/R4/http.html#delete
     * @param authorization
     * @param resourceType
     * @param id
     * @return
     */
    @DeleteMapping("/{resourceType}/{id}")
    public ResponseEntity<String> delete(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                         @PathVariable String resourceType,
                                         @PathVariable String id,
                                         @RequestParam Map<String,String> params) { // todo : make body required when implementing
        HttpHeaders responseHeaders = new HttpHeaders();
        appendContentTypeResponseHeader(responseHeaders, params.get(PARAM_FORMAT));

        try {
            ClientInfo clientInfo = registrationService.get(extractBearerToken(authorization));

            // todo : implement this

            OperationOutcome outcome = new OperationOutcome();
            outcome.addIssue()
                    .setCode(OperationOutcome.IssueType.NOTSUPPORTED)
                    .setDiagnostics("delete not supported");

            return new ResponseEntity<>(encodeResponse(outcome, params), responseHeaders, HttpStatus.METHOD_NOT_ALLOWED);

        } catch (ClientInfoNotFoundException cinfe) {
            logger.warn("client info not found for authorization=" + authorization);
            OperationOutcome outcome = new OperationOutcome();
            outcome.addIssue()
                    .setCode(OperationOutcome.IssueType.FORBIDDEN)
                    .setDiagnostics("invalid authorization");

            return new ResponseEntity<>(encodeResponse(outcome, params), responseHeaders, HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            logger.error("caught " + e.getClass().getSimpleName() + " while processing request - " + e.getMessage());
            logger.debug("stack trace: ", e);

            OperationOutcome outcome = new OperationOutcome();
            outcome.addIssue()
                    .setCode(OperationOutcome.IssueType.EXCEPTION)
                    .setDiagnostics(e.getMessage());

            return new ResponseEntity<>(encodeResponse(outcome, params), responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Create a resource
     * Implements https://www.hl7.org/fhir/R4/http.html#create
     * @param authorization
     * @param resourceType
     * @param params
     * @param body
     * @return
     */
    @PostMapping("/{resourceType}")
    public ResponseEntity<String> create(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                         @PathVariable String resourceType,
                                         @RequestParam Map<String,String> params,
                                         @RequestBody(required = false) String body) { // todo : make body required when implementing
        HttpHeaders responseHeaders = new HttpHeaders();
        appendContentTypeResponseHeader(responseHeaders, params.get(PARAM_FORMAT));

        try {
            ClientInfo clientInfo = registrationService.get(extractBearerToken(authorization));

            // todo : implement this

            OperationOutcome outcome = new OperationOutcome();
            outcome.addIssue()
                    .setCode(OperationOutcome.IssueType.NOTSUPPORTED)
                    .setDiagnostics("create not supported");

            return new ResponseEntity<>(encodeResponse(outcome, params), responseHeaders, HttpStatus.METHOD_NOT_ALLOWED);

        } catch (ClientInfoNotFoundException cinfe) {
            logger.warn("client info not found for authorization=" + authorization);
            OperationOutcome outcome = new OperationOutcome();
            outcome.addIssue()
                    .setCode(OperationOutcome.IssueType.FORBIDDEN)
                    .setDiagnostics("invalid authorization");

            return new ResponseEntity<>(encodeResponse(outcome, params), responseHeaders, HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            logger.error("caught " + e.getClass().getSimpleName() + " while processing request - " + e.getMessage());
            logger.debug("stack trace: ", e);

            OperationOutcome outcome = new OperationOutcome();
            outcome.addIssue()
                    .setCode(OperationOutcome.IssueType.EXCEPTION)
                    .setDiagnostics(e.getMessage());

            return new ResponseEntity<>(encodeResponse(outcome, params), responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Search for resources - GET strategy
     * Implements https://www.hl7.org/fhir/R4/http.html#search
     * Also see: https://build.fhir.org/http.html#search
     * @param authorization
     * @param pageLimit
     * @param resourceType
     * @param params
     * @return
     */
    @GetMapping(value = {"/{resourceType}", "/{resourceType}/"})
    public ResponseEntity<String> searchByGet(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                              @RequestHeader(value = REQUEST_HEADER_PAGE_LIMIT, required = false) Integer pageLimit,
                                              @PathVariable String resourceType,
                                              @RequestParam Map<String,String> params) {
        return doSearch(authorization, resourceType, params, pageLimit);
    }

    /**
     * Search for resources - POST strategy
     * Implements https://www.hl7.org/fhir/R4/http.html#search
     * Also see: https://build.fhir.org/http.html#search
     * @param authorization
     * @param pageLimit
     * @param resourceType
     * @param params
     * @return
     */
    @PostMapping("/{resourceType}/_search")
    public ResponseEntity<String> searchByPost(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                               @RequestHeader(value = REQUEST_HEADER_PAGE_LIMIT, required = false) Integer pageLimit,
                                               @PathVariable String resourceType,
                                               @RequestParam Map<String,String> params) {
        return doSearch(authorization, resourceType, params, pageLimit);
    }

///////////////////////////////////////////////////////////////////////////////////
/// private methods
///

    private ResponseEntity<String> doSearch(String authorization, String resourceType, Map<String,String> params,
                                            Integer pageLimit) {
        HttpHeaders responseHeaders = new HttpHeaders();
        appendContentTypeResponseHeader(responseHeaders, params.get(PARAM_FORMAT));

        try {
            ClientInfo clientInfo = registrationService.get(extractBearerToken(authorization));

            Bundle bundle = proxyService.search(clientInfo, resourceType, params, pageLimit);

            return new ResponseEntity<>(encodeResponse(bundle, params), responseHeaders, HttpStatus.OK);

        } catch (ClientInfoNotFoundException cinfe) {
            logger.warn("client info not found for authorization=" + authorization);
            OperationOutcome outcome = new OperationOutcome();
            outcome.addIssue()
                    .setCode(OperationOutcome.IssueType.FORBIDDEN)
                    .setDiagnostics("invalid authorization");

            return new ResponseEntity<>(encodeResponse(outcome, params), responseHeaders, HttpStatus.UNAUTHORIZED);

        } catch (BaseServerResponseException bsre) {
            logger.error(bsre.getMessage());
            return new ResponseEntity<>(encodeResponse(bsre.getOperationOutcome(), params), responseHeaders, bsre.getStatusCode());

        } catch (Exception e) {
            logger.error("caught " + e.getClass().getSimpleName() + " while processing request - " + e.getMessage());
            logger.debug("stack trace: ", e);

            OperationOutcome outcome = new OperationOutcome();
            outcome.addIssue()
                    .setCode(OperationOutcome.IssueType.EXCEPTION)
                    .setDiagnostics(e.getMessage());

            return new ResponseEntity<>(encodeResponse(outcome, params), responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void appendContentTypeResponseHeader(HttpHeaders responseHeaders, String format) {
        if (StringUtils.isBlank(format) || doEncodeJson(format)) {
            responseHeaders.add("Content-Type", "application/fhir+json");
        } else if (doEncodeRDF(format)) {
            responseHeaders.add("Content-Type", "application/fhir+turtle");
        } else {
            responseHeaders.add("Content-Type", "application/fhir+xml");
        }
    }

    private String encodeResponse(IBaseResource resource, Map<String,String> params) {
        String format = params.get(PARAM_FORMAT);
        boolean prettyPrint = doPrettyPrint(params);

        String response;
        if (StringUtils.isBlank(format) || doEncodeJson(format)) {
            response = FhirUtil.toJson(resource, prettyPrint);
        } else if (doEncodeRDF(format)) {
            response = FhirUtil.toRDF(resource, prettyPrint);
        } else {
            response = FhirUtil.toXml(resource, prettyPrint);
        }
        return response;
    }

    // see https://www.hl7.org/fhir/R4/http.html#parameters
    private boolean doEncodeJson(String format) {
        return StringUtils.isNotBlank(format) && (
                format.equalsIgnoreCase("json") ||
                        format.equalsIgnoreCase("application/json") ||
                        format.equalsIgnoreCase("application/fhir+json")
        );
    }

    // see https://www.hl7.org/fhir/R4/http.html#parameters
    private boolean doEncodeRDF(String format) {
        return StringUtils.isNotBlank(format) && (
                format.equalsIgnoreCase("ttl") ||
                        format.equalsIgnoreCase("application/fhir+turtle") ||
                        format.equalsIgnoreCase("text/turtle")
        );
    }

    // see https://www.hl7.org/fhir/R4/http.html#parameters
    private boolean doEncodeXml(String format) {
        return StringUtils.isNotBlank(format) && (
                format.equalsIgnoreCase("xml") ||
                        format.equalsIgnoreCase("text/xml") ||
                        format.equalsIgnoreCase("application/xml") ||
                        format.equalsIgnoreCase("application/fhir+xml")
        );
    }

    private boolean doPrettyPrint(Map<String,String> params) {
        String pretty = params.get("_pretty");
        return StringUtils.isNotBlank(pretty) && pretty.equalsIgnoreCase("true");
    }

    private static final String BEARER_TOKEN_PREFIX = "Bearer ";

    private String extractBearerToken(String authorization) {
        if (StringUtils.isBlank(authorization))
            throw new IllegalArgumentException("authorization is blank");

        if ( ! authorization.startsWith(BEARER_TOKEN_PREFIX) )
            throw new IllegalArgumentException("authorization is not a bearer token");

        String key = authorization.substring(BEARER_TOKEN_PREFIX.length());
        if (StringUtils.isBlank(key))
            throw new IllegalArgumentException("bearer token is blank");

        return key;
    }
}
