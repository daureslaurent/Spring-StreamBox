[![Maven Package](https://github.com/daureslaurent/Spring-StreamBox/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/daureslaurent/Spring-StreamBox/actions/workflows/maven-publish.yml)
[![Lib Tag](https://github.com/daureslaurent/Spring-StreamBox/actions/workflows/maven-publish-tag.yml/badge.svg)](https://github.com/daureslaurent/Spring-StreamBox/actions/workflows/maven-publish-tag.yml)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.2-6DB33F?style=flat-square&logo=springboot)
![Event Driven](https://img.shields.io/badge/Event_Driven-orange?style=flat-square)
![Outbox/Inbox](https://img.shields.io/badge/Outbox--Inbox-lightgrey?style=flat-square&logo=database)

# StreamBox

**StreamBox** is a lightweight library for managing **outbox and inbox events** in a CQRS-style architecture. It simplifies batching, JSON serialization, and transactional processing of events in a Spring Boot + JPA application.

---

## Features

* Generic support for any event payload.
* Easy integration with Spring Boot.
* Batch processing with **row-level locking** (`FOR UPDATE SKIP LOCKED`) for parallel consumers.
* JSON serialization/deserialization of payloads.
* Extensible entity base classes for auditing and status tracking.
* Lightweight, zero-dependency beyond Spring Boot and Jackson.

---

## Maven Dependency

```xml
<dependency>
    <groupId>com.lda</groupId>
    <artifactId>streambox</artifactId>
    <version>0.1.0</version>
</dependency>
```

---

## Quick Start

### 1. Define your event entity

Extend `StreamBoxBasePayloadEntity`:

```java
import com.lda.streambox.entity.StreamBoxBasePayloadEntity;
import jakarta.persistence.Entity;

@Entity
public class OrderEventEntity extends StreamBoxBasePayloadEntity {
    // Optional: extra fields specific to your event
}
```

### 2. Create a repository

```java
import com.lda.streambox.repository.StreamBoxRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderEventRepository extends StreamBoxRepository<OrderEventEntity> {
}
```

### 3. Consume events

```java
import com.lda.streambox.port.StreamBoxInput;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderEventProcessor implements StreamBoxInput<OrderEventEntity> {

    private final OrderEventRepository repository;

    public OrderEventProcessor(OrderEventRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public List<OrderEventEntity> lockNextBatch(int limit) {
        return repository.lockNextBatch(limit);
    }

    @Override
    @Transactional
    public void finish(OrderEventEntity event) {
        event.setStatus(StreamBoxBaseStatusEnum.PROCESSED);
        repository.save(event);
    }
}
```

### 4. Sending events

```java
import com.lda.streambox.entity.StreamBoxBasePayloadEntity;
import com.lda.streambox.json.JsonConverter;

@Service
public class OrderEventPublisher {

    private final OrderEventRepository repository;
    private final JsonConverter jsonConverter;

    public OrderEventPublisher(OrderEventRepository repository, JsonConverter jsonConverter) {
        this.repository = repository;
        this.jsonConverter = jsonConverter;
    }

    public void publish(Object payload, String type) {
        String json = jsonConverter.toJson(payload);
        OrderEventEntity entity = OrderEventEntity.builder()
                .type(type)
                .payload(json)
                .build();
        repository.save(entity);
    }
}
```

---

## Configuration

StreamBox uses **Spring Boot autoconfiguration**:

It provides a `JsonConverter` bean automatically if Jackson is on the classpath.

---

## Recommended Practices

* Use `@Transactional` when calling `lockNextBatch` and `finish` to ensure proper row locking.
* Consider adding a `PROCESSED` and `FAILED` status to track event processing.
* For parallel consumers, `FOR UPDATE SKIP LOCKED` ensures no two consumers process the same event.

---

## Future Updates

* Support for automatic scheduled event processing.
* Retry and dead-letter queue (DLQ) support for failed events.
* Enhanced auditing with `processedAt` and `attemptCount` fields.
* Integration with messaging systems like RabbitMQ or Kafka.
* Customizable batch size and concurrency configuration.

---

## License

MIT License.

---

## Acknowledgements

Inspired by the **CQRS + Outbox Pattern** for reliable event processing in distributed systems.
