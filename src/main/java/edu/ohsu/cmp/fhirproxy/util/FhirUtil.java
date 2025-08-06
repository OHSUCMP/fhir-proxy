package edu.ohsu.cmp.fhirproxy.util;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import edu.ohsu.cmp.fhirproxy.model.ClientInfo;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class FhirUtil {
    public static String toJson(IBaseResource r, boolean pretty) {
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();
        parser.setPrettyPrint(pretty);
        return parser.encodeResourceToString(r);
    }

    public static String toXml(IBaseResource r, boolean pretty) {
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newXmlParser();
        parser.setPrettyPrint(pretty);
        return parser.encodeResourceToString(r);
    }

    public static String toRDF(IBaseResource r, boolean pretty) {
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newRDFParser();
        parser.setPrettyPrint(pretty);
        return parser.encodeResourceToString(r);
    }

    public static IGenericClient buildClient(ClientInfo clientInfo, Integer socketTimeout) {
        FhirContext ctx = FhirContext.forR4();
        ctx.getRestfulClientFactory().setSocketTimeout(socketTimeout);
        IGenericClient client = ctx.newRestfulGenericClient(clientInfo.getServerUrl());

        BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(clientInfo.getBearerToken());
        client.registerInterceptor(authInterceptor);

        return client;
    }

    public static String getPatientSearchKeyForResource(String resourceType) {
        if (StringUtils.equalsIgnoreCase(resourceType, "Immunization")) {
            return "patient";
        } else {
            return "subject";
        }
    }
}
