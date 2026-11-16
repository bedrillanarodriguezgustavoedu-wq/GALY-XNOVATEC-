package com.galyxnovatec.tienda.data.manager

import android.content.Context
import android.util.Log
import com.galyxnovatec.tienda.data.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object LocalCacheManager {
    private const val PREF_NAME = "galy_offline_cache"
    private const val KEY_PRODUCTS = "cache_products"
    private const val KEY_CATEGORIES = "cache_categories"
    private const val KEY_BRANDS = "cache_brands"
    private const val KEY_BANNERS = "cache_banners"
    private const val KEY_CONTACT = "cache_contact"
    private val gson = Gson()

    fun guardarProductos(context: Context, lista: List<Producto>) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val json = gson.toJson(lista.filter { it.id > 0 })
            prefs.edit().putString(KEY_PRODUCTS, json).apply()
        } catch (e: Exception) {
            Log.e("GALY_CACHE", "Error guardando productos en cache: ${e.message}")
        }
    }

    fun obtenerProductos(context: Context): List<Producto> {
        return try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val json = prefs.getString(KEY_PRODUCTS, null) ?: return emptyList()
            val type = object : TypeToken<List<Producto>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun guardarCategorias(context: Context, lista: List<Categoria>) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val json = gson.toJson(lista)
            prefs.edit().putString(KEY_CATEGORIES, json).apply()
        } catch (e: Exception) {
            Log.e("GALY_CACHE", "Error guardando categorias en cache: ${e.message}")
        }
    }

    fun obtenerCategorias(context: Context): List<Categoria> {
        return try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val json = prefs.getString(KEY_CATEGORIES, null) ?: return emptyList()
            val type = object : TypeToken<List<Categoria>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun guardarBanners(context: Context, lista: List<Banner>) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val json = gson.toJson(lista)
            prefs.edit().putString(KEY_BANNERS, json).apply()
        } catch (e: Exception) {
            Log.e("GALY_CACHE", "Error guardando banners en cache: ${e.message}")
        }
    }

    fun obtenerBanners(context: Context): List<Banner> {
        return try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val json = prefs.getString(KEY_BANNERS, null) ?: return emptyList()
            val type = object : TypeToken<List<Banner>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun guardarMarcas(context: Context, lista: List<MarcaCompartida>) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val json = gson.toJson(lista)
            prefs.edit().putString(KEY_BRANDS, json).apply()
        } catch (e: Exception) {
            Log.e("GALY_CACHE", "Error guardando marcas en cache: ${e.message}")
        }
    }

    fun obtenerMarcas(context: Context): List<MarcaCompartida> {
        return try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val json = prefs.getString(KEY_BRANDS, null) ?: return emptyList()
            val type = object : TypeToken<List<MarcaCompartida>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
