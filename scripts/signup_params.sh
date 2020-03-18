#!/bin/bash

# script parameters
name=$1
email=$2
pass=$3

curl \
 --header "Content-type: application/json" \
 --request PUT \
 --data '{"userName":"'$name'","emailAddress":"'$email'", "password":"'$pass'"}' \
 http://localhost:8080/user/signup