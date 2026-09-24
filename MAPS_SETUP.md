# Detail map

## Marker clustering

The full-screen map uses `maps-compose-utils` at the same version as `maps-compose`. Nearby earthquakes are grouped according to zoom using the library's default renderer. Tapping a group centers the camera there and zooms in two levels; tapping an individual earthquake opens its existing detail screen. The initial camera remains centered on the contiguous United States.

Clustering is a presentation change: it uses the same loaded feed, preserves event IDs, and does not trigger a USGS request. The Compose clustering API requires a local `MapsComposeExperimentalApi` opt-in. Events at identical coordinates can remain overlapping or grouped even at maximum zoom; a group event picker is a possible future enhancement.

## Map configuration

The detail screen uses the existing earthquake feed and does not request the USGS detail endpoint. Its embedded Google Map loads map tiles separately.

To enable the map, configure a Google Maps Platform key with Maps SDK for Android enabled. Add the following line, without quotes, to `local.properties` in the project root (beside `gradlew`), preserving the existing `sdk.dir` setting:

```properties
MAPS_API_KEY=your_key
```

Sync Gradle and rebuild the app. `local.properties` is excluded from Git; each developer configures their own local file. The map key is read only from this file, not from user-level `gradle.properties`. If you previously configured it there, copy it into the project's `local.properties`.

Restrict the key to Maps SDK for Android, the Android application ID `com.sartori.brick`, and the signing certificate used to run the app. A reviewer compiling with a different certificate must use their own configured key or have their certificate SHA-1 authorized for the shared key. Do not commit the key to the repository.

Without a key, the app displays a map-unavailable placeholder and keeps the earthquake details accessible. Offline map tiles are not guaranteed; earthquake details remain available independently of map loading.

`EarthquakeNavHost` uses Navigation Compose with typed list, map, and detail routes. The detail route carries only the event ID; all destinations share the activity-scoped list ViewModel. Navigation Compose manages the back stack and saved screen state, including the list scroll position. After process recreation, the ViewModel reloads the feed using its existing network/cache fallback. If the event has left the daily feed, the detail screen offers retry and back.

The list toolbar opens a full-screen map of the same feed. Tap a marker to open its details; Back returns to the map. Events without valid coordinates remain in the list and are omitted from the map with a notice. The initial camera is centered on the contiguous United States; worldwide markers remain available by panning or zooming. Returning from details preserves the camera position. The map does not request device location and remains usable without that permission. Context relative to the user's current location is still pending.
