import com.dampcake.bencode.Bencode
import com.dampcake.bencode.Type
import java.nio.ByteBuffer
import java.security.MessageDigest

class Torrent(bytes: ByteArray) {
    var announce: String? = null
    var length: Long = 0
    var infoHash: ByteArray? = null
    var pieceLength: Long = 0
    var pieceHashes: List<String> = listOf()

    init {
        val bencode = Bencode(false)
        val bencode2 = Bencode(true)

        val root = bencode.decode(bytes, Type.DICTIONARY) as Map<String, Any>
        val info = root["info"] as Map<*, *>

        announce = root["announce"] as String?
        length = info["length"] as Long
        pieceLength = info["piece length"] as Long

        val root2 = bencode2.decode(bytes, Type.DICTIONARY) as Map<String, Any>
        val info2 = root2["info"] as Map<*, *>

        val pieceHashByteArray = when (val pieces = info2["pieces"]) {
            is ByteBuffer -> {
                val byteArray = ByteArray(pieces.remaining())
                pieces.get(byteArray)
                byteArray
            }

            is ByteArray -> pieces
            else -> throw IllegalStateException("Unexpected type for pieces field")
        }

        pieceHashes = pieceHashByteArray.toList().chunked(20).map { chunk ->
            chunk.joinToString("") { "%02x".format(it) }
        }

        val digest2 = MessageDigest.getInstance("SHA-1")
        infoHash = digest2.digest(
            bencode2.encode(
                bencode2.decode(bytes, Type.DICTIONARY)["info"] as Map<*, *>
            )
        )
    }
}
