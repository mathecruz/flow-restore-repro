# flow-restore-repro

## Test
```shell
docker-compose up -d
java -Ddemo.start=true -jar target/quarkus-app/quarkus-run.jar   # espere o "Task 'wait' started", depois:
kill -9 <pid>
java -Ddemo.send=true -jar target/quarkus-app/quarkus-run.jar
```

## Executing in another port

```shell
java -Ddemo.start=true -Dquarkus.http.port=5555 -jar target/quarkus-app/quarkus-run.jar
```

