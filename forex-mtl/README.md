## Running the application

1. Run the OneFrame API image on `port 8000` using `docker run -p 8000:8080 paidyinc/one-frame`
2. Run the Scala application defined in `src/main/scala/forex/Main.scala`

## Problem Overview

### User Story
1. As a Paidy engineer, I want to be able to easily get foreign exchange rates for the services I am developing.

### Constraints
1. The rate should not be older than 5 minutes.
2. The One-Frame service supports a maximum of 1000 requests per day for any given authentication token.
3. The service should support at least 10,000 successful requests per day with 1 API token.

## Proposed Solution
Because the number of the requests the Forex service must support is greater than the maximum daily calls allowed by the One-Frame API, we cannot simply forward every request from the service to the One-Frame API.

Instead, we can do the following:

1. Send a request to the One-Frame API with all pair combinations as query parameters.
2. Store the rates from the One-Frame API in a cache and fulfill Forex requests by querying this cache. 
3. Whenever the results go stale, get the latest rates from the One-Frame API and update the cache (i.e. repeat step 1)

## API

Endpoint: `GET /rates`
### Parameters
* **`from`**: `string` Starting currency
* **`to`**: `string` Ending currency

Example request: `GET /rates?from=USD&to=JPY`

### Responses
Success
```
{
  "from":"USD",
  "to":"JPY",
  "price":0.39625560098102343,
  "timestamp":"2024-03-08T23:44:04.16Z"
}
```

### Error
Example
```
{
   "error":"invalid_rate",
   "message":"JPYd is not a valid currency."
}
```

#### error
* `invalid_rate`: query parameter contains an unsupported rate.
* `interpreter_error`: server error (likely due to a bug)

#### message
Contains details regarding the error.

### Http Status Summary
* `200` OK
* `400` The request was unacceptable, often due to missing a required parameter
* `500` Something went wrong with the interpreter (usually a bug)

### Minimizing rounding errors
To minimize rounding errors that occurs from taking inverses of currency exchange rates (i.e. if 1 USD = 150.3 yen, the inverse is 1 yen = 1/150.3), we want to include all **permutations** of rate pairs in the query parameters. In other words:
```
/rates?pair=USDJPY&pair=JPYUSD
```

### Why cache and why Caffiene?
The reasons to use a cache are the following:
* **Extremely fast reads.** If a cache is well designed and has a high hit rate, it can serve a request in milliseconds, making it able to handle a large volume of requests easily.
* **Easy to expire data.** Since we want to serve the latest rates, removing outdated rates keeps
* **Not relational data.** The rates don't to have relational integrity with other entities, so a relational database isn't required.

As for why Caffiene was chosen, it was mostly due to how easy it is to develop locally with it. In addition, the amount of data we need to store is very low (72 pairs of rates) so an in-memory cache is fine.

### Calculating cache expiration time
The **maximum number of requests** we can send to the One-Frame API is as follows:
```
1,000 requests per 24 hours
41.6 requests per hour
3.47 requests per 5 minutes
1 request every 1.44 minutes
```

As long as there is **between 1.44 minutes and 5 minutes** between every One-Frame API request, we will not hit the daily limit of 1,000 requests. In other words, we can return rates than are at least 1.44 minutes and 5 minutes old. 

Now, we can theoretically set the cache expiration time to 5 minutes. However, due to network latency, the returned rate might be a few seconds old by the time the response is received.

Therefore, we will use **3 minutes** as the cache expiration.

### Edge cases

#### Rate is not supported by OneFrame API
If a currency in `forex.domain.Currency` is not supported by the One Frame API, then unfortunately the request will fail. However, third party API changes should be prepared for well in advance. If this occurs, I have set up logging so that this issue is easy to detect.

#### Number of supported currencies increases
Currently, there are 9 currencies supported, which means 72 pairs of currencies are sent to the One Frame API. If this increases, then the number of pairs will increase exponentially and the URL may become too long to handle in single request.

If this requirement change happens, then we will have to*
* Get all rates from the OneFrame API in multiple requests (as opposed to a single request)
* In order to stay within 1000 requests, we may need to possibly increase the SLA from 5 minutes since the number of requests to the OneFrame API will increase

## Limitations 
### Caffiene is in-memory and cannot be used by multiple instances
If more than one instance of this service is spun up, then each instance will have its own separate cache. Because the caches do not share data, they each need to fetch data using the OneFrame API client once its data expires. This will increase the number of API requests.

To prevent both issues, a centralized cache can be used (like Redis) by all instances.

### Historical rates cannot be queried
Because rates are stored in a cache and expire after 3 minutes, old rates cannot be queried. If this becomes a requirement in the future, then we can add this feature and save the rates to a database in addition to storing the data to a cache.

## Improvements & final thoughts
* Centralize service instansiations in one place using dependency injection.
* Refactor deeply nested `match` blocks that handle monads (as this is my first time using Scala, the code I have written is not the most idiomatic)

