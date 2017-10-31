# An SPA that displays weather information

Build a Single Page Application displaying weather information which you get through an api.

__Requirements__

At Paidy, we use react as a javascript framework for the frontend. Ideally, you would complete this exercise in react. We're however open if you would use other frameworks, or languages transpiling to javascript that are inspired by the same or similar concepts (e.g. vue.js, purescript halogen, ...).

We would like to see an in-browser application that creates an interface over the [Yahoo Weather API](https://developer.yahoo.com/weather/). You can display the information in whatever way you wish to (graphs, icons, animations, ...), but try to make it both appealing as well as insightful for the end-user (as a minimum, we would like to see current conditions, as well as a visualisation of the forecast).

Amongst others, we at least expect the following to be addressed:

- being able to select which city/location to display the information for
- when selecting a new city, the data displayed should transition nicely
- the end-user should be able to refresh the data fetched. If the data is updated, a similar transition would happen.
- make sure you handle failure modes. Some examples of the latter are:
  - the api takes a long time before returning data
  - the api doesn't respond
