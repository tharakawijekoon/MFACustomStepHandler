package org.soadecoder.extensions.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.soadecoder.extensions.MFACustomStepHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.step.impl.DefaultStepHandler;
import org.wso2.carbon.user.core.service.RealmService;



@Component(name = "org.soadecoder.extensions.internal.ServiceComponent", service = ServiceComponent.class , immediate = true)
public class ServiceComponent {

    private static final Log log = LogFactory.getLog(ServiceComponent.class);

    private static RealmService realmService;

    public static RealmService getRealmService() {
        return realmService;
    }

    @Activate
    protected void activate(ComponentContext context) {

        try {

            if (log.isDebugEnabled()) {
                log.debug("service component is enabled");
            }
            BundleContext bundleContext = context.getBundleContext();

            log.info("Activated Custom MFA Step Handler");

            MFACustomStepHandler handler = new MFACustomStepHandler();
            bundleContext.registerService(DefaultStepHandler.class.getName(), handler, null);

            //DataHolder.watchFile();


        } catch (Throwable e) {
            log.error("Error while activating Custom MFA Step Handler component.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Custom MFA Step Handler bundle is de-activated");
        }
    }

    @Reference(
            name = "RealmService",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        log.debug("Setting the Realm Service");
        ServiceComponent.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        log.debug("UnSetting the Realm Service");
        ServiceComponent.realmService = null;
    }
}
