# Detail map

The detail screen uses the existing earthquake feed and does not request the USGS detail endpoint. Its embedded Google Map loads map tiles separately.

To enable the map, configure a Google Maps Platform key with Maps SDK for Android enabled. Add the following line, without quotes, to `local.properties` in the project root (beside `gradlew`), preserving the existing `sdk.dir` setting:

```properties
MAPS_API_KEY=your_key
```

Sync Gradle and rebuild the app. `local.properties` is excluded from Git; each developer configures their own local file. The map key is read only from this file, not from user-level `gradle.properties`. If you previously configured it there, copy it into the project's `local.properties`.

Restrict the key to Maps SDK for Android, the Android application ID `com.sartori.brick`, and the signing certificate used to run the app. A reviewer compiling with a different certificate must use their own configured key or have their certificate SHA-1 authorized for the shared key. Do not commit the key to the repository.

Without a key, the app displays a map-unavailable placeholder and keeps the earthquake details accessible. Offline map tiles are not guaranteed; earthquake details remain available independently of map loading.

`EarthquakeNavHost` uses Navigation Compose with typed list and detail routes. The detail route carries only the event ID; both destinations share the activity-scoped list ViewModel. Navigation Compose manages the back stack and saved screen state, including the list scroll position. After process recreation, the ViewModel reloads the feed using its existing network/cache fallback. If the event has left the daily feed, the detail screen offers retry and back.

This embedded map does not yet implement the assignment's full map of all events or context relative to the user's location.
