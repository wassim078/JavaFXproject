package com.example.livecycle.controllers.backoffice;

import com.example.livecycle.entities.CategoryForum;
import com.example.livecycle.entities.Comment;
import com.example.livecycle.entities.Post;
import com.example.livecycle.utils.DBConnexion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class AdminForumController implements Initializable {

    @FXML private ComboBox<CategoryForum> cbCategories;
    @FXML private TableView<Post> postsTable;
    @FXML private TableView<Comment> commentsTable;
    @FXML private TableColumn<Post, Void> postActionCol;
    @FXML private TableColumn<Comment, Void> commentActionCol;
    @FXML
    private ListView<CategoryForum> listCategories;


    private ObservableList<Post> posts = FXCollections.observableArrayList();
    private ObservableList<Comment> comments = FXCollections.observableArrayList();
    private ObservableList<CategoryForum> categories = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configureTableColumns();
        setupActionButtons();
        loadCategories();
        setupCategoryComboBox();
        loadComments();
    }

    private void configureTableColumns() {
        // Configuration des colonnes pour les posts
        postsTable.getColumns().forEach(col -> {
            if (col.getText().equals("Titre")) col.setCellValueFactory(new PropertyValueFactory<>("title"));
            if (col.getText().equals("Contenu")) col.setCellValueFactory(new PropertyValueFactory<>("contenu"));
            if (col.getText().equals("Date")) col.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        });

        // Configuration des colonnes pour les commentaires
        commentsTable.getColumns().forEach(col -> {
            if (col.getText().equals("Contenu")) col.setCellValueFactory(new PropertyValueFactory<>("content"));
            if (col.getText().equals("Date")) col.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        });
    }

    private void setupActionButtons() {
        // Configuration des boutons de suppression
        postActionCol.setCellFactory(createDeleteButtonFactory(postsTable, this::deletePost));
        commentActionCol.setCellFactory(createDeleteButtonFactory(commentsTable, this::deleteComment));
    }

    private <T> Callback<TableColumn<T, Void>, TableCell<T, Void>> createDeleteButtonFactory(
            TableView<T> tableView, java.util.function.Consumer<T> deleteHandler) {

        return param -> new TableCell<>() {
            private final Button btn = new Button("Supprimer");

            {
                btn.setOnAction(event -> {
                    T item = tableView.getItems().get(getIndex());
                    deleteHandler.accept(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        };
    }

    private void loadCategories() {
        categories.clear();
        String query = "SELECT * FROM categorie_forum";

        try (Connection con = DBConnexion.getCon();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while(rs.next()) {
                categories.add(new CategoryForum(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description")
                ));
            }

        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupCategoryComboBox() {
        cbCategories.setItems(categories);
        cbCategories.setConverter(new StringConverter<CategoryForum>() {
            @Override
            public String toString(CategoryForum category) {
                return category != null ? category.getName() : "";
            }

            @Override
            public CategoryForum fromString(String string) {
                return null;
            }
        });

        cbCategories.valueProperty().addListener((obs, oldVal, newVal) -> {
            if(newVal != null) {
                loadPostsForCategory(newVal.getId());
            }
        });
    }

    private void loadPostsForCategory(int categoryId) {
        posts.clear();
        String query = "SELECT * FROM post WHERE forum_id = ?";

        try (Connection con = DBConnexion.getCon();
             PreparedStatement st = con.prepareStatement(query)) {

            st.setInt(1, categoryId);
            ResultSet rs = st.executeQuery();

            while(rs.next()) {
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
            postsTable.setItems(posts);

        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadComments() {
        comments.clear();
        String query = "SELECT * FROM commentaire";

        try (Connection con = DBConnexion.getCon();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(query)) {

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
            commentsTable.setItems(comments);

        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

   /* private void deletePost(Post post) {
        String query = "DELETE FROM post WHERE id = ?";
        executeDelete(query, post.getId());
        loadPostsForCategory(post.getForumId());
    }

    private void deleteComment(Comment comment) {
        String query = "DELETE FROM commentaire WHERE id = ?";
        executeDelete(query, comment.getId());
        loadComments();
    }*/
   private void deletePost(Post post) {
       // Supprimer d'abord les réactions liées aux commentaires du post
       String deleteCommentReactions = "DELETE FROM comment_reaction WHERE comment_id IN "
               + "(SELECT id FROM commentaire WHERE post_id = ?)";

       // Supprimer les commentaires du post
       String deleteComments = "DELETE FROM commentaire WHERE post_id = ?";

       // Supprimer les ratings du post
       String deleteRatings = "DELETE FROM post_rating WHERE post_id = ?";

       // Enfin supprimer le post
       String deletePost = "DELETE FROM post WHERE id = ?";

       try (Connection con = DBConnexion.getCon()) {
           con.setAutoCommit(false); // Début de la transaction

           try (PreparedStatement st1 = con.prepareStatement(deleteCommentReactions);
                PreparedStatement st2 = con.prepareStatement(deleteComments);
                PreparedStatement st3 = con.prepareStatement(deleteRatings);
                PreparedStatement st4 = con.prepareStatement(deletePost)) {

               // Suppression des réactions aux commentaires
               st1.setInt(1, post.getId());
               st1.executeUpdate();

               // Suppression des commentaires
               st2.setInt(1, post.getId());
               st2.executeUpdate();

               // Suppression des ratings
               st3.setInt(1, post.getId());
               st3.executeUpdate();

               // Suppression du post
               st4.setInt(1, post.getId());
               st4.executeUpdate();

               con.commit(); // Validation de la transaction
               loadPostsForCategory(post.getForumId());

           } catch (SQLException e) {
               con.rollback(); // Annulation en cas d'erreur
               showAlert("Erreur SQL", e.getMessage(), Alert.AlertType.ERROR);
           }
       } catch (SQLException e) {
           showAlert("Erreur SQL", e.getMessage(), Alert.AlertType.ERROR);
       }
   }

    private void deleteComment(Comment comment) {
        // Supprimer d'abord les réactions
        String deleteReactions = "DELETE FROM comment_reaction WHERE comment_id = ?";
        String deleteComment = "DELETE FROM commentaire WHERE id = ?";

        try (Connection con = DBConnexion.getCon()) {
            con.setAutoCommit(false);

            try (PreparedStatement st1 = con.prepareStatement(deleteReactions);
                 PreparedStatement st2 = con.prepareStatement(deleteComment)) {

                // Suppression des réactions
                st1.setInt(1, comment.getId());
                st1.executeUpdate();

                // Suppression du commentaire
                st2.setInt(1, comment.getId());
                st2.executeUpdate();

                con.commit();
                loadComments();

            } catch (SQLException e) {
                con.rollback();
                showAlert("Erreur SQL", e.getMessage(), Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void executeDelete(String query, int id) {
        try (Connection con = DBConnexion.getCon();
             PreparedStatement st = con.prepareStatement(query)) {

            st.setInt(1, id);
            st.executeUpdate();

        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}