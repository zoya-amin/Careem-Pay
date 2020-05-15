Feature: Careem pay: Add card
  As a QA
  I want to see all transactions history
  So that i can verify that the transactions history is maintained

  Scenario: Get transaction history
    Given user is authenticated
    When user get transactions history
    Then user verify status code 200 in response
    And user verify that transaction currency is "AED"

  Scenario Outline: user purchase voucher
    When user get all vouchers
    And user select a voucher to generate invoice
      | voucherAmount |
      | <Voucher>     |
    And user purchase the voucher
    Then user verify that the voucher is purchased successfully
    Examples:
      | Voucher |
      | 50 AED  |

  Scenario: latest transaction is added in the history
    Given user wait for transaction list to be updated with "50.0"
    When user get transactions history
    Then user verify status code 200 in response
    And user verify that transaction currency is "AED"
    And user verify latest transaction in transaction history is "50.0"

  Scenario: Get transaction history from un authenticated user
    When user get transactions history with unauthenticated user
    Then user verify status code 401 in response