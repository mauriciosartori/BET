package com.sartori.brick.feature.earthquakemap

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.sartori.brick.data.earthquake.Earthquake

// Keep the event identity: different earthquakes can share the same coordinates.
internal data class EarthquakeClusterItem(val earthquake: Earthquake) : ClusterItem {
    private val coordinates = LatLng(
        requireNotNull(earthquake.latitude),
        requireNotNull(earthquake.longitude)
    )

    override fun getPosition(): LatLng = coordinates
    override fun getTitle(): String? = earthquake.place
    override fun getSnippet(): String? = null
    override fun getZIndex(): Float = 0f
}
