package svcs

import java.io.File
import java.math.BigInteger
import java.security.MessageDigest


fun File.copyToDir(toPath: String) {
    val newFile = File(toPath + "\\${this.name}")
    this.copyTo(newFile, true)
}

fun getFilesHashMap(filesListIndexed: List<String>): Map<String, File> {
    val map = mutableMapOf<String, File>()
    for (e in filesListIndexed) {
        File("./")
            .walkTopDown()
            .filter { it.isFile && it.name == e }
            .forEach { map[md5(it.readText())] = it }
    }
    return map
}

fun getHashFromFiles(filesListIndexed: List<String>) =
    md5(getFilesHashMap(filesListIndexed).map { it.key }.joinToString())

fun md5(input:String): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
}

fun String.adjust(length: Int): String {
    return this + " ".repeat(length - this.length)
}