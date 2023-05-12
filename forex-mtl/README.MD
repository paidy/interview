### Time spent
10 hours


### Assumptions
it was assumed that calls to
http://localhost:9090/rates?pair=XXXYYY

and

http://localhost:9090/rates?pair=YYYXXX

provide same value for 'price' attribute and this attribute contains desired value.


### Simplifications
1) caching architecture
there is regular scheduled cache refresh process started by server. 
caching refresh is done every 2 minutes in order to mitigate possible network delays or failures.
for simplification it is assumed that there is only one instance of forex-mtl service and there will be no races between 
mentioned schedulers.

in case of multiple instances external locking mechanism might be introduced in schedulers, say,
redis semaphore locks with timestamps as values.
possible approach:
- only one node can execute caching process
i.e. if node has got lock and timestamp of its creation is after by current time by 2 minutes when there is need to update cache
- after updating cache node should set new timestamp
- if node can't acquire lock this means that there is other node running the process
- lease timer should for semaphore should be set assuring that in case of some node failure there will be no gaps in updates
and other node will be able to actualize cache on next attempt

2) caching approach 
caching is done for all 9 currencies present in initial service template which means:
- there are possible 72 permutations
- there are 863 chars added after api url <http://localhost:9090/rates?> (each pair in form 'pair=XXXYYY&', 12 chars)

general recommendations for url-encoded location length is 2047 which allows 
132 possible pairs for 12 currencies with 1584 chars after api url.

maximum length or url-encoded request accepted by **One-Frame** service before replying with 400 is 4095 which allows
306 possible pairs for 18 currencies with 3672 chars after api url.

af it was mentioned caching refresh is done every 2 minutes in order to mitigate possible network delays or failures.
i.e. where are ( 60 / 2 ) * 24 = 720 reqs per day.

these calculations can be used for further improvements.
say, introducing some sub-sets of currencies which have different caching policies depending on
frequency of conversion requests:

https://www.investopedia.com/ask/answers/06/maincurrencypairs.asp
(18 pairs are mentioned in article, hehehe)

so, for example, these 18 are refreshed 3 minutes each while others (statistically less important) are not cached and 
requested directly.


### Test coverage
well...
simply absent.
a decision was made for this task to be timeboxed and it already took significant time amount.


### Starting application
for cache DB
```
docker-compose up
```
which will init create required DB table and start One-Frame as well

request to get rate against running Forex-Mtl
```
http://localhost:9090/rates?from=EUR&to=JPY
```

