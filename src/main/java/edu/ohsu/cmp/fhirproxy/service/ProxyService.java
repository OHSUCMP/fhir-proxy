package edu.ohsu.cmp.fhirproxy.service;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import edu.ohsu.cmp.fhirproxy.model.ClientInfo;
import edu.ohsu.cmp.fhirproxy.util.FhirUtil;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProxyService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${socket.timeout:300000}")
    private Integer socketTimeout;

    public IBaseResource read(ClientInfo clientInfo, String resourceType, String id, Map<String, String> paramsMap) {
        logger.info("read: " + resourceType + "/" + id);

        IGenericClient client = FhirUtil.buildClient(clientInfo, socketTimeout);

        // todo : incorporate params

        IBaseResource resource = client.read()
                .resource(resourceType)
                .withId(id)
                .execute();

        return resource;
    }

    public IBaseResource vread(ClientInfo clientInfo, String resourceType, String id, String vid, Map<String, String> paramsMap) {
        logger.info("vread: " + resourceType + "/" + id + "/_history/" + vid);

        IGenericClient client = FhirUtil.buildClient(clientInfo, socketTimeout);

        // todo : incorporate params

        IBaseResource resource = client.read()
                .resource(resourceType)
                .withIdAndVersion(id, vid)
                .execute();

        return resource;
    }

    public Bundle search(ClientInfo clientInfo, String resourceType, Map<String, String> paramsMap) {
        IGenericClient client = FhirUtil.buildClient(clientInfo, socketTimeout);

        List<String> paramsList = new ArrayList<>();
        for (Map.Entry<String,String> entry : paramsMap.entrySet()) {
            paramsList.add(entry.getKey() + "=" + entry.getValue());
        }
        String params = StringUtils.join(paramsList, "&");

        logger.info("search: " + resourceType + "?" + params);

        Bundle bundle = client.search()
                .byUrl(resourceType + "?" + params)
                .returnBundle(Bundle.class)
                .execute();

        // todo : implement paging

        return bundle;
    }
}
