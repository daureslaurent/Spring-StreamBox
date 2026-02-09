[![Maven Package](https://github.com/daureslaurent/Spring-StreamBox/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/daureslaurent/Spring-StreamBox/actions/workflows/maven-publish.yml)
[![Lib Tag](https://github.com/daureslaurent/Spring-StreamBox/actions/workflows/maven-publish-tag.yml/badge.svg)](https://github.com/daureslaurent/Spring-StreamBox/actions/workflows/maven-publish-tag.yml)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.2-6DB33F?style=flat-square&logo=springboot)
![Event Driven](https://img.shields.io/badge/Event_Driven-orange?style=flat-square)
![Outbox/Inbox](https://img.shields.io/badge/Outbox--Inbox-lightgrey?style=flat-square&logo=database)

# 📦 StreamBox — Inbox/Outbox Framework for Spring Boot
*A lightweight, type‑safe, event‑driven framework for relational databases.*

StreamBox implements the **Inbox / Outbox pattern**, enabling safe asynchronous event processing for CQRS, projections, and inter‑service communication.

It provides:

- Event entities and repositories
- JSON serialization / deserialization
- Event type registry
- Inbox & Outbox adapters
- Clean event projection handlers
- **SmartLifecycle-based schedulers**
- Highly configurable scheduling per type or instance

---

# 🚀 Quick Start

## 1. Add maven the dependency

### Maven Dependency

```xml
<dependency>
    <groupId>com.lda</groupId>
    <artifactId>streambox</artifactId>
    <version>0.2.0</version>
</dependency>
```

## 🧩 2. Define an Inbox or Outbox entity

```java
@RequiredArgsConstructor
@Data
@SuperBuilder
@Entity(name = "PRODUCT_INBOX")
@Table(name = "PRODUCT_INBOX")
public class ProductInboxEventEntity extends StreamBoxBasePayloadEntity {

    @Column(nullable = false, unique = true)
    private UUID refOutbox;
}
```

---

## 📦 3. Define your events

Each event must implement `StreamBoxEvent`  
and declare its type with `@StreamBoxEventType`.

```java
@Builder
@StreamBoxEventType("ProductChangeQuantityEvent")
public record ProductChangeQuantityEvent(
        UUID idProduct,
        Integer changeValue,
        boolean isIncrease
) implements StreamBoxEvent {}
```

---

## 🗃️ 4. Create your repository

```java
@Repository
@Transactional(transactionManager = "readTransactionManager")
public interface ProductInboxRepository extends StreamBoxRepository<ProductInboxEventEntity> {}
```

---

## 🏭 5. Create an InboxFactory

```java
@Component
public class InboxFactory extends InboxFactoryAbstract<ProductInboxEventEntity, StreamBoxEvent> {

    public InboxFactory(StreamBoxEventRegistry registry) {
        super(registry, ProductInboxEventEntity.class);
    }

    @Override
    public ProductInboxEventEntity createEntity(String json, JsonConverter jsonConverter) {
        ProductInboxEventEntity entity = jsonConverter.fromJson(json, ProductInboxEventEntity.class);
        entity.setRefOutbox(entity.getId());
        entity.setId(null);
        return entity;
    }
}
```

---

## 🧠 6. Implement your Inbox Adapter

```java
@Slf4j
public class ProductInboxAdapter extends InboxAdapter<ProductInboxEventEntity, StreamBoxEvent> {

    private final ProductProjectionAdapter projections;

    public ProductInboxAdapter(
            JsonConverter jsonConverter,
            StreamBoxRepository<ProductInboxEventEntity> repository,
            InboxFactory factory,
            ProductProjectionAdapter projections
    ) {
        super(jsonConverter, repository, factory);
        this.projections = projections;
    }

    @Transactional(transactionManager = "readTransactionManager")
    public void doHandle(ProductInboxEventEntity entity) {
        this.handleEvent(entity);
    }

    @Override
    protected void handleProjection(StreamBoxEvent event) {
        switch (event) {
            case ProductCreateEvent e -> projections.createProductProjection(e);
            case ProductChangeQuantityEvent e -> projections.changeQuantityProjection(e);
            default -> log.error("Event type not handled: {}", event);
        }
    }
}
```

---

## ⚙️ 7. Register the StreamBox module

```java
@Configuration
@EnableScheduling
@StreamBox(
        type = StreamBoxType.INBOX,
        scanBasePackages = {"my.app.market.infra.persistence.projection"}
)
public class StreamBoxConfig {

    @Bean
    public ProductInboxAdapter productInboxAdapter(
            JsonConverter jsonConverter,
            StreamBoxRepository<ProductInboxEventEntity> repository,
            InboxFactory factory,
            ProductProjectionAdapter projectionAdapter
    ) {
        return new ProductInboxAdapter(jsonConverter, repository, factory, projectionAdapter);
    }

}
```

---

## ⏱️ 8. Configure SmartLifecycle Schedulers

Schedulers run automatically and poll Inbox/Outbox tables.

Enable:

```yaml
streambox:
  scheduler:
    enabled: true

    defaults:
      fixed-rate: PT7S
      initial-delay: PT1S
      limit: 5
```

Override per **type**:

```yaml
streambox:
  scheduler:
    types:
      inbox:
        fixed-rate: PT5S
        limit: 200
      outbox:
        fixed-rate: PT10S
        limit: 50
```

Override per **instance** (bean name):

```yaml
streambox:
  scheduler:
    instances:
      productInboxAdapter:
        fixed-rate: PT2S
        limit: 500
      orderOutboxAdapter:
        fixed-rate: "3000"
```

---

## 🔄 9. Producing events (Outbox)

```java
@Service
public class ProductPublisher {

    private final ProductInboxAdapter productInboxAdapter;
    private final JsonConverter jsonConverter;

    public void publish(Object payload, String type) {
        productInboxAdapter.addFromConsumer();
        String json = jsonConverter.toJson(payload);
        productInboxAdapter.addFromConsumer(json);
    }
}
```

---

## 🛠️ 10. Testing StreamBox (manual consumption)

Disable automatic scheduling:

```yaml
spring.task.scheduling.enabled=false
streambox.scheduler.enabled=true
```

Then use the Scheduler Registry:

```java
@Autowired
StreamBoxSchedulerRegistry registry;

@Test
void testProcessing() {
    var outbox = registry.get("productOutboxAdapter");
    var inbox  = registry.get("productInboxAdapter");

    outbox.consume(5);
    fakeKafka.fakeMessaging();
    inbox.consume(5);
}
```

---

# 🧱 Architecture

```
 ┌───────────────┐
 │  Outbox Table  │◄── save event in write transaction
 └───────┬───────┘
         │
         ▼
   Outbox Scheduler (SmartLifecycle)
         │
         ▼
  External Transport (Kafka, REST, FakeKafka…)
         │
         ▼
 ┌───────────────┐
 │  Inbox Table   │
 └───────┬───────┘
         │
         ▼
   Inbox Scheduler
         │
         ▼
   Projection Handler
```

## Recommended Practices

* Use `@Transactional` when calling `lockNextBatch` and `finish` to ensure proper row locking.
* Consider adding a `PROCESSED` and `FAILED` status to track event processing.
* For parallel consumers, `FOR UPDATE SKIP LOCKED` ensures no two consumers process the same event.

---

## Future Updates

* Retry and dead-letter queue (DLQ) support for failed events.

---

## License

MIT License.

---

## Acknowledgements

Inspired by the **CQRS + Outbox Pattern** for reliable event processing in distributed systems.
