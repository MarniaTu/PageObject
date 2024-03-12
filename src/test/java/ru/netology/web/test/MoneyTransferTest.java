package ru.netology.web.test;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeOptions;
import ru.netology.web.data.DataHelper;
import ru.netology.web.page.LoginPage;

import java.util.HashMap;
import java.util.Map;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.netology.web.data.DataHelper.*;


class MoneyTransferTest {

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        Map<String, Object> prefs = new HashMap<String, Object>();

        prefs.put("credentials_enable_service", false);
        prefs.put("password_manager_enabled", false);

        options.setExperimentalOption("prefs", prefs);
        Configuration.browserCapabilities = options;
    }


    @Test
    void shouldTransferMoneyFromSecondToFirstCard() {
        open("http://localhost:9999");
        var loginPage = new LoginPage();
        var authInfo = DataHelper.getAuthInfo();
        var verificationPage = loginPage.validLogin(authInfo);
        var verificationCode = DataHelper.getVerificationCodeFor(authInfo);
        var dashboardPage = verificationPage.validVerify(verificationCode);
        var firstCardDetails = DataHelper.getFirstCardDetails();
        var secondCardDetails = DataHelper.getSecondCardDetails();
        var firstCardBalance = dashboardPage.getCardBalance(getMaskedCardNumber(firstCardDetails.getCardNumber()));
        var secondCardBalance = dashboardPage.getCardBalance(getMaskedCardNumber(secondCardDetails.getCardNumber()));

        var amount = generateValidTransferAmount(secondCardBalance);
        var expectedFirstCardBalance = firstCardBalance + amount;
        var expectedSecondCardBalance = secondCardBalance - amount;
        var transferPage = dashboardPage.selectCardToTransfer(firstCardDetails);
        dashboardPage = transferPage.makeValidTransfer(String.valueOf(amount), secondCardDetails);
        var actualBalanceFirstCard = dashboardPage.getCardBalance(getMaskedCardNumber(firstCardDetails.getCardNumber()));
        var actualBalanceSecondCard = dashboardPage.getCardBalance(getMaskedCardNumber(secondCardDetails.getCardNumber()));

        assertAll(() -> assertEquals(expectedFirstCardBalance, actualBalanceFirstCard),
                () -> assertEquals(expectedSecondCardBalance, actualBalanceSecondCard));
    }

    @Test
    void shouldGetErrorMessageIfInvalidTransferAmount() {
        open("http://localhost:9999");
        var loginPage = new LoginPage();
        var authInfo = DataHelper.getAuthInfo();
        var verificationPage = loginPage.validLogin(authInfo);
        var verificationCode = DataHelper.getVerificationCodeFor(authInfo);
        var dashboardPage = verificationPage.validVerify(verificationCode);
        var firstCardDetails = DataHelper.getFirstCardDetails();
        var secondCardDetails = DataHelper.getSecondCardDetails();
        var firstCardBalance = dashboardPage.getCardBalance(getMaskedCardNumber(firstCardDetails.getCardNumber()));
        var secondCardBalance = dashboardPage.getCardBalance(getMaskedCardNumber(secondCardDetails.getCardNumber()));

        var amount = generateInvalidTransferAmount(firstCardBalance);
        var transferPage = dashboardPage.selectCardToTransfer(secondCardDetails);
        transferPage.makeTransfer(String.valueOf(amount), firstCardDetails);
        transferPage.getErrorMessage("Ошибка! Невозможно перевести сумму, превышающую текущий баланс на выбранной карте.");
        dashboardPage = transferPage.makeValidTransfer(String.valueOf(amount), firstCardDetails);
        var actualBalanceFirstCard = dashboardPage.getCardBalance(getMaskedCardNumber(firstCardDetails.getCardNumber()));
        var actualBalanceSecondCard = dashboardPage.getCardBalance(getMaskedCardNumber(secondCardDetails.getCardNumber()));

        assertAll(() -> assertEquals(firstCardBalance, actualBalanceFirstCard),
                () -> assertEquals(secondCardBalance, actualBalanceSecondCard));
    }
}

