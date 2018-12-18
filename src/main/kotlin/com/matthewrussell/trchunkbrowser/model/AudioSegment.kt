package com.matthewrussell.trchunkbrowser.model

import com.matthewrussell.trwav.Metadata
import java.io.File

data class AudioSegment(
    val sort: Int,
    val src: File,
    val begin: Double,
    val end: Double,
    val label: String,
    val sourceMetadata: Metadata
)