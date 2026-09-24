# Brick

## Google Maps setup

Request the Google Maps API key from Mauricio. You do not need to create your own key for this evaluation.

Add the provided key to `local.properties` in the project root, next to `gradlew`, without changing the existing `sdk.dir` setting:

```properties
MAPS_API_KEY=KEY_PROVIDED_BY_MAURICIO
```

Replace the placeholder with the provided key, without quotes. Sync the project with Gradle in Android Studio, then build and run the app.

`local.properties` is excluded from Git. Do not commit the API key. Without a configured key, earthquake data remains accessible, but the map displays an unavailable message.
