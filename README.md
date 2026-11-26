# flow-restore-repro

Reproduz um problema no restore de instâncias do Quarkus Flow: uma instância parada em um `listen` é restaurada do banco após um crash, mas não continua quando o evento esperado chega.

O workflow `wait` (`WaitFlow.java`) tem três tasks:

1. `prepare`: copia o `key` do input;
2. `wait`: espera um evento `demo.go.v1` com o mesmo `key` (correlação por `dataFields("key")`);
3. `done`: loga `done {...}`.

## Requirements

- Java 25
- Docker (Postgres na porta `5433`)
- `quarkus-flow` `2.0.0-SNAPSHOT` instalado no repositório Maven local (`./mvnw install` no repositório do quarkus-flow)

## Build

```shell
docker-compose up -d
./mvnw package -DskipTests
```

## Test

1. Inicie uma instância do workflow `wait` com `key=k1`:

```shell
java -Ddemo.start=true -jar target/quarkus-app/quarkus-run.jar   # espere o "Task 'wait' started"
```

2. Mate o processo sem shutdown gracioso, para que a instância fique persistida no Postgres esperando o evento:

```shell
kill -9 <pid>
```

3. Suba a aplicação de novo. Ela restaura a instância do banco e, após 3 segundos (`-Ddemo.send-delay-ms`), envia um evento `demo.go.v1` com `{"key":"k1"}` para o canal `flow-in`:

```shell
java -Ddemo.send=true -jar target/quarkus-app/quarkus-run.jar
```

## Expected

A instância restaurada recebe o evento, executa a task `done` e loga:

```
done {key=k1}
```

## Actual

A instância é restaurada (veja os logs `DEBUG` de `io.quarkiverse.flow.persistence`) e o `Sending demo.go.v1 with key=k1 to flow-in` aparece, mas o `done` nunca é logado: a instância continua parada na task `wait`.

## Executing in another port

```shell
java -Ddemo.start=true -Dquarkus.http.port=5555 -jar target/quarkus-app/quarkus-run.jar
```

## Reset

Para começar do zero, apague o banco:

```shell
docker-compose down -v
```
