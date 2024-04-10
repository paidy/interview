<img src="/paidy.png?raw=true" width=300 style="background-color:white;">

# Paidy's Take-Home Technical Product Manager Exercise: Address validation engine

## What to expect?
As a next step of our interview process, we’d like you to prepare a technical challenge that is aimed to check your abilities in regards to business and system analysis, as well as utilizing available technical documentation to define a scope of a feature and prioritize its implementation. This assignment should also be a good opportunity for you to experience and see the type of work you’ll be asked to complete in this role at Paidy!

Take time to get familiar with it and submit your answers once you’ve completed it. In the next interview, you will be asked to present your proposal and the session will be used as a practice for grooming the feature.

Please be mindful that there is not really a right or wrong answer as long as you can justify your choices and reasoning. Please make as many assumptions and hypotheses as you need and/or see fit to complete the assignment. Please don’t hesitate to reach out to us if you have any questions or if anything is unclear regarding the assignment.

We appreciate your efforts and hope you’ll have fun doing this homework assignment! :) 

## What we are looking for?
**Keep it simple**. Read the requirements and restrictions carefully and focus on solving the problem.

**Approach this task as you would with a real-world project .** Develop your specifications in the same manner as you would for any project intended for deployment. Although these may be simulated exercises, we are keen to understand how you formulate and document requirements in your regular practice.

## How to submit?
Please include all the work requested under the [guidance section](./AddressValidation.md#guidance) in a PDF file and email it to the Paidy talent acquisition team.

## The Follow-Up Interview
During the follow-up interview, you'll have the opportunity to meet more members of our team. You'll be asked to discuss the decisions you made, explain the reasoning behind them, and review the requirements you outlined.

## Requirements: Address validation engine
### Context

When a consumer makes a transaction at a merchant using Paidy, we receive via API some key information about the transaction. One of these key information is the shipping address entered by the consumer on the checkout page of the merchant. Address validity is a key topic not only for marketing and logistics reasons but also for fraud. For fraudsters, a mailing address offers a lot of payment fraud and account take over opportunities. 

Imagine you’re a Risk TPM at Paidy trying to implement an address verification system to catch fraudulent transactions before they cause significant harm to Paidy’s business. How would you go about designing and implementing such a system at Paidy using a third party address validation vendor?

_Notes: the shipping address is entered on the merchant website and is communicated to Paidy via API [checkout documentation here](https://paidy.com/docs/api/en/#1-introduction). Assume that we cannot change the UI of the merchant website to prompt the consumer to correct the address. The shipping address is communicated as it was shared with the merchant so we are only thinking about implementing further checks once Paidy receives the payment information._

### Guidance

* Please look at the below list of possible vendors to integrate with. Look at them and select one vendor you would like to integrate with and explain your thought process about why you selected that vendor.
  * https://www.smarty.com/products/international-address-verification
  * https://www.egon.com/
  * https://developers.google.com/maps/documentation/address-validation/requests-validate-address
  * https://www.postgrid.com/address-verification/
* What KPIs would you use to measure the performance of your new system?
* Write the Request for Comments document with the requirements for this feature and break it down in actionable small steps
* In the next session, you will be asked to present the feature and scope proposal to Engineering lead and other product managers in a grooming session. Prepare how you want to present and communicate the user stories in this grooming session [You can choose whatever format you want]

## F.A.Q.
[Please click here for the F.A.Q.](./README.md#faq)
