package com.matthewrussell.trchunkbrowser.app.mainview

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.matthewrussell.trchunkbrowser.domain.ExportSegments
import com.matthewrussell.trchunkbrowser.domain.GetWavSegments
import com.matthewrussell.trchunkbrowser.model.AudioSegment
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import tornadofx.*
import java.io.File

class MainViewModel : ViewModel() {
    val segments = FXCollections.observableArrayList<AudioSegment>()
    val hasSegments = SimpleListProperty<AudioSegment>().let {
        it.bind(SimpleObjectProperty(segments))
        it.emptyProperty().not()
    }
    val selectedSegments = FXCollections.observableArrayList<AudioSegment>()
    val selectedCount = selectedSegments.sizeProperty
    val snackBarMessages = PublishSubject.create<String>()

    fun importFile(file: File) {
        if (file.extension != "wav" && file.extension != "WAV") {
            snackBarMessages.onNext(messages["not_wav_file"])
            return
        }

        if (segments.map { it.src.name }.contains(file.name)) {
            snackBarMessages.onNext(messages["file_already_imported"])
            return
        }

        GetWavSegments(file)
            .segments()
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .onErrorReturn {
                snackBarMessages.onNext(messages["import_error"])
                listOf()
            }
            .doOnSuccess { retrieved ->
                val new = retrieved.filter { !segments.map { it.label }.contains(it.label) }
                if (new.isEmpty()) snackBarMessages.onNext(messages["label_already_exists"])
                segments.addAll(new)
                segments.setAll(segments.sortedBy { it.label })
            }
            .subscribe()
    }

    fun split(outputDir: File) {
        ExportSegments(selectedSegments)
            .exportSeparate(outputDir)
            .observeOnFx()
            .subscribe {
                snackBarMessages.onNext(messages["done_exporting"])
            }
        clearSelected()
    }

    fun merge(outputDir: File) {
        ExportSegments(selectedSegments)
            .exportMerged(outputDir)
            .observeOnFx()
            .subscribe { result ->
                if (result == ExportSegments.MergeResult.SUCCESS) {
                    snackBarMessages.onNext(messages["done_exporting"])
                }
            }
        clearSelected()
    }

    fun deleteSelected() {
        segments.removeAll(selectedSegments)
        clearSelected()
    }

    fun select(segment: AudioSegment) {
        if (!selectedSegments.contains(segment)) selectedSegments.add(segment)
        selectedSegments.sortBy { it.label }
    }

    fun deselect(segment: AudioSegment) {
        selectedSegments.remove(segment)
    }

    fun selectAll() {
        selectedSegments.clear()
        selectedSegments.addAll(segments)
    }

    fun clearSelected() {
        selectedSegments.clear()
    }

    fun reset() {
        segments.clear()
    }
}