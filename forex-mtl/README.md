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

## Analysis

In short, we can add a configurable cache layer to achieve the requirements.

According to the requirements, I know that 1000 requests limitation per one token per day. However, this local proxy will provide more than 10000 requests daily.
If we assume the token is used for an external commercial API service, the naive idea is to buy more quotas or more tokens. But that raises the cost and may not be the expectation of an engineer.

Luckily, the other requirement says the rate should not be older than five minutes. Let's calculate if we send the request to the external service every four minutes and keep remembering the results. 

86400 (sec) / 240 (sec) = 360 

This means we will only make 360 requests per day which is far from the 1000 limitation, and also keep the data fresh in four minutes.
Thus, let's add and implement a cache layer into the solution.

## How do I do?

* How can you navigate through an existing codebase?

    After quickly going through the forex-mtl project, I know the scaffold was built with tagless final encoding. Define the domain objects first, define the service algebra, and implement the interpreter. Each submodule consists of an algebra, one or more interpreters, and serval accessories like Protocol and errors. Finally, a program runs the actual business logic by combining the services.

    In the beginning, I planned to add or modify the following items.
    
    * Add a cache service with functions get and set.
    * Implement the one-frame HTTP client service instead of the dummy one.
    * Modify the program. First, query the result from the cache. If there is no or expired, query to external service, save the successful outcome to cache-store, and return the result. Return it directly if there is a result in the cache.
    * Before the cache service and HTTP client service, it will need cache store and HTTP client resources, respectively.
    * For all long-running services, there should be a health check endpoint for monitoring. The endpoint should also be able to check the external resources.
    * The is no logger outside the http4s itself, which is hard to develop and debug.
    * No unit test suites in the scaffold project.


* How easily do you pick up concepts, techniques and/or libraries you might not have encountered/used before?

    In my experience, I had only heard of http4s before and did use it in my daily tasks.
    And I also notice that the Cats Effect library major version is 2, which is a little old version affecting many other libraries in the ecosystem.

    Fortunately, I can still find the document and code example in the older version. 
    Here are the dependencies I added.

    * redis4cats for the Redis. I will explain the choice later.
    * http4s-blaze-client since the project uses the blaze server.
    * log4cats for the logger.
    * weaver for the testing of the Cats Effect.


* How do you work with third-party APIs that might not be (as) complete (as we would wish them to be)?

    I implemented the one-frame HTTP client service with the provided algebra, it returns an Either inside of the effect F, which is simply mapping the successful result to the Right, and keeps the failed information on the Left.

* How do you work around restrictions?

    By caching, we can conquer the requests limitation per day.


* What design choices do you make?

    For the caching storage, I use external Redis rather than the native in-memory data type like Ref. It is because now the day, people are building microservices and deploying on k8s. K8s can scale out the service by adding replicas of pods. And using the ingress service to distribute the traffic. For each pod being scaled, it should not keep the state inside. Says if we use Ref, the Ref in pod-0 and Ref in pod-1 would be different in some time. We can use an external in-memory store like Redis to avoid this conflict. Redis's key expiration functionality can also help keep the data fresh. 


* How do you think beyond the happy path?

    If we lose the connection to Redis, it will break the program. Thus, I create the health check endpoint to monitor the status. I cannot take responsibility for their reliability for one-frame or other external API services. Still, as a local proxy, I can pass the error and message from them to my result.

## What could be improved?

The first thing I would like to mention regarding the current status of the forex-mtl project is the dependencies should be upgraded. At least from Cats Effect 2 to 3.

During the development, I always need to seek back the older document and lose the newer version's convenience features.

However, migrating CE2 to CE3 is complex work. Before that, we will need more plans to do it. For instance, to ensure all the current dependencies can upgrade, we have to check the implementation detail is also able to upgrade.

The test coverage is still low; I only add some test suites, like the demonstration, due to the time restriction.


## Open Discussion

* Should we have the Enoder/Decoder with domain objects together?
    
    We can see many different Protocol objects containing encoders and decoders for the module. These Protocol objects are boilerplate code or repeating work. I understand that each module may need different encode/decode implementations. But for these commonly used domain objects, we can define encoder and encoder where it describes or even derivates them.

* How should we keep the token of external service?

    Currently, I made the token an explicit value in the configuration.
    The choice could be to let our downstream proxy user keep the token. And we receive it from the request and pass it to external service.
    Or I store it somewhere implicitly; for instance, if we deploy the proxy app with k8s, the token should be a k8s secret.
    
---

## Addition Update on 7th Jan.

After received the feedback on 4th Jan, I found my original solution could not fulfill the requirement if the server gets arbitrary currency pair during the cache expiration period.
Thus, I updated my solution as follows.
* Make the One-Frame rates requests ask for all the combinations of supported currency and cache it.
* One-Frame returns nothing if the currency combination is self-repeated; for instance, pair=USDUSD
* Introduce the `enumeratum` library to the original Currency implementation for convenient Enum operation.
* Add RatesHttpRoute test suite.
* Improve the information when receiving an invalidated request.