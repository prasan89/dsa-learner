-- ── V8: System Design Topics + Q&A Problems ──────────────────────────────────

ALTER TABLE patterns ADD COLUMN IF NOT EXISTS category VARCHAR(50) NOT NULL DEFAULT 'DSA';

-- ── 8 System Design Topic Patterns ───────────────────────────────────────────
INSERT INTO patterns (id, slug, name, summary, recognition_clues, template_code, display_order, category, lesson_markdown, time_complexity, space_complexity) VALUES

-- 1. URL Shortener
('a0000000-0000-0000-0000-000000000001',
 'url-shortener',
 'URL Shortener',
 'Design a scalable service that maps long URLs to short codes (e.g. bit.ly, tinyurl).',
 'shorten a URL
generate unique short code
handle redirects
analytics / click tracking
high read:write ratio',
 NULL, 101, 'SYSTEM_DESIGN',
$$## URL Shortener (bit.ly / TinyURL)

### Core Requirements
**Functional:** shorten URL → short code; redirect short code → original URL; optional custom alias; analytics (click count, geo).
**Non-functional:** 100M URLs/day write, 10:1 read ratio → 1B reads/day; 99.99% availability; <10ms redirect latency.

### Capacity Estimation
| Metric | Value |
|---|---|
| Writes/sec | ~1,160 |
| Reads/sec | ~11,600 |
| Storage (5 yr) | ~300 GB (500B avg URL × 100M/day × 365 × 5) |
| Cache RAM | ~20 GB (20% hot URLs) |

### Short Code Generation
**Option A – Base62 encoding of auto-increment ID**
```
ID: 100000 → base62 → "VgE" (6 chars = 62⁶ ≈ 56B URLs)
```
Pros: simple, sequential. Cons: predictable, single-point counter.

**Option B – MD5/SHA256 hash + truncate (7 chars)**
```java
String hash = DigestUtils.md5Hex(longUrl).substring(0, 7);
```
Pros: no central counter. Cons: collision possible (~0.1% at 1B URLs) → append user_id or timestamp on collision.

**Option C – Random + uniqueness check (recommended for scale)**
```java
String code = RandomStringUtils.randomAlphanumeric(7);
// INSERT ... ON CONFLICT → retry
```

### Schema
```sql
CREATE TABLE urls (
    code       VARCHAR(10)  PRIMARY KEY,
    long_url   TEXT         NOT NULL,
    user_id    BIGINT,
    created_at TIMESTAMPTZ  DEFAULT NOW(),
    expires_at TIMESTAMPTZ,
    clicks     BIGINT       DEFAULT 0
);
CREATE INDEX idx_long_url ON urls(long_url); -- dedup check
```

### Architecture
```
Client → CDN / Edge (cache hot redirects)
       → API Gateway
       → Redirect Service  →  Redis (TTL 24h)
                           →  DB (Postgres / Cassandra)
       → Shorten Service   →  ID Generator (Snowflake or counter)
                           →  DB write
       → Analytics Service →  Kafka → ClickHouse / BigQuery
```

### Read Path (Redirect)
1. Check Redis: `GET code` → O(1) → return 302.
2. Cache miss → DB lookup → populate Redis → return 302.
3. Async: publish click event to Kafka.

### Write Path (Shorten)
1. Validate URL format.
2. Check dedup cache (long_url → code) — avoid duplicates.
3. Generate code, INSERT into DB.
4. Cache: `SET code longUrl EX 86400`.
5. Return short URL.

### Scale-out
- **Read replicas** for DB (reads >> writes).
- **Consistent hashing** shards Redis across nodes.
- **CDN** caches popular redirects at edge (Cache-Control: max-age=3600).
- **Geo-distributed** deployments with eventual consistency for analytics.

### Key Trade-offs
| Decision | Trade-off |
|---|---|
| 302 vs 301 redirect | 302 → every request hits server (accurate analytics). 301 → browser caches, saves server hops. |
| SQL vs NoSQL | SQL: strong consistency, easy dedup. NoSQL (Cassandra): better write scale. |
| Hash vs ID | Hash: distributed. ID: shorter codes, predictable. |
$$,
 'O(1) redirect', 'O(N) storage'),

-- 2. Rate Limiter
('a0000000-0000-0000-0000-000000000002',
 'rate-limiter',
 'Rate Limiter',
 'Design a system that limits how many requests a user/IP can make in a time window.',
 'throttle API calls
per-user / per-IP limits
sliding vs fixed window
distributed rate limiting
429 Too Many Requests',
 NULL, 102, 'SYSTEM_DESIGN',
$$## Rate Limiter

### Core Requirements
**Functional:** limit requests per user/IP/API-key to N per time window T; return 429 when exceeded; configurable limits per endpoint.
**Non-functional:** <1ms overhead; consistent across distributed servers; handle 100K+ req/sec.

### Algorithms

#### 1. Token Bucket (recommended)
```
Bucket capacity = max burst (e.g. 100 tokens)
Refill rate      = N tokens/sec
Request consumes 1 token; if empty → 429
```
Pros: allows bursts. Cons: slightly complex counter.

#### 2. Fixed Window Counter
```
Window = current minute
Key    = "user:1234:2024-01-15-14:32"
INCR key; EXPIRE key 60
if count > limit → 429
```
Pros: simple. Cons: boundary attack — 200 req in last second of window + first second of next.

#### 3. Sliding Window Log
Store timestamps of each request; remove old ones; check count.
Pros: accurate. Cons: memory O(N requests).

#### 4. Sliding Window Counter (best balance)
```
count = prev_window_count × (1 - elapsed/window) + curr_window_count
```
Pros: smooth, memory efficient. Cons: approximate.

### Redis Implementation (Token Bucket)
```lua
-- Lua script (atomic)
local key     = KEYS[1]
local capacity = tonumber(ARGV[1])
local refill  = tonumber(ARGV[2])   -- tokens/sec
local now     = tonumber(ARGV[3])   -- epoch ms

local data    = redis.call('HMGET', key, 'tokens', 'last_refill')
local tokens  = tonumber(data[1]) or capacity
local last    = tonumber(data[2]) or now

local delta   = (now - last) / 1000 * refill
tokens        = math.min(capacity, tokens + delta)

if tokens >= 1 then
    tokens = tokens - 1
    redis.call('HMSET', key, 'tokens', tokens, 'last_refill', now)
    redis.call('EXPIRE', key, 3600)
    return 1   -- allowed
else
    return 0   -- denied
end
```

### Architecture
```
Request → API Gateway (middleware)
        → Rate Limiter Service
          → Redis Cluster (per-user counters)
          → Return allow/deny + headers:
              X-RateLimit-Limit: 100
              X-RateLimit-Remaining: 42
              X-RateLimit-Reset: 1705312800
        → Backend Service (if allowed)
```

### Distributed Concerns
- Use **Redis cluster** — all nodes share state via consistent hashing.
- **Race condition**: use Lua scripts or `SET ... NX` for atomicity.
- **Synchronization lag**: accept ~0.5% over-limit rather than strong consistency.

### Key Trade-offs
| Decision | Trade-off |
|---|---|
| Client-side vs server-side | Server: accurate but latency. Client: fast but gameable. |
| Hard vs soft limit | Soft: allow small burst. Hard: strict cap. |
| Redis vs in-memory | Redis: shared across pods. In-memory: faster but per-pod divergence. |
$$,
 'O(1) per request', 'O(U) users'),

-- 3. Design Twitter / News Feed
('a0000000-0000-0000-0000-000000000003',
 'news-feed',
 'News Feed (Twitter/Instagram)',
 'Design a social feed where users see posts from people they follow in reverse-chronological order.',
 'timeline / feed generation
fan-out on write vs read
celebrity problem
ranking / relevance
infinite scroll pagination',
 NULL, 103, 'SYSTEM_DESIGN',
$$## News Feed System (Twitter / Instagram)

### Core Requirements
**Functional:** post tweets; follow/unfollow; home timeline (posts from followees, reverse-chron); user timeline; like/retweet.
**Non-functional:** 300M DAU; 500M tweets/day; timeline load <200ms; eventual consistency OK for feed.

### Data Model
```sql
users    (user_id, name, follower_count)
tweets   (tweet_id BIGINT, user_id, content, created_at)
follows  (follower_id, followee_id, created_at)
feed     (user_id, tweet_id, score)  -- materialized cache
```
Use **Snowflake IDs** for tweet_id: 64-bit (timestamp + datacenter + sequence) → sortable by time.

### Fan-out Strategies

#### Fan-out on Write (push model)
When user A tweets → write tweet to all A's followers' feed caches immediately.
```
Tweet → Fanout Service → foreach follower: Redis LPUSH feed:{follower_id} tweet_id
                       → LTRIM feed:{follower_id} 0 999  (keep latest 1000)
```
**Pros:** O(1) read (just LRANGE). **Cons:** O(N followers) write; celebrities with 100M followers = 100M Redis writes.

#### Fan-out on Read (pull model)
When user opens feed → fetch followee list → fetch latest tweets from each → merge sort.
**Pros:** no write amplification. **Cons:** O(followees) read latency; hard to cache.

#### Hybrid (recommended for Twitter scale)
- **Regular users** (< 10K followers): fan-out on write.
- **Celebrities** (> 10K followers): fan-out on read at query time, merged with pre-built feed.

### Architecture
```
Post Tweet:
Client → Tweet Service → Kafka "new-tweet" topic
                       → Fanout Worker (consumes Kafka)
                         → write to follower feed caches (Redis)
                         → write to tweets table (Cassandra)

Load Feed:
Client → Feed Service → Redis: LRANGE feed:{uid} 0 19
                      → (cache miss) pull from Cassandra + sort
                      → (celebrity injection) merge celebrity tweets
                      → return paginated results
```

### Storage
| Data | Store | Why |
|---|---|---|
| Tweets | Cassandra | Write-heavy, time-series, scale |
| User / Follow graph | MySQL | Relational, joins |
| Feed cache | Redis | Sub-ms reads, LRU eviction |
| Media (images/video) | S3 + CDN | Blob storage |
| Search | Elasticsearch | Full-text tweet search |

### Key Trade-offs
| Decision | Trade-off |
|---|---|
| Push vs pull | Push: fast read. Pull: no write amplification. Hybrid wins at scale. |
| Chronological vs ranked | Chron: simple, predictable. Ranked (ML score): better engagement. |
| Strong vs eventual consistency | Eventual: tweet may appear with ~seconds delay. Acceptable for social. |
$$,
 'O(1) feed read (cached)', 'O(U × 1000) Redis'),

-- 4. Consistent Hashing
('a0000000-0000-0000-0000-000000000004',
 'consistent-hashing',
 'Consistent Hashing',
 'Distribute data across nodes so that adding/removing a node only remaps a minimal fraction of keys.',
 'distributed cache
sharding strategy
node add/remove without full rehash
load balancing
DHT (Distributed Hash Table)',
 NULL, 104, 'SYSTEM_DESIGN',
$$## Consistent Hashing

### Problem with Naive Hashing
`node = hash(key) % N` — when N changes (add/remove server), almost every key remaps → cache invalidation storm, hot spots.

### Consistent Hashing Ring
1. Map each server to a point on a 0–2³² ring: `hash(server_ip)`.
2. Map each key to a point: `hash(key)`.
3. Assign key to the **first server clockwise** from key's position.

```
Ring: 0 ────── Server A (hash=100) ───── Server B (hash=200) ───── Server C (hash=300) ────── 2³²
Key X (hash=150) → Server B (next clockwise)
```

**Add Server D (hash=250):** only keys between 200–250 move from B → D.
**Remove Server B:** only keys between 100–200 move to C.
Expected remapped keys = `K/N` instead of `K`.

### Virtual Nodes (vnodes)
Each physical server gets **150–200 virtual nodes** spread around the ring.
→ More uniform distribution, better handles heterogeneous server capacities.

```java
TreeMap<Integer, String> ring = new TreeMap<>();

void addNode(String node, int replicas) {
    for (int i = 0; i < replicas; i++) {
        int hash = hash(node + "#" + i);
        ring.put(hash, node);
    }
}

String getNode(String key) {
    int h = hash(key);
    Map.Entry<Integer, String> entry = ring.ceilingEntry(h);
    return (entry != null ? entry : ring.firstEntry()).getValue();
}
```

### Real-world Usage
| System | How used |
|---|---|
| Amazon DynamoDB | Partition keys onto nodes |
| Apache Cassandra | Token ring for data distribution |
| Redis Cluster | Hash slots (16384 slots mapped to nodes) |
| Nginx / HAProxy | Upstream load balancing |
| CDN (Akamai) | Map content to edge servers |

### Key Trade-offs
| Decision | Trade-off |
|---|---|
| More vnodes | Better balance but more ring memory |
| Replication factor | More copies → higher availability, more storage |
| Hot key handling | Need separate strategy (e.g. add random suffix to split) |
$$,
 'O(log N) lookup', 'O(N × vnodes)'),

-- 5. Distributed Cache
('a0000000-0000-0000-0000-000000000005',
 'distributed-cache',
 'Distributed Cache (Redis)',
 'Design a distributed caching layer to reduce database load and serve hot data at low latency.',
 'reduce DB load
in-memory store
cache eviction policies
cache invalidation
write-through vs write-back',
 NULL, 105, 'SYSTEM_DESIGN',
$$## Distributed Cache (Redis-style)

### Why Cache?
DB can handle ~10K QPS; cache handles 1M+ QPS at sub-ms latency. Cache hot 20% of data that serves 80% of reads (Pareto principle).

### Cache Patterns

#### Cache-Aside (Lazy Loading) — most common
```java
String value = cache.get(key);
if (value == null) {
    value = db.query(key);   // cache miss
    cache.set(key, value, TTL);
}
return value;
```
Pros: only caches requested data. Cons: cold start latency; stale data risk.

#### Write-Through
Every DB write also updates cache synchronously.
Pros: cache always fresh. Cons: write latency doubles; caches data never read.

#### Write-Back (Write-Behind)
Write to cache only; async batch flush to DB.
Pros: fast writes. Cons: data loss risk on cache crash.

#### Read-Through
Cache sits in front of DB; app only talks to cache; cache fetches from DB on miss.

### Eviction Policies
| Policy | When to use |
|---|---|
| LRU (Least Recently Used) | General purpose — evict coldest data |
| LFU (Least Frequently Used) | Frequency matters (viral content stays) |
| TTL-based | Time-sensitive data (sessions, OTPs) |
| FIFO | Queue-like access patterns |

### Cache Invalidation Strategies
1. **TTL expiry**: simple but may serve stale data until expiry.
2. **Event-driven**: on DB update, publish event → cache delete.
3. **Cache-aside write**: update DB then `cache.del(key)`.
4. **Versioning**: include version in key (`user:123:v5`) — old cached responses naturally expire.

### Redis Architecture
```
Client → Redis Sentinel (HA) or Redis Cluster
         ├── Primary (writes)
         ├── Replica 1 (reads)
         └── Replica 2 (reads)
```
**Redis Cluster**: 16,384 hash slots distributed across shards.
Key → `CRC16(key) % 16384` → slot → node.

### Thundering Herd / Cache Stampede
When a hot key expires, 10K concurrent requests hit DB simultaneously.
**Fix**: probabilistic early expiration, mutex lock on first miss, background refresh.

```java
// Mutex pattern
String lock = "lock:" + key;
if (cache.setnx(lock, "1", 5)) {        // only one request refreshes
    String val = db.query(key);
    cache.set(key, val, TTL);
    cache.del(lock);
} else {
    Thread.sleep(50);                    // others wait briefly
    return cache.get(key);
}
```

### Key Trade-offs
| Decision | Trade-off |
|---|---|
| Write-through vs cache-aside | Write-through: consistent. Cache-aside: only caches hot data. |
| TTL length | Short: fresher data, more DB hits. Long: stale risk, fewer DB hits. |
| Memory vs persistence | Redis AOF/RDB adds durability but slows writes. |
$$,
 'O(1) read/write', 'O(N) cached items'),

-- 6. Search Autocomplete
('a0000000-0000-0000-0000-000000000006',
 'search-autocomplete',
 'Search Autocomplete (Typeahead)',
 'Design a real-time suggestion system that shows completions as the user types.',
 'typeahead / autocomplete
prefix matching
top-k suggestions
trie data structure
low latency (<100ms)',
 NULL, 106, 'SYSTEM_DESIGN',
$$## Search Autocomplete (Typeahead)

### Core Requirements
**Functional:** as user types, return top 5 suggestions matching the prefix; suggestions ranked by popularity/recency.
**Non-functional:** <100ms response; 5B queries/day (Google scale); freshness within hours for trending terms.

### Data Structure: Trie

```
Insert "apple", "app", "apt":
         root
          |
          a
          |
          p
         / \
        p   t
        |   |
        l   (apt*)
        |
        e
        |
       (apple*)
```

Each node stores the **top-k (5) most popular completions** starting from that prefix, precomputed.

```java
class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    List<String> topK = new ArrayList<>(); // top 5 completions
}
```

**Query**: traverse prefix path O(L), return stored topK → O(1) after traversal.
**Problem**: trie in memory is huge (billions of terms). Solution: serialize to storage + cache hot prefixes.

### Architecture
```
User types "app" →
  Client (debounce 100ms) →
  Autocomplete Service →
    Redis: GET suggestions:app   (cache hit → return immediately)
    Cache miss → Trie service (RPC) →
      pre-built trie sharded by first letter →
      return top 5
    Cache: SET suggestions:app [...] EX 3600

Batch job (every hour):
  Query logs → frequency count → update trie →
  rebuild top-k at each node → push to Redis
```

### Scaling the Trie
- **Shard by first character**: 'a'-'z' → 26 shards (uneven, so shard by hash of first 2 chars).
- **Serialize trie** to Cassandra: `(prefix, suggestions_json)`.
- **Cache hot prefixes** in Redis: top 5000 most-queried prefixes cover 90% of traffic.

### Ranking Signals
- Raw frequency (past 7 days)
- Recency boost (trending last 1h)
- Personalization (user's past queries, location)
- Business rules (promoted terms, block list)

### Key Trade-offs
| Decision | Trade-off |
|---|---|
| Trie vs DB prefix scan | Trie: O(L) fast. DB LIKE 'prefix%' needs index but simpler. |
| Real-time vs batch updates | Real-time: always fresh, complex. Batch (hourly): slight lag, simple. |
| Client debounce | 100ms debounce cuts requests by 5×; too long feels sluggish. |
$$,
 'O(L) prefix match', 'O(N × chars) trie'),

-- 7. Notification System
('a0000000-0000-0000-0000-000000000007',
 'notification-system',
 'Notification System',
 'Design a scalable push/email/SMS notification delivery pipeline.',
 'push notifications (FCM/APNS)
email delivery at scale
SMS gateway
fan-out to millions of users
delivery guarantees / retries',
 NULL, 107, 'SYSTEM_DESIGN',
$$## Notification System

### Core Requirements
**Functional:** send push, email, SMS; support immediate and scheduled delivery; user preferences (opt-out per channel); delivery tracking.
**Non-functional:** 10M notifications/day; <5s delivery for push; at-least-once delivery; deduplication.

### Architecture
```
Event Source (Order placed, Like, Alert)
    │
    ▼
Notification Service  ←──  User Preference Service
    │   (validate, enrich, filter opted-out users)
    ▼
Message Queue (Kafka, partitioned by user_id)
    │
    ├──▶ Push Worker  → FCM (Android) / APNS (iOS)
    ├──▶ Email Worker → SendGrid / SES
    └──▶ SMS Worker   → Twilio / SNS
             │
             ▼
        Delivery Log (Cassandra)
             │
             ▼
        Retry Queue (DLQ after 3 failures)
```

### Notification Record Schema
```sql
CREATE TABLE notifications (
    id           UUID PRIMARY KEY,
    user_id      BIGINT NOT NULL,
    type         VARCHAR(20),    -- PUSH, EMAIL, SMS
    template_id  VARCHAR(50),
    payload      JSONB,
    status       VARCHAR(20) DEFAULT 'PENDING',
    created_at   TIMESTAMPTZ DEFAULT NOW(),
    sent_at      TIMESTAMPTZ,
    error        TEXT
);
```

### Fan-out at Scale
For sending to 1M users (e.g. breaking news):
1. Fetch user IDs in batches of 1000.
2. Produce 1000-message batches to Kafka.
3. Workers consume in parallel (100 workers × 10K msg/sec = 1M/sec throughput).

### Deduplication
```
idempotency_key = hash(user_id + template_id + event_id)
Redis: SET dedup:{key} 1 NX EX 86400
If SET returns nil → duplicate, skip.
```

### Retry Strategy
- Exponential backoff: 1s → 2s → 4s → 8s → DLQ.
- After DLQ, alert ops; do not retry indefinitely.
- Distinguish **transient** (503) vs **permanent** (invalid token) failures.

### Key Trade-offs
| Decision | Trade-off |
|---|---|
| At-least-once vs exactly-once | At-least-once simpler; use dedup to handle duplicate delivery. |
| Single queue vs per-channel | Per-channel: isolated failures, different rate limits per provider. |
| Template rendering | Pre-render on produce (fast delivery) vs render on consume (fresher data). |
$$,
 'O(1) enqueue', 'O(U) pending messages'),

-- 8. Distributed Message Queue
('a0000000-0000-0000-0000-000000000008',
 'message-queue',
 'Message Queue (Kafka / SQS)',
 'Design a durable, scalable message queue for async communication between services.',
 'decouple producers and consumers
guaranteed delivery
ordering guarantees
Kafka vs SQS vs RabbitMQ
consumer groups / competing consumers',
 NULL, 108, 'SYSTEM_DESIGN',
$$## Distributed Message Queue

### Why Message Queues?
Decouple services: producer doesn't wait for consumer. Buffer spikes. Enable async processing, retries, fan-out.

### Core Concepts
| Term | Meaning |
|---|---|
| Topic/Queue | Named channel |
| Producer | Writes messages |
| Consumer | Reads messages |
| Consumer Group | Multiple consumers share a topic's partitions (Kafka) |
| Offset | Position in partition log |
| ACK | Consumer signals successful processing |
| DLQ | Dead Letter Queue — failed messages after N retries |

### Kafka Architecture
```
Topic "orders" with 3 partitions:

Producer → Partition 0 (key hash % 3 = 0)
         → Partition 1
         → Partition 2

Consumer Group A (order-service):
  Consumer A1 reads Partition 0
  Consumer A2 reads Partition 1
  Consumer A3 reads Partition 2

Consumer Group B (analytics-service): reads all partitions independently
```

**Retention**: messages kept for 7 days (regardless of consumption) — can replay.
**Ordering**: guaranteed within a partition, not across partitions.
**Throughput**: millions of messages/sec, sequential disk I/O via append-only log.

### Message Delivery Semantics
| Semantic | How | Risk |
|---|---|---|
| At-most-once | Auto-commit offset before processing | Data loss on crash |
| At-least-once | Commit offset after processing | Duplicate delivery |
| Exactly-once | Transactional producers + idempotent consumers | Complex, slow |

### Kafka vs SQS vs RabbitMQ
| | Kafka | SQS | RabbitMQ |
|---|---|---|---|
| Retention | Days/weeks (log) | 4 days max | Until ACK |
| Ordering | Per partition | FIFO queue option | Per queue |
| Throughput | Millions/sec | ~3000 msg/sec/queue | ~50K msg/sec |
| Replay | Yes | No | No |
| Use case | Event streaming, log | Simple task queue | Complex routing |

### Schema: Message Storage
```sql
-- For a custom queue implementation:
CREATE TABLE messages (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    queue_name   VARCHAR(100) NOT NULL,
    payload      JSONB NOT NULL,
    status       VARCHAR(20) DEFAULT 'PENDING',  -- PENDING/PROCESSING/DONE/FAILED
    attempts     INT DEFAULT 0,
    visible_at   TIMESTAMPTZ DEFAULT NOW(),   -- invisible during processing
    created_at   TIMESTAMPTZ DEFAULT NOW()
);
-- Poll: SELECT ... WHERE status='PENDING' AND visible_at <= NOW() FOR UPDATE SKIP LOCKED
-- ACK: UPDATE ... SET status='DONE'
-- Timeout: UPDATE ... SET status='PENDING', visible_at=NOW()+30s WHERE status='PROCESSING' AND visible_at < NOW()-30s
```

### Key Trade-offs
| Decision | Trade-off |
|---|---|
| Push vs pull | Push (RabbitMQ): low latency. Pull (Kafka/SQS): consumer controls rate. |
| Partition count | More partitions: more parallelism, more open file handles. |
| Replication factor | 3 replicas: durable but 3× storage and write latency. |
$$,
 'O(1) produce/consume', 'O(retention × throughput)')

ON CONFLICT (slug) DO NOTHING;

-- ── System Design Q&A Problems ───────────────────────────────────────────────
-- These are conceptual interview questions (no test cases / code execution)
-- difficulty reflects interview frequency and depth

INSERT INTO problems (id, slug, title, difficulty, description, constraints, examples, active) VALUES

-- URL Shortener questions
('b0000000-0000-0000-0000-000000000001', 'sd-url-shortener-basic',
 'URL Shortener — Core Design',
 'MEDIUM',
 E'## Design a URL Shortener (like bit.ly)\n\n**System Design Interview Question**\n\nDesign a URL shortening service that converts long URLs into short, shareable links.\n\n### Requirements to clarify\n1. How many URLs shortened per day? (assume 100M)\n2. Read:write ratio? (assume 10:1)\n3. Should short URLs be random or sequential?\n4. Do we need analytics (click tracking)?\n5. URL expiration support?\n\n### Answer the following\n\n**Part 1 — Capacity Estimation**  \nCalculate storage needed for 5 years. Calculate read and write QPS.\n\n**Part 2 — API Design**  \nDefine REST endpoints for: shorten, redirect, delete, analytics.\n\n**Part 3 — Short Code Generation**  \nCompare: base62 encoding of auto-increment ID vs MD5 hash vs random string. Which do you choose and why?\n\n**Part 4 — Database Schema**  \nDesign the schema. Choose SQL vs NoSQL — justify.\n\n**Part 5 — Caching Strategy**  \nWhere do you add Redis? What is the cache key? What TTL? What eviction policy?\n\n**Part 6 — Scale**  \nHow do you handle 10× traffic growth? What breaks first?',
 'Design for 100M URL creations per day, 10:1 read:write ratio, 5-year data retention, 99.99% availability',
 '[{"input":"System Design Interview","output":"See lesson for full solution walkthrough","explanation":"Focus on trade-offs: base62 vs hash, SQL vs NoSQL, 302 vs 301 redirect"}]',
 TRUE),

('b0000000-0000-0000-0000-000000000002', 'sd-url-shortener-scale',
 'URL Shortener — Scale to 1 Billion Users',
 'HARD',
 E'## URL Shortener at 1 Billion Users\n\n**Advanced System Design**\n\nYour URL shortener has grown from 100M to 1B daily active users. Identify and solve the bottlenecks.\n\n### Current pain points to address\n\n1. **Single database write bottleneck** — how do you shard the `urls` table?\n2. **Cache stampede** — when a viral URL''s cache entry expires, 10K requests hit DB simultaneously. How do you prevent this?\n3. **Custom aliases** — millions of users want vanity URLs (e.g. `bit.ly/mybrand`). How do you handle conflicts efficiently?\n4. **Global latency** — users in Asia experience 200ms latency to your US servers. How do you reduce to <20ms?\n5. **Analytics at scale** — you want per-click geo/device breakdowns. How do you process 10B click events/day?\n\n### For each problem: diagnose → propose solution → state trade-offs',
 'Scale to 1B DAU; 10B clicks/day; <20ms global redirect latency; real-time analytics',
 '[{"input":"Scaling bottlenecks","output":"Sharding + CDN + Kafka analytics pipeline","explanation":"Each bottleneck has multiple valid solutions — justify your choice"}]',
 TRUE),

-- Rate Limiter questions
('b0000000-0000-0000-0000-000000000003', 'sd-rate-limiter-algorithms',
 'Rate Limiter — Algorithm Comparison',
 'MEDIUM',
 E'## Rate Limiter Algorithms\n\n**System Design Interview Question**\n\nYou are building an API gateway that must limit requests to 100 requests per minute per user.\n\n### Compare the following algorithms\n\n**1. Fixed Window Counter**  \n- Describe the algorithm  \n- What is the "boundary attack" vulnerability?  \n- When would you still choose it?\n\n**2. Sliding Window Log**  \n- How does it eliminate the boundary problem?  \n- What is the memory cost per user?  \n- At what scale does this become impractical?\n\n**3. Token Bucket**  \n- How does it work?  \n- How does it allow bursts while maintaining average rate?  \n- Why is it preferred for most API rate limiting?\n\n**4. Leaky Bucket**  \n- Difference from token bucket?  \n- When does the queue overflow?  \n- Use case: smooth output rate (e.g. video streaming).\n\n### Implementation Challenge\nWrite pseudocode for a Redis-based Token Bucket that is atomic (handles race conditions).',
 '100 req/min per user; distributed across 50 API servers; <1ms overhead',
 '[{"input":"Token Bucket pseudocode","output":"Lua script with HMGET/HMSET for atomic read-modify-write","explanation":"Lua scripts in Redis execute atomically, solving the race condition"}]',
 TRUE),

('b0000000-0000-0000-0000-000000000004', 'sd-rate-limiter-distributed',
 'Rate Limiter — Distributed Implementation',
 'HARD',
 E'## Distributed Rate Limiter\n\n**Advanced System Design**\n\nDesign a rate limiter that works consistently across 100 API gateway servers.\n\n### Problems to solve\n\n**Problem 1 — Race Condition**  \nTwo requests arrive at the same millisecond on different servers. Both read count=99 (limit=100), both increment to 100, both allow. 101 requests served. Fix this.\n\n**Problem 2 — Network Latency to Redis**  \nEvery request hits Redis for a counter check adds ~1ms latency. At 100K req/sec, this is 100K Redis calls/sec. How do you reduce this load while keeping limits reasonably accurate?\n\n**Problem 3 — Redis Failure**  \nYour Redis cluster goes down. Should you: (a) fail open (allow all requests), (b) fail closed (block all requests), or (c) fall back to in-memory per-server limits? Justify.\n\n**Problem 4 — Different Limits per Endpoint**  \nGET /search → 1000/min, POST /tweet → 50/min, DELETE /user → 5/day. Design a configuration system for this.\n\n**Problem 5 — User vs IP Limiting**  \nAuthenticated users get per-user limits; unauthenticated get per-IP limits. How do you handle shared IPs (corporate NAT, VPN)?',
 'Distributed; <0.5ms overhead; accurate to within 1%; handles Redis failure gracefully',
 '[{"input":"Race condition fix","output":"Redis Lua script for atomic check-and-increment","explanation":"Lua scripts are atomic in Redis — no WATCH/MULTI needed"}]',
 TRUE),

-- News Feed questions
('b0000000-0000-0000-0000-000000000005', 'sd-news-feed-basic',
 'News Feed — Design Twitter Timeline',
 'MEDIUM',
 E'## Design Twitter Home Timeline\n\n**System Design Interview Question**\n\nDesign the home timeline feature: when a user opens Twitter, they see recent tweets from people they follow.\n\n### Requirements\n- 300M DAU; average 200 followees per user\n- Timeline loads in <200ms\n- New tweets appear within 5 seconds\n- Infinite scroll (pagination)\n\n### Answer the following\n\n**Part 1 — Data Model**  \nDesign tables for: users, tweets, follows, feed. What ID strategy for tweets? (hint: why not UUID?)\n\n**Part 2 — Fan-out on Write vs Read**  \nExplain both approaches. What happens when @BarackObama (100M followers) tweets?\n\n**Part 3 — Timeline Generation**  \nWalk through the exact steps when user opens the app:\n1. What does the Feed Service query?\n2. Where is the data stored?\n3. How is it paginated?\n\n**Part 4 — Storage Choices**  \nWhy Cassandra for tweets? Why Redis for feed cache? Why MySQL for the follow graph?\n\n**Part 5 — Keeping Feed Fresh**  \nA tweet is deleted. How do you remove it from 50M followers'' cached feeds?',
 '300M DAU; 500M tweets/day; <200ms timeline load; 5s tweet visibility SLA',
 '[{"input":"Fan-out design","output":"Hybrid: write fan-out for normal users, read fan-out for celebrities","explanation":"Celebrity problem: 100M Redis writes per tweet is too slow — merge at read time"}]',
 TRUE),

('b0000000-0000-0000-0000-000000000006', 'sd-news-feed-ranking',
 'News Feed — Ranking and Personalization',
 'HARD',
 E'## News Feed Ranking\n\n**Advanced System Design**\n\nTwitter/Instagram moved from chronological to ranked feeds. Design the ranking system.\n\n### Signals to rank by\n- Recency (when was it posted?)\n- Engagement (likes, retweets, shares so far)\n- Relationship strength (how often do you interact with this person?)\n- Content affinity (you like football, this tweet is about football)\n- Diversity (avoid showing 10 tweets from same person)\n\n### Architecture Questions\n\n**1. Candidate Generation**  \nYou follow 500 people. How do you efficiently get the 500 most recent tweets to score?\n\n**2. Feature Extraction**  \nFor each candidate tweet, you need engagement stats, author metadata, content features. How do you fetch all this efficiently without N+1 queries?\n\n**3. ML Scoring**  \nYou run a gradient boosted tree model to score each candidate. The model takes 10ms per batch of 100. Feed has 500 candidates — is this fast enough for <200ms SLA?\n\n**4. Feedback Loop**  \nHow do you collect implicit feedback (impression without click = negative signal) to retrain the model daily?\n\n**5. A/B Testing**  \nHow do you test if ranked feed improves engagement without affecting everyone?',
 '500 candidates per feed; <200ms ranking latency; ML model retrained daily',
 '[{"input":"Feature extraction N+1 problem","output":"Batch fetch all authors + engagement stats in 2 queries after candidate retrieval","explanation":"Fan-out the N fetches into batch operations — Redis pipeline or IN clause"}]',
 TRUE),

-- Consistent Hashing questions
('b0000000-0000-0000-0000-000000000007', 'sd-consistent-hashing-basic',
 'Consistent Hashing — Core Concept',
 'MEDIUM',
 E'## Consistent Hashing\n\n**System Design Interview Question**\n\n### Part 1 — The Problem\nYou have a distributed cache with 3 servers (A, B, C). You use `hash(key) % 3` to decide which server holds each key.\n\nYou add a 4th server D. How many keys need to move? Show the math.\n\nWhy is this a problem for a production cache?\n\n### Part 2 — The Solution\nExplain consistent hashing using the ring analogy:\n1. How are servers placed on the ring?\n2. How is a key assigned to a server?\n3. When server D is added at position 250, which keys move and where do they go?\n4. When server B fails, which keys move and where do they go?\n\n### Part 3 — Virtual Nodes\nWith 3 physical servers, naive consistent hashing gives poor distribution (one server may get 60% of keys).\n\nHow do virtual nodes fix this? Draw or describe a ring with 3 servers × 3 vnodes each.\n\n### Part 4 — Real Systems\nHow does Apache Cassandra use consistent hashing? What is a "token"? What is a "replication factor"?\n\n### Part 5 — Hot Key Problem\nUser "justinbieber" has 100M requests/sec — all hash to Server A. Consistent hashing doesn''t help. What do you do?',
 'Distributed cache with 10 nodes; minimize key movement on add/remove; even distribution',
 '[{"input":"% 3 → % 4 key movement","output":"(N-1)/N keys must move on average = 75% with 4 servers","explanation":"With consistent hashing only K/N keys move = 25% for 1 new server out of 4"}]',
 TRUE),

-- Distributed Cache questions
('b0000000-0000-0000-0000-000000000008', 'sd-cache-strategies',
 'Caching Strategies — Write Patterns',
 'MEDIUM',
 E'## Caching Write Strategies\n\n**System Design Interview Question**\n\n### Scenario\nYou have a user profile service backed by PostgreSQL with Redis cache. Users update their profile ~5 times per day. Profile is read 1000× per day.\n\n### Compare these strategies\n\n**Cache-Aside (Lazy Loading)**\n- On read: check cache → miss → read DB → populate cache\n- On write: update DB → invalidate (DELETE) cache entry\n- Question: what happens if you update cache instead of deleting it after a write?\n\n**Write-Through**\n- On every write: update DB AND update cache synchronously\n- Question: if 1000 users update their profile but only 100 are ever read, what''s the waste?\n\n**Write-Back (Write-Behind)**\n- On write: update cache only, async flush to DB every 5 seconds\n- Question: the cache server crashes. What data is lost? Is this acceptable for user profiles?\n\n### Deep Dive Questions\n\n1. **Cache invalidation across services**: Service A and Service B both cache user profiles independently. User updates profile. How do both caches stay consistent?\n\n2. **TTL selection**: Should user profiles have TTL of 60 seconds, 1 hour, or 24 hours? What are the trade-offs?\n\n3. **Cache warming**: Your Redis cluster restarts. 100% cache miss → DB overwhelmed. How do you warm the cache without killing the DB?\n\n4. **Thundering herd**: 50,000 users all request the same viral post at 9:00:00 AM, exactly when its TTL expires. How do you prevent 50,000 simultaneous DB queries?',
 'Read:write ratio 200:1; consistency within 5 seconds of write; DB can handle max 5,000 QPS',
 '[{"input":"TTL trade-off","output":"1 hour TTL: miss rate low, stale window acceptable for profiles; use event-driven invalidation for critical fields","explanation":"Short TTL = fresh but more DB hits; long TTL = stale risk; event-driven = best of both"}]',
 TRUE),

-- Autocomplete questions
('b0000000-0000-0000-0000-000000000009', 'sd-autocomplete-design',
 'Search Autocomplete — Trie vs Database',
 'MEDIUM',
 E'## Search Autocomplete System\n\n**System Design Interview Question**\n\nDesign Google Search''s autocomplete — as you type, show top 5 completions.\n\n### Requirements\n- 5 billion searches/day\n- Suggestions must appear within 100ms\n- Suggestions ranked by global search frequency\n- Trending terms update within 1 hour\n\n### Part 1 — Trie Data Structure\nDraw a trie containing: "apple", "app", "apt", "application".\n\nWhere do you store the top-5 completions at each node?\nWhat is the time complexity to retrieve suggestions for prefix "app"?\n\n### Part 2 — Scaling the Trie\nA full trie for all Google queries would require TBs of RAM. How do you:\n1. Persist the trie to disk?\n2. Shard across multiple servers?\n3. Cache only the hot prefixes?\n\n### Part 3 — Keeping Suggestions Fresh\nDescribe the data pipeline:\n1. How do you count query frequency from 5B searches/day?\n2. How often do you rebuild the trie?\n3. How do you push updates to the serving layer without downtime?\n\n### Part 4 — Client Optimizations\nThe user types "algorithm". They''ve made 9 keystrokes. How do you minimize the number of server requests?\n(Hint: debounce, client-side cache, prefix tree in browser)\n\n### Part 5 — Personalization\nUser A always searches for Python tutorials. User B always searches for Python (the snake). Same prefix "python" → different suggestions. How do you handle this?',
 '5B searches/day; <100ms latency; top-5 suggestions; hourly freshness',
 '[{"input":"Trie node structure","output":"Each node stores char + children map + top5 completions list","explanation":"Precompute top-k at each node to avoid traversal at query time"}]',
 TRUE),

-- Notification System questions
('b0000000-0000-0000-0000-000000000010', 'sd-notification-system',
 'Notification System — Multi-Channel Delivery',
 'MEDIUM',
 E'## Notification System Design\n\n**System Design Interview Question**\n\nDesign a notification system for a food delivery app (like Uber Eats). Notifications include: order confirmed, driver assigned, driver nearby, order delivered, promotional offers.\n\n### Requirements\n- Channels: push (iOS/Android), email, SMS\n- 10M notifications/day\n- User can mute push but keep email\n- Delivery guaranteed (if push fails, fallback to SMS)\n- Promotional notifications throttled (max 3/week per user)\n\n### Part 1 — Architecture\nDraw the flow from "Order Placed" event to push notification appearing on the user''s phone. Name each component.\n\n### Part 2 — User Preferences\nHow do you store and enforce:\n- Per-channel opt-out\n- Do Not Disturb hours (10pm–8am)\n- Notification frequency caps\n\n### Part 3 — Reliability\n1. What happens if FCM (Firebase) returns an error?\n2. How many retries? With what backoff?\n3. When do you give up and mark the notification as failed?\n4. How do you avoid sending a notification twice (duplicate on retry)?\n\n### Part 4 — Fan-out at Scale\nYou need to send a promotional notification to all 5M users simultaneously. Walk through the implementation. How long does it take? What can go wrong?\n\n### Part 5 — Analytics\nProduct team wants to know: open rate, click-through rate, unsubscribe rate per notification type. What data do you collect and how?',
 '10M/day; <5s push delivery; at-least-once; user preferences enforced; fallback channels',
 '[{"input":"FCM failure handling","output":"Exponential backoff 3 retries → fallback to SMS → DLQ → alert ops","explanation":"Distinguish transient (503) vs permanent (invalid token) failures — only retry transient"}]',
 TRUE),

-- Message Queue questions
('b0000000-0000-0000-0000-000000000011', 'sd-kafka-vs-sqs',
 'Message Queue — Kafka vs SQS vs RabbitMQ',
 'MEDIUM',
 E'## Message Queue Comparison\n\n**System Design Interview Question**\n\nYour team needs to choose a message queue. Compare Kafka, SQS, and RabbitMQ for three different use cases.\n\n### Use Case 1: Order Processing\nE-commerce: "Order Placed" events must be processed by inventory, payment, email, and analytics services. Orders must be processed exactly once. Order may not be processed out of sequence.\n\n**Which queue? Why? What ordering guarantee does it provide?**\n\n### Use Case 2: Log Aggregation\nYou have 500 microservices each emitting 10K log events/sec = 5M events/sec total. Logs are stored for 30 days and queried for debugging. Teams frequently replay logs from the past 24 hours to reprocess a bad deployment.\n\n**Which queue? Why? How does replay work?**\n\n### Use Case 3: Task Queue (Background Jobs)\nA video platform: when user uploads a video, it needs transcoding (1 job), thumbnail generation (1 job), and virus scan (1 job). Each job is independent, can be retried. Jobs should be processed FIFO.\n\n**Which queue? Why? How do you handle job failure?**\n\n### Part 4 — Deep Dive: Kafka Partitions\nYou have a Kafka topic with 3 partitions and 6 consumers in one consumer group.\n- How many consumers are active?\n- What happens to the idle consumers if one active consumer crashes?\n- How do you ensure all orders from user X are processed in order?',
 'Compare for: exactly-once ordering, replay capability, simple task queues',
 '[{"input":"6 consumers, 3 partitions","output":"3 consumers active (one per partition), 3 idle; on crash Kafka rebalances partition to idle consumer","explanation":"Kafka: max parallelism = partition count; extra consumers are hot standbys"}]',
 TRUE),

('b0000000-0000-0000-0000-000000000012', 'sd-design-pastebin',
 'Design Pastebin',
 'EASY',
 E'## Design Pastebin\n\n**Entry-Level System Design**\n\nDesign a service where users can store and share plain text snippets (like pastebin.com, GitHub Gist).\n\n### Requirements\n- Upload text snippet (up to 10MB) → get shareable URL\n- Anyone with URL can view the paste\n- Optional: expiration date, syntax highlighting language\n- 10M pastes/day; 100M reads/day\n\n### Part 1 — API Design\nDefine endpoints for: create paste, get paste, list user pastes, delete paste.\n\n### Part 2 — Storage Strategy\nTwo fields need storage: metadata (title, language, expiry, user_id) and content (the actual text).\n\nShould you store the content in the database or in object storage (S3)? Justify.\n\n### Part 3 — Key Generation\nHow do you generate the paste key (e.g. `https://paste.io/xK2pQr`)?\nRequirements: 6 characters, URL-safe, no collisions.\n\n### Part 4 — Caching\nWhat percentage of pastes account for most reads? What do you cache and where?\n\n### Part 5 — Scale\nAfter 2 years you have 7 billion pastes. SQL queries slow down. What do you change?',
 '10M pastes/day; 100M reads/day; paste size up to 10MB; 99.9% availability',
 '[{"input":"Storage: DB vs S3","output":"Metadata in DB (queryable); content in S3 (cheap, scalable for large blobs)","explanation":"Storing 10MB blobs in DB rows kills index performance — S3 for blob, DB for metadata"}]',
 TRUE),

('b0000000-0000-0000-0000-000000000013', 'sd-design-youtube',
 'Design YouTube (Video Streaming)',
 'HARD',
 E'## Design YouTube\n\n**Advanced System Design**\n\nDesign a video streaming platform supporting upload, transcoding, and streaming at YouTube scale.\n\n### Scale\n- 500 hours of video uploaded per minute\n- 1 billion hours watched per day\n- Videos must be available in multiple resolutions: 360p, 720p, 1080p, 4K\n- <500ms to start playback after click\n\n### Part 1 — Video Upload Pipeline\n1. User uploads raw video file (2GB). How does the upload work without timing out?\n2. Once uploaded, how is transcoding triggered?\n3. How long does transcoding take? How do you parallelize it across 4 resolutions?\n4. When does the video become "available to watch"?\n\n### Part 2 — Video Storage\nYou store 720p + 1080p for every video. At 500 hours/min × 1GB/hour = 500GB/min = 700TB/day.\nHow do you store this? What redundancy strategy?\n\n### Part 3 — Video Streaming\nExplain how HTTP Adaptive Bitrate Streaming (HLS/DASH) works:\n- What are `.m3u8` playlist files?\n- How does the player switch from 360p to 1080p seamlessly?\n- Where does CDN fit into this?\n\n### Part 4 — Recommendation System\nWhen you finish watching a video, YouTube shows 20 related videos.\nDescribe the two-stage pipeline: candidate generation → ranking.\n\n### Part 5 — Search\nUser searches "how to make sourdough bread." There are 500,000 matching videos.\nHow do you index and retrieve relevant results?',
 '500hrs upload/min; 1B hours watched/day; <500ms playback start; multi-resolution support',
 '[{"input":"Transcoding pipeline","output":"Upload to S3 → trigger Kafka event → transcoding workers process each resolution in parallel → write to S3 → update DB status → CDN invalidation","explanation":"Parallel transcoding: 4 resolutions × 2min video = 4 concurrent jobs, not sequential"}]',
 TRUE),

('b0000000-0000-0000-0000-000000000014', 'sd-design-uber',
 'Design Uber (Ride Sharing)',
 'HARD',
 E'## Design Uber\n\n**Advanced System Design**\n\nDesign a ride-sharing platform: passengers request rides, nearby drivers are matched, location is tracked in real time.\n\n### Core Features\n- Passenger requests ride from location A to B\n- System finds nearest available driver\n- Driver accepts → real-time location tracking\n- Fare calculation\n- Rating system\n\n### Part 1 — Location Data\nDrivers send their GPS location every 4 seconds. With 1M active drivers, that is 250K location updates/sec.\n\n1. Where do you store current driver locations? (hint: think spatial data)\n2. How do you query "find all drivers within 5km of passenger"?\n3. How does a geohash work? What geohash precision for 5km radius?\n\n### Part 2 — Matching Algorithm\n1. When a passenger requests a ride, how do you find the top 10 nearest available drivers?\n2. How do you send the request to those drivers and handle the case where the first driver declines?\n3. What prevents two passengers from matching with the same driver simultaneously?\n\n### Part 3 — Real-time Tracking\nOnce matched, the passenger''s app shows the driver''s location updating every 4 seconds.\n1. REST polling vs WebSocket vs Server-Sent Events — which is best here?\n2. How many concurrent WebSocket connections can one server handle?\n3. How do you scale the real-time tracking to 10M concurrent rides?\n\n### Part 4 — Surge Pricing\nIn a thunderstorm, demand doubles but driver supply stays constant.\n1. How do you detect a supply/demand imbalance per geographic cell?\n2. How do you calculate and apply a surge multiplier?\n3. How do you communicate surge pricing to users before they book?',
 '1M active drivers; 10M concurrent rides; location updates every 4s; <2min match time',
 '[{"input":"Nearest driver query","output":"Geohash (precision 6 = ~1km cells) stored in Redis GEOADD; GEORADIUS query for nearby drivers","explanation":"Redis GEO commands: O(N+log M) for radius search; fallback to expanding geohash ring if no drivers"}]',
 TRUE),

('b0000000-0000-0000-0000-000000000015', 'sd-design-whatsapp',
 'Design WhatsApp (Chat System)',
 'HARD',
 E'## Design WhatsApp\n\n**Advanced System Design**\n\nDesign a real-time chat system supporting 1-on-1 messaging and group chats.\n\n### Scale\n- 2B users; 100B messages/day\n- End-to-end delivery guarantee\n- Message ordering preserved\n- Online/offline status\n- Message seen receipts\n\n### Part 1 — Message Delivery\n1. User A sends message to User B. User B is offline. Describe end-to-end flow.\n2. Once User B comes online, how are undelivered messages retrieved?\n3. How does the "double check" (delivered) and "blue tick" (read) work technically?\n\n### Part 2 — Connection Management\n1. Each connected user holds a WebSocket connection. 2B users, even 10% online = 200M WebSocket connections. How do you scale WebSocket servers?\n2. How does a message from User A (connected to server S1) reach User B (connected to server S3)?\n3. What protocol does WhatsApp actually use? (hint: XMPP → custom protocol)\n\n### Part 3 — Message Storage\n1. Should you store messages on the server long-term or only until delivery?\n2. Design the message schema. How do you handle 100B messages/day storage cost?\n3. How does media (photos/videos) storage differ from text messages?\n\n### Part 4 — Group Chats\n1. A group has 256 members. When someone sends a message, how many "deliver to user" operations happen?\n2. For a group with 1000 members (business group chat), fan-out on write becomes expensive. Alternative?\n3. How does WhatsApp ensure message ordering in groups when members are on different servers?\n\n### Part 5 — End-to-End Encryption\nDescribe the Signal Protocol key exchange at a high level. Why can WhatsApp not read your messages even if they wanted to?',
 '2B users; 100B messages/day; E2E encryption; <1s delivery; guaranteed delivery',
 '[{"input":"WebSocket routing S1→S3","output":"Message → Kafka → Message Router consumes → look up connection registry (Redis: userId→serverId) → forward to S3 via internal gRPC","explanation":"Connection registry maps userId to WebSocket server; pub/sub between servers via Kafka"}]',
 TRUE)

ON CONFLICT (slug) DO NOTHING;

-- ── Link SD problems to their parent pattern ─────────────────────────────────
INSERT INTO problem_patterns (problem_id, pattern_id) VALUES
('b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001'), -- url shortener basic
('b0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000001'), -- url shortener scale
('b0000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000002'), -- rate limiter algorithms
('b0000000-0000-0000-0000-000000000004', 'a0000000-0000-0000-0000-000000000002'), -- rate limiter distributed
('b0000000-0000-0000-0000-000000000005', 'a0000000-0000-0000-0000-000000000003'), -- news feed basic
('b0000000-0000-0000-0000-000000000006', 'a0000000-0000-0000-0000-000000000003'), -- news feed ranking
('b0000000-0000-0000-0000-000000000007', 'a0000000-0000-0000-0000-000000000004'), -- consistent hashing
('b0000000-0000-0000-0000-000000000008', 'a0000000-0000-0000-0000-000000000005'), -- cache strategies
('b0000000-0000-0000-0000-000000000009', 'a0000000-0000-0000-0000-000000000006'), -- autocomplete
('b0000000-0000-0000-0000-000000000010', 'a0000000-0000-0000-0000-000000000007'), -- notification system
('b0000000-0000-0000-0000-000000000011', 'a0000000-0000-0000-0000-000000000008'), -- kafka vs sqs
('b0000000-0000-0000-0000-000000000012', 'a0000000-0000-0000-0000-000000000001'), -- pastebin -> url shortener (similar concepts)
('b0000000-0000-0000-0000-000000000013', 'a0000000-0000-0000-0000-000000000005'), -- youtube -> distributed cache
('b0000000-0000-0000-0000-000000000014', 'a0000000-0000-0000-0000-000000000004'), -- uber -> consistent hashing
('b0000000-0000-0000-0000-000000000015', 'a0000000-0000-0000-0000-000000000008')  -- whatsapp -> message queue
ON CONFLICT DO NOTHING;
