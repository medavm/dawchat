package dawchat

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration

class UtilsTests {

    class TestClock : Clock {
        // Initialized this way to reduce precision to seconds
        private var testNow: Instant = Instant.fromEpochSeconds(Clock.System.now().epochSeconds)

        fun advance(duration: Duration) {
            testNow = testNow.plus(duration)
        }

        override fun now() = testNow
    }


}