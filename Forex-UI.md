<img src="/paidy.png?raw=true" width=300 style="background-color:white;">

# Paidy Take-Home Coding Exercises

## What we are looking for?
**Keep it simple**. Read the requirements and restrictions carefully and focus on solving the problem.

**Treat it like production code**. That is, develop your software in the same way that you would for any code that is intended to be deployed to production. These may be toy exercises, but we really would like to get an idea of how you build code on a day-to-day basis.

## How to submit?
You can do this however you see fit - you can email us a tarball, a pointer to download your code from somewhere or just a link to a source control repository. Make sure your submission includes a small **README**, documenting any assumptions, simplifications and/or choices you made, as well as a short description of how to run the code and/or tests. Finally, to help us review your code, please split your commit history in sensible chunks.

# An in-browser Forex rates SPA

Build a Single Page Application displaying Forex rates information which you get through an api.

## Requirements

We would like to see an in-browser application that creates an interface over the [One-Frame API](https://hub.docker.com/r/paidyinc/one-frame). You can display the information in whatever way you wish to (graphs, icons, animations, ...), but try to make it both appealing as well as insightful for the end-user.

Amongst others, we at least expect the following to be addressed:

- being able to select which currency pair to display the information for
- when selecting a new currency pair, the data displayed should transition nicely
- the end-user should be able to refresh the data fetched. If the data is updated, a similar transition would happen.
- make sure you handle failure modes. Some examples of the latter are:
  - the api takes a long time before returning data
  - the api doesn't respond

### The One-Frame service

#### How to run locally

* Pull the docker image with `docker pull paidyinc/one-frame`
* Run the service locally on port 8080 with `docker run -p 8080:8080 paidyinc/one-frame`

#### Usage
__API__

The One-Frame API offers two different APIs, for this exercise please use the streaming one, This is a never ending stream, after opening a connection the API will keep returning new exchange rates for the queried currency pairs until the connection is closed.

```
GET /streaming/rates?pair={currency_pair_0}&pair={currency_pair_1}&...pair={currency_pair_n}
```

* `pair`: Required query parameter that is the concatenation of two different currency codes, e.g. `USDJPY`. One or more pairs per request are allowed.

* `token`: Header required for authentication. `10dc303535874aeccc86a8251e6992f5` is the only accepted value in the current implementation.

__Example cURL request__
```
$ curl --no-buffer -s -H "token: 10dc303535874aeccc86a8251e6992f5" 'localhost:8080/streaming/rates?pair=USDJPY'

[{"from":"USD","to":"JPY","bid":0.6118225421857174,"ask":0.8243869101616611,"price":0.71810472617368925,"time_stamp":"2022-01-11T07:47:40.734Z"}][{"from":"USD","to":"JPY","bid":0.8435259660697864,"ask":0.4175532166907524,"price":0.6305395913802694,"time_stamp":"2022-01-11T07:47:41.739Z"}][{"from":"USD","to":"JPY","bid":0.1350922166954046,"ask":0.13871074418376472,"price":0.13690148043958466,"time_stamp":"2022-01-11T07:47:42.74Z"}]

$ curl --no-buffer -s -H "token: 10dc303535874aeccc86a8251e6992f5" 'localhost:8080/streaming/rates?pair=USDJPY' | jq


[
  {
    "from": "USD",
    "to": "JPY",
    "bid": 0.6118225421857174,
    "ask": 0.8243869101616611,
    "price": 0.7181047261736893,
    "time_stamp": "2022-01-11T07:48:09.94Z"
  }
]
[
  {
    "from": "USD",
    "to": "JPY",
    "bid": 0.8435259660697864,
    "ask": 0.4175532166907524,
    "price": 0.6305395913802694,
    "time_stamp": "2022-01-11T07:48:10.943Z"
  }
]

```

## F.A.Q.
[Please click here for the F.A.Q.](../README.md)

