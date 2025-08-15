package edu.ohsu.cmp.fhirproxy.service;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import edu.ohsu.cmp.fhirproxy.model.ClientInfo;
import edu.ohsu.cmp.fhirproxy.util.FhirUtil;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseBundle;
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
        logger.info("read: " + clientInfo.getServerUrl() + "/" + resourceType + "/" + id);

        IGenericClient client = FhirUtil.buildClient(clientInfo, socketTimeout);

        // todo : incorporate params

        IBaseResource resource = client.read()
                .resource(resourceType)
                .withId(id)
                .execute();

        return resource;
    }

    public IBaseResource vread(ClientInfo clientInfo, String resourceType, String id, String vid, Map<String, String> paramsMap) {
        logger.info("vread: " + clientInfo.getServerUrl() + "/" + resourceType + "/" + id + "/_history/" + vid);

        IGenericClient client = FhirUtil.buildClient(clientInfo, socketTimeout);

        // todo : incorporate params

        IBaseResource resource = client.read()
                .resource(resourceType)
                .withIdAndVersion(id, vid)
                .execute();

        return resource;
    }

    public Bundle search(ClientInfo clientInfo, String resourceType, Map<String, String> paramsMap,
                         Integer pageLimit) {
        IGenericClient client = FhirUtil.buildClient(clientInfo, socketTimeout);

        List<String> paramsList = new ArrayList<>();

        for (Map.Entry<String,String> entry : paramsMap.entrySet()) {
            paramsList.add(entry.getKey() + "=" + entry.getValue());
        }

        String path = resourceType + "?" + StringUtils.join(paramsList, "&");;

        logger.info("search: " + clientInfo.getServerUrl() + "/" + path);

        Bundle bundle = client.search()
                .byUrl(path)
                .returnBundle(Bundle.class)
                .execute();

        int total = bundle.getTotal();

        if (bundle.getLink(IBaseBundle.LINK_NEXT) == null || (pageLimit != null && pageLimit == 1)) {
            return bundle;

        } else {
            // see: https://hapifhir.io/hapi-fhir/docs/client/examples.html#fetch-all-pages-of-a-bundle

            List<Bundle.BundleEntryComponent> entryList = new ArrayList<>();
            entryList.addAll(bundle.getEntry());

            int pagesIncorporated = 1;

            while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
                logger.info("search: fetching next page (" + (pagesIncorporated + 1) + ") from " +
                        bundle.getLink(IBaseBundle.LINK_NEXT).getUrl());

                bundle = client.loadPage().next(bundle).execute();
                entryList.addAll(bundle.getEntry());
                pagesIncorporated++;

                if (pageLimit != null && pageLimit != 0 && pagesIncorporated >= pageLimit) {
                    logger.info("search: reached page limit of " + pageLimit + ", stopping search.");
                    break;
                }
            }

            Bundle compositeBundle = new Bundle();
            compositeBundle.setType(Bundle.BundleType.SEARCHSET);
            compositeBundle.setEntry(entryList);
            compositeBundle.setTotal(total);

            return compositeBundle;
        }
    }
}
