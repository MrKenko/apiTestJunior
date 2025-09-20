package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;
@Getter
public class UserDashboard extends BasePage<UserDashboard> {
    private final SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    private final SelenideElement createNewAccount = $(Selectors.byText("➕ Create New Account"));
    private final SelenideElement depositMoneyButton = $(Selectors.byText("\uD83D\uDCB0 Deposit Money"));
    private final SelenideElement makeATransfer = $(Selectors.byText("\uD83D\uDD04 Make a Transfer"));
    private final SelenideElement userNameButton = $(By.cssSelector(".profile-header .user-name"));
    private final SelenideElement profileName = $(".user-name");
    private final SelenideElement welcomeName = $(".welcome-text span");

    @Override
    public String url() {
        return "/dashboard";
    }

    public UserDashboard createNewAccount(){
        createNewAccount.click();
        return this;
    }

    public UserDashboard depositMoneyButton(){
        depositMoneyButton.click();
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(Condition.visible);
        return this;
    }

    public UserDashboard makeATransfer(){
        makeATransfer.click();
        return this;
    }

    public UserDashboard userNameButton(){
        userNameButton.click();
        $(Selectors.byText("✏\uFE0F Edit Profile")).shouldBe(Condition.visible);
        return this;
    }
}