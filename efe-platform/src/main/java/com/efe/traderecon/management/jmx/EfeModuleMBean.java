package com.efe.traderecon.management.jmx;

import com.efe.traderecon.ikasan.engine.IkasanEngine;
import com.efe.traderecon.ikasan.model.FlowState;
import com.efe.traderecon.ikasan.model.IkasanFlow;
import com.efe.traderecon.ikasan.model.IkasanModule;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ManagedResource(
        objectName = "com.efe:type=Module,name=enterprise-flow-engine",
        description = "EFE Platform Module JMX Management"
)
public class EfeModuleMBean {

    private final IkasanEngine ikasanEngine;

    public EfeModuleMBean(IkasanEngine ikasanEngine) {
        this.ikasanEngine = ikasanEngine;
    }

    @ManagedAttribute(description = "Module Name")
    public String getModuleName() {
        IkasanModule module = ikasanEngine.getModule();
        return module != null ? module.getName() : "UNKNOWN";
    }

    @ManagedAttribute(description = "Module State")
    public String getStatus() {
        IkasanModule module = ikasanEngine.getModule();
        return (module != null && module.isRunning()) ? "RUNNING" : "STOPPED";
    }

    @ManagedAttribute(description = "Number of Registered Flows")
    public int getFlowCount() {
        IkasanModule module = ikasanEngine.getModule();
        return module != null ? module.getFlows().size() : 0;
    }

    @ManagedAttribute(description = "Names of Registered Flows")
    public List<String> getFlowNames() {
        IkasanModule module = ikasanEngine.getModule();
        return module != null ? module.getFlows().stream().map(IkasanFlow::getName).toList() : List.of();
    }

    @ManagedOperation(description = "Start the EFE Module")
    public void start() {
        ikasanEngine.start();
    }

    @ManagedOperation(description = "Stop the EFE Module")
    public void stop() {
        ikasanEngine.stop();
    }
}
