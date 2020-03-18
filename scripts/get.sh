#!/bin/bash

# $1 - first parameter of the script

curl \
 --header "Content-type: application/json" \
 --request GET \
 http://localhost:8080/user/$1