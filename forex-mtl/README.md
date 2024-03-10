## Running the application

1. Run the OneFrame API image on `port 8000` using `docker run -p 8000:8080 paidyinc/one-frame`
2. Run the Scala application defined in `src/main/scala/forex/Main.scala`

Example request: `http://localhost:8080/rates?from=USD&to=JPY`