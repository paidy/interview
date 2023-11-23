# Forex Proxy Service

## Overview

The Forex Proxy Service acts as a local caching proxy server for foreign exchange (Forex) rates. It allows internal services to obtain exchange rates for supported currency pairs without having to interact directly with external Forex rate providers.

## Features

- **Exchange Rate Retrieval**: Provides exchange rates for requested currency pairs.
- **Cache System**: Caches rates to avoid frequent external API calls, ensuring that rates are no older than 5 minutes.
- **Rate Limit Handling**: Supports at least 10,000 successful requests per day with a single API token, using rate throttling and concurrency control.
- **Concurrency Control**: Handles concurrent requests efficiently to ensure the external API's rate limit is not exceeded.

## Local Setup

### Requirements

- Docker
- Scala Build Tool (sbt)

### Starting the Service Locally

1. **Clone the repository**:
   ```
   git clone https://https://github.com/r3dr4bb1t/paid.git
   cd paid/forex-mtl
   ```

2. **Run the external Forex rate provider simulation**:
   ```
   docker pull paidyinc/one-frame
   docker run -p 8080:8080 paidyinc/one-frame
   ```
   This simulates the One-Frame service running on `localhost:8080`.

3. **Start the Forex Proxy Service**:
   ```
   sbt run
   ```
   This starts the local proxy service, which by default listens on `http://localhost:8080`.

### API Usage

To retrieve exchange rates, make a GET request to the `/rates` endpoint with the desired currency pairs:

```
GET http://localhost:8080/rates?from=USD&to=EUR
```

The service will return the exchange rates for the specified currency pairs, provided they are supported and the request does not exceed rate limits.

## Implementation Details

- **Caching**: Implemented using a `TrieMap` with entries that include a timestamp to check for data freshness.
- **Concurrency Control**: Managed through careful request handling and caching logic to avoid simultaneous updates.
- **Rate Limiting**: Monitored to ensure the service does not exceed the allowed number of API calls per day.
- **Reliability**: Retries with exponential backoff are implemented to handle transient network errors.

## Testing

To run tests, execute the following command:

```
sbt test
```

This will run all the unit tests for the service, including tests for rate retrieval, caching behavior, and error handling.

---

This `README` provides a high-level outline for getting started with the Forex Proxy Service. Adjust the commands and descriptions as necessary to fit your actual repository URL, local setup steps, and any additional features or endpoints your service provides.

## Review & ToDo

* I tried to handle the query param but it syntax was too difficult for me, I changed to use List[pair] although.
* Spent nearly 7 hours
* If I had chance or better understanding, I would've handled the request with cache like below
 to ensure it's within 5 mins and enhance performance 
```scala
import java.time.Instant
import scala.collection.concurrent.TrieMap

class RatesCache[F[_]: Sync] {
  private val cache = new TrieMap[Rate.Pair, (Rate, Instant)]()

  def getRate(pair: Rate.Pair): F[Option[Rate]] = Sync[F].delay {
    cache.get(pair).filter { case (_, timestamp) =>
      Duration.between(timestamp, Instant.now()).toMinutes < 5
    }.map(_._1)
  }

  def updateRate(pair: Rate.Pair, rate: Rate): F[Unit] = Sync[F].delay {
    cache.put(pair, (rate, Instant.now()))
  }
}
```
and call this in get() of service.

* I would've added many more tests, and handle errors. Couldn't understand how concurrent work in a short time
* I found error handling is keep giving me syntax error.
