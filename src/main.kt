import java.io.File
import kotlin.system.exitProcess





fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("illegal arguments")
        exitProcess(-1)
    }
    val inputFile = args[0]
    val outputFile = "${args[0]}.out.txt"

    val defines = mutableMapOf<String, Int>()
    var pr = false;
    var instrCnt = 31
    val comment = Regex("\\s*[#][\\w\\d]*")
    val labelCom = Regex("\\s*[\\w\\d]+:(\\s*\\w\\s*(\\d+|\\[[\\w\\d]+\\])\\s+[LS])*")
    val com = Regex("\\s*\\w\\s*(\\d+|\\[[\\w\\d]+\\])\\s+[LS]")

    File(inputFile).useLines { seq ->
        for (line in seq) {
            val l = line.trim()
            if (!pr) {
                if (l.isEmpty()) continue
                if (l == "#p"){pr = true; continue}

                val def = l.split(" ")
                if (def.size != 2) println("Unknown syntax")
                else {
                    defines[def[0]] = def[1].toInt()
                }
            } else {
                if (comment.matches(line)) continue
                if (labelCom.matches(line)) {
                    val tag = line.trim().substring(0, line.indexOf(":"))
                    defines[tag] = instrCnt
                    instrCnt += 1
                }
                if (com.matches(line)) {
                    instrCnt += 1
                }
            }
        }
    }
    val outPrnt = File(outputFile).bufferedWriter()

    File(inputFile).useLines { seq ->
        var foundStart = false
        seq.forEach { line ->
            if (!foundStart) {
                val tmp = line.trim()
                if (tmp == "#p") foundStart = true
            } else {
                if (!line.contains("#")) {

                    var outLine: String? = null
                    for (key in defines.keys) {
                        if (line.contains("[$key]")) {
                            outLine = line.replace("[$key]", "${defines[key]}").trim()
                        }
                    }
                    if (outLine == null) outLine = line.trim();

                    if (outLine.contains(Regex("[\\w\\d]+:"))) {
                        outLine = outLine.replace(Regex("[\\w\\d]+:"), "     ").trim()
                    }

                    outPrnt.write("\t")
                    outPrnt.write(outLine ?: line)
                    outPrnt.write("\n")
                    outPrnt.flush()
                }
            }
        }
    }
    println("success!")
}