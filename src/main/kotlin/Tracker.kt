import com.dampcake.bencode.Bencode
import com.dampcake.bencode.Type
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths

@Suppress("DEPRECATION")
class Tracker(torrentFilePath: String) {
    private val torrent: Torrent = Torrent(Files.readAllBytes(Paths.get(torrentFilePath)))

    fun requestPeers(): String {

        val announce = torrent.announce ?: return "No announce URL found in torrent."
        val peerId = "00112233445566778899"
        val left = torrent.length

        val infoHash = torrent.infoHash ?: return "No info hash found."
        val encodedInfoHash = infoHash.joinToString("") { byte ->
            if (byte in 0x30..0x39 || byte in 0x41..0x5A || byte in 0x61..0x7A) {
                byte.toChar().toString()
            } else {
                String.format("%%%02X", byte)
            }
        }

        val fullUrl =
            "$announce?info_hash=$encodedInfoHash&peer_id=$peerId&port=6881&uploaded=0&downloaded=0&left=$left&compact=1"

        return try {
            val url = URL(fullUrl)
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"
                val responseCode = responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseBytes = inputStream.readBytes()
                    val bencode = Bencode(true)
                    val responseDict = bencode.decode(responseBytes, Type.DICTIONARY) as Map<String, Any>

                    val peersBytes = when (val peersObject = responseDict["peers"]) {
                        is ByteBuffer -> {
                            val bytes = ByteArray(peersObject.remaining())
                            peersObject.get(bytes)
                            bytes
                        }

                        is ByteArray -> peersObject
                        else -> null
                    }

                    if (peersBytes != null) {
                        val peersList = StringBuilder()
                        for (i in peersBytes.indices step 6) {
                            if (i + 5 < peersBytes.size) {
                                val ip =
                                    "${peersBytes[i].toInt() and 0xff}.${peersBytes[i + 1].toInt() and 0xff}.${peersBytes[i + 2].toInt() and 0xff}.${peersBytes[i + 3].toInt() and 0xff}"
                                val port =
                                    ((peersBytes[i + 4].toInt() and 0xff) shl 8) or (peersBytes[i + 5].toInt() and 0xff)
                                peersList.append("$ip:$port\n")
                            }
                        }
                        return peersList.toString()
                    } else {
                        "No peers found in response."
                    }
                } else {
                    "Failed to get response from tracker. Response code: $responseCode"
                }
            }
        } catch (e: Exception) {
            "Error occurred while requesting peers: ${e.message}"
        }

    }
}