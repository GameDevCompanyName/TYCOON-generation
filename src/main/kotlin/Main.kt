fun main() {
    val world = WorldGeneration.generateWorld(1000.0, 100L, 1488L)
    world.save()
}
