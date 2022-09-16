# Project Design

Given the following constraints

1. OneFrame maximum rate of 1000 queries per day
2. OneFrame api support for multiple currency pairs per request
3. Maximum data staleness of 5 minutes

it became clear that to support 10,000 reqs per day, caching would need to be used. The first step in doing so is
determining the minimum number of daily calls to the service:

(1440 minutes / day) * (1 / 5 minute cache time) = 288 calls per day

Thus, the service can handle OneFrame's limits by sending every currency pair to OneFrame when requesting new data and
storing that data for 5 minutes. It's even possible to split the currency pairs in half if the API becomes more
restrictive.

To ensure that concurrent requests don't all request data when the cache is stale, it is necessary to gate the fetch
behavior using a Semaphore and the double-checked locking pattern. 

# Running the Service

sbt compile
sbt run

# Running the Tests

I wasn't able to figure out why sbt test doesn't pick up my test; I had to run them from within IntelliJ. Big sad

# Further Improvements

Due to the time-consuming nature of learning a new tech stack, I was unable to implement the following features that
are required for robustness

* Retries
* External caching (likely in Redis)
* Logging
* Metrics
* Tracing
* More extensive testing suite
* Use config files instead of hardcoded literals