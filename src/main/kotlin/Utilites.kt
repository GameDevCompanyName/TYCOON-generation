import org.slf4j.LoggerFactory
import java.util.*
import kotlin.random.Random

object Utilities {

    private const val RANDOM_STRING_SOURCE =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ_" +
                "=-+0123456789zxcvbnm,./;lkjh" +
                "gfdsaqwertyuiop[]ячсмитьбюфы" +
                "вапролджэйцукенгшщзхъйЁЙЦУКЕ" +
                "НГШЩЗХЪФЫВАПРОЛДЖЭЯЧСМИТЬБЮ"

    private val conScanner = Scanner(System.`in`)
    private val logger = LoggerFactory.getLogger(Utilities::class.java)

    fun getUserIntParameter(text: String): Int {
        logger.info("Getting INT from user")
        while (true) {
            println("$text (int) : ")
            val buffer = conScanner.next()
            try {
                return buffer.toInt()
            } catch (e: NumberFormatException) {
                println("Wrong format")
            }
        }
    }

    fun getUserBooleanParameter(text: String): Boolean {
        while (true) {
            println("$text [y/n] : ")
            val string = conScanner.next().toLowerCase()
            if (string == "y" || string == "yes")
                return true
            else if (string == "n" || string == "no")
                return false
            println("Wrong format")
        }
    }

    fun getUserStringParameter(text: String): String {
        println("$text (String) : ")
        val str = conScanner.next()
        println()
        return str
    }

    fun generateRandomString(lenght: Int, randomizer: Random = Random.Default): String {
        val sequence = sequence {
            yieldAll(generateSequence { randomizer.nextInt(RANDOM_STRING_SOURCE.length) })
        }
        return sequence
            .take(lenght)
            .map(RANDOM_STRING_SOURCE::get)
            .joinToString("")
    }

}