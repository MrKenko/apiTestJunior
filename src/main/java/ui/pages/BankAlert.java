package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS("Username must be between 3 and 15 characters"),
    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number: "),
    SUCCESS_DEPOSIT("✅ Successfully deposited $%s to account %s!"),
    INVALID_AMOUNT("❌ Please enter a valid amount."),
    NOT_SELECT_ACCOUNT("❌ Please select an account."),
    SUCCESS_TRANSFER("✅ Successfully transferred $%s to account %s!"),
    FILL_ALL_FIELDS("❌ Please fill all fields and confirm."),
    NO_USER_FOUND("❌ No user found with this account number."),
    SUCCESS_UPDATE_NAME("✅ Name updated successfully!"),
    ENTER_A_VALID_NAME("❌ Please enter a valid name.");

    private final String message;

    BankAlert(String message) {
        this.message = message;
    }

    public String format(Object... args) {
        return String.format(message, args);
    }

}