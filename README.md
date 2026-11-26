# flow-restore-repro

Reproduces an issue with Quarkus Flow instance restore: an instance waiting on a `listen` task is restored from the database after a crash, but does not resume when the expected event arrives.

The `wait` workflow (`WaitFlow.java`) has three tasks:

1. `prepare`: copies `key` from the input;
2. `wait`: waits for a `demo.go.v1` event with the same `key` (correlated via `dataFields("key")`);
3. `done`: logs `done {...}`.

## Requirements

- Java 25
- Docker (Postgres on port `5433`)
- `quarkus-flow` `2.0.0-SNAPSHOT` installed in the local Maven repository (`./mvnw install` in the quarkus-flow repository)

## Build

```shell
docker-compose up -d
./mvnw package -DskipTests
```

## Test

1. Start an instance of the `wait` workflow with `key=k1`:

```shell
java -Ddemo.start=true -jar target/quarkus-app/quarkus-run.jar   # wait for "Task 'wait' started"
```

2. Kill the process without a graceful shutdown, so the instance stays persisted in Postgres waiting for the event:

```shell
kill -9 <pid>
```

3. Start the application again. It restores the instance from the database and, after 3 seconds (`-Ddemo.send-delay-ms`), sends a `demo.go.v1` event with `{"key":"k1"}` to the `flow-in` channel:

```shell
java -Ddemo.send=true -jar target/quarkus-app/quarkus-run.jar
```

## Expected

The restored instance receives the event, runs the `done` task and logs:

```
done {key=k1}
```

## Actual

The instance is restored (see the `DEBUG` logs from `io.quarkiverse.flow.persistence`) and `Sending demo.go.v1 with key=k1 to flow-in` is logged, but `done` is never logged: the instance stays stuck on the `wait` task.

## Executing in another port

```shell
java -Ddemo.start=true -Dquarkus.http.port=5555 -jar target/quarkus-app/quarkus-run.jar
```

## Reset

To start from scratch, drop the database:

```shell
docker-compose down -v
```
