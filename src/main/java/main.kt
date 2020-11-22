import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.io.File
import java.io.OutputStream
import java.util.*
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

object Screenshot {

    private var disposable: Disposable? = null
    private const val DELAY_SECONDS = 5L
    private const val SCREENSHOTS_ROOT_DIRECTORY = "C:\\\\Users\\user\\"
    private const val SCREENSHOTS_DIRECTORY_PREFIX = "screenshots_"
    private const val CONFIG = "screenshoter.cnf"
    private const val SIGNATURE_FILE = "signature.txt"

    @JvmStatic
    fun main(args: Array<String>) {

        println(generalPrefix)
        println()
        println()

        val directory = getScreenshotDirectory()
        val path = directory.directory
        val signatureIO = directory.signatureStream

        println("Screenshoting is started...")

        disposable = Observable.interval(DELAY_SECONDS, TimeUnit.SECONDS)
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

    private fun getScreenshotDirectory(): ScreenshotDirectory {
        val configFile = File("$SCREENSHOTS_ROOT_DIRECTORY$CONFIG")
        if (!configFile.exists()) configFile.createNewFile()
        val lastScreenshotsDirectory = configFile.readText()

        val directory = if (lastScreenshotsDirectory.isBlank() || !File(lastScreenshotsDirectory).exists()) {
            createNewDirectory()
        } else {
            print("Do you want to continue writing into $lastScreenshotsDirectory? (y/n): ")
            var useOldDirectory: Boolean? = null
            while (useOldDirectory == null) {
                when (readLine()?.toLowerCase()) {
                    "y", "yes" -> useOldDirectory = true
                    "n", "no" -> useOldDirectory = false
                }
            }
            if (useOldDirectory) {
                val oldSignatureFile = File("$lastScreenshotsDirectory$SIGNATURE_FILE")
                val oldSignatureStr = oldSignatureFile.readText()

                val oldSignatureIO = oldSignatureFile.outputStream().apply {
                    write(oldSignatureStr.toByteArray())
                }

                ScreenshotDirectory(directory = lastScreenshotsDirectory, signatureStream = oldSignatureIO)
            } else {
                createNewDirectory()
            }
        }

        configFile.outputStream().write(directory.directory.toByteArray())

        return directory
    }

    private fun createNewDirectory(): ScreenshotDirectory {
        val path = "$SCREENSHOTS_ROOT_DIRECTORY$SCREENSHOTS_DIRECTORY_PREFIX${System.currentTimeMillis()}\\"
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
