Implemented the Proxy Live Interpreter which accepts currency and returns the response calling the Rates API.

1. Created Unit test cases which is successful scenario which returns response successfully.
2. Created test case to fail when we are passing some other currency which is not valid input to the Rates API. For that I have added UNK means (unknown language) so that Rates API gives with fail response.
3. If the Rates API is down somehow, handled it properly instead of failing with some exceptions.
4. Made the Rates API and token configurable in the application.conf and used them internally so if the endpoint changes, we need not make any code changes. 

Tested in the intellij Setup and ran the test cases.