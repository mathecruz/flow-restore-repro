package org.acme.demo;

import static io.quarkiverse.flow.dsl.FlowDSL.consumed;
import static io.quarkiverse.flow.dsl.FlowDSL.function;
import static io.quarkiverse.flow.dsl.FlowDSL.listen;
import static io.quarkiverse.flow.dsl.FlowDSL.toOne;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.flow.Flow;
import io.quarkiverse.flow.dsl.FlowWorkflowBuilder;
import io.quarkus.logging.Log;
import io.serverlessworkflow.api.types.Workflow;

@ApplicationScoped
public class WaitFlow extends Flow {

    @Override
    @SuppressWarnings({"rawtypes" })
    public Workflow descriptor() {
        return FlowWorkflowBuilder.workflow("wait", "demo", "1.0.0")
                .tasks(
                        function("prepare", (Map in) -> Map.of("key", in.get("key")), Map.class),
                        listen("wait", toOne(consumed("demo.go.v1").dataFields("key")).first()),
                        function("done", (Map e) -> {
                            Log.info("done " + e);
                            return e;
                        }, Map.class))
                .build();
    }
}
