from datetime import datetime
import json
import random
import requests
import time

base_url = "http://localhost:8080/v1"
simultaneous_req = 10
sleep_time_sec_min = 5
sleep_time_sec_max = 15


def with_time(str):
    return datetime.now().strftime("[%Y-%m-%d %H:%M:%S] ") + str


def get_all_tables():
    result = requests.get(base_url + "/tables")
    ret = json.loads(result.text)
    log = list(map(lambda x: x["name"], ret))
    print(with_time(f"FYI: Available Tables -> {log}"))
    return ret


def get_all_menus():
    result = requests.get(base_url + "/menus")
    ret = json.loads(result.text)
    log = list(map(lambda x: x["name"] + f" ({x['cookingTimeSec']}s prep)", ret))
    print(with_time(f"FYI: Available Menus -> {log}"))
    return ret


def get_orders_for_table(table_id):
    result = requests.get(base_url + "/orders?table=" + str(table_id))
    ret = json.loads(result.text)
    log = list(map(lambda x: x["menu"]["name"] + f" x{x['quantity']}", ret))
    print(with_time(f"[Table {table_id}] Orders List -> {log}"))
    return ret


def create_orders_for_table(table_id, orders: list[(int, int)]):
    body = list(map(lambda x: {"tableId": table_id, "menuId": x[0], "quantity": x[1]}, orders))
    result = requests.post(base_url + "/orders", json=body)
    ret = json.loads(result.text)
    log = list(map(lambda x: x["menu"]["name"] + f" x{x['quantity']}", ret))
    print(with_time(f"[Table {table_id}] Creating Orders -> {log}"))
    return ret


def delete_order(order):
    result = requests.delete(base_url + "/orders/" + str(order["id"]))
    print(with_time(f"[Table {order['table']['id']}] Deleting Order ID #{order['id']} ({order['menu']['name']} x{order['quantity']})"))
    return result.status_code == 200


def delete_orders_for_table(table_id):
    result = requests.delete(base_url + "/orders?table=" + str(table_id))
    print(with_time(f"[Table {table_id}] Deleting All Orders"))
    return result.status_code == 200


print(with_time("Starting Simulation..."))
print(with_time(f"FYI: Running {simultaneous_req} requests every {sleep_time_sec_min}-{sleep_time_sec_max} seconds"))
print("------------------------------------------------------------")
tables = get_all_tables()
menus = get_all_menus()
print("------------------------------------------------------------")

while True:
    table_ids = list(map(lambda x: x["id"], tables))
    for i in range(simultaneous_req):
        table_id = random.choice(table_ids)
        table_ids.remove(table_id)
        table = list(filter(lambda x: x["id"] == table_id, tables))[0]

        random_orders = [(random.choice(menus)["id"], random.randint(1, 5)) for i in range(2, random.randint(3, 10))]
        additional_orders = [(random.choice(menus)["id"], random.randint(1, 3)) for i in range(1, random.randint(2, 4))]

        get_orders_for_table(table["id"])

        create_orders_for_table(table["id"], random_orders)
        current_orders = get_orders_for_table(table["id"])

        delete_order(random.choice(current_orders))
        get_orders_for_table(table["id"])

        create_orders_for_table(table["id"], additional_orders)
        get_orders_for_table(table["id"])

        delete_orders_for_table(table["id"])

    print("------------------------------------------------------------")
    time.sleep(random.randint(sleep_time_sec_min, sleep_time_sec_max))