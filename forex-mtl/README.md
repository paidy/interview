Assumptions 
1. As this is an internally used service, token has been set in service only. Otherwise, depending on the use case each client might need their own token.
2. Have implemented cache assuming there is only one instance of this service is running. Otherwise, would need a centralized cache like redis.

Implementation
1. Have used Caffeine in memory cache, check RatesCache class. For each pair caching the response in "rate:$from:$to" key for 5 min. This way, we can get a valid response from this proxy a lot more than 10000 per day.
2. For a pair of keys only (2 req/5 min) or (288 req/day) are possible and with optimization mentioned below (1 req/5 min) or (144 req/day). We are managing 9 currencies and in that case 9C2 or 36 unique currency pairs are possible. This can lead to 36 unique req every 5 min without any cache hit. Which means in this very specific case our token would expire for the day before we are able to hit 10000 req. An option in that case could be showing stale data.
3. In case of no cache hit, hitting the one frame API using OneFrameHttpClient.

Possible improvements and points of discussion
1. Implementation of One token HTTP client is synchronous. As there is not much scale to be considered for this activity, it should be fine. Might be different in production.
2. One possible optimization to reduce the workload to downstream service even more(possibly by half) is trying to fetch from cache for both "rate:$from$to" as well as "rate$to:$from" key. We can take inverse of price in case we get a hit for "rate$to:$from".
3. As this is my first time writing scala, I have spent a lot of time on this assignment. Have skipped some error cases like token getting expired.