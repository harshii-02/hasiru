package com.example.myapplication.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.TreeEntity
import com.example.myapplication.data.remote.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class TreeViewModel(application: Application) : AndroidViewModel(application) {
    private val treeDao = AppDatabase.getDatabase(application).treeDao()
    private val firebaseRepository = FirebaseRepository()

    private val _trees = MutableStateFlow<List<TreeEntity>>(emptyList())
    val trees: StateFlow<List<TreeEntity>> = _trees.asStateFlow()

    init {
        // Collect from local database
        viewModelScope.launch {
            treeDao.getAllTrees().collect { localTrees ->
                _trees.value = localTrees
            }
        }
        
        // Fetch from cloud and cache locally
        syncTreesFromCloud()
    }

    private fun syncTreesFromCloud() {
        viewModelScope.launch {
            val cloudTrees = firebaseRepository.getAllTrees()
            cloudTrees.forEach { tree ->
                treeDao.insertTree(tree)
            }
        }
    }

    fun addTree(
        latitude: Double,
        longitude: Double,
        title: String,
        snippet: String,
        type: String,
        species: String = "Unknown",
        girth: Double = 0.0,
        health: String = "Healthy",
        isEmptyPit: Boolean = false
    ) {
        viewModelScope.launch {
            val newTree = TreeEntity(
                id = UUID.randomUUID().toString(),
                latitude = latitude,
                longitude = longitude,
                title = title,
                snippet = snippet,
                type = type,
                species = species,
                girth = girth,
                health = health,
                isEmptyPit = isEmptyPit
            )
            
            treeDao.insertTree(newTree)
            firebaseRepository.addTree(newTree)
        }
    }

    fun calculateOxygenScore(trees: List<TreeEntity>): Double {
        return trees.filter { !it.isEmptyPit }.sumOf { tree ->
            val factor = when (tree.species.lowercase()) {
                "neem", "ಬೇವು" -> 1.5
                "honge", "ಹೊಂಗೆ" -> 1.2
                "peepal", "ಅರಳಿ ಮರ" -> 2.0
                else -> 1.0
            }
            tree.girth * factor
        }
    }
}
