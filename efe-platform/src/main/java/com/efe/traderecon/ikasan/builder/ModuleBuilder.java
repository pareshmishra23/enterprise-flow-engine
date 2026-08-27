package com.efe.traderecon.ikasan.builder;

import com.efe.traderecon.ikasan.model.IkasanFlow;
import com.efe.traderecon.ikasan.model.IkasanModule;

import java.util.ArrayList;
import java.util.List;

public class ModuleBuilder {
    private final String name;
    private String description = "";
    private final List<IkasanFlow> flows = new ArrayList<>();

    public ModuleBuilder(String name) {
        this.name = name;
    }

    public ModuleBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public FlowBuilder createFlow(String flowName) {
        return new FlowBuilder(flowName, this.name);
    }

    public ModuleBuilder addFlow(IkasanFlow flow) {
        this.flows.add(flow);
        return this;
    }

    public IkasanModule build() {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Module name must not be empty");
        }
        return new IkasanModule(name, description, flows);
    }
}
