package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class UpdateNamePage extends BasePage<UpdateNamePage>{
    private final SelenideElement inputNewName = $(Selectors.byAttribute("placeholder", "Enter new name"));
    private final SelenideElement saveButton = $(Selectors.byText("\uD83D\uDCBE Save Changes"));

    @Override
    public String url() {
        return "/edit-profile";
    }

    public UpdateNamePage updateName(String newName){
        inputNewName.setValue(newName);
        saveButton.click();
        return this;
    }
}
