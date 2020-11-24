import java.io.OutputStream

fun OutputStream.forFfmpeg() = this.apply { write("# use this for generate timelapse video via ffmpeg\n\n".toByteArray()) }

fun OutputStream.getDefault() = this.apply { write("$generalPrefix\n\n".toByteArray()) }
val generalPrefix = """
                                       _,,---.)\__
                                 ,'.          ""`.
                                f.:               \
                             ,-.|:  ,-.       ,-.  Y-.
                     ,-.    f , \. /:  \   . /     | j
                    f.  Y   `.`.       _`. ,'_     |f
                    |:  |     ) )      "`    "`    |'
                    l:. l    ( '          --.      j
                     Y:  Y_,--.Y:         __      (
                  ,-.|  ,'.::..):..    ,'"-'Y.     Y
                 f:.           \ ::.  '"'`--`      j
                 j::            Y-.__        __,,-'___
                f;\::.          |    ``""${'"'}${'"'}''__(""'_,.`--.   ,--.
                l:::::...       j--.       ,'.. `"'       Y-'.:::)
                 `-..::::::_,,-'   :).     `--'(::..     ,j..::--(
                     f`""${'"'}'.  .  )-(:.      .:::`---\:.-'Y;:::::::Y
                     j:::::::::..   Y:        ..:::::`;_,;;;::::::j
                    f::;;;;;::::::. j:           ...::::\;;:::_,,'
                    l;;::::::::: _,;:       (.,     .....Y::."\
                     Y;;;::::_,-';::..                   |:::. Y
                     l;;;;;:::`-;;;::....                j;;::.|
                      `;;;;;;;;;:);;;;:::::...          /\;;;;:j
                        "`------'-.;;;;::::::::...._,-'"  `---'
                                    ``""${'"'}${'"'}${'"'}${'"'}${'"'}${'"'}${'"'}${'"'}${'"'}''
                                                       f.cking.software
""".trimIndent()