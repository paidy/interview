# The Forex Service


### Abstract

A _Forex service_ is a simple proxy that provides access to 
a [_One-Frame service_](https://hub.docker.com/r/paidyinc/one-frame) and to serve currency 
exchange rates. The _One-Frame_ have limitation on 1000 requests/day. As a work around this limit, 
the _Forex service_ fetching _One-Frame_ data in a loop with a timeout in between iterations and store them in 
internal cache, from where they read by downstream clients.


### API

The _Forex service_ have only one endpoint with format:

```GET <host>:<port>/rates?from=<currency_id>&to=<currency_id >```

Where ```host```  and ```port``` is a _Forex service_ bind host and port 
configured in ```app.http.host``` and ```app.http.port``` parameters. And the ```currency_id``` one 
of 9 currently supported currency IDs: ```AUD```, ```CAD```, ```CHF```, ```EUR```, ```GBP```, 
```NZD```, ```JPY```, ```SGD```, ```USD```

###### Errors

* ```BadRequest(400)``` – incorrect request parameters.

* ```NotFound(404)``` – incorrect URL path or no data for requested currency pair.  

* ```ServiceUnavailable(503)``` – processing took more time than configured in ```app.http. timeout``` parameter.

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


### Architecture

![Architecture](/forex-mtl/docs/forex_arh.png)

Service composed out of 5 main components:

* [forex.http.rates.RatesHttpRoutes](/forex-mtl/src/main/scala/forex/http/rates/RatesHttpRoutes.scala) – definition 
of the _Forex service API_ described with _HTTP4S DSL_.

* [forex.programs.rates.Program](/forex-mtl/src/main/scala/forex/programs/rates/Program.scala) – contains 
implementation of API endpoints.

* [forex.cache.rates.RatesCache](/forex-mtl/src/main/scala/forex/cache/rates/RatesCache.scala) – implementation 
of rates data cache, built on _Scaffeine lib_.

* [forex.services.rates.OneFrameService](/forex-mtl/src/main/scala/forex/services/rates/OneFrameService.scala) – implements 
a worker look for fetching One-Frame data, built with _FS2 lib_.

* [forex.clients.rates.OneFrameClient](/forex-mtl/src/main/scala/forex/clients/rates/OneFrameClient.scala) – _HTTP4S client_, 
which executing call of _One-Frame service_.


### Configuration

All configuration of _Forex service_ is in [application.conf](/forex-mtl/src/main/resources/application.conf) and have next parameters:

* ```app.http.host``` – This service bind host. Default ```"0.0.0.0"```

* ```app.http.port``` – This service bind port. Default ```8080```

* ```app.http.timeout``` – Request timeout. If processing will take more time, then _503 Service Unavailable_ 
response will be returned. Default ```40 seconds```

* ```app.one-frame-client.host``` (env var ```ONE_FRAME_HOST```) – _One-Frame service_ host (note, request schema is part of 
host, i.e. ```"http://"``` or ```"https://"```). Default ```"http://127.0.0.1"```

* ```app.one-frame-client.port``` (env var ```ONE_FRAME_PORT```)  – _One-Frame service_ port. Default ```8081```

* ```app.one-frame-client.timeout``` – _One-Frame_ request timeout. If not response in time, then iteration 
will be failed and rates data will not update. The next attempt will be performed in ```rates-refresh-timeout```. 
Default ```10 seconds```

* ```app.one-frame-service. one-frame-tokens``` – A set of available _One-Frame_ tokens. On each call will 
be used next token in the list, when last token will be used it start from beginning of 
list. Default ```["10dc303535874aeccc86a8251e6992f5"]```

* ```app.one-frame-service.rates-refresh-timeout``` – Timeout on between _One-Frame_ calls. Incising of 
will save network traffic (reduce number of calls per day). Decreasing of it will make updates of rates 
faster. Default ```3 minutes```

* ```app.cache.expire-timeout``` – TTL of cached rates, if expire and data not updated in time, then rate 
will be removed from cache. Default ```5 minutes```


### How to run and test

###### Main app and unit tests with SBT

Open the terminal in project root folder and type one of next 
command (make sure you have [Java](https://www.oracle.com/java/technologies/downloads/) 
and [SBT](https://www.scala-sbt.org) installed):

* Run service locally: ```sbt run```

* Run unit tests: ```sbt test```

###### Run integration tests with SBT

1. Make sure you have [Docked](https://www.docker.com) installed and up.

2. Start dummy _One-Frame service_ with commands ```docker pull paidyinc/one-frame```
and ```docker run -p 8081:8080 paidyinc/one-frame``` (see [documentation](https://hub.docker.com/r/paidyinc/one-frame) 
for the details)

3. Run test by command: ```sbt 'integration-test/test'```

###### Create and run docker image

1. Pack the app using ```sbt docker``` command.

2. Now you can run it locally with ```docker compose up```

###### Manual testing

1. Start dummy _One-Frame service_ (if needed)

2. Run _Forex service_ locally 

3. Use [Postman](https://www.postman.com) query [collection](/forex-mtl/postman/Interview.postman_collection.json) 
to run API calls manually.


***

### Notes about _Paidy Take-Home Coding Exercises_

First of all, thank you for creating this exercise! Most time I have to write "Scala is better Java" 
like code, so it was fun to have my hands dirty with latest _cats-effect_ and _fs2_ libraries.

To not bother you much, I made bunch assumptions:

* Service should support only currencies that listed in Scala enum.

* Service should not support request with same currencies (like ```from=USD&to=USD```).

* In field ```price``` the _One-Frame service_ return a rate, so it not need any extra calculation.

Of course, in the real task all of this should be discussed and clarified.

Also, I assume you not have any hard requirement on the project structure, so moved code around a bit. From my
point of view it made codebase little cleaner and easier to navigate.

Thank you again! And waiting for your feedback :)