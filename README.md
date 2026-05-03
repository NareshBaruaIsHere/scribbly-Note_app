# Scribbly..

Scribbly is an Android notes app built with Jetpack Compose, Room, and DataStore.

## Features

- Create, edit, archive, and delete notes
- Search and sort notes
- Offline-first storage with Room
- Theme and preference settings with DataStore
- JSON backup and restore

## Tech Stack

- Kotlin
- Jetpack Compose
- Room
- DataStore Preferences
- Navigation Compose

## Run

```zsh
./gradlew :app:assembleDebug
```

```zsh
./gradlew :app:testDebugUnitTest
```

## Notes

The app uses a custom neumorphic Compose theme and a manual dependency injection container.
