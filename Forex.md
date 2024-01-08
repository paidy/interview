<img src="/paidy.png?raw=true" width=300 style="background-color:white;">

# Paidy Take-Home Coding Exercises

## What to expect?
We understand that your time is valuable, and in anyone's busy schedule solving these exercises may constitute a fairly substantial chunk of time, so we really appreciate any effort you put in to helping us build a solid team.

## What we are looking for?
**Keep it simple**. Read the requirements and restrictions carefully and focus on solving the problem.

**Treat it like production code**. That is, develop your software in the same way that you would for any code that is intended to be deployed to production. These may be toy exercises, but we really would like to get an idea of how you build code on a day-to-day basis.

## How to submit?
[Please click here for our submission guide](./README.md#how-to-submit)

# A local proxy for Forex rates

Build a local proxy for getting Currency Exchange Rates

## Requirements

[Forex](forex-mtl) is a simple application that acts as a local proxy for getting exchange rates. It's a service that can be consumed by other internal services to get the exchange rate between a set of currencies, so they don't have to care about the specifics of third-party providers.

We provide you with an initial scaffold for the application with some dummy interpretations/implementations. For starters we would like you to try and understand the structure of the application, so you can use this as the base to address the following use case:

* The service returns an exchange rate when provided with 2 supported currencies 
* The rate should not be older than 5 minutes
* The service should support at least 10,000 successful requests per day with 1 API token

Please note the following drawback of the [One-Frame service](https://hub.docker.com/r/paidyinc/one-frame): 

> The One-Frame service supports a maximum of 1000 requests per day for any given authentication token. 

## Guidance

In practice, this should require the following points:

1. Create a `live` interpreter for the `oneframe` service. This should consume the [One-Frame API](https://hub.docker.com/r/paidyinc/one-frame).

2. Adapt the `rates` processes (if necessary) to make sure you cover the requirements of the use case, and work around possible limitations of the third-party provider.

3. Make sure the service's own API gets updated to reflect the changes you made in point 1 & 2.

Some notes:
- Don't feel limited by the existing dependencies; you can include others.
- The algebras/interfaces provided act as an example/starting point. Feel free to add to improve or built on it when needed.
- The `rates` processes currently only use a single service. Don't feel limited, and do add others if you see fit.
- It's great for downstream users of the service (your colleagues) if the api returns descriptive errors in case something goes wrong.
- Feel free to fix any unsafe methods you might encounter.

Some of the traits/specifics we are looking for using this exercise:

- How can you navigate through an existing codebase;
- How easily do you pick up concepts, techniques and/or libraries you might not have encountered/used before;
- How do you work with third-party APIs that might not be (as) complete (as we would wish them to be);
- How do you work around restrictions;
- What design choices do you make;
- How do you think beyond the happy path.

### The One-Frame service

#### How to run locally

* Pull the docker image with `docker pull paidyinc/one-frame`
* Run the service locally on port 8080 with `docker run -p 8080:8080 paidyinc/one-frame`

#### Usage
__API__

The One-Frame API offers two different APIs, for this exercise please use the `GET /rates` one.

`GET /rates?pair={currency_pair_0}&pair={currency_pair_1}&...pair={currency_pair_n}`

pair: Required query parameter that is the concatenation of two different currency codes, e.g. `USDJPY`. One or more pairs per request are allowed.

token: Header required for authentication. `10dc303535874aeccc86a8251e6992f5` is the only accepted value in the current implementation.

__Example cURL request__
```
$ curl -H "token: 10dc303535874aeccc86a8251e6992f5" 'localhost:8080/rates?pair=USDJPY'

[{"from":"USD","to":"JPY","bid":0.61,"ask":0.82,"price":0.71,"time_stamp":"2019-01-01T00:00:00.000"}]
```

## F.A.Q.
[Please click here for the F.A.Q.](./README.md#faq)
