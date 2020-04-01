import kotlin.math.max
import kotlin.math.min

data class PolygonData (
    val pointsX : List<Double>,
    val pointsY : List<Double>
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

}