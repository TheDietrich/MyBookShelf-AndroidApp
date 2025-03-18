# MyBookSheelf

MyBookSheelf ist eine Android‑App, die in Kotlin mit Jetpack Compose entwickelt wurde. Die App ermöglicht es dir, deine Manga‑Sammlung übersichtlich zu verwalten, deinen Lesefortschritt zu tracken und Sonderreihen (z. B. Light Novels oder Spin-offs) zu organisieren. Zusätzlich bietet MyBookSheelf umfangreiche Backup‑ und Restore‑Funktionen, um deine Daten auch bei Updates zu sichern und wiederherzustellen.

## Features

- **Manga‑Verwaltung:**  
  Füge neue Manga hinzu, bearbeite bestehende Einträge und lösche sie bei Bedarf.
- **Lesefortschritt:**  
  Erfasse den aktuell gelesenen Band und die Anzahl der gekauften Bände für jeden Manga.
- **Sonderreihen:**  
  Organisiere zusätzliche Sonderreihen (z. B. Light Novels) zu einem Hauptmanga. Jede Sonderreihe hat ihren eigenen Fortschritt, der separat verwaltet werden kann.
- **Veröffentlichungsstatus:**  
  Setze den Status eines Manga auf "abgeschlossen" oder "laufend". Bei laufenden Serien kannst du ein Veröffentlichungsdatum eintragen oder angeben, dass das Datum unbekannt ist. Der Status wird übersichtlich in der Detailansicht und auf der Startseite angezeigt.
- **Backup & Restore:**  
  Exportiere deine Manga‑Daten (inklusive Sonderreihen und Veröffentlichungsstatus) als JSON‑Datei und importiere Backups – unterstützt sowohl alte als auch neue Formate.
- **Dark Mode:**  
  Umschaltbare Farbschemata (Dark/Light) sorgen für ein angenehmes Nutzererlebnis.
- **Moderne Benutzeroberfläche:**  
  Entwickelt mit Jetpack Compose für ein modernes, responsives und intuitives Design.

## Screenshots

_Füge hier Screenshots der wichtigsten Bildschirme ein, z. B.:_

- Startseite
- Manga‑Details (inklusive Sonderreihen und Veröffentlichungsstatus‑Dialog)
- Backup‑/Restore‑Ansicht
- Einstellungen (Dark Mode)

## Installation

### Voraussetzungen

- **Android Studio:** Empfohlen wird Android Studio 202x oder neuer.
- **Android SDK:** Minimum API Level 21 (Lollipop) oder höher.
- **Kotlin:** Die App wurde in Kotlin entwickelt – stelle sicher, dass Kotlin‑Support aktiviert ist.
- **Gradle:** Nutze die in der Projekt‑Konfiguration angegebene Gradle‑Version.

### Projekt einrichten

1. **Repository klonen:**  
   Öffne dein Terminal und führe den folgenden Befehl aus:

   git clone https://github.com/TheDietrich/MyBookSheelf.git

2. **Projekt in Android Studio öffnen:**  
   Öffne Android Studio, wähle "Open an Existing Project" und navigiere zu dem geklonten Repository. Lasse Gradle synchronisieren, um alle Abhängigkeiten herunterzuladen.

3. **Projekt bauen und ausführen:**  
   Wähle im Menü **Build > Make Project** (oder den entsprechenden Shortcut) und starte die App über **Run** auf einem Emulator oder einem physischen Gerät.

### Datenbankmigration

Die App verwendet Room als lokale Datenbank. Momentan wird `fallbackToDestructiveMigration()` verwendet, um bei Schemaänderungen alle Daten zu löschen. Für die Produktion solltest du eine Migrationsstrategie implementieren – siehe [Room-Migrationsdokumentation](https://developer.android.com/training/data-storage/room/migrating-db-versions).

## Nutzung

### Manga hinzufügen

- Tippe auf den Floating Action Button (Plus‑Symbol) auf der Startseite.
- Gib den Titel, den aktuell gelesenen Band, die Anzahl der gekauften Bände und ein Cover ein.
- Speichere den Eintrag.

### Manga‑Details

- Tippe auf einen Manga in der Liste, um die Detailansicht zu öffnen.
- Bearbeite den Lesefortschritt, ändere das Cover oder setze den Veröffentlichungsstatus:
    - Mit dem Button **"Veröffentlichungsstatus"** öffnet sich ein Dialog, in dem du auswählen kannst, ob der Manga abgeschlossen ist oder noch läuft.
    - Bei laufenden Serien kannst du ein Veröffentlichungsdatum eintragen oder angeben, dass das Datum unbekannt ist.
- Sonderreihen (z. B. Light Novels) können hinzugefügt, bearbeitet oder gelöscht werden.

### Backup und Restore

- **Backup erstellen:**  
  In den Einstellungen kannst du alle Daten als JSON‑Datei exportieren.
- **Backup importieren:**  
  Importiere ein zuvor erstelltes Backup. Der Import unterstützt sowohl das neue Format (mit zusätzlichen Feldern) als auch alte Backups, bei denen diese Felder fehlen. In diesem Fall werden die neuen Felder mit Standard‑Werten übernommen.

### Dark Mode

- In den Einstellungen kannst du zwischen Dark Mode und Light Mode über einen Schalter wechseln.

## Updates und Migration

Bevor du ein Update installierst, erstelle ein Backup deiner Daten. Nach der Aktualisierung kannst du dein altes Backup importieren. Der Importcode unterscheidet zwischen alten und neuen Formaten:

- **Neues Format:**  
  Enthält zusätzliche Felder (`isCompleted`, `nextVolumeDate`, `specialSeries`).
- **Altes Format:**  
  Besteht aus einem JSON‑Array von `MangaExportDto`; hier werden die neuen Felder mit Standard‑Werten übernommen.


## Contributing

Beiträge sind willkommen! So kannst du mitmachen:

1. Forke das Repository.
2. Erstelle einen neuen Branch für deine Änderungen:

   git checkout -b feature/dein-feature

3. Committe deine Änderungen.
4. Push den Branch und eröffne einen Pull Request.
5. Für Bug Reports oder Feature Requests, öffne bitte ein Issue auf GitHub.

## Lizenz

Dieses Projekt steht unter der MIT‑Lizenz – siehe [LICENSE](LICENSE) für Details.

## Support und Acknowledgements

- **Jetpack Compose:** [Compose Documentation](https://developer.android.com/jetpack/compose)
- **Room:** [Room Documentation](https://developer.android.com/training/data-storage/room)
- **Coil:** [Coil Documentation](https://coil-kt.github.io/coil/)
- **DataStore:** [DataStore Documentation](https://developer.android.com/topic/libraries/architecture/datastore)

Vielen Dank an alle, die zu diesem Projekt beigetragen haben!
