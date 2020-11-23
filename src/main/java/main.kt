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

    @JvmStatic
    fun main(args: Array<String>) {

        println(generalPrefix)
        println()
        println()

        val config = getOrCreateConfig()
        val directory = getScreenshotDirectory(config)
        val path = directory.directory
        val signatureIO = directory.signatureStream

        println("Screenshoting is started...")

        disposable = Observable.interval(config.screenshotInterval, TimeUnit.SECONDS)
            .map {
                val r = Robot()

                val time = System.currentTimeMillis()
                val screenshotPath = "$path$time.jpg"
                signatureIO.write("$time.jpg\n".toByteArray())

                val capture = Rectangle(Toolkit.getDefaultToolkit().screenSize)
                val image = r.createScreenCapture(capture)
                ImageIO.write(image, "jpg", File(screenshotPath))
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

                ScreenshotDirectory(directory = config.lastDir, signatureStream = oldSignatureIO)
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
        val directory = File(path)

        if (!directory.exists()) {
            directory.mkdir()
        }

        val signatureFile = File(screenshotsSignaturePath)
        val signatureIO = signatureFile.outputStream().getDefault()

        return ScreenshotDirectory(directory = path, signatureStream = signatureIO)
    }

    data class ScreenshotDirectory(val directory: String, val signatureStream: OutputStream)
}
