import com.dampcake.bencode.Bencode
import com.dampcake.bencode.Type
import com.google.gson.Gson
import java.nio.file.Files
import java.nio.file.Paths


object MainKt {

    private val gson = Gson()

    @JvmStatic
    fun main(args: Array<String>) {
        when (val command = args[0]) {
            "decode" -> {
                val bencodedValue = args[1]

                val decoded: String = when (bencodedValue[0]) {
                    'i' -> {
                        val bencode = Bencode(true)
                        bencode.decode(bencodedValue.toByteArray(), Type.NUMBER).toString()
                    }

                    'l' -> {
                        val bencode = Bencode(false)
                        gson.toJson(bencode.decode(bencodedValue.toByteArray(), Type.LIST))
                    }

                    'd' -> {
                        val bencode = Bencode(false)
                        gson.toJson(bencode.decode(bencodedValue.toByteArray(), Type.DICTIONARY))
                    }

                    else -> {
                        try {
                            gson.toJson(decodeBencode(bencodedValue))
                        } catch (e: RuntimeException) {
                            println(e.message)
                            return
                        }
                    }
                }
                println(decoded)
            }

            "info" -> {
                val filePath = args[1]
                val path = Paths.get(filePath)
                val fileBytes = Files.readAllBytes(path)
                val torrent = Torrent(fileBytes)

                println("Tracker URL: ${torrent.announce}")
                println("Length: ${torrent.length}")
                println("Info Hash: ${torrent.infoHash?.let { bytesToHex(it) }}")
                println("Piece Length: ${torrent.pieceLength}")
                println("Piece Hashes: ")
                torrent.pieceHashes.forEach { hash ->
                    println(hash)
                }
            }

            "peers" -> {
                val filePath = args[1]
                val tracker = Tracker(filePath)
                val peers = tracker.requestPeers()
                println(peers)
            }

            else -> {
                println("Unknown command: $command")
            }
        }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        return buildString {
            for (b in bytes) {
                append(String.format("%02x", b))
            }
        }
    }

    private fun decodeBencode(bencodedString: String): String {
        if (bencodedString[0].isDigit()) {
            val firstColonIndex = bencodedString.indexOf(':')
            val length = bencodedString.substring(0, firstColonIndex).toInt()
            return bencodedString.substring(firstColonIndex + 1, firstColonIndex + 1 + length)
        } else {
            throw RuntimeException("Only strings are supported at the moment")
        }
    }
}
