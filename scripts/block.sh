#!/bin/bash

curl \
 --header "Content-type: application/json" \
 --request POST \
 --data '{"id":"'$1'"}' \
 http://localhost:8080/user/block
