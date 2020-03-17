import java.io.File
import kotlin.system.exitProcess


fun main(args: Array<String>) {

    val outputFileName: String
    when (args.size) {
        1 -> outputFileName = "${File(args[0]).nameWithoutExtension}.out.txt"

        3 -> {
            if (args[1] == "-o") {
                outputFileName = args[2]
            } else {
                System.err.println("illegal arguments")
                exitProcess(-1)
            }
        }
        else -> {
            System.err.println("illegal arguments")
            exitProcess(-1)
        }
    }

    val bundle = EDSACBundle()
    val outputWriter = File(outputFileName).bufferedWriter()
    File(args[0]).bufferedReader().forEachLine { line->
        parseLine(line, bundle)
    }

    File(args[0]).bufferedReader().forEachLine { line->
        val s = translateLine(line, bundle)
        if ( s != null) {
            outputWriter.write(s)
            outputWriter.write("\n")
            outputWriter.flush()
        }
    }
}