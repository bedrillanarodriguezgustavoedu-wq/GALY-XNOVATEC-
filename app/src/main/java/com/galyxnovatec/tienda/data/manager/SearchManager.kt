package com.galyxnovatec.tienda.data.manager

import androidx.compose.runtime.mutableStateListOf

object SearchManager {
    private val _recentSearches = mutableStateListOf<String>()
    val recentSearches: List<String> get() = _recentSearches

    fun addSearch(query: String) {
        if (query.isBlank()) return
        _recentSearches.remove(query)
        _recentSearches.add(0, query)
        if (_recentSearches.size > 5) {
            _recentSearches.removeAt(5)
        }
    }

    fun clearHistory() {
        _recentSearches.clear()
    }
}