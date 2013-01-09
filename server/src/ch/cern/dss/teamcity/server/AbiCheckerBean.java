package ch.cern.dss.teamcity.server;

import ch.cern.dss.teamcity.common.AbiCheckerConstants;
import org.jetbrains.annotations.NotNull;

public class AbiCheckerBean {
    @NotNull
    public String getReferenceTagKey() {
        return AbiCheckerConstants.UI_TAG;
    }
}
