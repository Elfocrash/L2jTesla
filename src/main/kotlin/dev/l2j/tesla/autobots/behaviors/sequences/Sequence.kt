package dev.l2j.tesla.autobots.behaviors.sequences

import dev.l2j.tesla.autobots.CoScopes
import dev.l2j.tesla.autobots.extensions.getActiveSequence
import dev.l2j.tesla.autobots.extensions.setActiveSequence
import dev.l2j.tesla.autobots.utils.CancellationToken
import kotlinx.coroutines.async
import dev.l2j.tesla.gameserver.model.actor.Player

internal interface Sequence {
    val bot: Player
    
    var cancellationToken: CancellationToken?
    
    suspend fun definition()

    suspend fun execute(){
        if(bot.getActiveSequence() != null) {
            bot.getActiveSequence()?.cancellationToken?.cancelLambda?.invoke()
            bot.setActiveSequence(null)
        }
        
        val job = CoScopes.sequenceScope.async { definition() }
        cancellationToken = CancellationToken({ job.cancel() })
        job.invokeOnCompletion { 
            bot.setActiveSequence(null)
        }
        bot.setActiveSequence(this)
        job.await()
    }
}