# A local proxy for Forex rates

Build a local proxy for getting Currency Exchange Rates

__Requirements__

[Forex](forex) is a simple application that acts as a local proxy for getting exchange rates. It's a service that can be consumed by other internal services to get the exchange rate between a set of currencies, so they don't have to care about the specifics of third-party providers.

We provide you with an initial scaffold for the application with some dummy interpretations/implementations. For starters we would like you to try and understand the structure of the application, so you can use this as the base to address the following use case:

> An internal user of the application should be able to ask for an exchange rate between 2 given currencies, and get back a rate that is not older than 5 minutes. The application should at least support 10.000 requests per day.

In practice, this should require the following 2 points:

1. Create a `live` interpreter for the `oneforge` service. This should consume the [1forge API](https://1forge.com/forex-data-api/api-documentation), and do so using the [free tier](https://1forge.com/forex-data-api/pricing).

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

__Solution__

Initial requirements were:
 - support at least 10k request per day
 - return rate not older than 5 minutes
 - support 9 currencies (what gives 72 possible pairs)
 - limit calls to OneForge to 1000 requests per day
 
To fulfill them I've decided to periodically (every 2 minutes but it's configurable) make a batch call to OneForge API
to get current quotes for all supported pairs (their API allows this and counts it as 1 request).

Caching individual requests for 5 minutes and only calling OneForge if someone ask for a rate we don't have
was not an option because in the pessimistic scenario it would require more than 20k calls per day.

__Implementation details__

To cache rates I've added new effect to the stack (Memoized) and created additional service (RatesCache).
Rates process gets data from it when handles users' requests. 
At the start of the application cyclic call for current rates is being scheduled and the process of storing new rates is also
handled by Rates process. As those two tasks (fetching new data and handling users's calls) are done separately
the only exception (from Rates process) user ever gets is CurrentRateNotAvailable if for some reason rate is not available.
All exceptions related to calling OneForge and parsing their response are logged.
I've also added checking if user didn't provide the same currency for "from" and "to" as this is not a valid pair.


__How to run tests__

Simply run in console (you have to be in the project folder)
>sbt test


__How to run application__

To run application you have to pass you API key as an environment variable (ONE_FORGE_API_KEY).
You can do this by typing in console:

On Windows 
>set ONE_FORGE_API_KEY=<your_api_key>

On Linux/Mac
>export ONE_FORGE_API_KEY=<your_api_key>

Then you can run your application from console (you have to be in the project folder)
>sbt run


__Future work__

I limited my work on this exercise to the expected 6 hours. Unfortunately in this time I didn't 
manage to finish everything what I'd like to do. If I had more time I'd:
 - add integration tests
 - add unit tests for the api
 - check if it wouldn't be better to add Either as an effect instead of a result type
 - try to use State effect instead of Memoized as I have a feeling that I misused it.
 