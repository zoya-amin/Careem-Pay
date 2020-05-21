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
    When user wait for transaction status "success"
    And user balance is current balance plus <topUpAmount>
    Examples:
      | topUpAmount | Currency | cardNum                | topUpAmountInFraction |
      | 50          | AED      | VISA ****-0008 10/2020 | 0.5                   |
      | 100         | AED      | VISA ****-0008 10/2020 | 1.0                   |
      | 200         | AED      | VISA ****-0008 10/2020 | 2.0                   |

  Scenario Outline: Add other acceptable credit amount
    Given user is authenticated
    And user check the current balance
    And user select card "<cardNum>"
    When user choose amount <topUpAmount> to top up via card
    Then user verify status code 201 in response
    When user wait for transaction status "success"
    Then user balance is current balance plus <topUpAmount>
    Examples:
      | topUpAmount | Currency | cardNum                |
      | 1           | AED      | VISA ****-0008 10/2020 |
      | 4           | AED      | VISA ****-0008 10/2020 |
      | 5           | AED      | VISA ****-0008 10/2020 |
      | 10          | AED      | VISA ****-0008 10/2020 |
      | 25          | AED      | VISA ****-0008 10/2020 |
      | 49          | AED      | VISA ****-0008 10/2020 |
      | 51          | AED      | VISA ****-0008 10/2020 |
      | 75          | AED      | VISA ****-0008 10/2020 |
      | 99          | AED      | VISA ****-0008 10/2020 |
      | 101         | AED      | VISA ****-0008 10/2020 |
      | 150         | AED      | VISA ****-0008 10/2020 |
      | 175         | AED      | VISA ****-0008 10/2020 |
      | 199         | AED      | VISA ****-0008 10/2020 |
      | 201         | AED      | VISA ****-0008 10/2020 |
      | 225         | AED      | VISA ****-0008 10/2020 |
      | 250         | AED      | VISA ****-0008 10/2020 |
      | 300         | AED      | VISA ****-0008 10/2020 |
      | 350         | AED      | VISA ****-0008 10/2020 |
      | 375         | AED      | VISA ****-0008 10/2020 |
      | 400         | AED      | VISA ****-0008 10/2020 |
      | 450         | AED      | VISA ****-0008 10/2020 |
      | 475         | AED      | VISA ****-0008 10/2020 |
      | 499         | AED      | VISA ****-0008 10/2020 |
      | 500         | AED      | VISA ****-0008 10/2020 |


  Scenario Outline: Add un acceptable credit amount
    Given user is authenticated
    And user check the current balance
    And user select card "<cardNum>"
    When user choose amount <topUpAmount> to top up via card
    Then user verify status code 400 in response
    And user verify error message in response is "<errorMessage>"
    And user balance is current balance plus 0
    Examples:
      | topUpAmount | Currency | cardNum                | errorMessage                |
      | 0           | AED      | VISA ****-0008 10/2020 | Bad request body            |
      | 501         | AED      | VISA ****-0008 10/2020 | Transaction above the limit |
      | 1000        | AED      | VISA ****-0008 10/2020 | Transaction above the limit |
      | 2000        | AED      | VISA ****-0008 10/2020 | Transaction above the limit |
      | 5000        | AED      | VISA ****-0008 10/2020 | Transaction above the limit |
      | -1          | AED      | VISA ****-0008 10/2020 | Bad request body            |

