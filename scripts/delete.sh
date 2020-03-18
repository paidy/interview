#!/bin/bash

curl \
 --header "Content-type: application/json" \
 --request DELETE \
 http://localhost:8080/user/$1