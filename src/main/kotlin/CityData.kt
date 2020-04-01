data class CityData(
    val siteId: Int,
    val name: String,
    val color: Int,
    val population: Int,
    val siteX: Double,
    val siteY: Double,
    val polygon: PolygonData,
    val neighbours: Set<Int>
)
