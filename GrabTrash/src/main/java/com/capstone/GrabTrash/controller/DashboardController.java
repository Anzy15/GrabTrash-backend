package com.capstone.GrabTrash.controller;

import com.capstone.GrabTrash.service.CollectionService;
import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final CollectionService collectionService;

    public DashboardController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    // Get driver's daily collection statistics
    @GetMapping("/driver/{driverId}/daily")
    public ResponseEntity<?> getDriverDailyStats(
            @PathVariable String driverId,
            @RequestParam(required = false) Date date) {
        try {
            // If no date provided, use current date
            if (date == null) {
                date = new Date();
            }

            Map<String, Object> stats = collectionService.getDriverDailyStats(driverId, date);
            return ResponseEntity.ok(stats);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .body("Error fetching driver statistics: " + e.getMessage());
        }
    }

    // Get barangay collection statistics
    @GetMapping("/barangay/{barangay}")
    public ResponseEntity<?> getBarangayStats(@PathVariable String barangay) {
        try {
            Map<String, Object> stats = collectionService.getBarangayStats(barangay);
            return ResponseEntity.ok(stats);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .body("Error fetching barangay statistics: " + e.getMessage());
        }
    }

    // Get overall collection statistics
    @GetMapping("/overall")
    public ResponseEntity<?> getOverallStats() {
        try {
            Map<String, Object> stats = collectionService.getOverallStats();
            return ResponseEntity.ok(stats);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .body("Error fetching overall statistics: " + e.getMessage());
        }
    }
} 
