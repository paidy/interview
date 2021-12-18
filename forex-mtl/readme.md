# Arvind's forex proxy

### Concept
The idea is to create a local proxy that polls forex rates and stores it locally in the cache.
The polling is set to a default of 90 seconds since we're rate limited to 1000 calls per day. 90 seconds is the default as a day has 86400 seconds and 86400 seconds/90 = 960 which is slightly < than our rate limit.
Calls to our query will either block or read from our cache.


### Design
The design contains the following classes:

* Dispatcher (to make the API calls based on the UrlConfig)
* Poller (schedules based on the PollConfig)
* ConfigReader - Some kind of Reader
* ForexStore (contains the latest set of forex rates)
* API - 

Ambiguities - if external API is down, what do we do with the result? Do we return the stale (> 5 min) results? Do we return not found? Perhaps it makes more sense to send the forex rate with the timestamp so we delegate this choice to the consumer.
