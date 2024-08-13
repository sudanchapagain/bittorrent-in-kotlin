import com.google.gson.Gson;
import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;

val gson = Gson()

fun main(args: Array<String>) {
    val command = args[0]
    when (command) {
        "decode" -> {
             val bencodedValue = args[1]
             val decoded = decodeBencode(bencodedValue)
             println(gson.toJson(decoded))
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
        else -> bencode.decode(bencodedString.toByteArray(), Type.STRING)
    }
    return decoded
}
