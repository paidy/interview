First of all, thank you for creating this exercise and sharing it with me. It's very interesting.

# Preliminary work

## Needs analysis

### Understandings
The service to implement is a proxy of another service called "One-Frame".
- Its endpoint `GET /rates` must serve an exchange rate when 2 currencies are correctly provided in the request.
- Rates in `GET /rates` response, must correspond to recent rate returned by "One-Frame" (< 5 minutes).
- It should be able to serve 10,000 successful requests/day, whereas it can query "One-Frame" no more than 1,000 times/day.

### Assumptions
Usually I would confirm the priority between the needs with a PO, as there are no much information about the use cases. So, let's assume the following points:
- **Availability is the most important criteria** for the proxy service, as 5 minutes tolerance is huge for a forex accurate use-case like trading. Otherwise, if accuracy would be the priority, another solution would have been suggested, like using several tokens for "One-Frame" to have accurate answers, but it would likely be expensive or have other drawbacks.
- A "**day**" in requirements, is a period of time **starting at 00:00** UTC+0 **and ending at 23:59** UTC+0 (and max seconds, max milliseconds, ...).
- A **single exchange rate** must be served by `GET /rates`, so it would be likely **the average price** of bid price and ask price (coherent why having less priority for accuracy).
