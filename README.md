# StockApp

StockApp to aplikacja mobilna na Androida do obserwowania spółek giełdowych. Użytkownik może dodać spółkę po tickerze, np. `AAPL`, a aplikacja pobiera jej aktualne dane z API, zapisuje ją lokalnie i pokazuje podstawowe informacje oraz szczegóły.

## Funkcje

- dodawanie spółki po tickerze,
- wyświetlanie listy obserwowanych spółek,
- pobieranie danych z REST API przez Retrofit,
- lokalne zapisywanie spółek w bazie Room,
- usuwanie spółek z listy,
- ręczne odświeżanie cen,
- automatyczne odświeżanie cen,
- ekran szczegółów spółki,
- prosty wykres porównujący ceny: open, high, low, previous close i current.

## Technologie

- Kotlin
- Android
- Jetpack Compose
- MVVM
- ViewModel
- Navigation Compose
- Retrofit
- Room
- Coil

## Architektura

Projekt jest zorganizowany zgodnie z prostą strukturą MVVM:

ui
  ekrany aplikacji i komponenty Compose

viewmodel
  logika ekranów i stan UI

data/api
  komunikacja z API Finnhub przez Retrofit

data/local
  lokalna baza danych Room

data/repository
  warstwa pośrednia między ViewModelami, API i bazą danych

model
  modele danych używane w aplikacji
Jak działa aplikacja
Po wpisaniu tickera aplikacja pobiera dane spółki z Finnhub API. Następnie zapisuje spółkę w lokalnej bazie Room i pokazuje ją na liście. Użytkownik może odświeżyć ceny, usunąć spółkę albo wejść w ekran szczegółów, gdzie widoczne są dodatkowe informacje.

Uruchomienie projektu
Sklonuj repozytorium:
git clone https://github.com/Nexon228/StockApp.git
Otwórz projekt w Android Studio.

Wykonaj synchronizację Gradle:

File > Sync Project with Gradle Files
Uruchom aplikację na emulatorze lub fizycznym urządzeniu.
API
Aplikacja korzysta z API Finnhub:

https://finnhub.io
Dane pobierane są przez Retrofit z endpointów profilu spółki i aktualnej wyceny.

Projekt wykonany jako aplikacja zaliczeniowa na Androida.

## Autor

Alan Woroch
