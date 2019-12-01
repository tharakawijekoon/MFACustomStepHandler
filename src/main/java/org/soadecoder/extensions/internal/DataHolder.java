package org.soadecoder.extensions.internal;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class DataHolder {

    private static volatile DataHolder dataHolder;

    private static final Log log = LogFactory.getLog(DataHolder.class);

    private static final String CONFIG_FILE_PATH = "repository/conf/identity/mfa_config.json";
    private static final String SERVICE_PROVIDERS = "ServiceProviders";
    private static final String SERVICE_PROVIDER_NAME = "ServiceProviderName";
    private static final String MFA_ROLE = "MFARole";

    private Map<String, String> serviceProviderMFARoleMap = null;

    // Private constructor to prevent instantiation.
    private DataHolder() {

        serviceProviderMFARoleMap = new HashMap<>();
    }

    public static DataHolder getInstance() {

        if (dataHolder == null) {
            synchronized (DataHolder.class) {
                if (dataHolder == null) {
                    dataHolder = new DataHolder();
                    dataHolder.readStepHandlerConfig();
                }
            }
        }
        return dataHolder;
    }

    public Map<String, String> getServiceProviderMFARoleMap() {
        return serviceProviderMFARoleMap;
    }

    /**
     * Read step handler configurations from the file 'mfa_config.json' and populate to data holders.
     *
     * @return
     */
    private void readStepHandlerConfig() {

        if (Files.exists(Paths.get(CONFIG_FILE_PATH))) {

            JSONParser parser = new JSONParser();
            try {
                JSONObject stepHandlerConfig = (JSONObject) parser.parse(new FileReader(CONFIG_FILE_PATH));
                JSONArray serviceProviders = (JSONArray) stepHandlerConfig.get(SERVICE_PROVIDERS);

                if (serviceProviders != null) {
                    for (Object serviceprovider1 : serviceProviders) {
                        JSONObject serviceprovider = (JSONObject) serviceprovider1;
                        String spname = ((String) serviceprovider.get(SERVICE_PROVIDER_NAME)).trim();
                        String mfarole = ((String) serviceprovider.get(MFA_ROLE)).trim();

                        if (StringUtils.isNotBlank(spname) && StringUtils.isNotBlank(mfarole)) {
                            serviceProviderMFARoleMap.put(spname, mfarole);
                        }
                    }
                }
            } catch (IOException e) {
                log.error("Error occurred while reading the file in the path : " + CONFIG_FILE_PATH, e);
            } catch (ParseException e) {
                log.error("Error occurred while parsing JSON in the file : " + CONFIG_FILE_PATH, e);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Could not find config file in the path : " + CONFIG_FILE_PATH);
            }
        }
    }
}
