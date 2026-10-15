package bob.colbaskin.stoq.feature.supplies.presentation

import bob.colbaskin.stoq.feature.supplies.data.ai.AiSupplyRecognition
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiSupplySession @Inject constructor() {
    private val results = mutableMapOf<String, AiSupplyRecognition>()

    fun put(result: AiSupplyRecognition): String = UUID.randomUUID().toString().also { results[it] = result }

    fun take(token: String): AiSupplyRecognition? = results.remove(token)
}
