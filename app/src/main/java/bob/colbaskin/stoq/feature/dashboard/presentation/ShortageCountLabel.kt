package bob.colbaskin.stoq.feature.dashboard.presentation

internal fun shortageCountLabel(count: Int): String =
    if (count % 100 !in 11..14 && count % 10 in 2..4) "раза" else "раз"
