-- V9: Expanded System Design curriculum
-- Core distributed-systems fundamentals + senior case studies.

INSERT INTO patterns
(id, slug, name, summary, recognition_clues, template_code, display_order, category, lesson_markdown, time_complexity, space_complexity)
VALUES

('b0000000-0000-0000-0000-000000000001','scalability-fundamentals','Scalability Fundamentals','Vertical vs horizontal scaling, stateless services, bottlenecks and capacity planning.','scale traffic
horizontal scaling
stateless services
capacity planning',NULL,201,'SYSTEM_DESIGN',$$## Scalability Fundamentals

Scalability means handling growth while keeping latency, availability and cost within targets.

### Core model
Vertical scaling adds CPU/RAM to one machine. Horizontal scaling adds instances behind a load balancer and is the usual approach for stateless application tiers.

Keep state in databases, caches, queues and object storage so any healthy instance can serve a request.

### Capacity
Estimate DAU, requests per user, peak multiplier, read/write ratio, payload size, storage growth and SLOs. Example: 10M DAU × 20 requests/day is 200M requests/day, about 2.3K average RPS; an 8× peak means about 18.5K peak RPS.

### Bottleneck workflow
Measure CPU, memory, DB QPS, connections, network, queue depth and p95/p99 latency before scaling.$$,'O(1) request routing','O(N) instances'),

('b0000000-0000-0000-0000-000000000002','availability-reliability','Availability & Reliability','Design for instance, zone, dependency and regional failures.','availability SLO
failover
redundancy
health checks
error budgets',NULL,202,'SYSTEM_DESIGN',$$## Availability & Reliability

Use redundancy across nodes and availability zones. Remove single points of failure from gateways, databases, queues and caches.

### Reliability patterns
Timeouts, exponential backoff with jitter, circuit breakers, bulkheads, idempotency, graceful degradation, health checks and dead-letter queues.

### RPO and RTO
RPO defines acceptable data loss. RTO defines recovery time. A production design should state both and explain backup, restore and failover.

### Interview drill
Ask what fails, how it is detected, what traffic does during failure, which data can be stale, and how recovery is verified.$$,'O(1) health decision','O(N) replicas'),

('b0000000-0000-0000-0000-000000000003','cap-theorem','CAP Theorem','Understand consistency and availability choices during network partitions.','CAP
partition
consistency
availability
quorum',NULL,203,'SYSTEM_DESIGN',$$## CAP Theorem

CAP concerns behavior when a distributed system experiences a network partition.

Consistency means reads observe the latest successful write according to the chosen model. Availability means requests continue receiving responses. During a partition, a distributed system cannot guarantee both perfectly.

### Quorum intuition
For N replicas, R + W > N gives read/write overlap under simplified quorum assumptions. Majority writes help prevent conflicting leaders.

### Interview trap
CAP is not simply pick any two at all times. Partition tolerance is a property of the distributed environment; the design choice is usually how much consistency or availability to preserve during a partition.$$,'O(1) quorum decision','O(N) replicas'),

('b0000000-0000-0000-0000-000000000004','consistency-models','Consistency Models','Compare strong, causal, session and eventual consistency.','strong consistency
eventual consistency
read-your-writes
causal consistency
replication lag',NULL,204,'SYSTEM_DESIGN',$$## Consistency Models

Strong consistency is useful for money, inventory and coordination. Eventual consistency is often sufficient for feeds, recommendations and analytics.

### Read-your-writes
After a user changes their profile, route their next read to a leader or otherwise ensure their own write is visible.

### Causal consistency
Preserves cause-and-effect ordering and can fit messaging or collaboration.

Choose the weakest model that still satisfies the product invariant; stronger guarantees usually increase coordination and latency.$$,'O(1) read decision','O(R) replica state'),

('b0000000-0000-0000-0000-000000000005','database-replication','Database Replication','Leader/follower replication, read scaling, lag and failover.','read replicas
leader follower
replication lag
failover
synchronous replication',NULL,205,'SYSTEM_DESIGN',$$## Database Replication

Writes can go to a leader while followers replicate the write log and serve reads.

Synchronous replication improves durability but adds latency. Asynchronous replication is faster but can lose recent writes during a catastrophic leader failure.

### Read scaling
Use replicas for read-heavy workloads, but account for lag. Read-your-writes flows may need leader routing.

### Failover
Detect failure, elect/promote a replica, redirect writes, prevent split brain and reconcile the old node.$$,'O(1) write leader','O(N) replica storage'),

('b0000000-0000-0000-0000-000000000006','database-partitioning-sharding','Partitioning & Sharding','Distribute large datasets while preserving query performance.','sharding
partition key
hot partition
range partition
hash partition',NULL,206,'SYSTEM_DESIGN',$$## Partitioning & Sharding

Range partitioning is useful for ordered scans but can create hot ranges. Hash partitioning distributes load but makes range queries harder. Directory-based partitioning gives flexibility at the cost of metadata.

### Good partition key
High cardinality, even distribution, frequently used in queries and stable over time.

### Resharding
Use logical buckets, consistent hashing or controlled partition movement so adding capacity does not require rewriting the entire dataset.

Avoid cross-shard joins and transactions where possible; build read models or asynchronous workflows instead.$$,'O(1) hash routing','O(N) shard storage'),

('b0000000-0000-0000-0000-000000000007','load-balancing','Load Balancers','L4/L7 routing, health checks, connection draining and stateless scaling.','load balancer
round robin
least connections
health check
sticky sessions',NULL,207,'SYSTEM_DESIGN',$$## Load Balancers

L4 balances using network information. L7 understands HTTP paths, headers and cookies.

Common algorithms are round robin, weighted round robin, least connections, least latency and consistent hashing.

Prefer stateless application instances over sticky sessions. Use readiness checks, connection draining and graceful shutdown during deployments.

For global systems, route traffic to healthy regions first, then to healthy zones and instances.$$,'O(N) backend selection','O(N) health state'),

('b0000000-0000-0000-0000-000000000008','cdn-edge-caching','CDN & Edge Caching','Serve cacheable content near users and protect origins.','CDN
edge cache
TTL
cache-control
purge',NULL,208,'SYSTEM_DESIGN',$$## CDN & Edge Caching

The request path is user → edge POP → cache → origin.

Use Cache-Control, ETag, versioned asset names and explicit TTLs. Versioned URLs are especially effective for immutable JavaScript, CSS and images.

Protect origins with origin shielding, request coalescing and stale-while-revalidate where safe.

Never cache personalized data without including the correct identity dimensions in the cache key.$$,'O(1) edge lookup','O(C) cached objects'),

('b0000000-0000-0000-0000-000000000009','api-gateway','API Gateway','Centralize routing, authentication, quotas, transformation and edge observability.','API gateway
authentication
routing
rate limiting
API versioning',NULL,209,'SYSTEM_DESIGN',$$## API Gateway

Typical responsibilities: TLS termination, authentication, routing, rate limiting, payload limits, API versioning, correlation IDs and access logs.

Keep domain logic in services instead of turning the gateway into a monolith.

A BFF can tailor APIs for web/mobile clients. Run gateways redundantly because the gateway itself must not become a single point of failure.$$,'O(1) route lookup','O(S) route configuration'),

('b0000000-0000-0000-0000-000000000010','kafka-event-streaming','Kafka & Event Streaming','Partitions, consumer groups, offsets, retries and durable event pipelines.','Kafka
partitions
consumer groups
offset
event streaming
DLQ',NULL,210,'SYSTEM_DESIGN',$$## Kafka & Event Streaming

Producer → topic → partitions → consumer group.

Ordering exists within a partition. Choose a partition key that preserves required business ordering, such as order_id.

Most business systems use at-least-once delivery plus idempotent consumers. Monitor consumer lag and use retry topics or DLQs for poison messages.

Kafka can act as a durable event log, enabling replay and rebuilding downstream projections.$$,'O(1) append per partition','O(P + retention) storage'),

('b0000000-0000-0000-0000-000000000011','distributed-locks','Distributed Locks','Coordinate work across application instances with leases and fencing.','distributed lock
lease
leader election
Redis lock
fencing token',NULL,211,'SYSTEM_DESIGN',$$## Distributed Locks

A Redis lease can use SET key token NX PX ttl. Release only if the stored token still belongs to the caller.

For critical workflows, combine locks with database constraints, idempotency and fencing tokens. A lock alone does not make a payment or inventory operation safe.

Alternatives include database row locks and coordination systems such as ZooKeeper or etcd.

Discuss lease expiry, crashes, delayed processes and split-brain behavior in interviews.$$,'O(1) lock operation','O(K) active locks'),

('b0000000-0000-0000-0000-000000000012','database-selection','SQL vs NoSQL','Choose storage from access patterns, consistency, relationships and scale.','SQL
NoSQL
Cassandra
DynamoDB
MongoDB
transactions',NULL,212,'SYSTEM_DESIGN',$$## SQL vs NoSQL

SQL is strong for relationships, constraints, transactions and flexible queries.

Key-value and wide-column databases fit predictable, very high-scale access patterns. Document stores fit aggregate-shaped data. Search engines fit full-text search and ranking. Object storage fits large blobs.

Start with access patterns, not brand names. State read/write volume, consistency, transaction boundaries, query shape, dataset size and retention before choosing storage.$$,'O(1) logical lookup','O(N) stored records'),

('b0000000-0000-0000-0000-000000000013','observability','Observability: Logs, Metrics & Traces','Design production visibility using metrics, logs, traces and SLOs.','metrics
logs
traces
OpenTelemetry
p95
p99
correlation ID',NULL,213,'SYSTEM_DESIGN',$$## Observability

Metrics measure traffic, errors, latency and saturation. Logs explain individual events. Traces show the path of a request through services.

Propagate a correlation/trace ID through gateway, services, queues and database calls.

Alert on user-impacting SLOs instead of every infrastructure fluctuation. Use structured logs, controlled metric cardinality, trace sampling and retention policies.$$,'O(H) trace hops','O(R) telemetry volume'),

('b0000000-0000-0000-0000-000000000014','service-mesh','Service Mesh','Service-to-service security, traffic management and telemetry through proxies.','mTLS
service discovery
sidecar
traffic splitting
service policy',NULL,214,'SYSTEM_DESIGN',$$## Service Mesh

A mesh puts a proxy beside services so networking concerns are standardized.

Capabilities include mTLS, service discovery, retries, timeouts, traffic splitting, tracing and authorization policy.

The trade-off is operational complexity and proxy overhead. Use it when the platform has enough services and repeated networking policy to justify the control plane.$$,'O(1) policy lookup','O(S) proxy instances'),

('b0000000-0000-0000-0000-000000000015','object-storage-file-system','Object Storage & File Systems','Separate large blobs from metadata using signed uploads, multipart transfer and CDN delivery.','object storage
S3
pre-signed URL
multipart upload
CDN
metadata',NULL,215,'SYSTEM_DESIGN',$$## Object Storage & File Systems

Keep file metadata in SQL/NoSQL and file bytes in object storage.

Upload flow: API creates a short-lived signed URL; client uploads directly to object storage. Large files use multipart/resumable upload.

After upload, publish events for virus scanning, OCR, thumbnails or transcoding. Mark the file ready only after required processing succeeds.

Use encryption, authorization, size limits, content validation and lifecycle cleanup.$$,'O(1) metadata lookup','O(B) bytes stored'),

('b0000000-0000-0000-0000-000000000016','distributed-job-scheduler','Distributed Job Scheduler','Reliable delayed and recurring background execution.','job scheduler
cron
delayed jobs
worker pool
retry
lease',NULL,216,'SYSTEM_DESIGN',$$## Distributed Job Scheduler

Scheduler → durable job store → queue → workers → result store.

Claim due jobs using a lease or transaction so multiple scheduler instances do not execute the same job concurrently.

At-least-once delivery means a worker may execute and crash before acknowledgement. Use idempotent job keys.

Add exponential retry, maximum attempts, DLQ, cancellation and backpressure. Partition jobs by due-time buckets or shard key.$$,'O(log N) due-job scheduling','O(N) queued jobs'),

('b0000000-0000-0000-0000-000000000017','advanced-rate-limiting','Advanced Rate Limiting & Quotas','Tenant quotas, fairness, distributed counters and adaptive protection.','tenant quota
fairness
sliding window
quota
backpressure',NULL,217,'SYSTEM_DESIGN',$$## Advanced Rate Limiting

Use hierarchical limits: Global → Tenant → User → Endpoint.

A rate limit protects short-term throughput; a quota controls consumption over a larger period.

Prevent noisy neighbors with tenant-aware limits. When downstream systems saturate, apply backpressure and return 429/503 rather than letting queues grow without bound.

Discuss Redis failure, fairness, burst capacity and retry behavior.$$,'O(1) counter operation','O(U + T) counters'),

('b0000000-0000-0000-0000-000000000018','distributed-transactions-sagas','Distributed Transactions & Sagas','Coordinate multi-service workflows without a global database transaction.','saga
outbox
compensation
workflow
distributed transaction',NULL,218,'SYSTEM_DESIGN',$$## Distributed Transactions & Sagas

A checkout may span order, inventory, payment and shipment services.

A Saga breaks the workflow into local transactions and compensating actions. Choreography uses events; orchestration uses a workflow coordinator.

A transactional outbox writes business state and the outgoing event in one local transaction, then publishes asynchronously.

Every consumer and compensation action must be idempotent. Model intermediate states explicitly.$$,'O(S) workflow steps','O(S) workflow state'),

('b0000000-0000-0000-0000-000000000019','pagination-search','Pagination & Large-Scale Search','Efficient collection APIs and search using cursor pagination and indexes.','cursor pagination
search
inverted index
filters
deep pagination',NULL,219,'SYSTEM_DESIGN',$$## Pagination & Search

Offset pagination becomes expensive for very large offsets. Cursor pagination uses a stable ordered key, for example created_at plus a unique ID.

Search engines use inverted indexes for full-text queries. Keep the source of truth separate from the search index when appropriate.

Discuss freshness, relevance, typo tolerance, stable pagination, index design and deep pagination.$$,'O(log N + K) indexed page','O(K) result page'),

('b0000000-0000-0000-0000-000000000020','multi-region-architecture','Multi-Region Architecture','Regional isolation, global routing, replication and disaster recovery.','multi-region
active-active
active-passive
geo routing
regional failover',NULL,220,'SYSTEM_DESIGN',$$## Multi-Region Architecture

Active-passive is simpler and can preserve a single writer. Active-active improves locality and availability but makes conflict resolution harder.

Use global traffic management with health checks. Define RPO/RTO, backup retention and tested failover procedures.

Do not choose active-active automatically. First determine the consistency requirement and whether the business actually needs regional writes.$$,'O(1) regional routing','O(R) regional state'),

('b0000000-0000-0000-0000-000000000021','distributed-ids','Distributed ID Generation','Unique sortable identifiers without a central database bottleneck.','Snowflake ID
UUID
distributed ID
sortable ID
sequence',NULL,221,'SYSTEM_DESIGN',$$## Distributed ID Generation

UUIDs are decentralized and simple. Snowflake-style IDs combine timestamp, worker identity and sequence for compact, approximately sortable IDs.

Clock rollback needs explicit handling. Database sequences are excellent when one write domain is sufficient.

Ask whether global ordering is actually required. Uniqueness plus approximate time ordering is usually much cheaper than strict global ordering.$$,'O(1) ID generation','O(1) state per worker'),

('b0000000-0000-0000-0000-000000000022','distributed-counting-analytics','Distributed Counters & Analytics','High-volume counters, stream aggregation and approximate analytics.','counter
aggregation
clicks
HyperLogLog
stream processing',NULL,222,'SYSTEM_DESIGN',$$## Distributed Counters & Analytics

A single hot counter row can become a contention point. Shard counters into buckets and aggregate asynchronously.

Operational events can flow through Kafka into stream processors and an OLAP store.

Define whether each metric is exact, eventually consistent, approximate or windowed. HyperLogLog is useful when approximate unique counts are acceptable.$$,'O(1) event append','O(B) counter buckets'),

('b0000000-0000-0000-0000-000000000023','realtime-websocket','Real-Time Systems & WebSockets','Low-latency bidirectional communication for chat, presence and live updates.','WebSocket
presence
pub-sub
connection state
fanout',NULL,223,'SYSTEM_DESIGN',$$## Real-Time Systems & WebSockets

WebSocket gateways hold long-lived connections. Keep durable business state outside the gateway so it can scale horizontally.

Use pub/sub for fanout. Presence is usually TTL-based and eventually consistent.

Clients need reconnect backoff and a cursor/sequence to request missed messages. Define ordering per room or conversation rather than globally.$$,'O(1) connection routing','O(C) active connections'),

('b0000000-0000-0000-0000-000000000024','security-distributed-systems','Security in Distributed Systems','Authentication, authorization, secrets, data protection and abuse prevention at scale.','authentication
authorization
OAuth
JWT
secrets
WAF
zero trust',NULL,224,'SYSTEM_DESIGN',$$## Security in Distributed Systems

Authenticate at the edge and authorize every sensitive operation.

Use TLS, least privilege, short-lived credentials, secret rotation and service-to-service authorization. Never hard-code secrets or log sensitive tokens.

JWTs reduce session lookups but require expiry, rotation and revocation strategy.

Threat-model assets and trust boundaries. Security is an architectural requirement, not a final checklist.$$,'O(1) policy check','O(U) active credentials'),

('b0000000-0000-0000-0000-000000000025','youtube','Design YouTube','Video upload, transcoding, storage, CDN playback and recommendations.','video upload
transcoding
HLS
CDN
object storage',NULL,225,'SYSTEM_DESIGN',$$## Case Study: YouTube

Client → Upload API → Object Storage → Processing Queue → Transcoding Workers → CDN.

Store metadata separately from video blobs. Generate multiple resolutions/codecs asynchronously. Use adaptive bitrate streaming such as HLS/DASH.

CDN carries the bandwidth-heavy playback path. Make processing jobs idempotent and retryable.

Follow-ups: live streaming, recommendations, moderation, comments and creator analytics.$$,'O(log R) quality selection','O(V) video storage'),

('b0000000-0000-0000-0000-000000000026','whatsapp','Design WhatsApp','Messaging with delivery state, ordering, offline storage and real-time connections.','chat
message ordering
offline delivery
WebSocket
read receipts
group chat',NULL,226,'SYSTEM_DESIGN',$$## Case Study: WhatsApp

Sender → Message Service → Durable Store → Recipient Gateway → WebSocket.

Persist before acknowledging durable delivery. Partition by conversation ID to preserve per-conversation ordering.

Use idempotency keys to prevent duplicate sends. Offline users receive messages from durable storage after reconnect.

Presence can be eventually consistent; delivery/read receipts are explicit state transitions.$$,'O(1) message append','O(M) conversation messages'),

('b0000000-0000-0000-0000-000000000027','uber','Design Uber / Ride Booking','Location ingestion, nearby-driver search, matching and trip state.','geospatial search
matching
trip state
location updates
concurrency',NULL,227,'SYSTEM_DESIGN',$$## Case Study: Uber

Drivers publish location updates into a geospatial index. Riders request trips using pickup and destination coordinates.

Find candidates in nearby cells, filter available drivers, estimate ETA and atomically reserve one driver.

Trip state is a durable state machine: REQUESTED → MATCHED → ARRIVING → IN_PROGRESS → COMPLETED/CANCELLED.

Location is high-volume ephemeral state; persist only what business requirements need.$$,'O(log D) spatial lookup','O(D) active drivers'),

('b0000000-0000-0000-0000-000000000028','instagram','Design Instagram','Media upload, feed generation, CDN, likes, comments and notifications.','photo sharing
feed
media processing
fanout
CDN',NULL,228,'SYSTEM_DESIGN',$$## Case Study: Instagram

Upload media directly to object storage, process thumbnails/resolutions asynchronously and serve through CDN.

Use hybrid feed fanout: push for ordinary accounts and pull/inject for celebrity accounts.

Engagement events feed notification and counter pipelines asynchronously.

Follow-ups: ranking, moderation, stories, reels, search and multi-region media.$$,'O(1) cached feed read','O(F) fanout per post'),

('b0000000-0000-0000-0000-000000000029','google-drive','Design Google Drive','Cloud file storage with upload, sharing, versions and synchronization.','file storage
sharing
versioning
sync
chunk upload',NULL,229,'SYSTEM_DESIGN',$$## Case Study: Google Drive

Keep file metadata separate from object-storage blobs. Large files use chunked resumable upload.

Represent versions explicitly. Authorization applies before issuing signed download URLs.

Clients synchronize using cursors and versions. Concurrent offline edits require conflict detection and a defined merge/conflict-copy policy.

Follow-ups: collaborative editing, quotas, search and multi-region replication.$$,'O(1) metadata lookup','O(V) file versions'),

('b0000000-0000-0000-0000-000000000030','payment-system','Design a Payment System','Idempotency, provider failures, webhooks, reconciliation and auditability.','payment
idempotency
webhooks
reconciliation
ledger
state machine',NULL,230,'SYSTEM_DESIGN',$$## Case Study: Payment System

Use a durable payment state machine: CREATED → AUTHORIZED → CAPTURED → SETTLED with failure/refund paths.

Every client request needs an idempotency key. A provider timeout is ambiguous: the provider may have processed the payment.

Use webhooks, reconciliation and an append-only ledger. Minimize PCI scope and keep payment secrets out of logs.

Follow-ups: refunds, duplicate webhooks, chargebacks, fraud and multi-currency.$$,'O(1) idempotency lookup','O(T) ledger entries'),

('b0000000-0000-0000-0000-000000000031','netflix','Design Netflix','Global video streaming with CDN, catalog, playback and recommendations.','video streaming
CDN
recommendations
catalog
adaptive bitrate',NULL,231,'SYSTEM_DESIGN',$$## Case Study: Netflix

The CDN carries video segments while APIs provide metadata, authorization and playback manifests.

Transcode source masters into multiple codecs/resolutions and publish them to edge caches.

Recommendations can be precomputed asynchronously and cached for low latency.

Follow-ups: DRM, subtitles, watch progress, offline downloads, parental controls and live events.$$,'O(1) edge segment lookup','O(V) media storage'),

('b0000000-0000-0000-0000-000000000032','ticket-booking','Design a Ticket Booking System','High-contention inventory, temporary holds, payment and expiration.','seat booking
inventory
reservation
contention
TTL hold',NULL,232,'SYSTEM_DESIGN',$$## Case Study: Ticket Booking

Seat state is AVAILABLE → HELD → SOLD. The AVAILABLE → HELD transition must be atomic.

A hold has an expiry timestamp. Payment failures release inventory safely.

Use idempotency for checkout. For huge events, add waiting rooms and admission control before requests reach inventory.

Follow-ups: seat maps, refunds, cancellations and disaster recovery.$$,'O(1) seat state lookup','O(S) seat inventory'),

('b0000000-0000-0000-0000-000000000033','food-delivery','Design Food Delivery','Restaurant workflow, driver dispatch, location and customer tracking.','order workflow
driver dispatch
geospatial
ETA
restaurant integration',NULL,233,'SYSTEM_DESIGN',$$## Case Study: Food Delivery

Order flow: CREATED → RESTAURANT_ACCEPTED → PREPARING → DRIVER_ASSIGNED → PICKED_UP → DELIVERED.

Use geospatial cells for nearby-driver discovery. Publish state changes to notification and tracking consumers.

Order invariants need durable state; location and ETA can be eventually consistent.

Follow-ups: batching, surge pricing, maps, restaurant failures and refunds.$$,'O(log D) driver lookup','O(D) active drivers'),

('b0000000-0000-0000-0000-000000000034','search-engine','Design a Search Engine','Crawling, inverted indexes, ranking, sharding and freshness.','crawler
inverted index
ranking
sharding
autocomplete',NULL,234,'SYSTEM_DESIGN',$$## Case Study: Search Engine

Crawler → parser → inverted index → ranking → query service.

Shard indexes and fan out a query only to relevant shards. Merge top candidates at the query layer.

Separate online query serving from offline indexing. Incremental indexing improves freshness; periodic rebuilds improve correctness and recoverability.

Follow-ups: autocomplete, typo correction, personalization and spam detection.$$,'O(Q log N + K)','O(D + index size)'),

('b0000000-0000-0000-0000-000000000035','logging-metrics-platform','Design a Logging & Metrics Platform','High-throughput telemetry ingestion, buffering, storage and alerting.','logs
metrics
stream ingestion
retention
alerting
OLAP',NULL,235,'SYSTEM_DESIGN',$$## Case Study: Logging & Metrics Platform

Agents → ingestion gateway → Kafka → stream processing → hot store → cold storage.

Buffer bursts so applications are not coupled to final analytics storage. Control metric cardinality and compress structured logs.

Keep recent data in a fast store and archive older data to object storage.

Alerts should be based on SLOs and deduplicated before notification.$$,'O(1) event append','O(T) telemetry retention'),

('b0000000-0000-0000-0000-000000000036','file-sync','Design Dropbox / File Sync','Reliable multi-device synchronization using chunks, versions and conflict handling.','file sync
chunking
conflict resolution
offline client
versioning',NULL,236,'SYSTEM_DESIGN',$$## Case Study: File Sync

Split large files into content-addressed chunks. Unchanged chunks need not be uploaded again.

Clients keep a cursor and request metadata changes since that cursor.

Concurrent offline edits create divergent versions. Detect the conflict and merge where possible or create a conflict copy.

Uploads must be resumable and metadata changes durable before the client advances its cursor.$$,'O(C) changed chunks','O(V) version metadata'),

('b0000000-0000-0000-0000-000000000037','service-discovery','Service Discovery & Configuration','Connect dynamic service instances using discovery, health checks and safe configuration rollout.','service discovery
DNS
registry
configuration
health checks',NULL,237,'SYSTEM_DESIGN',$$## Service Discovery & Configuration

Containers are ephemeral, so services need a stable way to find healthy endpoints.

Use DNS, platform-native discovery, a registry or a service mesh. Distinguish registered from ready instances.

Cache discovery briefly so a registry outage does not stop every request. Version configuration and support safe rollback.$$,'O(1) endpoint lookup','O(S) registered services'),

('b0000000-0000-0000-0000-000000000038','queue-backpressure','Queue Design & Backpressure','Absorb bursts while controlling throughput, retries, ordering and queue growth.','message queue
backpressure
consumer lag
retry
DLQ',NULL,238,'SYSTEM_DESIGN',$$## Queue Design & Backpressure

Queues decouple producers from consumers and absorb temporary bursts.

Define ordering scope, delivery semantics, visibility timeout, retry policy, DLQ, retention and maximum depth.

If consumer throughput is lower than producer throughput, backlog grows. Apply admission control or producer throttling before downstream resources are exhausted.

Global ordering is expensive; prefer partition-level ordering when the domain allows it.$$,'O(1) enqueue','O(N) queued messages'),

('b0000000-0000-0000-0000-000000000039','system-design-interview-framework','System Design Interview Framework','A repeatable senior interview method from requirements through trade-offs.','requirements
capacity estimation
API design
data model
architecture
trade-offs',NULL,239,'SYSTEM_DESIGN',$$## System Design Interview Framework

### 1. Clarify
Functional requirements, users, traffic, latency, availability, consistency, retention and geography.

### 2. Estimate
Requests/sec, peak multiplier, storage/day, bandwidth and read/write ratio.

### 3. APIs and data
Define representative endpoints/events, idempotency and access patterns. Choose indexes and partition keys.

### 4. Architecture
Start with Client → Gateway → Services → Cache/DB/Queue/Object Storage. Scale only where estimates demand it.

### 5. Deep dive
Pick the hardest bottleneck and explain its failure modes.

### 6. Finish
Security, observability, reliability, rollback and explicit trade-offs.

Do not name technologies first. Start from workload and requirements, then justify the technology.$$,'O(1) framework steps','O(1) design notes'),

('b0000000-0000-0000-0000-000000000040','senior-architecture-scenarios','Senior Architecture Scenarios','Practice production incidents, migrations, bottleneck analysis and technology trade-offs.','trade-offs
incident scenario
migration
bottleneck
architecture review',NULL,240,'SYSTEM_DESIGN',$$## Senior Architecture Scenarios

### p99 latency doubled
Check traffic, dependency latency, cache hit rate, DB connections, GC, queue depth and recent deployments using metrics and traces before changing architecture.

### Database CPU is 90%
Identify expensive queries and indexes first. Read replicas help read saturation; partitioning or workload reduction may be required for write saturation.

### One tenant dominates traffic
Use tenant-aware quotas, partitioning and noisy-neighbor isolation.

### Monolith migration
Define domain boundaries and data ownership. Use a strangler approach, outbox/events and incremental traffic migration.

### Cache outage
Estimate whether the database can absorb misses. Add request coalescing, stale data where safe, circuit breakers and controlled degradation.

### Payment exactly-once
Treat network timeouts as ambiguous. Use idempotency, durable state, provider webhooks, reconciliation and an auditable ledger.$$,
'O(1) scenario analysis','O(S) system components')

ON CONFLICT (slug) DO UPDATE SET
name=EXCLUDED.name,
summary=EXCLUDED.summary,
recognition_clues=EXCLUDED.recognition_clues,
display_order=EXCLUDED.display_order,
category=EXCLUDED.category,
lesson_markdown=EXCLUDED.lesson_markdown,
time_complexity=EXCLUDED.time_complexity,
space_complexity=EXCLUDED.space_complexity;
