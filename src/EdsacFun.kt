

class EDSACBundle {
    companion object{
        const val FIRST_INSTR_NUM = 31
    }
    val defines = mutableMapOf<String, Int>()
    var instrNum = 0;
}

private val defineRegex = Regex("def\\s+[\\w\\d]+\\s+[\\w\\d]+")
private val commandRegex = Regex("\\s*([\\w\\d]+:)*\\s*[A-Z@!&#.*]\\s+((\\d+)|(\\[[\\w\\d]+\\]))\\s+[LS]")
private val labelRegex = Regex("[\\w\\d]+:")
private val constRegex = Regex("([\\w\\d]+:)*\\s*\\{[\\w\\d]+\\}\\s*")
private const val endLabel = "end"

fun parseLine(line: String, bundle: EDSACBundle) {
    var s = line.trim()
    when {
        defineRegex.matches(s) -> {
            s = s.replace(Regex("def\\s+"), "")
            val tmp = s.split(" ")
            bundle.defines[tmp[0]] = tmp[1].toInt()
        }
        commandRegex.matches(s) -> {
            if (s.contains(labelRegex)) {
                bundle.defines[s.substring(0, s.indexOf(':'))] = bundle.instrNum + EDSACBundle.FIRST_INSTR_NUM
            }
            bundle.instrNum += 1
        }
        constRegex.matches(s) -> {
            if (s.contains(labelRegex)) {
                bundle.defines[s.substring(0, s.indexOf(':'))] = bundle.instrNum + EDSACBundle.FIRST_INSTR_NUM
            }
            bundle.instrNum += 1
        }
    }
}

fun translateLine(line: String, bundle: EDSACBundle):String? {
    var s = line.trim()

    if (constRegex.matches(s) || commandRegex.matches(s)) {
        if (s.contains(labelRegex)) { //erase label before command
            s = s.substring((s.indexOf(":")+1) until (s.length)).trim()
        }
        if (s.contains(Regex("\\[[\\w\\d]+\\]"))){ // replace [label] with its' address
            if (s.contains("[$endLabel]")) {
                s = s.replace("[$endLabel]", (bundle.instrNum + EDSACBundle.FIRST_INSTR_NUM).toString())
            } else {
                for (k in bundle.defines.keys) {
                    if (s.contains("[${k}]")) {
                        s = s.replace("[$k]", bundle.defines[k].toString())
                    }
                }
            }
        }
        if (s.contains(constRegex)) {
            s = constRegex.find(s)?.value.toString()
            s = s.substring((s.indexOf("{") + 1) until s.indexOf("}"))
            s = bundle.defines[s]?.let { translateNumber(it) }.toString()
        }

        s = s.replace(Regex("\\s+"), " ")
        return s
    }
    return null
}


fun translateNumber(n: Int): String{
    val codeToLetter = mapOf(0 to 'P', 1 to 'Q',2 to 'W',3 to 'E',4 to 'R',5 to 'T',6 to 'Y',7 to 'U',
        8 to 'I',9 to 'O',10 to 'J',11 to '#',12 to 'S',13 to 'Z',14 to 'K',15 to '*',16 to '.',17 to 'F',18 to '@',
        19 to 'D', 20 to '!',21 to 'H',22 to 'N',23 to 'M',24 to '&',25 to 'L',26 to 'X',27 to 'G',28 to 'A',
        29 to 'B',30 to 'C', 31 to 'V')
    var tmp = n

    val ans = mutableListOf<String>()
    when (tmp and 1) {
        0-> ans.add("S")
        1-> ans.add("L")
    }
    tmp = tmp shr 1
    ans.add((tmp and 0x3FF).toString())
    tmp = tmp shr 11
    ans.add(codeToLetter[tmp].toString())
    return ans.reversed().joinToString(" ")
}