# README

## Start Redis

Start Redis in a container
```
docker run --name recorder-redis -p 6379:6379 -d redis:alpine
```

## Start the application

```
mvn spring-boot:run
```

## How to test the application locally

Submit transaction

```
curl -X POST "http://localhost:9092/btc/transactions" -H  "accept: */*" -H  "Content-Type: application/json" -d "{  \"transactionBytes\": xxxxxxxxxxxxx}"
```

Check transaction

```
curl -X GET "http://localhost:9092/btc/transactions/dfsfdfsdfsd" -H  "accept: application/json"
```

Find the block hosting a given transaction by id.

```
curl -X GET "http://localhost:9092/btc/transactions/dfsfdfsdfsd/block" -H  "accept: application/json"
```

Get a block by id.

```
curl -X GET "http://localhost:9092/btc/blocks/00000000700e92a916b46b8b91a14d1303d5d91ef0b09eecc3151fb958fd9a2e" -H  "accept: application/json"
```

List all blocks within start and end height.

```
curl -X GET "http://localhost:9092/btc/blocks/height?startHeight=10&endHeight=20" -H  "accept: application/json"
```

List the next quantity blocks from startTime.

```
curl -X GET "http://localhost:9092/btc/blocks/time?startTime=1296689461&quantity=10" -H  "accept: application/json"
```

Performance Monitoring

```
curl http://localhost:9092/actuator

curl http://localhost:9092/actuator/health
```

## SWAGGER UI

https://$DIAL_HOSTNAME/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config

E.x.:

https://dial-data.mon-wallet.com/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config
