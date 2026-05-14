package com.example.myapplication.data.remote

import com.example.myapplication.data.local.TreeEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val treeCollection = firestore.collection("trees")

    suspend fun addTree(tree: TreeEntity): Boolean {
        return try {
            treeCollection.document(tree.id).set(tree).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAllTrees(): List<TreeEntity> {
        return try {
            val snapshot = treeCollection.get().await()
            snapshot.toObjects(TreeEntity::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
