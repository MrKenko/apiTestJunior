package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class DepositPage extends BasePage<DepositPage>{
    private final SelenideElement accountDropdown = $(Selectors.byText("-- Choose an account --"));
    private final SelenideElement amountInput = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private final SelenideElement depositButton = $(Selectors.byText("\uD83D\uDCB5 Deposit"));


    @Override
    public String url() {
        return "/deposit";
    }

    // выбрать аккаунт
    public DepositPage selectAccount(String accountNumber) {
        accountDropdown.click();
        $(Selectors.byText(accountNumber)).shouldBe(Condition.visible).click();
        return this;
    }

    // ввести сумму и нажать Deposit
    public DepositPage enterAmountAndConfirmDeposit(double amount) {
        amountInput.setValue(String.valueOf(amount));
        depositButton.click();
        return this;
    }


}
