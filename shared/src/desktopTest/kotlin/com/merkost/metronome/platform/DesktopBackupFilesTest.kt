package com.merkost.metronome.platform

import com.merkost.metronome.backup.MaximumBackupCharacters
import java.io.RandomAccessFile
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DesktopBackupFilesTest {
    @Test
    fun exportedJsonRoundTripsUnicodeWithoutChangingTheBackup() {
        val directory = Files.createTempDirectory("metronome-backup-test-").toFile()
        try {
            val file = directory.resolve("practice.json")
            val backup = "{\"name\":\"练习 — تمرين 🎵\",\"version\":1}"
            DesktopBackupFiles.write(file, backup)
            assertEquals(backup, DesktopBackupFiles.read(file))
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun oversizedBackupIsRejectedBeforeItsContentsAreLoaded() {
        val directory = Files.createTempDirectory("metronome-backup-test-").toFile()
        try {
            val file = directory.resolve("oversized.json")
            RandomAccessFile(file, "rw").use { it.setLength(MaximumBackupCharacters.toLong() * 4 + 1) }
            assertFailsWith<IllegalArgumentException> { DesktopBackupFiles.read(file) }
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun malformedUtf8CannotSilentlyChangeImportedPresetNames() {
        val directory = Files.createTempDirectory("metronome-backup-test-").toFile()
        try {
            val file = directory.resolve("invalid.json")
            file.writeBytes(byteArrayOf(0xC3.toByte(), 0x28))
            val failure = assertFailsWith<IllegalArgumentException> { DesktopBackupFiles.read(file) }
            assertEquals("Choose a UTF-8 text backup.", failure.message)
        } finally {
            directory.deleteRecursively()
        }
    }
}
