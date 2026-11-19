package ru.netology;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardOrderTest {
    private static final String VALID_NAME = "Иванова Анна";
    private static final String VALID_PHONE = "+79991234567";
    private static final String REQUIRED = "Поле обязательно для заполнения";
    private static final String INVALID_NAME = "Допустимы только русские буквы, пробелы и дефисы.";
    private static final String SUCCESS_MESSAGE =
            "Ваша заявка успешно отправлена! Наш менеджер свяжется с вами в ближайшее время.";
    private static final By NAME = By.cssSelector("[data-test-id='name'] input");
    private static final By PHONE = By.cssSelector("[data-test-id='phone'] input");
    private static final By AGREEMENT = By.cssSelector("[data-test-id='agreement'] .checkbox__text");
    private static final By SUBMIT = By.cssSelector("form button");
    private static final By SUCCESS = By.cssSelector("[data-test-id='order-success']");
    private static final By INVALID_FIELDS = By.cssSelector("[data-test-id].input_invalid");

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    static void setupDriver() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void openForm() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-dev-shm-usage", "--no-sandbox");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        driver.get(System.getProperty("sut.url"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(NAME));
    }

    @AfterEach
    void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {VALID_NAME, "Петрова-Сидорова Анна"})
    void submitsValidOrder(String name) {
        submit(name, VALID_PHONE, true);
        assertSuccess();
    }

    @Test
    void acceptsRussianLetterYo() {
        submit("Семёнова Алёна", VALID_PHONE, true);
        assertSuccess();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Ivan Petrov", "Иванова Анна2"})
    void rejectsInvalidName(String name) {
        submit(name, VALID_PHONE, true);
        assertFieldError("name", INVALID_NAME);
    }

    @ParameterizedTest
    @ValueSource(strings = {"79991234567", "+7999123456", "+799912345678", "+79991A34567"})
    void rejectsInvalidPhone(String phone) {
        submit(VALID_NAME, phone, true);
        assertFieldError("phone", "11 цифр");
    }

    @Test
    void rejectsPhoneWithLeadingSpace() {
        submit(VALID_NAME, " " + VALID_PHONE, true);
        assertFieldError("phone", "11 цифр");
    }

    @Test
    void rejectsEmptyName() {
        submit("", VALID_PHONE, true);
        assertFieldError("name", REQUIRED);
    }

    @Test
    void rejectsEmptyPhone() {
        submit(VALID_NAME, "", true);
        assertFieldError("phone", REQUIRED);
    }

    @Test
    void highlightsUncheckedAgreement() {
        submit(VALID_NAME, VALID_PHONE, false);
        assertOnlyInvalidField("agreement");
    }

    @Test
    void highlightsOnlyFirstInvalidField() {
        submit("Ivan Petrov", "", false);
        assertFieldError("name", INVALID_NAME);

        driver.findElement(NAME).sendKeys(Keys.chord(Keys.CONTROL, "a"), VALID_NAME);
        driver.findElement(SUBMIT).click();
        assertFieldError("phone", REQUIRED);

        driver.findElement(PHONE).sendKeys(VALID_PHONE);
        driver.findElement(SUBMIT).click();
        assertOnlyInvalidField("agreement");
    }

    private void submit(String name, String phone, boolean agreement) {
        driver.findElement(NAME).sendKeys(name);
        driver.findElement(PHONE).sendKeys(phone);
        if (agreement) {
            driver.findElement(AGREEMENT).click();
        }
        driver.findElement(SUBMIT).click();
    }

    private void assertSuccess() {
        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(SUCCESS));
        assertEquals(SUCCESS_MESSAGE, success.getText().strip());
    }

    private void assertFieldError(String field, String expectedMessage) {
        assertOnlyInvalidField(field);
        By errorMessage = By.cssSelector("[data-test-id='" + field + "'].input_invalid .input__sub");
        String actualMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).getText();
        assertTrue(actualMessage.contains(expectedMessage), () -> "Сообщение формы: " + actualMessage);
    }

    private void assertOnlyInvalidField(String field) {
        assertTrue(driver.findElements(SUCCESS).isEmpty(),
                "Форма приняла заявку с ошибкой в поле " + field);
        WebElement invalid = wait.until(ExpectedConditions.visibilityOfElementLocated(INVALID_FIELDS));
        assertEquals(1, driver.findElements(INVALID_FIELDS).size());
        assertEquals(field, invalid.getDomAttribute("data-test-id"));
    }
}
