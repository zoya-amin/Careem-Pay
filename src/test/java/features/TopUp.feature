Feature: Careem pay: Add credit using top up
  As a QA
  I want to add credit in my account using card top up
  So that i can use the credit later

  Scenario Outline: Add credit via card
    Given user is authenticated
    And user check the current balance
    And user select card "<cardNum>"
    When user choose amount <topUpAmount> to top up via card
    Then user verify status code 201 in response
    And user wait for transaction list to be updated with "<topUpAmountInFraction>"
    And user balance is current balance plus <topUpAmount>
    Examples:
      | topUpAmount | Currency | cardNum                | topUpAmountInFraction |
      | 50          | AED      | VISA ****-4305 10/2020 | 0.5                     |
      | 100         | AED      | VISA ****-4305 10/2020 | 1.0                     |
      | 200         | AED      | VISA ****-4305 10/2020 | 2.0                     |



