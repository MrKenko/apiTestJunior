package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import common.utils.RetryUtils;

import java.util.Objects;

import static com.codeborne.selenide.Selenide.$;

public class UpdateNamePage extends BasePage<UpdateNamePage>{
    private final SelenideElement inputNewName = $(Selectors.byAttribute("placeholder", "Enter new name"));
    private final SelenideElement saveButton = $(Selectors.byText("\uD83D\uDCBE Save Changes"));

    @Override
    public String url() {
        return "/edit-profile";
    }

    public UpdateNamePage updateName(String newName){
        RetryUtils.retry(
                () -> {
                    inputNewName.setValue(newName);
                    return Objects.requireNonNull(inputNewName.getValue());
                },
                newName::equals,   // проверяем, что введённое значение отобразилось
                3,
                1000
        );

        saveButton.click(); // кнопка вне ретрая

        return this;
    }
}
