package com.lda.streambox.port;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * StreamBoxInput is a minimal contract for implementing an outbox/inbox-style processing pipeline,
 * abstracted as a "stream box".
 * <p>
 * Typical lifecycle:
 * <ol>
 *   <li>{@link #addToBox(Object)} - enqueue an event into the stream box.</li>
 *   <li>{@link #lockNextBatch(int)} - atomically claim a batch of pending events for processing.</li>
 *   <li>For each claimed event:
 *     <ul>
 *       <li>Execute business logic in {@link #handleStreamBox(Object)} (ideally idempotent).</li>
 *       <li>Mark the event as completed via {@link #finish(Object)} to prevent reprocessing.</li>
 *     </ul>
 *   </li>
 * </ol>
 * <p>
 * Concurrency & Reliability:
 * <ul>
 *   <li>Implementations should use pessimistic or optimistic locking to ensure that
 *       {@link #lockNextBatch(int)} provides exclusive ownership of the returned events.</li>
 *   <li>Processing should be idempotent; failures must not corrupt state. Unfinished events
 *       should become eligible for re-delivery after a timeout or transaction rollback.</li>
 *   <li>{@link #handleStreamBox(Object)} is annotated with {@code @Transactional} to emphasize
 *       that event handling is typically atomic with side-effects and state transitions.</li>
 * </ul>
 *
 * @param <T> The concrete event type stored and processed by the stream box.
 */
public interface StreamBoxInput<T> {

    /**
     * Attempts to exclusively lock and return up to {@code limit} pending events for processing.
     * Locked events must not be returned to other callers until either:
     * <ul>
     *   <li>They are finished via {@link #finish(Object)}; or</li>
     *   <li>The lock is released/expired due to rollback or lease timeout (implementation-defined).</li>
     * </ul>
     * <p>
     * Contract expectations:
     * <ul>
     *   <li>Must be safe under concurrent callers (multiple workers/threads).</li>
     *   <li>Should be efficient and stable for large backlogs.</li>
     *   <li>May return fewer than {@code limit} events if insufficient are available.</li>
     * </ul>
     *
     * @param limit maximum number of events to claim in this call; must be &gt; 0.
     * @return a list of locked events, possibly empty but never {@code null}.
     * @throws IllegalArgumentException if {@code limit} &lt;= 0.
     */
    List<T> lockNextBatch(int limit);

    /**
     * Marks the given event as completed so it will not be reprocessed.
     * <p>
     * Implementations typically:
     * <ul>
     *   <li>Set a terminal status (e.g., DONE/SUCCESS),</li>
     *   <li>Persist processing metadata (timestamp, handler version, etc.),</li>
     *   <li>Release any locks/leases associated with the event.</li>
     * </ul>
     * <p>
     * This method should be idempotent—calling {@code finish} multiple times for the same event
     * must not cause an error or duplicate side-effects.
     *
     * @param streamBoxEvent the event previously locked and processed.
     * @throws IllegalStateException if the event was not locked by the current worker/session (implementation-defined).
     */
    void finish(T streamBoxEvent);

    /**
     * Enqueues a new event into the stream box for future processing.
     * <p>
     * Contract expectations:
     * <ul>
     *   <li>Must persist the event durably before returning, or clearly document at-least-once semantics.</li>
     *   <li>Should avoid duplicates if the upstream caller retries; consider deduplication keys.</li>
     * </ul>
     *
     * @param streamBoxEvent the event to add; must not be {@code null}.
     * @throws IllegalArgumentException if {@code streamBoxEvent} is invalid (implementation-defined).
     */
    void addToBox(T streamBoxEvent);

    /**
     * Handles a single event from the stream box in a transactional context.
     * <p>
     * Recommended flow inside implementations:
     * <ol>
     *   <li>Apply idempotent business logic (e.g., publish to a broker, call a downstream service, mutate state).</li>
     *   <li>On success, call {@link #finish(Object)} to mark the event as completed.</li>
     *   <li>On failure, throw an exception to trigger transaction rollback; the event remains eligible for retry.</li>
     * </ol>
     * <p>
     * Transactionality:
     * <ul>
     *   <li>Atomicity between business side-effects and {@link #finish(Object)} is strongly encouraged.</li>
     *   <li>Outbox pattern: write side-effects to a durable outbox within the same transaction, then publish asynchronously.</li>
     * </ul>
     *
     * @param streamBoxEvent the event to handle; generally expected to have been obtained via {@link #lockNextBatch(int)}.
     * @throws RuntimeException to signal a processing failure and force rollback.
     */
    @Transactional
    void handleStreamBox(T streamBoxEvent);
}
