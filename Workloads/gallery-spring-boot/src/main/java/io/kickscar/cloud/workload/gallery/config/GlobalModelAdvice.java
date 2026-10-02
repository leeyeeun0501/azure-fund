package io.kickscar.cloud.workload.gallery.config;

import io.kickscar.cloud.workload.gallery.runtime.HostIdentity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {
    private final HostIdentity hostIdentity;

    public GlobalModelAdvice(HostIdentity hostIdentity) {
        this.hostIdentity = hostIdentity;
    }

    @ModelAttribute("hostId")
    public String hostId() {
        return hostIdentity.getHostId();
    }
}
