import com.beust.klaxon.Klaxon
import java.io.File

data class World(
    val cities : Set<CityData>,
    val seed : Long
) {
    fun save() {
        val worldSave = File("world ${this.seed}.json")
        worldSave.writeText(Klaxon().toJsonString(this))
    }
}