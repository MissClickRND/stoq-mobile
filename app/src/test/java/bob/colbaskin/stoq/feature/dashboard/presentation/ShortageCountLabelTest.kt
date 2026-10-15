package bob.colbaskin.stoq.feature.dashboard.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class ShortageCountLabelTest {
    @Test fun countsUseReadableRussianLabels() {
        listOf(0, 1, 5, 11, 12, 14, 21, 25, 111).forEach { assertEquals("раз", shortageCountLabel(it)) }
        listOf(2, 3, 4, 22, 23, 24, 102).forEach { assertEquals("раза", shortageCountLabel(it)) }
    }
}
