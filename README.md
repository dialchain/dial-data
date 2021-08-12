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

Receive message
```
curl http://localhost:9092/messages/sender/recipient/messageId
```

Post message
```
curl -X POST http://localhost:9092/messages/sender/recipient/messageId -d "HERE_COME_THE_MESSAGE"
```

Delete
```
curl -X DELETE http://localhost:9092/messages/sender/recipient/messageId
```

Performance Monitoring

```
curl http://localhost:9092/actuator

curl http://localhost:9092/actuator/health
```