# Guidelines

(See "Assignment" section for assignment details)

Please use the Rust programming language to complete this assignment.

If you encounter any ambiguity, please document your assumptions and the reasoning behind them.

As a guideline, you can expect this assignment will take around 4-6 hours, but spend as much time as you feel is appropriate.
The focus will be on data structure choice, API design and 
implementation, internal implementation (especially regarding data correctness, multithreaded capacity, and 
proper unit testing).  How "functional" the solution is coded will also be a strong factor.  Please learn and use all 
the functional programming skills you can to complete this test, along with a good sense of when and where to apply 
them.  Knowing when to try to stay purely functional and when to deviate to a more procedural mindset is a 
skill which will come in handy for this test.

Please keep everything simple and focus on making each piece as solid as possible.  The parts that are in the 
solution should be tested. It is preferable to have a few pieces that are well-written and 
functional with unit tests rather than a broader solution which is buggy and untestable.

In general, consider your final solution to be a "production-ready" application, so whatever constitutes 
"production-ready" to you will be on display here. 

## Important Notes: 

Please don't fret too much about a lack of experience in Rust. 
Regard this as a chance to showcase your ability to pick up a new language, understand its basics, and apply that understanding 
in a short amount of time. Picking a new language and showing that you can write some basic code using the strengths of that language, 
even if the code isn’t mastery-level and doesn’t include super high-powered features, is a powerful showcase of your 
software engineering skills.  In addition, other basic software engineering skills will be on display regardless of your
familiarity with Rust. 

The solution should be designed to be production-ready as you would normally do for a real business application. You don't need to
deploy it somewhere, but keep in mind how the code would need to evolve when requested to add new features or if business requirements changed.
Try to make it as clean as you can (tested, documented, etc.), and please bear in mind that 
the point of this is to showcase your skills, particularly with functional programming, API design, data structure choice, 
and data manipulation techniques. The desire to use third-party tools to perform data updating, table and database management, etc. 
which would ordinarily be in a production-ready design is much appreciated and understood, however, these tools hide 
your skills behind already-built libraries, and make it difficult for us to understand your level with regards to skills 
vital to success at Paidy.  

So, please refrain from using tools which perform API and data structure design for you, or hide the data 
manipulation behind third-party library calls such as ORM tools (Diesel, JPA, Hibernate, SQLAlchemy, etc...), 
API generation tools (OpenAPI generator, etc.).   

Note: Libraries which deal with threading, thread channels, TCP/IP streaming, HTTP processing, REST 
webserver endpoints, and other functionality which bring the data into your app (Rocket, Axum, Actix, etc...) are fine.

Good luck!

## Submission
[Please click here for our submission guide.](./README.md#how-to-submit)

# Assignment

*Business Case*

Create a restaurant application which accepts menu items from various serving staff in the restaurant.  This 
application must then store the item along with a cooking time for the item to be completed.  The application 
must be able to give a quick snapshot of any or all items on its list at any time.  It must also be able to remove 
specific orders from the list of orders on demand.

## System Actors

### The application
Running on a “server” and accepting calls from devices carried by restaurant staff to process guest’s 
menu orders.  This is where the bulk of time should be spent.

### The client 
Multiple "tablets" carried by restaurant staff to take orders.  These will send requests to the “server”
to add, remove, and query menu items for each table.  Please make this as simple as possible.

## Requirements

* The server API MUST fully follow REST API principles and present a set of HTTP endpoints to connect to.
* The client (the restaurant staff “devices” making the requests) MUST be able to: add one or more items with a 
table number, remove an item for a table, and query the items still remaining for a table.
* The application MUST, upon creation request, store the item, the table number, and how long the item will take to cook.
* The application MUST, upon deletion request, remove a specified item for a specified table number.
* The application MUST, upon query request, show all items for a specified table number.
* The application MUST, upon query request, show a specified item for a specified table number.
* The application MUST accept at least 10 simultaneous incoming add/remove/query requests.
* The client MAY limit the number of specific tables in its requests to a finite set (at least 100).
* The application MAY assign a length of time for the item to prepare as a random time between 5-15 minutes.
* The application MAY keep the length of time for the item to prepare static (in other words, the time does not have 
to be counted down in real time, only upon item creation and then removed with the item upon item deletion).

### Allowed Assumptions

You may have your application assume the following to simplify the solution, if desired:

* The time to prepare does not have to be kept up-to-date.  It can also just be generated as some random amount 
of time between 5 and 15 minutes and kept static from then on.
* The table and items can be identified in any chosen manner, but it has to be consistent. So if a request comes in 
for table "4", for example, any other requests for table "4" must refer to the same table.
* “Clients” can be simulated as simple threads in a main() function calling the main server application with a 
variety of requests.  There should be more than one, preferably around 5-10 running at any one time.


