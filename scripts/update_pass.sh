curl \
 --header "Content-type: application/json" \
 --request POST \
 --data '{"id":"'$1'","password":"'$2'"}' \
 http://localhost:8080/user/update/password