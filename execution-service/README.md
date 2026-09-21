# Execution Service

Sandboxed Java code runner. Receives code + test inputs from the backend, compiles and executes in an isolated container, returns results.

## API Contract

### POST /execute

Request:
```json
{
  "code": "public class Solution { ... }",
  "language": "JAVA",
  "testCases": [
    { "id": "tc1", "input": "5\n1 2 3 4 5", "expectedOutput": "15" }
  ],
  "timeLimitMs": 5000,
  "memoryLimitMb": 128
}
```

Response:
```json
{
  "status": "ACCEPTED",
  "runtimeMs": 42,
  "memoryKb": 4096,
  "testResults": [
    {
      "testCaseId": "tc1",
      "passed": true,
      "actualOutput": "15",
      "executionTimeMs": 42
    }
  ]
}
```

## Security

- Runs submitted code as a non-root `sandbox` user
- CPU time limit enforced via `timeout` command
- Memory limit enforced via Docker `--memory` flag
- No network access inside the execution container
- Filesystem is read-only except `/tmp`

## Implementation (Phase 1 Week 3)

Replace `runner.sh` stub with a Spring Boot service that:
1. Receives the execute request
2. Writes code to a temp file
3. Compiles with `javac`
4. Runs with `java -cp /tmp/sandbox Solution` under `timeout`
5. Captures stdout/stderr
6. Returns structured result
