package com.github.yukulab.blockhideandseekmod.util

import kotlinx.coroutines.*
import kotlinx.coroutines.time.delay
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import java.time.Duration
import java.time.Instant

object CoroutineProvider {

    private val job = Job()

    @JvmStatic
    fun loop(delayTime: Duration, task: Runnable): Job = bhasScope.launch {
        do {
            val startTime = Instant.now()
            task.run()
            val lastTime = delayTime - Duration.between(startTime, Instant.now())
            if (!lastTime.isNegative) {
                delay(lastTime)
            }
        } while (true)
    }


    @JvmStatic
    fun init() {
        bhasScope = CoroutineScope(Dispatchers.Default + job)

        ServerLifecycleEvents.SERVER_STOPPING.register {
            runBlocking {
                job.cancelAndJoin()
            }
        }
    }
}

internal lateinit var bhasScope: CoroutineScope