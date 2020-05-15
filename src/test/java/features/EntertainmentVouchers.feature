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
      | 100 AED |
      | 200 AED |
      | 400 AED |

  @negative
  Scenario: Get entertainment voucher products with invalid authentication token
    Given user get all vouchers with invalid authentication token
    Then user verify status code 401 in response with message "token signature does not match claims"

  @negative
  Scenario: Get entertainment voucher products with expired authentication token
    Given user get all vouchers with expired authentication token
    Then user verify status code 401 in response with message "token signature does not match claims"

#    Not a valid testcase for QA env. As every promo is a valid promo code in qa env
#  @negative
#  Scenario Outline:  Generate invoice - with invalid promo code
#    When user select a "<voucher>" to generate invoice using the promo "<promoCode>"
#    Then user verify status code 400 in response with message "<message>"
#    Examples:
#      | test case          | promoCode | voucher | message            |
#      | invalid promo code | 12345     | 50      | invalid promo code |
#      | expired promo code | 123abc    | 40      | expired promo code |


  Scenario: Generate invoice - with additional fields in payload
    When user hit generate invoice API by passing an additional field in payload
      | payload                                  |
      | {"promoCode":"","additionalField":"abc"} |
    Then user verify status code 200 in response


  Scenario: Generate invoice - Invoice of a voucher that does not exist/invalid skucode
    When user generate invoice with invalid skucode "Z9AEAE24216"
    Then user verify status code 500 in response with message "Something Went Wrong"

