package xyz.mpv.rex.utils.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateFeatureTest {

    @Test
    fun parseCommitCount_extractsIntegerFromTags() {
        assertEquals(1010, UpdateManager.parseCommitCount("r1010"))
        assertEquals(1010, UpdateManager.parseCommitCount("R1010"))
        assertEquals(1010, UpdateManager.parseCommitCount("1010"))
        assertEquals(1010, UpdateManager.parseCommitCount("v1010"))
        assertEquals(1010, UpdateManager.parseCommitCount("  r1010  "))
        assertNull(UpdateManager.parseCommitCount("v5.3.0"))
        assertNull(UpdateManager.parseCommitCount("invalid"))
    }

    @Test
    fun isNewerPreviewVersion_comparesCommitCountsCorrectly() {
        // Remote is newer
        assertTrue(UpdateManager.isNewerPreviewVersion("r1015", 1010))
        assertTrue(UpdateManager.isNewerPreviewVersion("1015", 1010))

        // Remote is same
        assertFalse(UpdateManager.isNewerPreviewVersion("r1010", 1010))
        assertFalse(UpdateManager.isNewerPreviewVersion("1010", 1010))

        // Remote is older
        assertFalse(UpdateManager.isNewerPreviewVersion("r1005", 1010))
        assertFalse(UpdateManager.isNewerPreviewVersion("1005", 1010))

        // Current count is 0 (fallback)
        assertFalse(UpdateManager.isNewerPreviewVersion("r1010", 0))
    }

    @Test
    fun parseFileSizeToBytes_convertsUnitsCorrectly() {
        assertEquals(101L * 1024 * 1024, UpdateManager.parseFileSizeToBytes("101M"))
        assertEquals(40L * 1024 * 1024, UpdateManager.parseFileSizeToBytes("40MB"))
        assertEquals(1L * 1024 * 1024 * 1024, UpdateManager.parseFileSizeToBytes("1G"))
        assertEquals(500L * 1024, UpdateManager.parseFileSizeToBytes("500KB"))
        assertEquals(1024L, UpdateManager.parseFileSizeToBytes("1024B"))
        assertEquals(0L, UpdateManager.parseFileSizeToBytes("invalid"))
    }

    @Test
    fun parsePreviewHtml_extractsVersionBuildTimeAndApks() {
        val sampleHtml = """
            <!DOCTYPE html>
            <html>
            <body>
                <div class="version-badge">r1010</div>
                <div class="build-info">
                    Build time: 2026-10-12 13:26 UTC
                </div>
                <div class="download-list">
                    <div class="download-item">
                        <div class="item-size">101M</div>
                        <a href="REX-Player-preview-r1010-universal.apk" class="btn-download">Download</a>
                    </div>
                    <div class="download-item">
                        <div class="item-size">40M</div>
                        <a href="REX-Player-preview-r1010-arm64-v8a.apk" class="btn-download">Download</a>
                    </div>
                    <div class="download-item">
                        <div class="item-size">38M</div>
                        <a href="REX-Player-preview-r1010-armeabi-v7a.apk" class="btn-download">Download</a>
                    </div>
                    <div class="download-item">
                        <div class="item-size">41M</div>
                        <a href="REX-Player-preview-r1010-x86.apk" class="btn-download">Download</a>
                    </div>
                    <div class="download-item">
                        <div class="item-size">41M</div>
                        <a href="REX-Player-preview-r1010-x86_64.apk" class="btn-download">Download</a>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()

        val release = UpdateManager.parsePreviewHtml(sampleHtml)

        assertEquals("r1010", release.tagName)
        assertEquals("REX Player Preview r1010", release.name)
        assertEquals("2026-10-12 13:26 UTC", release.publishedAt)
        assertEquals("https://mpvrex.github.io/REX-Player/", release.htmlUrl)
        assertEquals(5, release.assets.size)

        val universal = release.assets.firstOrNull { it.name.contains("universal") }
        assertNotNull(universal)
        assertEquals("REX-Player-preview-r1010-universal.apk", universal!!.name)
        assertEquals("https://mpvrex.github.io/REX-Player/REX-Player-preview-r1010-universal.apk", universal.downloadUrl)
        assertEquals(101L * 1024 * 1024, universal.size)

        val arm64 = release.assets.firstOrNull { it.name.contains("arm64-v8a") }
        assertNotNull(arm64)
        assertEquals("REX-Player-preview-r1010-arm64-v8a.apk", arm64!!.name)
        assertEquals("https://mpvrex.github.io/REX-Player/REX-Player-preview-r1010-arm64-v8a.apk", arm64.downloadUrl)
        assertEquals(40L * 1024 * 1024, arm64.size)
    }
}
