# An API for managing users

Build an API around a Users service

__Requirements__

[Users](users) is a simple application that contains a user service. The algebra (or interface) of the application can be found [here](users/src/main/scala/users/services/usermanagement/Algebra.scala). The service allows some simple management of user accounts with specific actions that map to use cases.

The service needs an _http & json_ api in order to drive the frontend consumption of the service. You can think about the frontend real estate that runs on top of it as an internal admin interface to manage all accounts as well as an application for individual end-users to manage their own accounts.

The minimal set of operations that need to be supported by the api, are the ones in the service [interface](users/src/main/scala/users/services/usermanagement/Algebra.scala). We have no strict requirements on the structure, look, format, mappings, ... of the endpoints. We leave this open intentionally for you to make, and substantiate the soundness of, design decisions.

Some key points to take in mind when doing this:

- Do all use cases need to be handled (in the same way) by the admin or the end-user.
- Which data should (not) be shared
- How are exceptions handled & mapped
- Consistency in the specifications
- Backward compatibility
