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

Get blocks
```
curl http://localhost:9092/btc/blocks?startHeight=20&endHeight=30

curl http://localhost:9092/btc/blocks/20/30

```
Get block by id
```
curl http://localhost:9092/btc/block/xxx

```

Performance Monitoring

```
curl http://localhost:9092/actuator

curl http://localhost:9092/actuator/health
```