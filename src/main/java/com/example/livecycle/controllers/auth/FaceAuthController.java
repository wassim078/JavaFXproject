package com.example.livecycle.controllers.auth;

import com.example.livecycle.controllers.frontoffice.UserDashboardController;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.HOGDescriptor;
import org.opencv.videoio.VideoCapture;
import com.example.livecycle.entities.User;
import com.example.livecycle.services.UserService;
import com.example.livecycle.utils.SessionManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicBoolean;

public class FaceAuthController {
    @FXML private ImageView cameraView;
    @FXML private Label statusLabel;
    @FXML private Button actionButton;

    private VideoCapture capture;
    private final UserService userService = new UserService();
    private CascadeClassifier faceDetector;
    private byte[] capturedEncoding;
    private boolean isRegistration = false;
    private final AtomicBoolean isAuthenticating = new AtomicBoolean(false);
    private final AtomicBoolean isAuthenticated = new AtomicBoolean(false);
    private final AtomicBoolean isAuthenticationFailed = new AtomicBoolean(false); // New flag for failed authentication
    private Thread cameraThread;

    public void initialize() {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            initializeFaceDetector();
            startCamera();
        } catch (Exception e) {
            handleInitializationError(e);
        }
    }

    private void initializeFaceDetector() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/haarcascade_frontalface_alt.xml")) {
            if (is == null) {
                throw new IOException("Haar cascade file not found in resources");
            }

            File cascadeFile = File.createTempFile("haarcascade_", ".xml");
            cascadeFile.deleteOnExit();
            Files.copy(is, cascadeFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            faceDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());
            if (faceDetector.empty()) {
                throw new IOException("Failed to load Haar cascade classifier");
            }
        }
    }

    private void handleInitializationError(Exception e) {
        Platform.runLater(() -> {
            statusLabel.setText("Initialization Error: " + e.getMessage());
            showAlert("Initialization Error", e.getMessage());
        });
        e.printStackTrace();
    }

    public void setRegistrationMode(boolean isRegistration) {
        this.isRegistration = isRegistration;
        Platform.runLater(() -> {
            if (isRegistration) {
                actionButton.setText("Register Face");
                actionButton.setOnAction(e -> handleFaceRegistration());
                statusLabel.setText("Face Registration – look directly at camera");
            } else {
                actionButton.setText("Authenticate");
                actionButton.setOnAction(e -> handleFaceLogin());
                statusLabel.setText("Face Authentication – position your face in frame");
            }
        });
    }

    private void startCamera() {
        capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            Platform.runLater(() -> {
                statusLabel.setText("Error: Camera not opened");
                showAlert("Camera Error", "Unable to open camera");
            });
            return;
        }

        cameraThread = new Thread(() -> {
            Mat frame = new Mat();
            while (!Thread.currentThread().isInterrupted() && capture.isOpened()) {
                if (capture.read(frame) && !frame.empty()) {
                    processFrame(frame);
                    updateImageView(frame);
                } else {
                    System.err.println("Frame read failed");
                    break;
                }
            }
        });
        cameraThread.setDaemon(true);
        cameraThread.start();
    }

    private void processFrame(Mat frame) {
        if (faceDetector == null || isAuthenticated.get() || isAuthenticationFailed.get()) return;

        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(frame, faceDetections, 1.05, 2, 0, new Size(20, 20), new Size());

        Rect[] faces = faceDetections.toArray();
        boolean faceDetected = faces.length > 0;

        if (faceDetected) {
            Rect rect = faces[0];
            highlightFace(frame, rect);
            Mat faceRegion = new Mat(frame, rect);
            capturedEncoding = extractFaceEmbedding(faceRegion);

            if (!isRegistration && !isAuthenticating.get()) {
                Platform.runLater(this::handleFaceLogin);
            }
        } else {
            capturedEncoding = null;
        }
    }

    private void highlightFace(Mat frame, Rect rect) {
        Imgproc.rectangle(frame,
                new Point(rect.x, rect.y),
                new Point(rect.x + rect.width, rect.y + rect.height),
                new Scalar(0, 255, 0), 2
        );
    }

    private byte[] extractFaceEmbedding(Mat face) {
        Mat resizedFace = new Mat();
        Imgproc.resize(face, resizedFace, new Size(64, 128));
        Imgproc.cvtColor(resizedFace, resizedFace, Imgproc.COLOR_BGR2GRAY);

        HOGDescriptor hog = new HOGDescriptor(
                new Size(64, 128), new Size(16, 16), new Size(8, 8), new Size(8, 8), 9
        );

        MatOfFloat descriptors = new MatOfFloat();
        hog.compute(resizedFace, descriptors);

        if (descriptors.empty()) {
            throw new RuntimeException("HOG descriptors are empty");
        }

        float[] floatArray = descriptors.toArray();
        ByteBuffer byteBuffer = ByteBuffer.allocate(floatArray.length * 4)
                .order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.asFloatBuffer().put(floatArray);
        return byteBuffer.array();
    }

    private void updateImageView(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        Image image = new Image(new ByteArrayInputStream(buffer.toArray()));
        Platform.runLater(() -> cameraView.setImage(image));
    }

    private void handleFaceLogin() {
        if (isAuthenticating.get() || capturedEncoding == null || isAuthenticated.get() || isAuthenticationFailed.get()) return;

        isAuthenticating.set(true);
        Platform.runLater(() -> statusLabel.setText("Authenticating..."));

        Task<User> authTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                return userService.authenticateByFace(capturedEncoding);
            }
        };

        authTask.setOnSucceeded(event -> {
            isAuthenticating.set(false);
            User user = authTask.getValue();
            if (user != null) {
                Platform.runLater(() -> {
                    statusLabel.setText("Authentication successful!");
                    isAuthenticated.set(true);
                    handleSuccessfulLogin(user);
                });
            } else {
                Platform.runLater(() -> {
                    statusLabel.setText("Authentication failed");
                    isAuthenticationFailed.set(true); // Mark as failed
                    showAlert("Error", "No matching face found");
                    shutdown(); // Stop camera
                    closeWindow(); // Close the window
                });
            }
        });

        authTask.setOnFailed(event -> {
            isAuthenticating.set(false);
            Throwable e = authTask.getException();
            Platform.runLater(() -> {
                statusLabel.setText("Error: " + e.getMessage());
                isAuthenticationFailed.set(true); // Mark as failed
                showAlert("Error", e.getMessage());
                shutdown(); // Stop camera
                closeWindow(); // Close the window
            });
        });

        new Thread(authTask).start();
    }

    @FXML
    private void handleFaceRegistration() {
        if (capturedEncoding == null) {
            showAlert("Error", "No face detected!");
            return;
        }

        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showAlert("Error", "User not logged in!");
            return;
        }

        try {
            userService.storeFaceEncoding(currentUser.getId(), capturedEncoding);
            Platform.runLater(() -> {
                statusLabel.setText("Registration successful!");
                showAlert("Success", "Face registered successfully");
                shutdown(); // Stop camera
                closeWindow(); // Close window after successful registration
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                statusLabel.setText("Error: " + e.getMessage());
                showAlert("Registration Failed", e.getMessage());
            });
        }
    }

    private void handleSuccessfulLogin(User user) {
        SessionManager.saveSession(user.getId());
        shutdown();
        closeWindowAndRedirect(user);
    }

    private void closeWindowAndRedirect(User user) {
        try {
            Stage currentStage = (Stage) cameraView.getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/frontoffice/Home.fxml"));
            Stage newStage = new Stage();
            newStage.setScene(new Scene(loader.load()));
            UserDashboardController controller = loader.getController();
            controller.initData(user);
            newStage.show();
        } catch (IOException e) {
            Platform.runLater(() -> showAlert("Navigation Error", "Could not load dashboard: " + e.getMessage()));
        }
    }

    private void closeWindow() {
        Stage currentStage = (Stage) cameraView.getScene().getWindow();
        currentStage.close();
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void shutdown() {
        if (capture != null && capture.isOpened()) {
            capture.release();
        }
        if (cameraThread != null) {
            cameraThread.interrupt();
        }
    }

    @FXML
    private void handleCancel() {
        shutdown();
        Stage stage = (Stage) cameraView.getScene().getWindow();
        stage.close();
    }
}