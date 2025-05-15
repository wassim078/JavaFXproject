
livecycle/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com.example.livecycle/
│   │   │   │   ├── controllers/
│   │   │   │   │   ├── auth/
│   │   │   │   │   │   ├── FaceAuthController.java
│   │   │   │   │   │   ├── ForgotPasswordController.java
│   │   │   │   │   │   ├── LoginController.java
│   │   │   │   │   │   ├── RegisterController.java
│   │   │   │   │   │   └── ResetPasswordController.java
│   │   │   │   │   ├── backoffice/
│   │   │   │   │   │   ├── AdminDashboardController.java
│   │   │   │   │   │   ├── AdminDefault.java
│   │   │   │   │   │   ├── AdminForumController.java
│   │   │   │   │   │   ├── AnnonceManagementController.java
│   │   │   │   │   │   ├── CategorieForumController.java
│   │   │   │   │   │   ├── CategoryAnnonceManagement.java
│   │   │   │   │   │   ├── CategoryCollectManagement.java
│   │   │   │   │   │   ├── CategoryFormController.java
│   │   │   │   │   │   ├── CategoryForumController.java
│   │   │   │   │   │   ├── CollectManagement.java
│   │   │   │   │   │   ├── CommandeManagementController.java
│   │   │   │   │   │   ├── EditAnnonceController.java
│   │   │   │   │   │   ├── EditCollectController.java
│   │   │   │   │   │   └── EditCommandeController.java
│   │   │   │   │   ├── frontoffice/
│   │   │   │   │   │   ├── AnnonceManagementController.java
│   │   │   │   │   │   ├── ChatBotController.java
│   │   │   │   │   │   ├── CheckoutController.java
│   │   │   │   │   │   ├── CommandeController.java
│   │   │   │   │   │   ├── CreateAnnonceController.java
│   │   │   │   │   │   ├── CreateCollectController.java
│   │   │   │   │   │   ├── CreateComplaintController.java
│   │   │   │   │   │   ├── EditAnnonceController.java
│   │   │   │   │   │   ├── EditCollectController.java
│   │   │   │   │   │   ├── EditProfileController.java
│   │   │   │   │   │   ├── ForumController.java
│   │   │   │   │   │   ├── GoogleCalendarService.java
│   │   │   │   │   │   ├── MyAnnouncementsController.java
│   │   │   │   │   │   ├── MyComplaintsController.java
│   │   │   │   │   │   ├── NotificationController.java
│   │   │   │   │   │   ├── PanierController.java
│   │   │   │   │   │   ├── PaymentController.java
│   │   │   │   │   │   ├── ShowAllCollectsController.java
│   │   │   │   │   │   └── ShowMyCollectsController.java
│   │   │   │   ├── entities/
│   │   │   │   │   ├── Annonce.java
│   │   │   │   │   ├── CategorieCollect.java
│   │   │   │   │   ├── Category.java
│   │   │   │   │   ├── CategoryForum.java
│   │   │   │   │   ├── Collect.java
│   │   │   │   │   ├── Commande.java
│   │   │   │   │   ├── Comment.java
│   │   │   │   │   ├── Panier.java
│   │   │   │   │   ├── Post.java
│   │   │   │   │   ├── Reclamation.java
│   │   │   │   │   ├── Reponse.java
│   │   │   │   │   └── User.java
│   │   │   │   ├── services/
│   │   │   │   │   ├── AnnonceService.java
│   │   │   │   │   ├── CategorieCollectService.java
│   │   │   │   │   ├── CategorieForumService.java
│   │   │   │   │   ├── CategoryAnnonceService.java
│   │   │   │   │   ├── CollectService.java
│   │   │   │   │   ├── CommandeService.java
│   │   │   │   │   ├── EmailService.java
│   │   │   │   │   ├── PanierService.java
│   │   │   │   │   ├── ReclamationDAO.java
│   │   │   │   │   ├── Service.java
│   │   │   │   │   ├── SMSService.java
│   │   │   │   │   ├── StripeService.java
│   │   │   │   │   └── UserService.java
│   │   │   │   └── utils/
│   │   │   │       └── [Utility classes]
│   │   ├── resources/
│   │   │   ├── com/example/livecycle/
│   │   │   │   ├── auth/
│   │   │   │   ├── backoffice/
│   │   │   │   ├── frontoffice/
│   │   │   │   └── [FXML files matching controllers]
│   │   │   ├── annonce_management.fxml
│   │   │   ├── chatbot.fxml
│   │   │   ├── checkout.form.fxml
│   │   │   ├── commande_management.fxml
│   │   │   ├── create_annonce.fxml
│   │   │   ├── create_collect.fxml
│   │   │   ├── create_complaint.fxml
│   │   │   ├── dashboard.fxml
│   │   │   ├── edit_annonce.fxml
│   │   │   ├── edit_collect.fxml
│   │   │   ├── edit_profile.fxml
│   │   │   ├── forum.fxml
│   │   │   ├── Home.fxml
│   │   │   ├── my_announcements.fxml
│   │   │   ├── my_complaints.fxml
│   │   │   ├── notification.fxml
│   │   │   ├── panier.fxml
│   │   │   ├── payment.form.fxml
│   │   │   ├── showAllCollects.fxml
│   │   │   ├── showMyCollects.fxml
│   │   │   ├── UserCategoryView.fxml
│   │   │   └── WhatsApp.fxml
├── pom.xml
└── module-info.java

🛠️ Technology Stack
Core Technologies
JavaFX 17 - GUI framework
Maven - Build and dependency management
Java 17 - Programming language

Database
MySQL - Database management system
JDBC - Database connectivity

Security
JBCrypt - Password hashing
Face Authentication - Biometric security
Twilio - SMS verification

UI Components
Ikonli - Icon library (FontAwesome, Material Design)
JavaFX WebView - Embedded web content

External Services
Stripe - Payment processing
Google Calendar API - Event management
Twilio - SMS services
OkHttp - HTTP client
JavaMail - Email services

Utilities
Gson - JSON processing
iTextPDF - PDF generation
ZXing - Barcode generation
PDFBox - PDF manipulation
OpenCV - Computer vision (for face auth)

📋 Key Features

Authentication System
Face authentication
Traditional login/registration
Password reset via email/SMS
Secure password hashing

Backoffice (Admin) Features
Dashboard with analytics
Announcement management
Forum management
Category management
Collection management
Order management
Complaint management
User management

Frontoffice (User) Features
Announcement browsing/creation
Forum participation
Shopping cart
Checkout and payment
Order tracking
Complaint submission
Profile management
Calendar integration
WhatsApp integration
Chatbot

Utility Features
PDF generation
Barcode generation
Email notifications
SMS notifications
Payment processing

🚀 Getting Started
Prerequisites
Java 17 JDK
Maven 3.6+
MySQL 8.0+

Installation
Clone the repository
Configure MySQL database connection in your application
Build the project:
mvn clean install
Run the application:
mvn javafx:run
🔧 Configuration
Database Setup
Configure your MySQL connection in the appropriate service class (likely UserService or similar).

External Services
Set up API keys for:
Stripe (payment processing)
Twilio (SMS services)
Google Calendar API

