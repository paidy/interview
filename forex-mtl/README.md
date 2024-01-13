# A local proxy for Forex rates

This project is an implementation of the test task from Paidy - https://github.com/paidy/interview/blob/master/Forex.md

## Assumptions and limitations

1. Limit for calling this proxy is 10000 requests per day per token
2. Limit for calling external service is 1000 requests per day
3. Rate returned by the proxy shouldn't be older than 5 minutes
4. Proxy should be able to serve at least 10000 requests per day
5. It is possible that external service could fail or time out
6. Rates are not invertible, i.e. AUD-SGD != 1 / SGD-AUD

## Implementation

We have 4 major components in the project:

- `OneFrameService` which is a client for the external service for getting live rates.
- `CurrencyRateCacheAlgebra` which is a cache injected into OneFrameService for storing cached rates, this can be
  implemented in different ways, for example using Redis or in-memory cache. We choose **Redis**.
- `TokenProvider` which is injected into OneFrameService for providing tokens for the external service. This can be
  implemented in different ways, for example using Redis or config or SQL. We choose **Redis**.
- `RateLimitterAlgebra` which wraps the Http Calls to the Proxy and checks if the token is valid and if the rate limit
  is not exceeded. This can be implemented in different ways, for example using Redis or config or SQL. We choose
  **Redis**.

### Redis as design choice

I choose Redis for 3 purposes:

#### Cache, for caching of the rates for 5 minutes

Pros

1. This cache will be distributed and scalable (if we need to scale the Proxy horizontally)
2. It will be fast, since it's in-memory
3. It will be persistent, so we can recover from failures

Cons

1. We need to maintain another service instead of caching in our server.
2. If we go live we would need to run Redis in cluster mode, otherwise, we will have a single point of failure.

#### Rate limiter, for storing our proxy token and counting requests per token

Pros

1. If we have multiple instances of the Proxy, we need to have a shared storage for the tokens and their limits

Cons

1. If we do not have redis cluster, we will have a single point of failure.
2. If network latency is high, we will have a high latency for the requests to the Proxy.
3. We might need to use distributed locks for the rate limiter if we need high accuracy, which will add complexity to
   the system.

#### Token provider , for providing tokens for the external OneFrame service.

Pros : Same as for the rate limiter

Cons : Same as for the rate limiter. Additionally, if redis is down we will not be able to get tokens for the external
service

## Build and run

### docker-compose

Execute `docker-compose up --build` , this will build the image for the Proxy and pull the OneFrame image. After this,
it will start three containers in the same network:

- `forex-service` on **port 9000**
- `redis` on **port 6379**
- `one-frame` on **port 8080**

### Accessing API

## API Documentation

### Endpoint: Exchange Rate Lookup

Retrieves the exchange rate between two specified currencies.

**URL:** `http://localhost:9090/rates`

**Method:** `GET`

#### URL Parameters

| Parameter | Type   | Description              | Required |
|-----------|--------|--------------------------|----------|
| `from`    | string | The base currency code   | Yes      |
| `to`      | string | The target currency code | Yes      |

#### Supported Currencies

- AUD (Australian Dollar)
- CAD (Canadian Dollar)
- CHF (Swiss Franc)
- EUR (Euro)
- GBP (British Pound)
- NZD (New Zealand Dollar)
- JPY (Japanese Yen)
- SGD (Singapore Dollar)
- USD (United States Dollar)

#### Success Response

**Code:** `200 OK`

**Content example:**

```json
{
  "from": "JPY",
  "to": "EUR",
  "price": 0.71810472617368925,
  "timestamp": "2024-01-13T05:39:19.919Z"
}
```

```shell
curl 'http://127.0.0.1:9000/rates?from=USD&to=SGD'
```

#### Error Responses

**Condition:** If token is invalid or missing.

**Code:** `403 Forbidden`

**Content:**

```
Invalid Token
```

**Condition:** If rate limit is exceeded.

**Code:** `429 Too Many Requests`

**Content:**

``` 
Token 123 exhausted the limit 1
```

**Condition:** If parameters are invalid or missing.

**Code:** `400 Bad Request`

**Content:**

```
Invalid currency
```

**Condition:** If the external service is unavailable or redis is down

**Code:** `503 Service Unavailable`

**Content:**

```
Service Unavailable
```

## Possible improvements

- OneFrameService could be implemented as facade for multiple external services, for example, we could have multiple
  external services for different currencies, we could aggregate them in OneFrameService
- We could implement a local cache for the rates just as a fallback in case Redis is down
- If we run multiple instances of the Proxy, the rate limiter will not work correctly, we need to use distributed locks
  for the rate limiter if we need high accuracy, which will add complexity to the system.
- More comprehensive tests to cover all scenarios
- Metrics for checking response times and error rates as we are using Redis and external service
