package org.soadecoder.extensions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.soadecoder.extensions.internal.DataHolder;
import org.soadecoder.extensions.internal.ServiceComponent;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.step.impl.DefaultStepHandler;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class MFACustomStepHandler extends DefaultStepHandler {
    private static Log log = LogFactory.getLog(MFACustomStepHandler.class);

    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AuthenticationContext context) throws FrameworkException {

        Map<String, String> spMFARoleMap = DataHolder.getInstance().getServiceProviderMFARoleMap();

        int currentStep = context.getCurrentStep();
        StepConfig stepConfig = context.getSequenceConfig().getStepMap().get(currentStep);
        String spName = context.getServiceProviderName();

        if (log.isDebugEnabled()) {
            log.debug("Current Step: " + currentStep);
            log.debug("Service Provider name: " + spName);
        }

        if (currentStep == 1) {
                super.handle(request, response, context);
                if (stepConfig.isCompleted() && spMFARoleMap != null && spMFARoleMap.containsKey(spName)) {
                    try {
                        if (log.isDebugEnabled()) {
                            log.debug("Executing MFA custom step handler.");
                        }
                        boolean roleCheck = false;
                        String username = context.getSequenceConfig().getStepMap().get(currentStep)
                                .getAuthenticatedUser().getUserName();
                        int tenantId = ServiceComponent.getRealmService().getTenantManager().
                                getTenantId(MultitenantUtils.getTenantDomain(username));
                        UserStoreManager userStoreManager = (UserStoreManager) ServiceComponent.getRealmService().
                                getTenantUserRealm(tenantId).getUserStoreManager();

                        for (String role : spMFARoleMap.get(spName).split(",")) {
                            role = role.trim();
                            if (!role.isEmpty()) {
                                roleCheck = ((AbstractUserStoreManager) userStoreManager).isUserInRole(username, role);
                                if (roleCheck) {
                                    if (log.isDebugEnabled()) {
                                        log.debug("User: " + username + " passed role check with role: " + role);
                                    }
                                    break;
                                }
                            }
                        }

                        if (!roleCheck && context.getSequenceConfig().getStepMap().size() > 1) {
                            for (int i = 2; i <= context.getSequenceConfig().getStepMap().size(); i++) {
                                context.getSequenceConfig().getStepMap().remove(i);
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("Skipping other steps for user " + username);
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error occurred during executing MFA custom step handler.", e);
                    }
                }
        } else {
                //let DefaultStepHandler handle
                super.handle(request, response, context);
        }
    }
}
