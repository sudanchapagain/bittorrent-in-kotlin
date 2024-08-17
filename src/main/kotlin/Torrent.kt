import com.dampcake.bencode.Bencode
import com.dampcake.bencode.Type
import java.security.MessageDigest

class Torrent(bytes: ByteArray) {
    var announce: String? = null
    var length: Long = 0
    var infoHash: ByteArray? = null

    init {
        val bencode = Bencode(false)
        val bencode2 = Bencode(true)

        val root = bencode.decode(bytes, Type.DICTIONARY) as Map<String, Any>
        val info = root["info"] as Map<*, *>

        announce = root["announce"] as String?
        length = info["length"] as Long

        val digest2 = MessageDigest.getInstance("SHA-1")
        infoHash = digest2.digest(
            bencode2.encode(
                bencode2.decode(bytes, Type.DICTIONARY)["info"] as Map<*, *>
            )
        )
    }
}
