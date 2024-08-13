import com.google.gson.Gson;
import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;
import java.io.File

val gson = Gson()

fun main(args: Array<String>) {
    when (val command = args[0]) {
        "decode" -> {
            val bencodedValue = args[1]
            val decoded = decodeBencode(bencodedValue)
            println(gson.toJson(decoded))
            return
        }

        "info" -> {
            val fileName = args[1]
            printInfo(fileName)

            return
        }

        else -> println("Unknown command $command")
    }
}

fun decodeBencode(bencodedString: String): Any {
    val bencode = Bencode()

    val decoded: Any = when {
        // convert the string to a byte array and then decodes it as Type specified using
        // the decode method of the Bencode class.
        // TODO: redo this without using the bencode package
        bencodedString.startsWith("i") -> bencode.decode(bencodedString.toByteArray(), Type.NUMBER)
        bencodedString.startsWith("l") -> bencode.decode(bencodedString.toByteArray(), Type.LIST)
        bencodedString.startsWith("d") -> bencode.decode(bencodedString.toByteArray(), Type.DICTIONARY)
        else -> bencode.decode(bencodedString.toByteArray(), Type.STRING)
    }
    return decoded
}

fun printInfo(fileName: String) {
    val bencode = Bencode()
    val data = File(fileName).readBytes()
    val torrentData = bencode.decode(data, Type.DICTIONARY) as Map<String, Any>
    val url = torrentData["announce"] as String
    val info = torrentData["info"] as MutableMap<*, *>
    val length = info["length"]
    println("Tracker URL: $url")
    println("Length: $length")
}