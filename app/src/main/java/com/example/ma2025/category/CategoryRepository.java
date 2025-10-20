package com.example.ma2025.category;

import com.example.ma2025.model.Category;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class CategoryRepository {
    private final CollectionReference categoryCollection;

    public CategoryRepository(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        categoryCollection = db.collection("categories"); // ovakvo je ime kolekcije u firestoru napravljene
    }

    public void addCategory(Category category){
        if(category.getId() == null || category.getId().isEmpty()){
            String id = categoryCollection.document().getId(); //ova linija generise novi id
            category.setId(id);
        }
        categoryCollection.document(category.getId()).set(category);
    }

    public void updateCategory(Category category){
        categoryCollection.document(category.getId()).set(category);
    }

    public void deleteCategory(Category category){
        categoryCollection.document(category.getId()).delete();
    }

    public  CollectionReference getAllCategories(){
        return categoryCollection;
    }

    public Query getCategoriesByUserId(String userId) {
        return categoryCollection.whereEqualTo("userId", userId);
    }

}
