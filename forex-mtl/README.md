# Forex

Forex is a simple application that acts as a **local proxy** for getting exchange rates.
It's a service that can be consumed by other internal services to get the exchange rate between a set of currencies,
so they don't have to care about the specifics of third-party providers.

## API
The Forex service provides a single endpoint:

**GET** `<host>:<port>/rates?from=<currency_id>&to=<currency_id>`

- `<host>` and `<port>`: The Forex service bind host and port, configured in `app.http.host` and `app.http.port` parameters.
- `<currency_id>`: One of the currently supported currency IDs: `AUD`, `CAD`, `CHF`, `EUR`, `GBP`, `NZD`, `JPY`, `SGD`, `USD`
###### Errors
* ```BadRequest(400)``` – incorrect request parameters.
* ```NotFound(404)``` – incorrect URL path or no data for requested currency pair.
* ```ServiceUnavailable(503)``` – processing took more time than configured in ```app.http.timeout``` parameter
  or One-Frame call failed.
* ```InternalServerError(500)``` – fatal error.

###### Example of request
```curl '127.0.0.1:8080/rates?from=AUD&to=CAD'```

###### Example of response

```
{
    "from": "AUD",
    "to": "CAD",
    "price": 0.4643238185719433735,
    "timestamp": "2023-10-07T10:25:25.576Z"
}
```




## Requirement

We need to implement the following use case:
- [x] The service returns an exchange rate when provided with 2 supported currencies
- [ ] The rate should not be older than 5 minutes
- [ ] The service should support at least 10,000 successful requests per day with 1 API token


Please note the following drawback of the **One-Frame service**:
- The One-Frame service supports a maximum of 1000 requests per day for any given authentication token.


## Assumption
1. If we know `USD-JPY`, doesn't mean we can derive `JPY-USD = 1 / UDS-JPY`
2. If we know `USD-JPY` and `JPY-SGD`, doesn't mean we can derive `USD-SGD = USD-JPY * JPY-USD`

## Tackling the Problem

### Architecture

![Alt text](doc/architecture-0-basic.excalidraw)


### Counting the Worst Scenario
So, we have these requirements:
- The service should support at least 10,000 successful requests per day with 1 API token (that can only serve 1000 request)
  - Forex should serve 10,000 requests a day
  - One Frame can only serve 1,000 request a day
- The rate should not be older than 5 minutes

At worst, if all of our request are **Cache Miss**:
- Given 9 currency, there are `9P2 = 9*8 = 72` pair.
- Every 5 minute, there are 72 pair of request.
- In 24 hour or 1440 minutes, there are 1440/5 = 288 intervals of 5 minutes.
- In total, we need to hit One Frame `72 * 288 = 20,736 request`

If we lift the first assumption:
- If we cache `USDJPY`, we can calculate `JPYUSD = 1/USDJPY`
- There's only `72/2=36` pairs
- In total, we need to hit One Frame `36 * 288 = 10,368 request`
still not work

We need to lift the 2nd assumption:
- Say we need to get `JPYSGD`
- We need to check in cache:
  - does `JPYSGD` exists
  - if not, does `a=JPY???` and `b=???SGD`, return `a*b`
  - if not, does `a=???JPY` and `b=SGD???`, return `1/a * 1/b`
- In total, how many possible request a day? TODO

## Test Design


## HOWTO

### How to Use

1. Run `One Frame API`
2. Run Forex service
3. Hit: http://localhost:9090/rates?from=USD&to=EUR

### Testing

```bash
 docker run -p 8080:8080 paidyinc/one-frame
```