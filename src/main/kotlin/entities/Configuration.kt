package entities

data class Configuration (
    var lastDir: String = "",
    var screenshotInterval: Long = DELAY_SECONDS
) {
    companion object {
        private const val DELAY_SECONDS = 5L
    }
}