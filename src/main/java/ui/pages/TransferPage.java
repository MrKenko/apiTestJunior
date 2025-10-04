package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class TransferPage extends BasePage<TransferPage>{
    private final SelenideElement accountDropdown = $(Selectors.byText("-- Choose an account --"));
    private final SelenideElement inputSenderAccountNumber = $$(".form-group input").get(0);
    private final SelenideElement inputRecipientAccountNumber = $$(".form-group input").get(1);
    private final SelenideElement inputAmount = $$(".form-group input").get(2);
    private final SelenideElement checkbox = $(Selectors.byAttribute("id", "confirmCheck"));
    private final SelenideElement button = $(Selectors.byText("🚀 Send Transfer"));

    @Override
    public String url() {
        return "/transfer";
    }
    public TransferPage selectAccount(String accountNumber) {
        accountDropdown.click();
        $(Selectors.byText(accountNumber)).click();
        return this;
    }

    public TransferPage transfer(String senderAccountNumber, String recipientAccountNumber, double amount){
        inputSenderAccountNumber.setValue(senderAccountNumber);
        inputRecipientAccountNumber.setValue(recipientAccountNumber);
        inputAmount.setValue(String.valueOf(amount));
        checkbox.click();
        button.click();
        return this;
    }


}
