package io.autotrad.core

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.util.Locale

class DevCapture(private val context: Context) {
    private val cache: MutableMap<String, JSONObject> = mutableMapOf()

    fun appendPending(locale: Locale, key: String, value: String) {
        val name = "autotrad.pending.${locale.language}.json"
        val obj = cache.getOrPut(name) {
            runCatching {
                val f = File(context.filesDir, name)
                if (f.exists()) JSONObject(f.readText()) else JSONObject()
            }.getOrDefault(JSONObject())
        }
        if (!obj.has(key)) {
            obj.put(key, value)
            persist(name, obj)
        }
    }

    private fun persist(name: String, obj: JSONObject) {
        runCatching {
            val f = File(context.filesDir, name)
            f.writeText(obj.toString(2))
        }.onFailure { Log.w("AutoTrad", "Fail writing $name: ${it.message}") }
    }
}
