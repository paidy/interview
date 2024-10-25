<img src="/paidy.png?raw=true" width=300 style="background-color:white;">

# Paidy's Take-Home Technical Product Manager Exercise: Address validation engine

## What to expect?
As the next step in our interview process, we’d like you to complete a technical challenge designed to assess your abilities in business and system analysis, and your proficiency in using technical documentation to define the scope of a feature and prioritize its implementation. This assignment is also an opportunity for you to experience the type of work you'll be doing at Paidy as a Technical Product Manager.

Once finished, please submit your answers, and in the next interview, you’ll present your proposal. This session will simulate a project kick-off meeting where you’ll onboard senior management, other technical product managers, engineers, and analysts into the project. You will walk them through your assumptions, decisions, and the requirements you’ve outlined, just as you would in a real-world project setting.

There isn’t a right or wrong answer—what matters most is your reasoning and ability to justify your decisions. Feel free to make any assumptions and hypotheses that you believe are necessary to complete the assignment. If anything is unclear or you have any questions, don't hesitate to reach out.

We appreciate your time and effort and hope you enjoy working through this challenge!

## What we are looking for?
**Keep it simple**. Read the requirements and restrictions carefully and focus on solving the problem.

**Approach this task as you would with a real-world project .** Develop your specifications in the same manner as you would for any project intended for deployment. Although these may be simulated exercises, we are keen to understand how you formulate and document requirements in your regular practice.

## How to submit?
Please include all the work requested under the [guidance section](./AddressValidation.md#guidance) in a PDF file and email it to the Paidy talent acquisition team.

## The Follow-Up Interview

In the follow-up interview, you’ll meet additional members of our team and present your submission as if you were conducting a project kick-off meeting. This will simulate a real-world scenario where you’ll onboard senior management, other technical product managers, engineers, and analysts into the project. You will walk them through the assumptions, decisions, and requirements you’ve outlined, giving them a clear understanding of your approach.

We’d like to hear about:
- The decisions you made and why you made them
- An explanation of the requirements you wrote
- How you balanced business needs with technical constraints
- Any assumptions or trade-offs you made throughout the process

We’ll treat this interview as a collaborative session, similar to a project meeting where you’d gather input from key stakeholders. Be prepared to answer questions, justify your choices, and potentially adapt your solution based on feedback. This will give us insight into how you manage discussions and adjust plans in a dynamic team environment.

## Requirements: Address Validation Engine

### Context

When a consumer makes a transaction at a merchant using Paidy, we receive key information about the transaction via API, including the shipping address entered on the merchant’s checkout page. Ensuring the validity of this address is critical—not only for marketing and logistics but also to combat fraud. Fraudsters often exploit mailing addresses for payment fraud and account takeover opportunities.

As a Risk Technical Product Manager (TPM) at Paidy, your task is to design and implement an address validation system that can catch fraudulent transactions before they harm Paidy’s business. You will be using a third-party address validation vendor to accomplish this.

_Notes: The shipping address is entered on the merchant’s website and is communicated to Paidy via API [check the documentation here](https://paidy.com/docs/api/en/#1-introduction). Assume that we cannot change the UI of the merchant website to prompt the consumer to correct the address. Therefore, we will only implement further checks once Paidy receives the payment information._

### Guidance

1. **Vendor Selection**: Review the following list of potential vendors for integration. Select one vendor you would like to integrate with and explain your reasoning for this choice.
   - [Smarty](https://www.smarty.com/products/international-address-verification)
   - [Egon](https://www.egon.com/)
   - [Google Maps](https://developers.google.com/maps/documentation/address-validation/requests-validate-address)
   - [Postgrid](https://www.postgrid.com/address-verification/)

2. **Key Performance Indicators (KPIs)**: Identify the KPIs or metrics you would use to measure the performance of your new system and the business impact.

3. **Product Requirement Document (PRD)**: Write a PRD that outlines the requirements for this feature. Break down the implementation into actionable, small steps.

4. **Presentation Preparation**: In the next interview, you will present the feature and scope proposal to the Engineering Lead and other product managers during a project kick-off meeting. Choose a format that you believe will effectively communicate the user stories and how the feature will be implemented.

## F.A.Q.
[Please click here for the F.A.Q.](./README.md#faq)
