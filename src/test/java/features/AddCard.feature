Feature: Careem pay: Add card
  As a QA
  I want to add card
  So that i can verify that the card is added successfully and can be use for transactions
  Scenario: Add card
    Given user is authenticated
    When I add the card
    Then the card is added successfully