**BESTELL-PDF**

Man kann sich eine Liste seiner Bestellungen runterladen. Dazu muss man sich auf der SBB Webseite einloggen und findet dann unter seinem Profil den Menüpunkt Bestellungen.
Dort kann man sich dann auch ein PDF herunterladen.

**CSV-DATEI ERSTELLEN**

Mit diesem Programm kann man sich aus dem PDF eine CSV-Datei erstellen, die man dann in Excel importieren kann.

**WICHTIG: TICKET-TYPEN**

Um korrekt zu funktionieren, muss das Programm die in den Daten verwendeten Ticket-Typen kennen. 
Ticket-Typen sind z.B.
- Klassenwechsel Strecke
- Sparbillett
- Streckenbillett
- ZVV Einzelbillett

Das Programm erzeugt - wenn noch nicht vorhanden - eine Datei ./tickettypes.txt, mit den Ticekt-Typen, wie sie bei mir vorkamen. Darum ist es wichtig, das Programm immer mit dem gleichen Arbeitsverzeichnis laufenzulassen.
Erkennt das Programm gewisse Tickettypen nicht, so werden entsprechende Fehlermeldungen ausgegeben, und die fehlenden Tickettypen können in der Datei eingetragen werden.

**MEHRERE REISENDE**

Wurde ein Kauf für mehrere Reisende getätigt, so wird für jede(n) Reisende(n) eine eigene Zeile ausgegeben und der Gesamtpreis wird durch die Anzahl Reisenden dividiert. Dies funktioniert nur korrekt, wenn alle Reisenden ein Halbtax haben oder keines haben. Dies kann im Moment nur von Hand im CSV korrigiert werden.

**JAR FILE AUSFÜHREN**

Ein Jar File mit dependencies befindet sich im jar Folder. Das Programm benötigt einen Parameter, das SBB PDF. Der Output wird auf stdout ausgegeben.

**MAVEN**

Man kann das Programm auch selber bilden.
_mvn clean compile assembly:single_
