package net.rachel030219.poweramplrc

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import com.maxmpz.poweramp.player.PowerampAPI

class LogGenerator(private val context: Context) {
    private var message = StringBuilder()

    private fun init () {
        message = StringBuilder()
        message.append(genTitle("DEVICE INFORMATION") +
                "Brand: " + Build.BRAND + "\n" +
                "Device: " + Build.DEVICE + "\n" +
                "Model: " + Build.MODEL + "\n" +
                "Id: " + Build.ID + "\n" +
                "Product: " + Build.PRODUCT + "\n" +
                genTitle("FIRMWARE") +
                "SDK Version: " + Build.VERSION.SDK_INT + "\n" +
                "Release: " + Build.VERSION.RELEASE + "\n" +
                "App Version:" + context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode + "\n" +
                genTitle("PATHS"))
        context.run {
            for ((key, value) in getSharedPreferences("paths", Context.MODE_PRIVATE).all) {
                message.append("key: $key, value: $value\n")
            }
        }
        message.append(genTitle("FOLDERS"))
        context.run {
            for (folder in FoldersDatabaseHelper(this).fetchFolders()) {
                message.append("name: ${folder.name}, path: ${folder.path}\n")
            }
        }
    }

    fun generateFromException(e: Throwable): StringBuilder {
        init()
        message.append(genTitle("CAUSE OF ERROR"))
        message.append(e.toString() + "\n")
        e.stackTrace.forEach { message.append("$it\n") }

        return message
    }

    fun generate(): StringBuilder {
        init()
        message.append(genTitle("STATUS RAW DATA"))
        message.append(debugDumpIntent("STATUS", context.registerReceiver(null, IntentFilter(PowerampAPI.ACTION_STATUS_CHANGED))))
        message.append(genTitle("TRACK RAW DATA"))
        message.append(debugDumpIntent("TRACK", context.registerReceiver(null, IntentFilter(PowerampAPI.ACTION_TRACK_CHANGED))))
        return message
    }

    private fun genTitle (content: String): String {
        return "\n\n************ $content ************\n"
    }

    private fun debugDumpIntent(description: String, intent: Intent?): StringBuilder {
        val intentBuilder = StringBuilder()
        if (intent != null) {
            intentBuilder.append("$description debugDumpIntent action=" + intent.action + " extras=" + dumpBundle(intent.extras) + "\n")
            val track = intent.getBundleExtra(PowerampAPI.TRACK)
            if (track != null) {
                intentBuilder.append("track=" + dumpBundle(track) + "\n")
            }
        } else {
            intentBuilder.append("$description debugDumpIntent intent is null")
        }
        return intentBuilder
    }

    private fun dumpBundle(bundle: Bundle?): String {
        if (bundle == null) {
            return "null bundle"
        }
        val sb = java.lang.StringBuilder()
        val keys = bundle.keySet()
        sb.append("\n")
        for (key in keys) {
            sb.append('\t').append(key).append("=")
            val `val` = bundle[key]
            sb.append(`val`)
            if (`val` != null) {
                sb.append(" ").append(`val`.javaClass.simpleName)
            }
            sb.append("\n")
        }
        return sb.toString()
    }
}