package org.acme.demo;

import java.net.URI;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.reactive.messaging.ce.OutgoingCloudEventMetadata;

/**
 * Sends a {@code demo.go.v1} event with {@code {"key":"k1"}} to {@code flow-in} when {@code -Ddemo.send=true}.
 * <p>
 * No connector is configured, so {@code flow-in} is an in-memory channel: the event can only be sent from inside
 * the application. The event is sent with a delay to give the (async, in prod) restore time to finish.
 */
@ApplicationScoped
public class EventSender {

    @Inject
    @Channel("flow-in")
    Emitter<String> emitter;

    @ConfigProperty(name = "demo.send", defaultValue = "false")
    boolean send;

    @ConfigProperty(name = "demo.send-delay-ms", defaultValue = "3000")
    long delayMs;

    void onStart(@Observes StartupEvent ev) {
        if (!send) {
            return;
        }
        Thread.ofVirtual().start(() -> {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            Log.info("Sending demo.go.v1 with key=k1 to flow-in");
            emitter.send(Message.of("{\"key\":\"k1\"}")
                    .addMetadata(OutgoingCloudEventMetadata.builder()
                            .withId(UUID.randomUUID().toString())
                            .withSource(URI.create("demo"))
                            .withType("demo.go.v1")
                            .withDataContentType("application/json")
                            .build()));
        });
    }
}
