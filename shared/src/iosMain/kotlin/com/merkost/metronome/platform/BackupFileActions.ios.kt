package com.merkost.metronome.platform

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.merkost.metronome.backup.MaximumBackupCharacters
import com.merkost.metronome.components.MySecondaryButton
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber
import platform.Foundation.NSFileHandle
import platform.Foundation.fileHandleForReadingAtPath
import platform.Foundation.readDataOfLength
import platform.Foundation.closeFile
import platform.darwin.NSObject
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.writeToURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun BackupFileActions(enabled: Boolean, export: suspend () -> String, onImport: (String) -> Unit, onMessage: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    val importAction by rememberUpdatedState(onImport)
    val messageAction by rememberUpdatedState(onMessage)
    var working by remember { mutableStateOf(false) }
    var picker by remember { mutableStateOf<UIDocumentPickerViewController?>(null) }
    val delegate = remember {
        BackupDocumentDelegate(
            scope = scope,
            onImport = { importAction(it) },
            onMessage = { messageAction(it) },
            onFinished = { working = false; picker = null },
        )
    }
    DisposableEffect(Unit) {
        onDispose { picker?.delegate = null; picker?.dismissViewControllerAnimated(false, null) }
    }
    MySecondaryButton(onClick = {
        working = true
        scope.launch {
            try {
                val raw = export()
                val file = NSURL.fileURLWithPath(NSTemporaryDirectory() + "Metronome-backup.txt")
                require(NSString.create(string = raw).writeToURL(file, true, NSUTF8StringEncoding, null))
                val presenter = backupPresenter() ?: error("No file picker is available.")
                val controller = UIDocumentPickerViewController(forExportingURLs = listOf(file), asCopy = true)
                controller.delegate = delegate
                delegate.importing = false
                picker = controller
                presenter.presentViewController(controller, true, null)
            } catch (cancelled: CancellationException) {
                working = false
                throw cancelled
            } catch (_: Exception) {
                working = false
                messageAction("Couldn't export this backup.")
            }
        }
    }, enabled = enabled && !working, modifier = Modifier.fillMaxWidth()) { Text("Export backup") }
    MySecondaryButton(onClick = {
        val presenter = backupPresenter()
        if (presenter == null) messageAction("No file picker is available.") else {
            working = true
            delegate.importing = true
            val controller = UIDocumentPickerViewController(documentTypes = listOf("public.plain-text"), inMode = UIDocumentPickerMode.UIDocumentPickerModeImport)
            controller.delegate = delegate
            picker = controller
            presenter.presentViewController(controller, true, null)
        }
    }, enabled = enabled && !working, modifier = Modifier.fillMaxWidth()) { Text("Import backup") }
}

@OptIn(ExperimentalForeignApi::class)
private class BackupDocumentDelegate(
    private val scope: CoroutineScope,
    private val onImport: (String) -> Unit,
    private val onMessage: (String) -> Unit,
    private val onFinished: () -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {
    var importing = false

    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        scope.launch {
            try {
                if (importing) {
                    val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: error("No backup selected.")
                    val raw = withContext(Dispatchers.Default) {
                        val attributes = NSFileManager.defaultManager.attributesOfItemAtPath(url.path ?: error("Couldn't open this backup."), null)
                        val length = (attributes?.get(NSFileSize) as? NSNumber)?.longLongValue ?: error("Couldn't read the backup size.")
                        require(length in 0..MaximumBackupCharacters.toLong() * 4) { "This backup is too large." }
                        val handle = NSFileHandle.fileHandleForReadingAtPath(url.path ?: error("Couldn't open this backup.")) ?: error("Couldn't open this backup.")
                        val data = try { handle.readDataOfLength((MaximumBackupCharacters.toLong() * 4 + 1).toULong()) } finally { handle.closeFile() }
                        require(data.length <= MaximumBackupCharacters.toULong() * 4u) { "This backup is too large." }
                        NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString() ?: error("Choose a text backup.")
                    }
                    onImport(raw)
                } else onMessage("Backup exported.")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                onMessage(failure.message ?: "Couldn't open this backup.")
            } finally { onFinished() }
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) { onFinished() }
}

private fun backupPresenter(): UIViewController? {
    var presenter = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (presenter?.presentedViewController != null) presenter = presenter.presentedViewController
    return presenter
}
