Feature: Entertainment Voucher
  As a QA
  I want to test Entertainment voucher feature
  So that i can verify that the user can purchase a gift card using careem pay and use that on the stream account to make a purchase

  Scenario Outline: Verify that the entertainment voucher is purchased successfully - Happy path
    Given user is authenticated
    And user check the current balance
    When user get all vouchers
    And user select a voucher to generate invoice
      | voucherAmount |
      | <Voucher>     |
    And user purchase the voucher
    Then user verify that the voucher is purchased successfully
    And user CPAY account is deducted with voucher amount
    Examples:
      | Voucher |
      | 40 AED  |
      | 50 AED  |
      | 75 AED  |
      | 100 AED  |
      | 200 AED  |
      | 400 AED  |