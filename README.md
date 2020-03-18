# Paidy Take-Home Coding Exercises - API for managing users

The complete specification for this exercise can be found in the [UsersAPI.md](UsersAPI.md).

Table of contents
------------------

  * [Technologies](#tech)
  * [User representation](#user)
  * [Scripts](#scripts)


<a name="tech" />

## Technologies

IDE - IntelliJ IDEA

Web framework - [Scalatra](https://scalatra.org/)

JSON implementation - [spray-json](https://github.com/spray/spray-json)

Server - [Jetty](https://www.eclipse.org/jetty/)

### Why Scalatra

* open source framework
* great for beginners
* active community
* easy to set up
* extensive documentation

<a name="user" />

## User representation

`User` is represented as JSON object with following structure:

```
{
  "id": "a33ffccf-2648-4d0b-b1df-232427c809b3",
  "userName": "john",
  "emailAddress": "john@test.com",
  "password": "be6eg1",
  "metadata": {
    "version": 1,
    "createdAt": "2020-03-18T11:58:52.469938+01:00",
    "updatedAt": "2020-03-18T11:58:52.469938+01:00",
    "blockedAt": "",
    "deletedAt": ""
  }
}
```

Serialization and deserialization is implemented in `UserJsonProtocol` in directory `users/src/main/scala/users/api/json`.

<a name="scripts" />

## Scripts

There are several bash scripts in the directory `scripts` that can be used for testing purposes. They contain `curl` commands. Some of them take input parameters. Scripts will do something only if the server is running.

To run a script open a terminal in the `script` folder and use command `./name-of-the-script.sh`

For running with parameters use command `./name-of-the-script.sh param1 param2`

It might be necessary to make the script executable. Then use command `chmod +x name-of-the-script.sh`

* `getid.sh`
	- takes parameter `userId`
	- returns json with user information
* `signup.sh`
	- takes no parameter, alredy contais data for mock user John
	- returns signed up user
* `signup_params.sh`
	- takes parameters `userName`, `email`, `password`
	- returns signed up user
* `update_email.sh`
	- takes parameters `userId`, `newEmail`
	- returns user with updated email
* `update_pass.sh`
	- takes parameters `userId`, `newPassword`
	- returns user with updated password
* `reset_pass.sh`
	- takes parameters `userId`
	- returns user with empty password
* `block.sh`
	- takes parameters `userId`
	- returns user with changed `blockedAt` timestamp
* `unblock.sh`
	- takes parameters `userId`
	- returns user with empty `blockedAt` timestamp
* `delete.sh`
	- takes parameters `userId`
	- returns status 200 if user was deleted
