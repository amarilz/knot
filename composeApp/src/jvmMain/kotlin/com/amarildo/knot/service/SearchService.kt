package com.amarildo.knot.service

import com.amarildo.knot.data.Information
import java.io.File
import java.text.Normalizer
import java.util.Locale

class SearchService(databasePath: String) {

    private var informations: List<Information>

    init {
        informations = try {
            parseFileWithSeparators(databasePath)
        } catch (ex: Exception) {
            emptyList()
        }
    }

    fun getAll(): List<Information> = informations

    fun search(rawQuery: String): List<Information> {
        val queryWords = tokenize(rawQuery)
        if (queryWords.isEmpty()) return emptyList()

        return informations.asSequence()
            .mapNotNull { info ->
                val keyNorm = normalize(info.key)
                var score = 0
                for (q in queryWords) {
                    if (keyNorm.contains(q)) score++
                }
                if (score > 0) info.copy(score = score) else null
            }
            .sortedByDescending { it.score }
            .toList()
    }

    fun normalize(s: String): String {
        val lower = s.lowercase(Locale.ROOT)
        val norm = Normalizer.normalize(lower, Normalizer.Form.NFD)
        return norm.replace("\\p{M}+".toRegex(), "")
    }

    fun tokenize(text: String): List<String> = normalize(text)
        .split(Regex("\\s+"))
        .asSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toList()

    private fun parseFileWithSeparators(filePath: String): List<Information> {
        val content = File(filePath).readText()
        return content.split("---")
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { section ->
                val lines = section.lines()
                val firstLine = lines.firstOrNull()?.trim().orEmpty()
                val key = firstLine.replace("**", "").trim()
                val value = lines.drop(1).joinToString("\n").trim()
                Information(key = key, value = value)
            }
            .filter { it.key.isNotEmpty() }
            .toList()
    }
}
