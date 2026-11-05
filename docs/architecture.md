# Architecture and implementation notes

## Data ownership
Each business service owns its database. Customer, Account, Loan, Identity and Audit services do not share tables or foreign keys across databases. IDs are references across service boundaries.

## Account transfer locking
The Account Service starts a single PostgreSQL transaction, locks the two account rows in ascending primary-key order (`PESSIMISTIC_WRITE`), checks status and available funds, updates both balances, writes two ledger entries and stores the idempotency response. A failure rolls the entire database transaction back. The stable lock order reduces deadlock risk.

## Optimistic locking
The Account entity also has a JPA `@Version` field. The explicit row lock is the primary serialization strategy for the transfer operation; the version field detects stale writes in other update paths.

## Distributed lock
Loan disbursement obtains a Redisson lock with a 30-second lease. It releases the lock in `finally`, and Redis automatically expires the lease after a process crash. The loan service currently marks the loan as disbursed but does not credit the account service. That cross-service operation must be implemented as a Saga before calling it a completed banking disbursement.

## Idempotency
Transfer requests require an `Idempotency-Key`. The service stores the request hash and response in the same database transaction as the ledger updates. A retry with the same key and same request returns the stored result; same key with a different request returns HTTP 409.

## Known POC limitations
- JWT is issued by Identity Service, but the Gateway and downstream services do not yet validate it. Add a shared JWT resource-server configuration and role authorization before exposing any endpoint.
- Loan disbursement does not yet credit Account Service; implement a Saga with idempotent commands/events.
- Kafka publication from Account Service is best-effort and is not a transactional outbox. Add outbox + retry/DLQ for reliable audit delivery.
- Audit listener uses a payload-derived de-duplication key as a demonstration; production should use event UUIDs.
- Loan approval/disbursement roles, customer ownership checks, MFA, rate limits, data encryption, statements/export, daily transfer limits and minimum balance policies require further implementation.
