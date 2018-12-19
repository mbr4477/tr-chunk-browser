package com.matthewrussell.trchunkbrowser.domain

import io.reactivex.Completable
import java.io.File

class ConvertDirectory(private val root: File) {
    fun convert(outputDir: File = root.parentFile): Completable {
        return Completable.fromAction {
            // copy to the output directory
            val outDir = outputDir.resolve("${root.name}-out")
            root.copyRecursively(outDir)
            convertInPlace(outDir)
        }
    }

    private fun convertInPlace(dir: File) {
        val contents = dir.listFiles().toList()
        val wavFiles = contents.filter {
            it.isFile && (it.extension == "wav" || it.extension == "WAV")
        }
        for (wav in wavFiles) {
            ExportSegments(GetWavSegments(wav).segments().blockingGet())
                .exportSeparate(dir)
                .blockingAwait()
            wav.delete()
        }
        val subDirs = contents.filter { it.isDirectory }
        for (sub in subDirs) {
            convertInPlace(sub)
        }
    }
}