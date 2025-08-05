package edu.ohsu.cmp.fhirproxy.controller;

import edu.ohsu.cmp.fhirproxy.model.ClientInfo;
import edu.ohsu.cmp.fhirproxy.service.CacheService;
import edu.ohsu.cmp.fhirproxy.service.ProxyService;
import edu.ohsu.cmp.fhirproxy.util.FhirUtil;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@RequestMapping("/proxy")
public class ProxyController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String PARAM_FORMAT = "_format";
    private static final String PARAM_PRETTY = "_pretty";

    @Value("${socket.timeout:300000}")
    private Integer socketTimeout;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private ProxyService proxyService;

    /**
     * Read a resource
     * Implements https://www.hl7.org/fhir/R4/http.html#read
     * Implements https://www.hl7.org/fhir/R4/http.html#vread
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

        try {
            ClientInfo clientInfo = cacheService.get(extractBearerToken(authorization));

            IBaseResource resource = proxyService.read(clientInfo, resourceType, id, params);

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add("ETag", resource.getMeta().getVersionId());
            responseHeaders.add("Last-Modified", resource.getMeta().getLastUpdated().toString());

            return new ResponseEntity<>(encodeResponse(resource, params.get(PARAM_FORMAT)), responseHeaders, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("caught " + e.getClass().getSimpleName() + " while processing request - " + e.getMessage(), e);

            OperationOutcome outcome = new OperationOutcome();
            outcome.addIssue()
                    .setCode(OperationOutcome.IssueType.EXCEPTION)
                    .setDiagnostics(e.getMessage());

            return new ResponseEntity<>(encodeResponse(outcome, params.get(PARAM_FORMAT)), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Read a resource
     * Implements https://www.hl7.org/fhir/R4/http.html#read
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

        try {
            ClientInfo clientInfo = cacheService.get(extractBearerToken(authorization));

            IBaseResource resource = proxyService.vread(clientInfo, resourceType, id, vid, params);

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add("ETag", resource.getMeta().getVersionId());
            responseHeaders.add("Last-Modified", resource.getMeta().getLastUpdated().toString());

            return new ResponseEntity<>(encodeResponse(resource, params.get(PARAM_FORMAT)), responseHeaders, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("caught " + e.getClass().getSimpleName() + " while processing request - " + e.getMessage(), e);

            OperationOutcome outcome = new OperationOutcome();
            outcome.addIssue()
                    .setCode(OperationOutcome.IssueType.EXCEPTION)
                    .setDiagnostics(e.getMessage());

            return new ResponseEntity<>(encodeResponse(outcome, params.get(PARAM_FORMAT)), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update a resource
     * Implements https://www.hl7.org/fhir/R4/http.html#update
     * @param authorization
     * @param resourceType
     * @param id
     * @param params
     * @return
     */
    @PutMapping("/{resourceType}/{id}")
    public ResponseEntity<String> update(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                         @PathVariable String resourceType,
                                         @PathVariable String id,
                                         @RequestParam Map<String,String> params) {
        throw new NotImplementedException("update not implemented");
    }

    /**
     * Patch a resource
     * Implements https://www.hl7.org/fhir/R4/http.html#patch
     * @param authorization
     * @param resourceType
     * @param id
     * @param params
     * @return
     */
    @PatchMapping("/{resourceType}/{id}")
    public ResponseEntity<String> patch(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                        @PathVariable String resourceType,
                                        @PathVariable String id,
                                        @RequestParam Map<String,String> params) {
        throw new NotImplementedException("patch not implemented");
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
                                         @PathVariable String id) {
        throw new NotImplementedException("delete not implemented");
    }

    /**
     * Create a resource
     * Implements https://www.hl7.org/fhir/R4/http.html#create
     * @param authorization
     * @param resourceType
     * @param params
     * @return
     */
    @PostMapping("/{resourceType}")
    public ResponseEntity<String> create(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                         @PathVariable String resourceType,
                                         @RequestParam Map<String,String> params) {
        throw new NotImplementedException("create not implemented");
    }

    /**
     * Search for resources - GET strategy
     * Implements https://www.hl7.org/fhir/R4/http.html#search
     * Also see: https://build.fhir.org/http.html#search
     * @param authorization
     * @param resourceType
     * @param params
     * @return
     */
    @GetMapping(value = {"/{resourceType}", "/{resourceType}/"})
    public ResponseEntity<String> searchByGet(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                              @PathVariable String resourceType,
                                              @RequestParam Map<String,String> params) {
        return doSearch(authorization, resourceType, params);
    }

    /**
     * Search for resources - POST strategy
     * Implements https://www.hl7.org/fhir/R4/http.html#search
     * Also see: https://build.fhir.org/http.html#search
     * @param authorization
     * @param resourceType
     * @param params
     * @return
     */
    @PostMapping("/{resourceType}/_search")
    public ResponseEntity<String> searchByPost(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                               @PathVariable String resourceType,
                                               @RequestParam Map<String,String> params) {
        return doSearch(authorization, resourceType, params);
    }


///////////////////////////////////////////////////////////////////////////////////
/// private methods
///

    private ResponseEntity<String> doSearch(String authorization, String resourceType, Map<String,String> params) {
        try {
            ClientInfo clientInfo = cacheService.get(extractBearerToken(authorization));

            Bundle bundle = proxyService.search(clientInfo, resourceType, params);

            return new ResponseEntity<>(encodeResponse(bundle, params.get(PARAM_FORMAT)), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("caught " + e.getClass().getSimpleName() + " while processing request - " + e.getMessage(), e);

            OperationOutcome outcome = new OperationOutcome();
            outcome.addIssue()
                    .setCode(OperationOutcome.IssueType.EXCEPTION)
                    .setDiagnostics(e.getMessage());

            return new ResponseEntity<>(encodeResponse(outcome, params.get(PARAM_FORMAT)), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String encodeResponse(IBaseResource resource, String format) {
        String response;
        if      (doEncodeJson(format))  response = FhirUtil.toJson(resource);
        else if (doEncodeRDF(format))   response = FhirUtil.toRDF(resource);
        else                            response = FhirUtil.toXml(resource);
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
