package org.acme.demo;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import io.quarkus.logging.Log;

/**
 * No connector is configured, so {@code flow-out} needs an in-app consumer.
 */
@ApplicationScoped
public class FlowOutConsumer {

    @Incoming("flow-out")
    void consume(Object event) {
        Log.infof("flow-out: %s", event);
    }
}
