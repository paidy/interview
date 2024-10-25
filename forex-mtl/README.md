# Forex

A micro-service which proxies requests to a rate service.

## API

`/rates?from=currency&to=currency` - returns rate for provided currencies

Available currencies:

- AUD
- CAD
- CHF
- EUR
- GBP
- NZD
- JPY
- SGD
- USD

## Internals

Internally the service uses in-memory cache with `EXPIRE_PERIOD` ttl. When request comes, it tries to get data from the cache.
If it doesn't have a rate for provided currencies, the service sends requests to the rate API and obtains rates for all 
possible pairs of supported currencies and put them into the cache.

## Running

### Prerequisites

You should have installed and running docker

### Starting the app

1. Run image [one-frame](https://hub.docker.com/r/paidyinc/one-frame) by a command:
    ```shell
    docker run -p 8087:8080 paidyinc/one-frame
    ```
2. Run the app by a command:
    ```shell
    sbt run
    ```
    
    If you want to set env variables you should use next command(example):
    ```shell
    API_URI=http://localhost:9090 sbt run
    ```
    
    Available ENV variables:
    * API_URI - url to API of rate service. Format is: `http://host:port`
    * API_TOKEN - auth token of the rate service API. It is passed in a header `token`
    * EXPIRE_PERIOD - cache entry ttl. Format is: `length units`, e.g. `5 minutes`. PLease note this value is being 
validated in order to prevent exceeding API requests limit. If you enter too small number, for example, `1 minute` then the service won't start.
It happens because API requests limit is 1000 requests per day and validation looks like: `24 * 60 / EXPIRE_PERIOD (in minutes) < 1000`

## Testing

### Prerequisites

You should have installed and running docker

- simple tests running
    ```shell
    sbt test
    ```
- running tests with coverage
    ```shell
    sbt coverage test coverageReport coverageAggregate
    ```
