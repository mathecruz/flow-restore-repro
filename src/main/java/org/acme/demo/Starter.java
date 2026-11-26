package org.acme.demo;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;

/**
 * Starts one instance of {@link WaitFlow} when {@code -Ddemo.start=true}.
 */
@ApplicationScoped
public class Starter {

    @Inject
    WaitFlow flow;

    @ConfigProperty(name = "demo.start", defaultValue = "false")
    boolean start;

    void onStart(@Observes StartupEvent ev) {
        if (start) {
            Log.info("Starting a 'wait' instance with key=k1");
            flow.instance(Map.of("key", "k1")).start();
        }
    }
}
