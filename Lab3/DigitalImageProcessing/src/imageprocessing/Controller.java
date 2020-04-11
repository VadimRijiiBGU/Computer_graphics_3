package imageprocessing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public class Controller implements Initializable {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private ImageView originalImageView, filteredImageView;

    @FXML
    private Label originalLabel, filteredLabel;

    @FXML
    private ComboBox filterComboBox;

    @FXML
    private ToggleGroup filterTypeGroup;

    @FXML
    private RadioButton highPassRadioButton, thresholdRadioButton;

    private BufferedImage originalImage;

    private boolean threshhold;

    @FXML
    private void handleFileButtonAction(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose an image");

            Stage stage = (Stage) anchorPane.getScene().getWindow();
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                originalImage = ImageIO.read(file);
                originalImageView.setImage(new Image(file.toURI().toString()));
                originalLabel.setVisible(true);
                filtration();
            }
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void handleComboBoxAction(ActionEvent event) {
        if (originalImage != null) {
            filtration();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        threshhold = false;

        filterComboBox.setItems(FXCollections.observableArrayList(
                "Laplasian 1", "Laplasian 2", "Laplasian 3", "LoG"));
        filterTypeGroup.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) -> {
            String filterType = ((RadioButton) filterTypeGroup.getSelectedToggle()).getText();
            if (filterType.equals("Threshold")) {
                filterComboBox.setItems(FXCollections.observableArrayList("Local (Niblack)", "Local (min max)", "Adaptive"));
                threshhold = true;
            } else {
                filterComboBox.setItems(FXCollections.observableArrayList(
                        "Laplasian 1", "Laplasian 2", "Laplasian 3", "LoG"));
                threshhold = false;
            }
        });
        
        highPassRadioButton.setToggleGroup(filterTypeGroup);
        thresholdRadioButton.setToggleGroup(filterTypeGroup);
    }
    
    private void filtration() {
        int selectedIndex = filterComboBox.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1) {
            BufferedImage filteredImage = null;
            if (threshhold) {
                if (selectedIndex == 2) {
                    filteredImage = ImageProcessing.adaptiveThresholding(originalImage);
                } else {
                    filteredImage = ImageProcessing.localThreshHoldFilter(selectedIndex, originalImage);
                }
            } else {
                filteredImage = ImageProcessing.highPassFilter(selectedIndex, originalImage);
            }
            filteredImageView.setImage(SwingFXUtils.toFXImage(filteredImage, null));
            filteredLabel.setVisible(true);
        }
    }
}
