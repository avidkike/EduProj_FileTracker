package svcs

import java.io.File

fun callCheckout(argList: List<String>) {

    if (argList.isEmpty()) {
        println("Commit id was not passed.")
        return
    }
    val commitsDir = File(".\\vcs\\commits")
    val commitDir = commitsDir.walkTopDown()
        .filter { it.isDirectory && it.name == argList[0] }
        .firstOrNull()


    if (commitDir?.exists() == true) {
        commitDir.listFiles()?.forEach { it.copyToDir(".") }
        println("Switched to commit ${commitDir.name}.")
    } else {
        println("Commit does not exist.")
    }
}

fun callLog(logFile: File) {

    val log = mutableListOf<String>()

    val logRev = logFile.readText()
        .split("\n")
        .filter { it != "" }
        .reversed()

    var strInd = 0
    for (i in logRev.indices) {
        strInd++

        if (strInd % 3 == 0) {
            log.add(i-2, logRev[i])
            log.add(i-1, logRev[i-1])
            log.add(i-0, logRev[i-2])
        }
    }

    if (log.isEmpty()) {
        println("No commits yet.")
    } else {
        println(log.joinToString("\n"))
    }
}

fun callCommit(logFile: File,
               indexFile: File,
               author: String,
               argList: List<String>) {
    if (argList.isEmpty()) {
        println("Message was not passed.")
    } else {

        //HASH COMPARISON
        val addedFiles = getAddedFiles(indexFile)
        val commitHash = getHashFromFiles(addedFiles)

        val logHash = logFile.readLines()
            .takeIf {
                it.firstOrNull() != null
            }?.component3()?.split(" ")?.last() ?: ""

        //COMMIT
        if (commitHash == logHash) {
            println("Nothing to commit.")
        } else {

            val dirCommit = File(".\\vcs\\commits\\$commitHash")
            dirCommit.mkdir()

            val hashedFilesMap = getFilesHashMap(addedFiles)

            for (file in hashedFilesMap.values) {
                file.copyToDir(dirCommit.path)
            }
            updateLog(logFile, commitHash, author, argList[0])
        }
    }
}

fun updateLog(logFile: File,
              commitHash: String,
              author: String,
              change: String) {
    val commitText =
"""
commit $commitHash
Author: $author
$change
"""

    logFile.appendText("\n" + commitText)
    println("Changes are committed.")
}

fun getCurrConfig(fileConfig: File): String {
    return fileConfig.readText()
}

fun callConfig(fileConfig: File, argList: List<String>) {

    var configCurrent = getCurrConfig(fileConfig)

    if (configCurrent == "" && argList.isEmpty()) {
        println("Please, tell me who you are.")
    } else {
        if (argList.isNotEmpty() && argList[0] != configCurrent) {

            fileConfig.writeText(argList[0])
            configCurrent = argList[0]
        }
        println("The username is ${configCurrent}.")
    }
}

fun execCommand(command: String, argList: List<String>) {

    val vscDir = File(".\\vcs")
    if (!vscDir.exists()) {
        vscDir.mkdir()
    }

    val commitDir = File(".\\vcs\\commits")
    if (!commitDir.exists()) {
        commitDir.mkdir()
    }

    val configFile = File(".\\vcs\\config.txt")
    if (!configFile.exists()){
        configFile.createNewFile()
    }

    val indexFile = File(".\\vcs\\index.txt")
    if (!indexFile.exists()){
        indexFile.createNewFile()
    }

    val logFile = File(".\\vcs\\log.txt")
    if (!logFile.exists()){
        logFile.createNewFile()
    }

    when(command) {
        "checkout" -> callCheckout(argList)
        "config" -> callConfig(configFile, argList)
        "commit" -> callCommit(logFile, indexFile, getCurrConfig(configFile), argList)
        "add" -> callAdd(indexFile, argList)
        "log" -> callLog(logFile)
        else -> return
    }
}

fun processCommand(command: String, argList: List<String>) {

    val length = Menu.values().maxOf { it.command.length }

    if (command == Menu.HELP.command || command == "") {
        println("These are SVCS commands:")
        for (e in Menu.values().filter { menu -> menu.command != Menu.HELP.command }) {
            println(e.command.adjust(length) + "   " + e.descr)
        }
        return
    }

    if (Menu.values().none { menu -> menu.command == command }) {
        println("'$command' is not a SVCS command.")
        return
    } else {
        execCommand(command, argList)
    }
}

fun main(args: Array<String>) {

    var argList = mutableListOf<String>()
    val allList = args.toList()

    val command = if (allList.isEmpty()) "" else allList[0]

    if (allList.size > 1) {
        argList = allList.slice(1..allList.lastIndex) as MutableList<String>
    }
    processCommand(command, argList)
}

enum class Menu (val command: String, val descr: String) {
    CONFIG  ("config", "Get and set a username."),
    HELP    ("--help", "prints the help page;"),
    ADD     ("add", "Add a file to the index."),
    LOG     ("log", "Show commit logs."),
    COMMIT  ("commit", "Save changes."),
    CHECKOUT("checkout", "Restore a file.");
}

fun getAddedFiles(indexFile: File): List<String> {
    return indexFile.readText().split("\n").filter { it != "" }
}

fun callAdd(indexFile: File, argList: List<String>) {

    if (argList.isEmpty()) {
        if (indexFile.readText() == "") {
            println("Add a file to the index.")
        } else {
            println("Tracked files:")
            println(getAddedFiles(indexFile).joinToString("\n"))
        }
    } else {
        if (File("./").listFiles()?.any { it.name == argList[0] } == true) {
            if (indexFile.readText().split("\n")
                    .any { it != argList[0] }) {
                indexFile.appendText("\n" + argList[0])
            }
            println("The file '${argList[0]}' is tracked.")
        } else {
            println("Can't find '${argList[0]}'.")
        }
    }
}