package com.capstone.GrabTrash.service;

import com.capstone.GrabTrash.model.CollectionRecord;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class CollectionService {

    private final Firestore firestore;

    @Autowired
    public CollectionService(Firestore firestore) {
        this.firestore = firestore;
    }

    // Get daily collection statistics for a driver
    public Map<String, Object> getDriverDailyStats(String driverId, Date date) throws ExecutionException, InterruptedException {
        CollectionReference collections = firestore.collection("collections");
        
        // Create start and end of day timestamps
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Timestamp startOfDay = Timestamp.of(calendar.getTime());
        
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Timestamp endOfDay = Timestamp.of(calendar.getTime());

        // Query collections for the driver on the specified date
        Query query = collections
                .whereEqualTo("driverId", driverId)
                .whereEqualTo("collectionType", "PRIVATE")
                .whereEqualTo("status", "COMPLETED")
                .whereGreaterThanOrEqualTo("collectionDate", startOfDay)
                .whereLessThanOrEqualTo("collectionDate", endOfDay);

        ApiFuture<QuerySnapshot> future = query.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        // Calculate statistics
        int totalCollections = documents.size();
        double totalWeight = 0.0;
        Map<String, Integer> barangayCounts = new HashMap<>();

        for (QueryDocumentSnapshot document : documents) {
            CollectionRecord record = document.toObject(CollectionRecord.class);
            totalWeight += record.getWeight();
            barangayCounts.merge(record.getBarangay(), 1, Integer::sum);
        }

        // Prepare response
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCollections", totalCollections);
        stats.put("totalWeight", totalWeight);
        stats.put("barangayBreakdown", barangayCounts);
        stats.put("date", date);

        return stats;
    }

    // Get barangay-wide collection statistics
    public Map<String, Object> getBarangayStats(String barangay) throws ExecutionException, InterruptedException {
        CollectionReference collections = firestore.collection("collections");
        
        // Query all completed private collections for the barangay
        Query query = collections
                .whereEqualTo("barangay", barangay)
                .whereEqualTo("collectionType", "PRIVATE")
                .whereEqualTo("status", "COMPLETED");

        ApiFuture<QuerySnapshot> future = query.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        // Calculate statistics
        int totalCollections = documents.size();
        double totalWeight = 0.0;
        Map<String, Integer> driverBreakdown = new HashMap<>();

        for (QueryDocumentSnapshot document : documents) {
            CollectionRecord record = document.toObject(CollectionRecord.class);
            totalWeight += record.getWeight();
            driverBreakdown.merge(record.getDriverId(), 1, Integer::sum);
        }

        // Prepare response
        Map<String, Object> stats = new HashMap<>();
        stats.put("barangay", barangay);
        stats.put("totalCollections", totalCollections);
        stats.put("totalWeight", totalWeight);
        stats.put("driverBreakdown", driverBreakdown);

        return stats;
    }

    // Get overall collection statistics
    public Map<String, Object> getOverallStats() throws ExecutionException, InterruptedException {
        CollectionReference collections = firestore.collection("collections");
        
        // Query all completed private collections
        Query query = collections
                .whereEqualTo("collectionType", "PRIVATE")
                .whereEqualTo("status", "COMPLETED");

        ApiFuture<QuerySnapshot> future = query.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        // Calculate statistics
        int totalCollections = documents.size();
        double totalWeight = 0.0;
        Map<String, Integer> barangayBreakdown = new HashMap<>();
        Map<String, Integer> driverBreakdown = new HashMap<>();

        for (QueryDocumentSnapshot document : documents) {
            CollectionRecord record = document.toObject(CollectionRecord.class);
            totalWeight += record.getWeight();
            barangayBreakdown.merge(record.getBarangay(), 1, Integer::sum);
            driverBreakdown.merge(record.getDriverId(), 1, Integer::sum);
        }

        // Prepare response
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCollections", totalCollections);
        stats.put("totalWeight", totalWeight);
        stats.put("barangayBreakdown", barangayBreakdown);
        stats.put("driverBreakdown", driverBreakdown);

        return stats;
    }
} 