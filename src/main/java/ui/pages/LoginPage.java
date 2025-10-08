package ui.pages;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class LoginPage extends BasePage <LoginPage>{

   private SelenideElement button = $("button");


    @Override
    public String url() {
        return "/login";
    }

    public LoginPage login(String username, String password){
        userNameInput.sendKeys(username);
        userPasswordInput.sendKeys(password);
        button.click();
        return this;
    }
}
