package com.merkost.metronome.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.merkost.metronome.backup.MaximumBackupCharacters
import com.merkost.metronome.backup.backupError
import com.merkost.metronome.backup.backupText
import com.merkost.metronome.backup.requireBackup
import com.merkost.metronome.model.UiText
import com.merkost.metronome.components.PracticeBackupActions
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
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.backup_choose_text
import metronome.shared.generated.resources.backup_export_failed
import metronome.shared.generated.resources.backup_exported
import metronome.shared.generated.resources.backup_no_file_picker
import metronome.shared.generated.resources.backup_none_selected
import metronome.shared.generated.resources.backup_open_failed
import metronome.shared.generated.resources.backup_size_unreadable
import metronome.shared.generated.resources.backup_too_large

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun BackupFileActions(enabled: Boolean, export: suspend () -> String, onImport: (String) -> Unit, onMessage: (UiText) -> Unit) {
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
    PracticeBackupActions(
        enabled = enabled && !working,
        onExport = {
            working = true
            scope.launch {
                try {
                    val raw = export()
                    val file = NSURL.fileURLWithPath(NSTemporaryDirectory() + "Metronome-backup.txt")
                    require(NSString.create(string = raw).writeToURL(file, true, NSUTF8StringEncoding, null))
                    val presenter = backupPresenter() ?: backupError(Res.string.backup_no_file_picker)
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
                    messageAction(UiText.of(Res.string.backup_export_failed))
                }
            }
        },
        onImport = {
            val presenter = backupPresenter()
            if (presenter == null) messageAction(UiText.of(Res.string.backup_no_file_picker)) else {
                working = true
                delegate.importing = true
                val controller = UIDocumentPickerViewController(documentTypes = listOf("public.plain-text"), inMode = UIDocumentPickerMode.UIDocumentPickerModeImport)
                controller.delegate = delegate
                picker = controller
                presenter.presentViewController(controller, true, null)
            }
        },
    )
}

@OptIn(ExperimentalForeignApi::class)
private class BackupDocumentDelegate(
    private val scope: CoroutineScope,
    private val onImport: (String) -> Unit,
    private val onMessage: (UiText) -> Unit,
    private val onFinished: () -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {
    var importing = false

    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        scope.launch {
            try {
                if (importing) {
                    val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: backupError(Res.string.backup_none_selected)
                    val raw = withContext(Dispatchers.Default) {
                        val attributes = NSFileManager.defaultManager.attributesOfItemAtPath(url.path ?: backupError(Res.string.backup_open_failed), null)
                        val length = (attributes?.get(NSFileSize) as? NSNumber)?.longLongValue ?: backupError(Res.string.backup_size_unreadable)
                        requireBackup(length in 0..MaximumBackupCharacters.toLong() * 4, Res.string.backup_too_large)
                        val handle = NSFileHandle.fileHandleForReadingAtPath(url.path ?: backupError(Res.string.backup_open_failed)) ?: backupError(Res.string.backup_open_failed)
                        val data = try { handle.readDataOfLength((MaximumBackupCharacters.toLong() * 4 + 1).toULong()) } finally { handle.closeFile() }
                        requireBackup(data.length <= MaximumBackupCharacters.toULong() * 4u, Res.string.backup_too_large)
                        NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString() ?: backupError(Res.string.backup_choose_text)
                    }
                    onImport(raw)
                } else onMessage(UiText.of(Res.string.backup_exported))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                onMessage(failure.backupText(Res.string.backup_open_failed))
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
