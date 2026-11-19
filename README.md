# Автотесты формы заказа дебетовой карты

Учебный проект Нетологии по веб-тестированию. В одном репозитории находятся тесты успешной отправки заявки и тесты валидации.

## Что проверяют тесты

- Успешную отправку заявки с корректными ФИО, телефоном и отмеченным согласием.
- Допустимые русские буквы, включая «ё», пробелы и дефис в ФИО.
- Обязательность полей и согласия, отклонение латиницы и цифр в ФИО.
- Формат номера `+XXXXXXXXXXX`, включая длину, положение плюса и наличие посторонних символов.
- Порядок сообщений об ошибках, когда одновременно заполнены неверно несколько полей.

## Локальный запуск

Нужны JDK 11 и установленный Google Chrome. WebDriverManager подбирает ChromeDriver, Gradle Wrapper загружает Gradle. Тесты запускают браузер в headless-режиме.

В первом терминале из корня проекта запустите учебный сервис.

```shell
java -jar ./artifacts/app-order.jar
```

Во втором терминале запустите тесты.

```shell
./gradlew test
```

В Windows используйте `gradlew.bat test` из Command Prompt или `.\gradlew.bat test` из PowerShell. Отчёт открывается в `build/reports/tests/test/index.html`.

Для запуска на другом порту укажите его обоим процессам, например `java -jar ./artifacts/app-order.jar -port=7777` и `./gradlew test -Dsut.url=http://localhost:7777`.

## GitHub Actions

Сборка `Gradle Selenium` запускается на push и pull request. Результаты тестов видны в журнале шага `Run Selenium tests`.

[![Gradle Selenium](https://github.com/banifaci9/aqa-web-card-order/actions/workflows/gradle.yml/badge.svg)](https://github.com/banifaci9/aqa-web-card-order/actions/workflows/gradle.yml)

## Дефекты учебного приложения

Два теста фиксируют подтверждённые отклонения от условий задания. Из-за них полный прогон завершается с ошибкой.

- [Форма отклоняет ФИО с русской буквой «ё»](docs/issues/name-with-yo.md).
- [Форма принимает телефон с пробелом перед знаком «+»](docs/issues/phone-leading-space.md).
