# DFA Visualizer

Interaktywny, krok-po-kroku wizualizator działania deterministycznego automatu skończonego (DFA), akceptujący słowa zawierające dokładnie dwa wystąpienia "11", napisany w Javie (Swing, Java2D). Pozwala wprowadzić słowo binarne i przechodzić krok po kroku przez kolejne przejścia automatu, widząc na żywo, który stan jest aktywny.

> **Uwaga dot. zakresu:** to jest demo skonkretnego, zahardkodowanego automatu - **nie** generyczny wizualizator wczytujący dowolne DFA z pliku. Definicja stanów i przejść (w tym sam rysunek grafu) jest zapisana na sztywno w kodzie pod ten jeden konkretny automat. Świadoma decyzja: projekt miał demonstrować wizualizację działania DFA, a nie być uniwersalnym narzędziem.

![dfa-visualizer](docs/screenshot.png)

## Automat

Automat akceptuje słowa binarne (alfabet `{0, 1}`) zawierające **dokładnie dwa wystąpienia** podciągu `"11"`.

- 7 stanów: `q0`–`q6`
- Stany akceptujące: `q4`, `q5`
- `q6` to stan pułapka (trzecie wystąpienie `"11"` → odrzucenie, bez powrotu)

## Funkcje

- Wprowadzanie dowolnego słowa binarnego (walidacja wejścia — akceptuje tylko `0` i `1`)
- Krok po kroku: przyciski **Poprzedni / Następny**, podświetlenie aktualnego stanu
- Kolorowe oznaczenie wyniku po przetworzeniu całego słowa (zaakceptowane / odrzucone)
- Zakrzywione strzałki przejść (krzywe Béziera) i pętle własne dla przejść do tego samego stanu
- Wizualne rozróżnienie stanów akceptujących (podwójny okrąg)

## Uruchomienie

Wymaga JDK (testowane na Java 21).

```bash
javac *.java
java DFAVisualizer
```

## Stack

- Java
- Swing (GUI)
- Java2D (`Graphics2D`, `QuadCurve2D` — rysowanie grafu automatu)

## Możliwy dalszy rozwój

Naturalnym następnym krokiem byłoby uogólnienie wizualizatora: wczytywanie definicji dowolnego automatu z pliku (JSON/tekst - stany, przejścia, stany akceptujące) zamiast hardkodowania, wraz z automatycznym rozmieszczaniem stanów na płótnie.