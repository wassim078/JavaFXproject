package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.CategoryForum;
import com.example.livecycle.entities.Comment;
import com.example.livecycle.entities.Post;
import com.example.livecycle.utils.DBConnexion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

import java.net.HttpURLConnection;
import java.net.URLEncoder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.net.URL;
import java.io.InputStreamReader;
import java.util.ResourceBundle;

import com.google.gson.JsonParser;

public class ForumController implements Initializable {

    // Éléments UI pour les posts
    @FXML private Label lblCategoryName;
    @FXML private TableView<Post> tablePosts;
    @FXML private TableColumn<Post, Integer> colId;
    @FXML private TableColumn<Post, String> colTitle, colContenu, colCreatedAt;
    @FXML private TextField txtPostTitle, txtPostContent;
    @FXML private Label lblPostId;
    @FXML private TableColumn<Post, Number> colRating;
    @FXML private TableColumn<Comment, Number> colLikes, colDislikes;

//    // Ajoutez ces variables de classe
//    @FXML private ComboBox<String> cbSourceLang;
//    @FXML private ComboBox<String> cbTargetLang;

    // Éléments UI pour les commentaires
    @FXML private TextField txtComment;
    @FXML private TableView<Comment> tableComments;
    @FXML private TableColumn<Comment, Integer> colCommentId;
    @FXML private TableColumn<Comment, String> colCommentContent, colCommentDate;

    private ObservableList<Comment> comments = FXCollections.observableArrayList();
    // Populate with Comment objects
    private Post selectedPost;
    private CategoryForum category;
    // Au début de la classe avec les autres @FXML
    @FXML private ComboBox<String> cbCommentTargetLang;

    // Ajoutez cette map comme variable de classe
    private final Map<String, String> langCodes = new LinkedHashMap<String, String>() {{
        put("Français", "fr");
        put("Anglais", "en");
        put("Espagnol", "es");
        put("Allemand", "de");
        put("Arabe", "ar");
    }};
    /*public void setCategory(CategoryForum category) {
        this.category = category;
        lblCategoryName.setText("Forum: " + category.getName());
        initializeTable();
        loadPosts();
    }*/
    public void setCategory(CategoryForum category) {
        this.category = category;
        lblCategoryName.setText("Forum: " + category.getName());
        initializeTable();
        loadPosts(); // Déplacer l'appel ici
    }

   /* private void initializeTable() {
        // Configuration des colonnes pour les posts
        colId.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        colTitle.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        colContenu.setCellValueFactory(cellData -> cellData.getValue().contenuProperty());
        colCreatedAt.setCellValueFactory(cellData -> cellData.getValue().createdAtProperty());

        // Configuration des colonnes pour les commentaires
        colCommentId.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        colCommentContent.setCellValueFactory(cellData -> cellData.getValue().contentProperty());
        colCommentDate.setCellValueFactory(cellData -> cellData.getValue().createdAtProperty());
        // Dans initializeTable()
        TableColumn<Post, Number> colRating = new TableColumn<>("Rating");
        colRating.setCellValueFactory(cellData -> cellData.getValue().averageRatingProperty());
        tablePosts.getColumns().add(colRating);

        // Dans initializeTable()
        TableColumn<Comment, Number> colLikes = new TableColumn<>("Likes");
        colLikes.setCellValueFactory(cellData -> cellData.getValue().likesProperty());
        TableColumn<Comment, Number> colDislikes = new TableColumn<>("Dislikes");
        colDislikes.setCellValueFactory(cellData -> cellData.getValue().dislikesProperty());
        tableComments.getColumns().addAll(colLikes, colDislikes);
    }
*/
   private void initializeTable() {
       colId.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
       colTitle.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
       colContenu.setCellValueFactory(cellData -> cellData.getValue().contenuProperty());
       colCreatedAt.setCellValueFactory(cellData -> cellData.getValue().createdAtProperty());
       colRating.setCellValueFactory(cellData -> cellData.getValue().averageRatingProperty());

       colCommentId.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
       colCommentContent.setCellValueFactory(cellData -> cellData.getValue().contentProperty());
       colCommentDate.setCellValueFactory(cellData -> cellData.getValue().createdAtProperty());
       colLikes.setCellValueFactory(cellData -> cellData.getValue().likesProperty());
       colDislikes.setCellValueFactory(cellData -> cellData.getValue().dislikesProperty());

       Map<String, String> languages = new LinkedHashMap<>();
       languages.put("Français", "fr");
       languages.put("Anglais", "en");
       languages.put("Espagnol", "es");
       languages.put("Allemand", "de");
       languages.put("Arabe", "ar");

//       cbSourceLang.getItems().addAll(languages.keySet());
//       cbTargetLang.getItems().addAll(languages.keySet());
//       cbSourceLang.setValue("Français");
//       cbTargetLang.setValue("Anglais");
   }
    // Ajoutez cette méthode INITIALIZE
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeTable();
        initializeLanguages(); // Appel de la nouvelle méthode des langues
       // loadPosts();
    }

    // Ajoutez cette nouvelle méthode
    private void initializeLanguages() {
        // Initialisation des ComboBox pour les posts
//        cbSourceLang.getItems().addAll(langCodes.keySet());
//        cbTargetLang.getItems().addAll(langCodes.keySet());
//        cbSourceLang.setValue("Français");
//        cbTargetLang.setValue("Anglais");
//
//        // Initialisation spécifique aux commentaires
//        cbCommentTargetLang.getItems().addAll(langCodes.keySet());
//        cbCommentTargetLang.setValue("Anglais");
    }
  /*  private void loadPosts() {
        ObservableList<Post> posts = FXCollections.observableArrayList();
        String query = "SELECT * FROM post WHERE forum_id = ?";

        try (Connection con = DBConnexion.getCon();
             PreparedStatement st = con.prepareStatement(query)) {

            st.setInt(1, category.getId());
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                posts.add(new Post(
                        rs.getInt("id"),
                        rs.getInt("forum_id"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getString("contenu"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getDouble("average_rating") // Ajoutez cette colonne
                ));
            }
            tablePosts.setItems(posts);

        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage(), Alert.AlertType.ERROR);
        }
    }
*/

    private void loadPosts() {
        if (category == null) {
            showAlert("Erreur", "Catégorie non sélectionnée", Alert.AlertType.ERROR);
            return;
        }
        ObservableList<Post> posts = FXCollections.observableArrayList();
        String query = "SELECT * FROM post WHERE forum_id = ?";

        try (Connection con = DBConnexion.getCon();
             PreparedStatement st = con.prepareStatement(query)) {

            st.setInt(1, category.getId());
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                posts.add(new Post(
                        rs.getInt("id"),
                        rs.getInt("forum_id"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getString("contenu"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getDouble("average_rating")
                ));
            }
            tablePosts.setItems(posts);

        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    // CRUD pour les posts
    /*@FXML
    void createPost(ActionEvent event) {
        String title = txtPostTitle.getText();
        String content = txtPostContent.getText();

        if(title.isEmpty() || content.isEmpty()) {
            showAlert("Erreur", "Titre et contenu obligatoires", Alert.AlertType.ERROR);
            return;
        }

        String query = "INSERT INTO post (forum_id, user_id, title, contenu, created_at, updated_at) VALUES (?, ?, ?, ?, NOW(), NOW())";

        try (Connection con = DBConnexion.getCon();
             PreparedStatement st = con.prepareStatement(query)) {

            st.setInt(1, category.getId());
            st.setInt(2, 1); // ID utilisateur temporaire
            st.setString(3, title);
            st.setString(4, content);
            st.executeUpdate();

            loadPosts();
            clearPost(null);

        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage(), Alert.AlertType.ERROR);
        }
    }*/
    @FXML
    void createPost(ActionEvent event) {
        String title = txtPostTitle.getText().trim();
        String content = txtPostContent.getText().trim();

        // Validation des champs
        if (title.isEmpty() || content.isEmpty()) {
            showAlert("Erreur", "Titre et contenu obligatoires", Alert.AlertType.ERROR);
            return;
        }

        // Vérification de la catégorie
        if (category == null) {
            showAlert("Erreur", "Aucune catégorie sélectionnée", Alert.AlertType.ERROR);
            return;
        }

        // Utilisation d'un user_id VALIDE (3 ou 4 selon votre table)
        int validUserId = 3; // ⚠️ Remplacez par 4 ou par l'ID dynamique de l'utilisateur connecté

        String query = "INSERT INTO post (forum_id, user_id, title, contenu, created_at, updated_at) VALUES (?, ?, ?, ?, NOW(), NOW())";

        try (Connection con = DBConnexion.getCon();
             PreparedStatement st = con.prepareStatement(query)) {

            // Paramétrage des valeurs
            st.setInt(1, category.getId());
            st.setInt(2, validUserId); // ID utilisateur corrigé
            st.setString(3, title);
            st.setString(4, content);

            // Exécution
            int rowsAffected = st.executeUpdate();

            if (rowsAffected > 0) {
                loadPosts(); // Recharger les posts
                clearPost(null); // Vider les champs
            }

        } catch (SQLException e) {
            showAlert("Erreur SQL", "Erreur lors de la création du post :\n" + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur inattendue :\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void updatePost(ActionEvent event) {
        if(lblPostId.getText().isEmpty()) {
            showAlert("Erreur", "Sélectionnez un post à modifier", Alert.AlertType.ERROR);
            return;
        }

        String query = "UPDATE post SET title=?, contenu=?, updated_at=NOW() WHERE id=?";

        try (Connection con = DBConnexion.getCon();
             PreparedStatement st = con.prepareStatement(query)) {

            st.setString(1, txtPostTitle.getText());
            st.setString(2, txtPostContent.getText());
            st.setInt(3, Integer.parseInt(lblPostId.getText()));
            st.executeUpdate();

            loadPosts();
            clearPost(null);

        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void deletePost(ActionEvent event) {
        if(lblPostId.getText().isEmpty()) {
            showAlert("Erreur", "Sélectionnez un post à supprimer", Alert.AlertType.ERROR);
            return;
        }

        String query = "DELETE FROM post WHERE id=?";

        try (Connection con = DBConnexion.getCon();
             PreparedStatement st = con.prepareStatement(query)) {

            st.setInt(1, Integer.parseInt(lblPostId.getText()));
            st.executeUpdate();
            loadPosts();
            clearPost(null);

        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // CRUD pour les commentaires
   /* @FXML
    void createComment(ActionEvent event) {
        if(selectedPost == null) {
            showAlert("Erreur", "Sélectionnez un post d'abord", Alert.AlertType.ERROR);
            return;
        }

        String content = txtComment.getText();
        if(content.isEmpty()) {
            showAlert("Erreur", "Le commentaire ne peut pas être vide", Alert.AlertType.ERROR);
            return;
        }

        String query = "INSERT INTO commentaire (post_id, user_id, content, created_at,updated_at) VALUES (?, ?, ?, NOW())";

        try (Connection con = DBConnexion.getCon();
             PreparedStatement st = con.prepareStatement(query)) {

            st.setInt(1, selectedPost.getId());
            st.setInt(2, 1); // ID utilisateur temporaire
            st.setString(3, content);
            st.executeUpdate();

            loadComments();
            txtComment.clear();

        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage(), Alert.AlertType.ERROR);
        }
    }
*/
    @FXML
    void createComment(ActionEvent event) {
        if (selectedPost == null) {
            showAlert("Erreur", "Sélectionnez un post d'abord", Alert.AlertType.ERROR);
            return;
        }

        // Récupération et validation du contenu
        String content = txtComment.getText().trim(); // Ajout de .trim()
        if (content.isEmpty()) {
            showAlert("Erreur", "Le commentaire ne peut pas être vide", Alert.AlertType.ERROR);
            return;
        }

        // Vérification supplémentaire de l'ID du post
        if (selectedPost.getId() <= 0) {
            showAlert("Erreur", "Post invalide", Alert.AlertType.ERROR);
            return;
        }

        // Utilisation d'un user_id VALIDE (3 ou 4 selon votre table)
        int validUserId = 4; // ⚠️ À remplacer par l'ID dynamique de l'utilisateur connecté

        String query = "INSERT INTO commentaire (post_id, user_id, content, created_at, updated_at) "
                + "VALUES (?, ?, ?, NOW(), NOW())";

        try (Connection con = DBConnexion.getCon();
             PreparedStatement st = con.prepareStatement(query)) {

            // Paramétrage des valeurs
            st.setInt(1, selectedPost.getId());
            st.setInt(2, validUserId); // Correction du user_id codé en dur
            st.setString(3, content);

            int rowsAffected = st.executeUpdate();

            if (rowsAffected > 0) {
                loadComments(); // Recharger les commentaires
                txtComment.clear();
            } else {
                showAlert("Erreur", "Échec de la création du commentaire", Alert.AlertType.ERROR);
            }

        } catch (SQLException e) {
            showAlert("Erreur SQL", "Erreur de base de données :\n" + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur inattendue :\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void deleteComment(ActionEvent event) {
        Comment selected = tableComments.getSelectionModel().getSelectedItem();
        if(selected == null) return;

        String query = "DELETE FROM commentaire WHERE id = ?";

        try (Connection con = DBConnexion.getCon();
             PreparedStatement st = con.prepareStatement(query)) {

            st.setInt(1, selected.getId());
            st.executeUpdate();
            loadComments();

        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    // Méthodes helpers
    private void loadComments() {
        comments.clear();
        if(selectedPost == null) return;

        String query = "SELECT * FROM commentaire WHERE post_id = ?";

        try (Connection con = DBConnexion.getCon();
             PreparedStatement st = con.prepareStatement(query)) {

            st.setInt(1, selectedPost.getId());
            ResultSet rs = st.executeQuery();

            while(rs.next()) {
                comments.add(new Comment(
                        rs.getInt("id"),
                        rs.getInt("post_id"),
                        rs.getInt("user_id"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getInt("likes"),
                        rs.getInt("dislikes")
                ));
            }
            tableComments.setItems(comments);

        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void selectPost() {
        selectedPost = tablePosts.getSelectionModel().getSelectedItem();
        if(selectedPost != null) {
            lblPostId.setText(String.valueOf(selectedPost.getId()));
            txtPostTitle.setText(selectedPost.getTitle());
            txtPostContent.setText(selectedPost.getContenu());
            loadComments();
        }
    }

    @FXML
    void clearPost(ActionEvent event) {
        lblPostId.setText("");
        txtPostTitle.clear();
        txtPostContent.clear();
        comments.clear();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    // Nouvelle méthode pour gérer le rating
    @FXML
    void ratePost(ActionEvent event) {
        Post post = tablePosts.getSelectionModel().getSelectedItem();
        if (post == null) return;

        // Créez une boîte de dialogue pour sélectionner la note
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(1, 1, 2, 3, 4, 5);
        dialog.setTitle("Noter le post");
        dialog.setHeaderText("Choisissez une note entre 1 et 5");
        Optional<Integer> result = dialog.showAndWait();

        result.ifPresent(rating -> {
            String query = "INSERT INTO post_rating (post_id, user_id, rating) VALUES (?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE rating = ?";
            try (Connection con = DBConnexion.getCon();
                 PreparedStatement st = con.prepareStatement(query)) {

                st.setInt(1, post.getId());
                st.setInt(2, 1); // Remplacer par l'ID utilisateur réel
                st.setInt(3, rating);
                st.setInt(4, rating);
                st.executeUpdate();

                updatePostRating(post.getId());
                loadPosts();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void updatePostRating(int postId) {
        String query = "UPDATE post p SET average_rating = "
                + "(SELECT AVG(rating) FROM post_rating WHERE post_id = ?) WHERE id = ?";
        try (Connection con = DBConnexion.getCon();
             PreparedStatement st = con.prepareStatement(query)) {

            st.setInt(1, postId);
            st.setInt(2, postId);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // Méthodes de gestion des réactions
    @FXML
    void likeComment(ActionEvent event) {
        handleReaction("like");
    }

    @FXML
    void dislikeComment(ActionEvent event) {
        handleReaction("dislike");
    }

    private void handleReaction(String reactionType) {
        Comment comment = tableComments.getSelectionModel().getSelectedItem();
        if (comment == null) return;

        String query = "INSERT INTO comment_reaction (comment_id, user_id, reaction) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE reaction = ?";
        try (Connection con = DBConnexion.getCon();
             PreparedStatement st = con.prepareStatement(query)) {

            st.setInt(1, comment.getId());
            st.setInt(2, 1); // Remplacer par l'ID utilisateur réel
            st.setString(3, reactionType);
            st.setString(4, reactionType);
            st.executeUpdate();

            updateCommentReactions(comment.getId());
            loadComments();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateCommentReactions(int commentId) {
        String query = "UPDATE commentaire c SET "
                + "likes = (SELECT COUNT(*) FROM comment_reaction WHERE comment_id = ? AND reaction = 'like'), "
                + "dislikes = (SELECT COUNT(*) FROM comment_reaction WHERE comment_id = ? AND reaction = 'dislike') "
                + "WHERE id = ?";
        try (Connection con = DBConnexion.getCon();
             PreparedStatement st = con.prepareStatement(query)) {

            st.setInt(1, commentId);
            st.setInt(2, commentId);
            st.setInt(3, commentId);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

   /* void translateContent(ActionEvent event) {
        if (selectedPost != null) {
            try {
                String originalText = selectedPost.getContenu();

                String encodedText = URLEncoder.encode(originalText, "UTF-8");

                // Modification ici : auto|en pour traduire vers l'anglais
                URL url = new URL("https://api.mymemory.translated.net/get?q="
                        + encodedText + "&langpair=auto|en");

                JsonObject response = JsonParser.parseReader(
                                new InputStreamReader(url.openStream(), "UTF-8"))
                        .getAsJsonObject();

                String translatedText = response.get("responseData")
                        .getAsJsonObject().get("translatedText").getAsString();

                txtPostContent.setText(translatedText);

            } catch (Exception e) {
                txtPostContent.setText("Translation error");
            }
        }
    }
    @FXML
    void translateComment(ActionEvent event) {
        Comment selected = tableComments.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                String encodedText = URLEncoder.encode(selected.getContent(), "UTF-8");
                URL url = new URL("https://api.mymemory.translated.net/get?q="
                        + encodedText + "&langpair=auto|en");

                JsonObject response = JsonParser.parseReader(
                                new InputStreamReader(url.openStream(), "UTF-8"))
                        .getAsJsonObject();

                String translatedText = response.get("responseData")
                        .getAsJsonObject().get("translatedText").getAsString();

                txtComment.setText(translatedText);

            } catch (Exception e) {
                txtComment.setText("Translation error");
            }
        }
    }
*/
    // Nouvelle méthode de traduction
//    @FXML
//    void translateContent(ActionEvent event) {
//        if (selectedPost == null) return;
//
//        try {
//            Map<String, String> langCodes = Map.of(
//                    "Français", "fr",
//                    "Anglais", "en",
//                    "Espagnol", "es",
//                    "Allemand", "de",
//                    "Arabe", "ar"
//            );
//
//            String source = langCodes.get(cbSourceLang.getValue());
//            String target = langCodes.get(cbTargetLang.getValue());
//            String text = selectedPost.getContenu();
//
//            // Détection automatique si la langue source est inconnue
//            if (source == null) {
//                source = detectLanguage(text);
//            }
//
//            String translated = translateText(text, source, target);
//            txtPostContent.setText(translated);
//
//        } catch (Exception e) {
//            showAlert("Erreur", "Échec de la traduction", Alert.AlertType.ERROR);
//        }
//    }

    // Méthode de détection de langue
    private String detectLanguage(String text) throws Exception {
        String encodedText = URLEncoder.encode(text, "UTF-8");
        URL detectUrl = new URL("https://api.mymemory.translated.net/detect?q=" + encodedText);

        JsonObject response = JsonParser.parseReader(
                        new InputStreamReader(detectUrl.openStream(), "UTF-8"))
                .getAsJsonObject();

        return response.get("detections")
                .getAsJsonArray()
                .get(0).getAsJsonArray()
                .get(0).getAsJsonObject()
                .get("language").getAsString();
    }

    // Méthode de traduction
    private String translateText(String text, String source, String target) throws Exception {
        // Remplacer 'auto' par une détection réelle ou une valeur par défaut
        if (source.equalsIgnoreCase("auto")) {
            // Méthode alternative si vous ne voulez pas implémenter la détection
            source = detectLanguage(text); // À implémenter
            // OU solution simple :
            source = "fr"; // Définir une langue par défaut
        }

        // Validation des codes de langue
        if (source.length() != 2 || target.length() != 2) {
            throw new IllegalArgumentException("Les codes langue doivent être sur 2 caractères");
        }

        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.name());

        URL translateUrl = new URL("https://api.mymemory.translated.net/get?q="
                + encodedText + "&langpair=" + source + "|" + target);

        HttpURLConnection conn = (HttpURLConnection) translateUrl.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);

        if (conn.getResponseCode() != 200) {
            throw new IOException("Échec de la requête : " + conn.getResponseCode());
        }

        try (InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
            JsonObject response = JsonParser.parseReader(reader).getAsJsonObject();

            if (!response.has("responseData")) {
                throw new IOException("Réponse API invalide");
            }

            return response.get("responseData")
                    .getAsJsonObject()
                    .get("translatedText")
                    .getAsString();
        }
    }

    private String detectCommentLanguage(String text) throws Exception {
        String encodedText = URLEncoder.encode(text, "UTF-8");
        URL detectUrl = new URL("https://api.mymemory.translated.net/detect?q=" + encodedText);

        JsonObject response = JsonParser.parseReader(
                        new InputStreamReader(detectUrl.openStream(), "UTF-8"))
                .getAsJsonObject();

        return response.get("detections")
                .getAsJsonArray()
                .get(0).getAsJsonArray()
                .get(0).getAsJsonObject()
                .get("language").getAsString();
    }

    @FXML
    void translateComment(ActionEvent event) {
        Comment selected = tableComments.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                String targetLang = langCodes.get(cbCommentTargetLang.getValue());
                String translated = translateText(
                        selected.getContent(),
                        "fr", // Langue source forcée à français
                        targetLang
                );
                txtComment.setText(translated);
            } catch (Exception e) {
                showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
}