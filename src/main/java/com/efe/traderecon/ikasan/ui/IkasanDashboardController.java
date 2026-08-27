package com.efe.traderecon.ikasan.ui;

import com.efe.traderecon.ikasan.engine.IkasanEngine;
import com.efe.traderecon.ikasan.model.FlowElement;
import com.efe.traderecon.ikasan.model.IkasanFlow;
import com.efe.traderecon.ikasan.model.IkasanModule;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/ikasan")
public class IkasanDashboardController {

    private final IkasanEngine ikasanEngine;

    public IkasanDashboardController(IkasanEngine ikasanEngine) {
        this.ikasanEngine = ikasanEngine;
    }

    @GetMapping("/module")
    public ResponseEntity<Map<String, Object>> getModuleDetails() {
        IkasanModule module = ikasanEngine.getModule();
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("moduleName", module.getName());
        resp.put("description", module.getDescription());
        resp.put("running", module.isRunning());

        List<Map<String, Object>> flowList = new ArrayList<>();
        for (IkasanFlow flow : module.getFlows()) {
            Map<String, Object> flowMap = new LinkedHashMap<>();
            flowMap.put("name", flow.getName());
            flowMap.put("state", flow.getState().name());
            flowMap.put("consumer", Map.of(
                    "name", flow.getConsumer() != null ? flow.getConsumer().getName() : "None",
                    "running", flow.getConsumer() != null && flow.getConsumer().isRunning()
            ));

            List<Map<String, Object>> elements = new ArrayList<>();
            for (FlowElement el : flow.getElements()) {
                Map<String, Object> elMap = new LinkedHashMap<>();
                elMap.put("name", el.getName());
                elMap.put("type", el.getType().name());
                elMap.put("invocationCount", el.getInvocationCount());
                elMap.put("errorCount", el.getErrorCount());
                elMap.put("lastExecutionTimeMs", el.getLastExecutionTimeMs());

                if (!el.getRoutes().isEmpty()) {
                    Map<String, String> routeMap = new LinkedHashMap<>();
                    el.getRoutes().forEach((rName, prod) -> routeMap.put(rName, prod.getName()));
                    elMap.put("routes", routeMap);
                }

                elements.add(elMap);
            }
            flowMap.put("components", elements);

            flowMap.put("producer", flow.getProducer() != null ? Map.of(
                    "name", flow.getProducer().getName()
            ) : null);

            flowMap.put("totalEventsProcessed", flow.getTotalEventsProcessed());
            flowMap.put("totalEventsFailed", flow.getTotalEventsFailed());
            flowList.add(flowMap);
        }
        resp.put("flows", flowList);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/flows/{flowName}/start")
    public ResponseEntity<Map<String, String>> startFlow(@PathVariable String flowName) {
        Optional<IkasanFlow> flowOpt = ikasanEngine.getFlow(flowName);
        if (flowOpt.isPresent()) {
            flowOpt.get().start();
            return ResponseEntity.ok(Map.of("message", "Flow " + flowName + " started", "state", flowOpt.get().getState().name()));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/flows/{flowName}/stop")
    public ResponseEntity<Map<String, String>> stopFlow(@PathVariable String flowName) {
        Optional<IkasanFlow> flowOpt = ikasanEngine.getFlow(flowName);
        if (flowOpt.isPresent()) {
            flowOpt.get().stop();
            return ResponseEntity.ok(Map.of("message", "Flow " + flowName + " stopped", "state", flowOpt.get().getState().name()));
        }
        return ResponseEntity.notFound().build();
    }
}
