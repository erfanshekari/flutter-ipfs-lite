package com.example.ipfs

import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import threads.lite.IPFS

class MainActivity: FlutterActivity() {
    private val CHANNEL = "ipfs.lite/node"

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
                call, result ->
            if (call.method == "init") {
                try {
                    result.success(IPFS.getInstance(applicationContext).identity.toString())
                } catch (e: Exception) {
                    result.error(e.toString(), e.message, null)
                }
            } else if (call.method == "bootstrap") {
                try {
                    result.success(IPFS.getInstance(applicationContext).bootstrap())
                } catch (e: Exception) {
                    result.error(e.toString(), e.message, null)
                }
            }
            else if (call.method == "resolveIPNS") {
                try {
                    result.success("")
                } catch (e: Exception) {
                    result.error(e.toString(), e.message, null)
                }
            }
            else {
                result.notImplemented()
            }
        }
    }
}

