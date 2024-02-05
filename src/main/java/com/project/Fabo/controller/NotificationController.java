package com.project.Fabo.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.Fabo.service.AdminCommentService;

@RestController
public class NotificationController {
	
	 @Autowired
	    private AdminCommentService adminCommentsService;
	 
	 private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);


	 @GetMapping("/api/notifications")
	    public List<String> getNotifications() {
	        List<String> notifications = adminCommentsService.getNotificationsForCurrentUser();
	        logger.debug("Retrieved notifications: {}", notifications);
	        return notifications;
	    }
	 
/*	// Controller for handling notifications
	 @RestController
	 @RequestMapping("/api/notifications")
	 public class NotificationController {

	     @Autowired
	     private NotificationService notificationService;

	     // Endpoint to fetch all unread notifications
	     @GetMapping
	     public List<Notification> getUnreadNotifications() {
	         return notificationService.getUnreadNotifications();
	     }

	     // Endpoint to mark a notification as viewed
	     @PutMapping("/{notificationId}/viewed")
	     public ResponseEntity<?> markNotificationAsViewed(@PathVariable Long notificationId) {
	         try {
	             notificationService.markNotificationAsViewed(notificationId);
	             return ResponseEntity.ok().build();
	         } catch (Exception e) {
	             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to mark notification as viewed");
	         }
	     }
	 }

	 // Notification entity class
	 @Entity
	 public class Notification {
	     @Id
	     @GeneratedValue(strategy = GenerationType.IDENTITY)
	     private Long id;

	     private String text;

	     private boolean viewed;

	     // Getters and setters
	 }

	 // Notification service interface
	 public interface NotificationService {
	     List<Notification> getUnreadNotifications();

	     void markNotificationAsViewed(Long notificationId);
	 }

	 // Notification service implementation
	 @Service
	 public class NotificationServiceImpl implements NotificationService {

	     @Autowired
	     private NotificationRepository notificationRepository;

	     @Override
	     public List<Notification> getUnreadNotifications() {
	         return notificationRepository.findByViewedFalse();
	     }

	     @Override
	     public void markNotificationAsViewed(Long notificationId) {
	         Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
	         if (optionalNotification.isPresent()) {
	             Notification notification = optionalNotification.get();
	             notification.setViewed(true);
	             notificationRepository.save(notification);
	         } else {
	             throw new EntityNotFoundException("Notification not found with id: " + notificationId);
	         }
	     }
	 }

	 // Notification repository interface
	 public interface NotificationRepository extends JpaRepository<Notification, Long> {
	     List<Notification> findByViewedFalse();
	 }
	 
	 	<script>
		
		document.addEventListener("DOMContentLoaded", function() {
    const notificationsDropdownToggle = document.getElementById("notificationsDropdownToggle");
    const notificationsDropdown = document.getElementById("notificationsDropdown");

    notificationsDropdownToggle.addEventListener("click", function() {
        if (notificationsDropdown.style.display === "block") {
            notificationsDropdown.style.display = "none";
        } else {
            fetchNotifications();
        }
    });

    // Function to fetch and display notifications
    function fetchNotifications() {
        fetch("/api/notifications")
            .then(response => {
                if (!response.ok) {
                    throw new Error("Failed to fetch notifications");
                }
                return response.json();
            })
            .then(notifications => {
                const notificationsContainer = document.getElementById("notificationsDropdown");
                notificationsContainer.innerHTML = ""; // Clear existing notifications

                notifications.forEach((notification, index) => {
                    if (!notification.viewed) {
                        const notificationItem = document.createElement("a");
                        notificationItem.classList.add("dropdown-item");
                        notificationItem.textContent = `${index + 1}. ${notification.text}`; // Append index and notification text
                        notificationItem.addEventListener("click", () => markNotificationAsViewed(notification.id));
                        notificationsContainer.appendChild(notificationItem);
                    }
                });

                document.getElementById("notificationCount").textContent = notifications.length; // Update notification count
                notificationsDropdown.style.display = "block"; // Show dropdown after fetching notifications
            })
            .catch(error => {
                console.error("Error fetching notifications:", error);
                // Handle error: Display a message or retry fetching notifications
            });
    }

    // Function to mark notification as viewed
    function markNotificationAsViewed(notificationId) {
        fetch(`/api/notifications/${notificationId}/viewed`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ viewed: true })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error("Failed to mark notification as viewed");
            }
            // Refresh notifications after marking as viewed
            fetchNotifications();
        })
        .catch(error => {
            console.error("Error marking notification as viewed:", error);
            // Handle error: Display a message or retry marking notification as viewed
        });
    }
});

	</script>
*/
	
}
