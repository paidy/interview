# simple-restaurant-api - Paidy Take-Home Coding Exercises

## API

### Important Endpoints
```
GET /v1/menus - Get all available menus
GET /v1/tables - Get all tables in the restaurant
GET /v1/orders - Get all orders
GET /v1/orders?table={tableId} - Get all orders for a table
POST /v1/orders - Create a new order
PUT /v1/orders/{orderId} - Update an order
DELETE /v1/orders/{orderId} - Delete a specific order
DELETE /v1/orders?table={tableId} - Delete all orders for a table
```

### Auxiliary Endpoints
```
GET /v1/menus/{menuId} - Get a specific menu
PUT /v1/menus/{menuId} - Update a menu
DELETE /v1/menus/{menuId} - Delete a menu
GET /v1/tables/{tableId} - Get a specific table
PUT /v1/tables/{tableId} - Update a table
DELETE /v1/tables/{tableId} - Delete a table
```

### Sample Request - Create a new order

This will create 3 items for table 1.

```bash
curl --location --request POST 'http://localhost:8080/v1/orders/' \
--header 'Content-Type: application/json' \
--data-raw '[
    {
        "tableId": 1,
        "menuId": 1,
        "quantity": 1
    },
    {
        "tableId": 1,
        "menuId": 2,
        "quantity": 2
    },
    {
        "tableId": 1,
        "menuId": 4,
        "quantity": 1
    }
]'
```

## Run with Docker Compose

`docker-compose.yml` is available and will spin up the application on port 8080 and the database on port 3306 (make sure the port is not in use).

If needed, set the database credentials in the environment variables set in `docker-compose.yml` file before running for the first time.

```bash
docker compose up
```

Once the containers are up, run the following command to create the database and tables:

```bash
./gradlew flywayMigrate
```

## Running the Client Simulation

Client simulation can be found in `client/simulation.py`.

It is a simple Python 3 script that will periodically send random requests to the API.

To run the simulation, first install the dependencies:

```bash
pip install requests
```

If needed, modify the configuration variables within the script.

```python
base_url = "http://localhost:8080/v1"
simultaneous_req = 10
sleep_time_sec_min = 5
sleep_time_sec_max = 15
```

Then run the script:

```bash
python ./client/simulation.py
```


## Postman Collection

Alternatively, you can import the Postman collection `simple-restaurant-api.postman_collection.json` to test the API.
