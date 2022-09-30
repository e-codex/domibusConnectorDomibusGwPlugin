package eu.ecodex.domibus.gw.init;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;

import java.util.logging.Logger;

public class UploadPModesHook implements LifecycleListener {

    private static Logger LOGGER = Logger.getLogger(UploadPModesHook.class.getName());

    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        Lifecycle lifecycle = event.getLifecycle();
        LifecycleState state = lifecycle.getState();

        if (LifecycleState.STARTED == state) {
            LOGGER.info("Running PModeUpload init");



        }
    }


}
