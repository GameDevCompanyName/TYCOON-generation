import com.github.javafaker.Faker
import com.github.javafaker.service.RandomService
import de.alsclo.voronoi.Voronoi
import de.alsclo.voronoi.graph.Edge
import de.alsclo.voronoi.graph.Graph
import de.alsclo.voronoi.graph.Point
import java.util.*
import java.util.stream.Stream
import kotlin.math.abs
import kotlin.random.Random
import kotlin.random.asJavaRandom
import kotlin.streams.toList

object WorldGeneration {
    private const val DELTA: Double = 0.1

    fun generateWorld(maxCoordinate: Double, quantity: Long, seed: Long): World {
        val random = Random(seed)
        val points: List<Point> = generatePoints(maxCoordinate, quantity, random)
        val graph: Graph = Voronoi(points).graph
        val edgeMap: Map<Point, Collection<Edge>> = mapFromEdgeStream(graph.edgeStream())

        val filteredEdgeMap = edgeMap.filter { entry ->
            val edges = entry.value
            edges.forEach {
                if (it.a == null || it.b == null) {
                    return@filter false
                }
            }
            return@filter true
        }

        val neighbours: Map<Point, Set<Point>> = neighbourMap(edgeMap)
        val polygons: Map<Point, PolygonData> = polygonsFromEdges(filteredEdgeMap).filter {
            return@filter it.value.validatePolygon(maxCoordinate, 0.0)
        }

        val ids: MutableMap<Point, Int> = mutableMapOf()
        points.forEachIndexed { index, point ->
            ids[point] = index
        }

        val cities = buildCityData(
            points,
            ids,
            polygons,
            neighbours,
            random
        )

        return World(cities, seed)
    }

    private fun buildCityData(
        points: List<Point>,
        ids: Map<Point, Int>,
        polygons: Map<Point, PolygonData>,
        neighbours: Map<Point, Set<Point>>,
        random: Random
    ): Set<CityData> {
        val faker = Faker(Locale("fr"), RandomService(random.asJavaRandom()))
        val cities = mutableSetOf<CityData>()
        for (point in points) {
            try {
                val id = ids[point] ?: continue
                val polygon = polygons[point] ?: continue
                val neighbourIds = neighbourIds(
                    point,
                    neighbours,
                    ids
                )
                val space = polygon.getSquareSpace()
                cities.add(
                    CityData(
                        id,
                        faker.address().cityName(),
                        random.nextInt(16777214),
                        space * 9 + random.nextInt(space * 2),
                        point.x,
                        point.y,
                        polygon,
                        neighbourIds
                    )
                )
            } catch (e: NullPointerException) {
                continue
            }
        }
        return cities
    }

    private fun neighbourIds(point: Point, neighbours: Map<Point, Set<Point>>, ids: Map<Point, Int>): Set<Int> {
        val neighbourIds = mutableSetOf<Int>()
        if (neighbours.containsKey(point)) {
            val neighbourPoints = neighbours[point]
            if (neighbourPoints != null) {
                for (neighbourPoint in neighbourPoints) {
                    val neighbourId = ids[neighbourPoint]
                    if (neighbourId != null) {
                        neighbourIds.add(neighbourId)
                    } else {
                        throw NullPointerException()
                    }
                }
            } else {
                throw NullPointerException()
            }
        } else {
            throw NullPointerException()
        }
        return neighbourIds
    }

    private fun neighbourMap(edgeMap: Map<Point, Collection<Edge>>): Map<Point, Set<Point>> {
        val map = mutableMapOf<Point, MutableSet<Point>>()
        edgeMap.entries.forEach { entry ->
            val point = entry.component1()
            entry.component2().forEach {
                if (it.site1 == point) {
                    map.getOrPut(point, { mutableSetOf() }).add(it.site2)
                }
                if (it.site2 == point) {
                    map.getOrPut(point, { mutableSetOf() }).add(it.site1)
                }
            }
        }
        return map
    }

    private fun generatePoints(max: Double, quantity: Long, random: Random): List<Point> {
        val pointSequence = Stream.generate {
            Point(random.nextDouble(max), random.nextDouble(max))
        }
        return pointSequence.limit(quantity).toList()
    }

    private fun mapFromEdgeStream(edgeStream: Stream<Edge>): Map<Point, MutableSet<Edge>> {
        val map = mutableMapOf<Point, MutableSet<Edge>>()
        edgeStream.forEach {
            val site1: Point = it.site1
            val site2: Point = it.site2
            map.getOrPut(site1, { mutableSetOf() }).add(it)
            map.getOrPut(site2, { mutableSetOf() }).add(it)
        }
        return map
    }

    private fun polygonsFromEdges(edges: Map<Point, Collection<Edge>>): Map<Point, PolygonData> {
        val polygonMap = mutableMapOf<Point, PolygonData>()

        edges.entries.stream().forEach { entry ->
            val list = mutableListOf<Edge>()
            list.addAll(entry.value)

            val points = mutableListOf<Point>()
            points.add(entry.value.first().a.location)

            var currentEdge = entry.value.first()
            var currentPoint = entry.value.first().b.location
            list.remove(currentEdge)
            while (list.isNotEmpty()) {
                for (it in list) {
                    if (twoPointsAreClose(it.a.location, currentPoint)) {
                        points.add(currentPoint)
                        currentPoint = it.b.location
                        currentEdge = it
                        break
                    }
                    if (twoPointsAreClose(it.b.location, currentPoint)) {
                        points.add(currentPoint)
                        currentPoint = it.a.location
                        currentEdge = it
                        break
                    }
                }
                list.remove(currentEdge)
            }

            val pointsX = mutableListOf<Double>()
            val pointsY = mutableListOf<Double>()

            points.forEach {
                pointsX.add(it.x)
                pointsY.add(it.y)
            }

            polygonMap[entry.key] = PolygonData(
                pointsX,
                pointsY
            )
        }

        return polygonMap
    }

    @Suppress("RedundantElseInIf", "LiftReturnOrAssignment", "RedundantIf")
    private fun twoPointsAreClose(first: Point, second: Point): Boolean {
        if (abs(first.x - second.x) < DELTA) {
            if (abs(first.y - second.y) < DELTA) {
                return true
            } else {
                return false
            }
        } else {
            return false
        }
    }

}