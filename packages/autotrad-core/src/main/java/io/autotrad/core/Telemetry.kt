package io.autotrad.core

import java.util.concurrent.atomic.AtomicLong

interface TelemetrySink {
  fun onTmHit() {}
  fun onMtCall(durationMs: Long) {}
  fun onQualityReject() {}
  fun onOverlayEdit() {}
}

object AutoTradTelemetry {
  private var sink: TelemetrySink? = null
  private val tmHits = AtomicLong(0)
  private val mtCalls = AtomicLong(0)
  private val qualityRejects = AtomicLong(0)

  fun register(s: TelemetrySink?) { sink = s }
  fun tmHit() { tmHits.incrementAndGet(); sink?.onTmHit() }
  fun mtCall(dMs: Long) { mtCalls.incrementAndGet(); sink?.onMtCall(dMs) }
  fun qualityReject() { qualityRejects.incrementAndGet(); sink?.onQualityReject() }

  fun snapshot(): Map<String, Long> = mapOf(
    "tmHits" to tmHits.get(),
    "mtCalls" to mtCalls.get(),
    "qualityRejects" to qualityRejects.get()
  )
}
