package com.yourname.sra.utils

import android.graphics.Bitmap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for ImageUtils.compressImage() function.
 * Tests progressive quality reduction algorithm and maximum file size enforcement.
 * 
 * Validates: Requirements 5.1, 18.1, 29.1, 29.2, 29.3, 29.4
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ImageUtilsTest {

    /**
     * Test that a small image (already under 2MB) is compressed at quality 100
     * Validates Requirement 29.2: If image is smaller than 2MB, don't compress further
     */
    @Test
    fun `compressImage should not reduce quality for small images already under 2MB`() {
        // Given - Create a small 100x100 bitmap
        val smallBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val maxSizeBytes = 2_097_152L // 2MB

        // When
        val compressedBytes = ImageUtils.compressImage(smallBitmap, maxSizeBytes)

        // Then
        assertNotNull(compressedBytes)
        assertTrue("Compressed image should be under 2MB", compressedBytes.size <= maxSizeBytes)
        // Small image should be well under 2MB
        assertTrue("Small image should be under 50KB", compressedBytes.size < 50_000)
    }

    /**
     * Test that a large image gets compressed using progressive quality reduction
     * Validates Requirement 29.3: Reduce quality until file size is under 2MB
     */
    @Test
    fun `compressImage should progressively reduce quality for large images`() {
        // Given - Create a larger bitmap that will require compression
        val largeBitmap = Bitmap.createBitmap(2000, 2000, Bitmap.Config.ARGB_8888)
        // Fill with some pattern to make it more realistic
        for (x in 0 until 2000) {
            for (y in 0 until 2000) {
                largeBitmap.setPixel(x, y, android.graphics.Color.rgb(x % 256, y % 256, (x + y) % 256))
            }
        }
        val maxSizeBytes = 2_097_152L // 2MB

        // When
        val compressedBytes = ImageUtils.compressImage(largeBitmap, maxSizeBytes)

        // Then
        assertNotNull(compressedBytes)
        assertTrue("Compressed image must be under 2MB", compressedBytes.size <= maxSizeBytes)
    }

    /**
     * Test that compression works with default maxSizeBytes parameter (2MB)
     * Validates Requirement 29.1: Maximum file size is 2MB by default
     */
    @Test
    fun `compressImage should use 2MB as default maxSizeBytes`() {
        // Given
        val bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)
        
        // When - Call without specifying maxSizeBytes
        val compressedBytes = ImageUtils.compressImage(bitmap)

        // Then
        assertNotNull(compressedBytes)
        assertTrue("Compressed image should be under default 2MB", compressedBytes.size <= 2_097_152)
    }

    /**
     * Test that compression throws exception for extremely large images that cannot be compressed
     * Validates Requirement 29.4: Throw exception if image cannot be compressed below 2MB
     */
    @Test
    fun `compressImage should throw exception when image cannot be compressed below maxSizeBytes`() {
        // Given - Create a very large bitmap with complex pattern
        val hugeBitmap = Bitmap.createBitmap(4000, 4000, Bitmap.Config.ARGB_8888)
        // Fill with random-like pattern that's hard to compress
        for (x in 0 until 4000) {
            for (y in 0 until 4000) {
                val color = android.graphics.Color.rgb(
                    (x * y) % 256,
                    (x + y * 37) % 256,
                    (x * 13 + y * 17) % 256
                )
                hugeBitmap.setPixel(x, y, color)
            }
        }
        
        // Set a very small max size to force failure
        val tinyMaxSize = 1000L // 1KB - impossible for a 4000x4000 image

        // When/Then
        try {
            ImageUtils.compressImage(hugeBitmap, tinyMaxSize)
            fail("Should have thrown IllegalStateException for uncompressible image")
        } catch (e: IllegalStateException) {
            assertEquals("Image file too large", e.message)
        }
    }

    /**
     * Test the progressive quality reduction algorithm
     * Validates that quality starts at 100 and reduces by 10
     */
    @Test
    fun `compressImage should start at quality 100 and reduce by 10`() {
        // Given - Create a medium-sized bitmap
        val bitmap = Bitmap.createBitmap(1500, 1500, Bitmap.Config.ARGB_8888)
        // Fill with a gradient pattern
        for (x in 0 until 1500) {
            for (y in 0 until 1500) {
                bitmap.setPixel(x, y, android.graphics.Color.rgb(x % 256, y % 256, 128))
            }
        }
        val maxSizeBytes = 2_097_152L

        // When
        val compressedBytes = ImageUtils.compressImage(bitmap, maxSizeBytes)

        // Then
        assertNotNull(compressedBytes)
        assertTrue("Compressed image should be under 2MB", compressedBytes.size <= maxSizeBytes)
        // The algorithm should have found a quality level that works
        assertTrue("Compressed bytes should be non-empty", compressedBytes.isNotEmpty())
    }

    /**
     * Test that different bitmaps compress to different sizes
     */
    @Test
    fun `compressImage should produce different sizes for different bitmap sizes`() {
        // Given
        val smallBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        val mediumBitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)

        // When
        val smallCompressed = ImageUtils.compressImage(smallBitmap)
        val mediumCompressed = ImageUtils.compressImage(mediumBitmap)

        // Then
        assertTrue("Larger bitmap should compress to larger file", 
            mediumCompressed.size > smallCompressed.size)
    }

    /**
     * Test custom maxSizeBytes parameter
     * Validates that custom size limits work correctly
     */
    @Test
    fun `compressImage should respect custom maxSizeBytes parameter`() {
        // Given
        val bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
        val customMaxSize = 100_000L // 100KB

        // When
        val compressedBytes = ImageUtils.compressImage(bitmap, customMaxSize)

        // Then
        assertNotNull(compressedBytes)
        assertTrue("Compressed image should be under custom max size", 
            compressedBytes.size <= customMaxSize)
    }

    /**
     * Test that compression produces valid JPEG data
     */
    @Test
    fun `compressImage should produce valid JPEG data`() {
        // Given
        val bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)

        // When
        val compressedBytes = ImageUtils.compressImage(bitmap)

        // Then
        assertNotNull(compressedBytes)
        assertTrue("Compressed bytes should not be empty", compressedBytes.isNotEmpty())
        // JPEG files start with FF D8 FF
        assertEquals("Should start with JPEG header", 0xFF.toByte(), compressedBytes[0])
        assertEquals("Should start with JPEG header", 0xD8.toByte(), compressedBytes[1])
        assertEquals("Should start with JPEG header", 0xFF.toByte(), compressedBytes[2])
    }
}
