package com.efe.traderecon.ikasan.builder;

import org.springframework.stereotype.Component;

@Component
public class BuilderFactory {

    public ModuleBuilder getModuleBuilder(String moduleName) {
        return new ModuleBuilder(moduleName);
    }
}
