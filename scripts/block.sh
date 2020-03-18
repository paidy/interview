#!/bin/bash

curl \
 --header "Content-type: application/json" \
 --request GET \
 http://localhost:8080/user/block/$1