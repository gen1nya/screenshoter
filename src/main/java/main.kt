import entities.Configuration
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.yaml.snakeyaml.Yaml
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.io.File
import java.io.OutputStream
import java.io.StringWriter
import java.util.*
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

object Screenshot {

    private var disposable: Disposable? = null

    private const val SCREENSHOTS_DIRECTORY_PREFIX = "screenshots_"
    private const val CONFIG = "screenshoter.yaml"
    private const val SIGNATURE_FILE = "signature.txt"
    private const val NAMES_FILE = "names.txt"

    @JvmStatic
    fun main(args: Array<String>) {

        println(generalPrefix)
        println()
        println()

        val config = getOrCreateConfig()
        val directory = getScreenshotDirectory(config)
        val path = directory.directory
        val signatureIO = directory.signatureStream
        val namesIO = directory.namesStream

        println("Screenshoting is started...")

        disposable = Observable.interval(config.screenshotInterval, TimeUnit.SECONDS)
            .map {
                val r = Robot()

                val time = System.currentTimeMillis()
                val screenshotPath = "$path$time.jpg"
                signatureIO.write("$time.jpg\n".toByteArray())

                val capture = Rectangle(Toolkit.getDefaultToolkit().screenSize)
                val image = r.createScreenCapture(capture)
                val screenshotFile = File(screenshotPath)
                ImageIO.write(image, "jpg", screenshotFile)

                namesIO.write("file \'${screenshotFile.absolutePath}\'\n".toByteArray())
                time
            }
            .subscribeOn(Schedulers.io())
            .subscribe({
                println("screen captured!" + Date(it).toGMTString())
            }) {
                print("FUCKING EXCEPTION!")
                it.printStackTrace()
            }

        while (true) { }
    }

    private fun getOrCreateConfig(): Configuration {
        val configFile = File(CONFIG)
        val configuration: Configuration
        if (!configFile.exists()) {
            configuration = Configuration()
            configFile.createNewFile()
            writeConfigToFile(configuration)
        } else {
            configuration = Yaml().load(configFile.inputStream())
        }
        return configuration
    }

    private fun getScreenshotDirectory(config: Configuration): ScreenshotDirectory {
        val directory = if (config.lastDir.isBlank() || !File(config.lastDir).exists()) {
            createNewDirectory()
        } else {
            print("Do you want to continue writing into ${config.lastDir}? (y/n): ")
            var useOldDirectory: Boolean? = null
            while (useOldDirectory == null) {
                when (readLine()?.toLowerCase()?.trim()) {
                    "y", "yes" -> useOldDirectory = true
                    "n", "no" -> useOldDirectory = false
                }
            }
            if (useOldDirectory) {
                val oldSignatureFile = File("${config.lastDir}$SIGNATURE_FILE")
                val oldSignatureStr = oldSignatureFile.readText()

                val oldSignatureIO = oldSignatureFile.outputStream().apply {
                    write(oldSignatureStr.toByteArray())
                }

                val oldNamesFile = File("${config.lastDir}$NAMES_FILE")
                val oldNamesStr = oldNamesFile.readText()

                val oldNamesIO = oldNamesFile.outputStream().apply {
                    write(oldNamesStr.toByteArray())
                }

                ScreenshotDirectory(directory = config.lastDir, signatureStream = oldSignatureIO, namesStream = oldNamesIO)
            } else {
                createNewDirectory()
            }
        }
        config.lastDir = directory.directory
        writeConfigToFile(config)
        return directory
    }

    private fun writeConfigToFile(config: Configuration) {
        val configFile = File(CONFIG)
        val yamlConfig = Yaml()
        val writer = StringWriter()
        yamlConfig.dump(config, writer)
        configFile.writeText(writer.toString())
    }

    private fun createNewDirectory(): ScreenshotDirectory {
        val path = "$SCREENSHOTS_DIRECTORY_PREFIX${System.currentTimeMillis()}\\"
        val screenshotsSignaturePath = "$path$SIGNATURE_FILE"
        val namesPath = "$path$NAMES_FILE"
        val directory = File(path)

        if (!directory.exists()) {
            directory.mkdir()
        }

        val signatureFile = File(screenshotsSignaturePath)
        val signatureIO = signatureFile.outputStream().getDefault()

        val namesFile = File(namesPath)
        val namesIO = namesFile.outputStream().forFfmpeg()

        return ScreenshotDirectory(directory = path, signatureStream = signatureIO, namesStream = namesIO)
    }

    data class ScreenshotDirectory(
        val directory: String,
        val signatureStream: OutputStream,
        val namesStream: OutputStream
    )
}
