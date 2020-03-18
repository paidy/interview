#!/bin/bash

curl \
 --header "Content-type: application/json" \
 --request DELETE \
 --data '{"id":"'$1'"}' \
 http://localhost:8080/user
