First of all, thank you for creating this exercise and sharing it with me. It's very interesting.

# Preliminary work

## Needs analysis

### Understandings
The service to implement is a proxy of another service called "One-Frame".
- Its endpoint `GET /rates` must serve an exchange rate when 2 currencies are correctly provided in the request.
- Rates in `GET /rates` response, must correspond to recent rate returned by "One-Frame" (< 5 minutes).
- It should be able to serve 10,000 successful requests/day, whereas it can query "One-Frame" no more than 1,000 times/day.
- **Currencies codes** are as defined by the **norm ISO 4217** (but the year is not specified, so recent ones might not be supported).

### Assumptions
Usually I would confirm the priority between the needs with a PO, as there are no much information about the use cases. So, let's assume the following points:
- **Availability is the most important criteria** for the proxy service, as 5 minutes tolerance is huge for a forex accurate use-case like trading. Otherwise, if accuracy would be the priority, another solution would have been suggested, like using several tokens for "One-Frame" to have accurate answers, but it would likely be expensive or have other drawbacks.
- A "**day**" in requirements, is a period of time **starting at 00:00** UTC+0 **and ending at 23:59** UTC+0 (and max seconds, max milliseconds, ...).
- A **single exchange rate** must be served by `GET /rates`, so it would be likely **the average price** of bid price and ask price (coherent why having less priority for accuracy).

## Feasibility study

The proxy service has two opposite constraints: the limited number of query to refresh rates & the freshness of data that must be younger than 5 minutes when served.
Let's ensure an implementation is as least theoretically possible.

### Data refreshing interval

There are only 1000 req/day available on "One-Frame", but that service can serves several pairs at a time (limitation studied below). Let's calculate the minimal number of 5 minutes intervals in a day:
> 24 h * 60 min/h = 1440 min
> 1440 min / 5 min = 288 intervals

The theoretical minimal number of intervals is less than the allowed number of request to "One-Frame". To reduce risks of serving to old data, within the limitation of 1000 req/day, we must know the maximum number of refreshing intervals the system can support.
> 14440 min / 1,000 req = 1.44 min/req = internals of 1 min 26.4 s

I suggest to refresh the data every 3 minutes.
> 1440 min / 3 min = 480 intervals

-> If we can refresh all currencies at the time,  **data can be fresh enough**: Ok!

### Supported currencies

According to the [dedicated article in Wikipedia](https://en.wikipedia.org/wiki/ISO_4217), there are 180 currency codes of 3 characters defined by the official ISO 4217, as of 1 April 2022. "One-frame" service might not support all official code, as the list evolves in time, so I prefer to ensure which codes are supported.

I used a Google spreadsheet ([c.f. this tab](https://docs.google.com/spreadsheets/d/1dZWvOpNQb2LFWNpj_0e6FCS3w37ch3XPTWUB41Tn_aE/edit#gid=1625989458)) to generate unix commands that stores the responses for each currency (paired with USD) to list up which currencies are supported.

In [another tab](https://docs.google.com/spreadsheets/d/1dZWvOpNQb2LFWNpj_0e6FCS3w37ch3XPTWUB41Tn_aE/edit#gid=911950125), I used other commends to automatically identify the invalid currencies:

```console
grep -B 1 "Invalid Currency Pair" currencies.txt | sed '/^--$/d'						
```

There are **26 invalid codes**. This is a small number, so I added a column to identify them and remove them for the list of the 154 valid currencies.

Then I created a curl query to ensure feasibility of querying "One-Frame" for all the pairs (153 pairs: one for each valid currency with `USD`, excluding `USDUSD`) at once.

```console
curl -H "token: 10dc303535874aeccc86a8251e6992f5" 'localhost:8080/rates?pair=USDAED&pair=USDAFN&pair=USDALL&pair=USDAMD&pair=USDANG&pair=USDAOA&pair=USDARS&pair=USDAUD&pair=USDAWG&pair=USDAZN&pair=USDBAM&pair=USDBBD&pair=USDBDT&pair=USDBGN&pair=USDBHD&pair=USDBIF&pair=USDBMD&pair=USDBND&pair=USDBOB&pair=USDBRL&pair=USDBSD&pair=USDBTN&pair=USDBWP&pair=USDBYN&pair=USDBZD&pair=USDCAD&pair=USDCDF&pair=USDCHF&pair=USDCLP&pair=USDCNY&pair=USDCOP&pair=USDCRC&pair=USDCUC&pair=USDCUP&pair=USDCVE&pair=USDCZK&pair=USDDJF&pair=USDDKK&pair=USDDOP&pair=USDDZD&pair=USDEGP&pair=USDERN&pair=USDETB&pair=USDEUR&pair=USDFJD&pair=USDFKP&pair=USDGBP&pair=USDGEL&pair=USDGHS&pair=USDGIP&pair=USDGMD&pair=USDGNF&pair=USDGTQ&pair=USDGYD&pair=USDHKD&pair=USDHNL&pair=USDHTG&pair=USDHUF&pair=USDIDR&pair=USDILS&pair=USDINR&pair=USDIQD&pair=USDIRR&pair=USDISK&pair=USDJMD&pair=USDJOD&pair=USDJPY&pair=USDKES&pair=USDKGS&pair=USDKHR&pair=USDKMF&pair=USDKPW&pair=USDKRW&pair=USDKWD&pair=USDKYD&pair=USDKZT&pair=USDLAK&pair=USDLBP&pair=USDLKR&pair=USDLRD&pair=USDLSL&pair=USDLYD&pair=USDMAD&pair=USDMDL&pair=USDMGA&pair=USDMKD&pair=USDMMK&pair=USDMNT&pair=USDMOP&pair=USDMRU&pair=USDMUR&pair=USDMVR&pair=USDMWK&pair=USDMXN&pair=USDMYR&pair=USDMZN&pair=USDNAD&pair=USDNGN&pair=USDNIO&pair=USDNOK&pair=USDNPR&pair=USDNZD&pair=USDOMR&pair=USDPAB&pair=USDPEN&pair=USDPGK&pair=USDPHP&pair=USDPKR&pair=USDPLN&pair=USDPYG&pair=USDQAR&pair=USDRON&pair=USDRSD&pair=USDRUB&pair=USDRWF&pair=USDSAR&pair=USDSBD&pair=USDSCR&pair=USDSDG&pair=USDSEK&pair=USDSGD&pair=USDSHP&pair=USDSLL&pair=USDSOS&pair=USDSRD&pair=USDSTN&pair=USDSVC&pair=USDSYP&pair=USDSZL&pair=USDTHB&pair=USDTJS&pair=USDTMT&pair=USDTND&pair=USDTOP&pair=USDTRY&pair=USDTTD&pair=USDTWD&pair=USDTZS&pair=USDUAH&pair=USDUGX&pair=USDUYU&pair=USDUZS&pair=USDVND&pair=USDVUV&pair=USDWST&pair=USDXAF&pair=USDXCD&pair=USDXDR&pair=USDXOF&pair=USDXPF&pair=USDYER&pair=USDZAR&pair=USDZMW'										
```
**It works** well!

Finally, I wanted to check my intuition about "One-Frame" limitations. It can serve results for several pairs at a time, but it might not be for thousands pairs at the time.

One-Frame still respond for 306 pairs, but stop being able to respond for 459 pairs and more (technically the limitation can be curl, but I assume this is the third-party system for this exercise). This is an important limitation considering the max number of allowed queries per day.

Let's be kind and assume the limit would be 458 pairs (as it won't change the conclusion even if I am too optimistic on this point to save time).


### Currencies pairs accuracy

I already assumed that accuracy of served rates is not the most important criteria (of course it should be as accurate as possible). But let's determine what level of accuracy we can reach with the limitations.

First, we only care about pairs between 2 different values, as we can assume that an exchange rate from a currency to itself makes no sense as it would be always `1`.

There are 154 supported currencies. So the total number of possible pairs 1-to-1 would be 23,562.
> Permutations calculation for tuples of `r` elements, from a total of `n` items is: P(n, r) = n*(n-1)*...*(n-r+1)

> In this case, P(254, 2) = 154*(154-2+1) = 154*153 = 23,562.

As the served value is assumed to be an average price between bid price and ask price (confirmed by peeking at the code), we can query One-Frame only for direction of exchange between 2 currencies, and calculate the opposite direction as the opposite number (1 / opposite rate).

Logically, **it remains 11,781 possibilities of pairs**. For the fun, let's confirm this number with combinations calculation:
> Combinations calculation for sub-sets of `k` elements, from a total of `n` items is: C(n, k) = n! / (k! * (n - k)!)

> In this case, C(254, 2) = 154! / (2! * (154 - 2)!) = 154! / (2! * 152!) = 11,781.

Even theoretically, if we would refresh only one chunk of possible pairs every 1 minutes and 26.4 s and use the 1000 allowed request each day (which is not wise), we would only be able to refresh 1374 pairs not older than 5 minutes.
> 458 pairs/req * 3 req/max-interval = 1374 pairs/max-interval

I also calculated the number of chars of the minimal URI to query One-Frame for all combinations, and this is too big.

-> It's **not possible** to query One-Frame **for any combination** of currencies.

However, as we only consider the average price for each exchange rates, we can approximate any exchange rate by transitivity between the pairs of all currencies with one "reference currency". Let's choose `USD` as the reference currency. Let's call "reference pairs" the all the valid pairs of `USD` and another currency.

-> With **transitivity approximation** for exhange rates, the service should be **able to serve fresh-enough exchange rates** based on regular query to One-Frame refreshing the value for the 153 reference pairs.


## Conclusion

I recommand to implement the proxy:
- to query One-Frame every 3 minutes
    - for the 153 reference pairs each time
    - and store the data (average price, timestamp, curency codes) for each item (a cache would be great)
- when `GET /rates` recieve a query
    - fetch the 1 or 2 reference pairs needed
    - generate the response from the fetched reference pairs (exhange rate, oldest timestamp, and currency codes)
