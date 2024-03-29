/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cgwy9femailviewer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javax.mail.Message;
import javax.mail.MessagingException;

/** 
 *
 * @author Clark Walcott
 */
public class MainMenuController extends AbstractModel implements Initializable, PropertyChangeListener, EmailViewerInterface {
    
    ObservableList<String> searchOptions;
    ObservableList<String> results;
    HashMap<Integer, Integer> resultsMessageNumbers = new HashMap<>();
    EmailSearcher searcher = null;
    
    @FXML
    private AnchorPane anchorPane;
    
    @FXML
    private ChoiceBox searchMenu;
    
    @FXML
    private ListView resultsList;
    
    @FXML
    private TextField searchTextField;
    
    @FXML
    private void handleLogin(ActionEvent event) throws Exception {
        FXMLLoader loader = openNewStage("LoginMenu.fxml");
        
        LoginMenuController loginController = loader.getController();
        loginController.addPropertyChangeListener(this);
    }
    
    @FXML
    private void handleSearch(ActionEvent event) throws Exception {
        String searchString = searchTextField.getText();
        if(!isLoggedIn()){
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Search Error");
            alert.setHeaderText(null);
            alert.setContentText("You are not logged in.");

            alert.showAndWait();
            return;
        }
        
        Message[] foundMessages = searcher.searchEmail(searchString, searchMenu.getValue().toString());
        results.clear();
        resultsMessageNumbers.clear();
        for(int i = 0; i < foundMessages.length; i++){
            Message message = foundMessages[i];
            String subject = message.getSubject();
            results.add(subject);
            resultsMessageNumbers.put(i, message.getMessageNumber());
        }
        resultsList.setItems(results);
    }
    
    
    
    // Adapted from NYTimesViewer(During Class) displayAboutAlert() method.
    @FXML
    private void handleAbout(ActionEvent event) throws Exception {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        
        // Changes window icon to email.png
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        Image icon = new Image(getClass().getResourceAsStream("email.png"));
        stage.getIcons().add(icon);
       
        alert.setTitle("About");
        alert.setHeaderText("Email Viewer");
        alert.setContentText("This application was developed by Clark Walcott for CS3330 at the University of Missouri.");
        
        TextArea textArea = new TextArea("The JavaMail API is used to obtain messages from the user's email.  Documentation for JavaMail is available at https://javaee.github.io/javamail/FAQ.");
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
            
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        alert.getDialogPane().setExpandableContent(expContent);
        
        alert.showAndWait();
    }
    
    @FXML
    private void handleClose(ActionEvent event) throws Exception {
        if(searcher != null){
            searcher.disconnectFromEmailServer();
        }
        Platform.exit();
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        
//        System.out.println("Evt: " + evt);
//        System.out.println("Value: " + evt.getNewValue());
        switch (evt.getPropertyName()) {
            case "EmailSearcher":
                searcher = (EmailSearcher) evt.getNewValue();
                break;
                default:
                    break;
        }
    }

    @Override
    public void viewEmail(int messageNumber) {
        FXMLLoader loader = openNewStage("EmailView.fxml");
        EmailViewController emailController = loader.getController();
     
        try {
            emailController.setMessage(searcher, messageNumber);
        } catch (IOException | MessagingException ex) {
            Logger.getLogger(MainMenuController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public FXMLLoader openNewStage(String stageFXMLName) {
        Stage newStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(stageFXMLName));
        Parent root = null;
        try {
            root = (Parent)loader.load();
        } catch (IOException ex) {
            Logger.getLogger(MainMenuController.class.getName()).log(Level.SEVERE, null, ex);
            return loader;
        }
        
        Scene scene = new Scene(root);
        scene.getRoot().requestFocus();       
        newStage.setScene(scene);
        newStage.show();
        return loader;
    }
    
    boolean isLoggedIn() {
        return isLoggedIn;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        searchOptions = FXCollections.observableArrayList("Subject", "From");
        searchMenu.setItems(searchOptions);
        searchMenu.setValue("Subject");
        
        results = FXCollections.observableArrayList();
        //resultsList.setItems(results);
        
        resultsList.setOnMouseClicked((MouseEvent click) -> {
            if (click.getClickCount() == 2){
//                    String currentSelectedItem = resultsList.getSelectionModel().getSelectedItem().toString();
                int selectedItemIndex = resultsList.getSelectionModel().getSelectedIndex();
//                    System.out.println(selectedItemIndex);
                viewEmail(resultsMessageNumbers.get(selectedItemIndex));
            }
        });
        
        BackgroundImage myBI= new BackgroundImage(new Image(getClass().getResourceAsStream("background.jpg")),
        BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        //then you set to your node
        anchorPane.setBackground(new Background(myBI));
          
    }    
    
}
