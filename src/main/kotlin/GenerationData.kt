import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import java.io.File
import kotlin.math.max
import kotlin.math.min

object GenerationData {

    data class World(
        val cities: Set<City>,
        val roads: Set<Road>,
        val resources: Set<Resource>,
        @Json(name = "city_resource")
        val storeItems: Set<StoreItem>,
        val seed: Long
    ) {
        fun save() {
            val worldSave = File("world ${this.seed}.json")
            worldSave.writeText(Klaxon().toJsonString(this))
        }
    }

    data class City(
        val id: Int,
        val name: String,
        val color: Int,
        val population: Int,
        val polygonData: String
    )

    data class Road(
        val from: Int,
        val to: Int
    )

    data class StoreItem(
        val cityId: Int,
        val resourceId: Int,
        val cost: Int,
        val quantity: Int
    )

    data class Resource(
        val id: Int,
        @Json(name = "name")
        val resourceName: String,
        @Json(ignored = true)
        val valueModifier: Double
    ) {
        companion object {
            private val defaultResources = setOf(
                Resource(0, "Дерево", 0.5),
                Resource(1, "Камень", 1.0),
                Resource(2, "Железо", 5.0),
                Resource(3, "Золото", 25.0)
            )

            fun all(): Set<Resource> {
                return defaultResources
            }
        }
    }

    data class PolygonData(
        val pointsX: List<Double>,
        val pointsY: List<Double>
    ) {
        fun validatePolygon(max: Double, min: Double): Boolean {
            val maxX = this.pointsX.max()
            val maxY = this.pointsY.max()
            val minX = this.pointsX.min()
            val minY = this.pointsY.min()
            if (maxX == null || maxY == null || minX == null || minY == null) {
                return false
            } else {
                if (max(maxX, maxY) > max || min(minX, minY) < min) {
                    return false
                }
            }
            return true
        }

        fun getSquareSpace(): Int {
            val maxX = this.pointsX.max()
            val maxY = this.pointsY.max()
            val minX = this.pointsX.min()
            val minY = this.pointsY.min()
            val width = maxX!! - minX!!
            val height = maxY!! - minY!!
            return ((width * height).toInt())
        }
    }

}