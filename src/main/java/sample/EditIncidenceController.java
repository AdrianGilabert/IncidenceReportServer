package sample;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import models.BadPractise;
import models.Incidence;
import org.omg.CORBA.BAD_CONTEXT;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class EditIncidenceController implements Initializable {
    @FXML
    private TableView<BadPractise> incidenceTable;
    @FXML
    private TextField plateField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TableColumn<BadPractise, String> titleColumn;
    @FXML
    private TableColumn<BadPractise, String> descriptionColumn;
    @FXML
    private Button atrasB;
    @FXML
    private Button playB;
    @FXML
    private Button guardarB;
    @FXML

    private ObservableList<BadPractise> datos = null; // Colecci�n vinculada a la vista.
    private ArrayList<BadPractise> misdatos;
    private ArrayList<BadPractise> misaudios;
    private Incidence incidencia;
    private String firstplate;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        descriptionColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<BadPractise, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<BadPractise, String> cellData) {
                        return new SimpleStringProperty(cellData.getValue().getDescripcion());
                    }
                });


        titleColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<BadPractise, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<BadPractise, String> cellData) {
                return new SimpleStringProperty(
                        cellData.getValue().getTitulo());
            }
        });

        misdatos = new ArrayList<BadPractise>();
        misaudios = new ArrayList<BadPractise>();


        guardarB.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Firestore db = FirestoreClient.getFirestore();
                incidencia.setMatricula(plateField.getText());
                incidencia.setDescripcion(descriptionField.getText());
                if (incidenceTable.getSelectionModel().getFocusedIndex() != -1)
                    incidencia.setMala_practica(incidenceTable.getSelectionModel().getSelectedItem());
                db.collection("incidences").document(incidencia.getId()).set(incidencia);
            }
        });

    }

    public void setIncidence(Incidence e) {
        incidencia = e;
        firstplate = e.getMatricula();
        try {
            firestoreAuth();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateUI() {
        plateField.setText(firstplate);
        descriptionField.setText(incidencia.getDescripcion());
        final File archivo = new File("/home/adrian/iatros_matriculas/audios/" + incidencia.getId() + ".mp3");
        if (archivo.exists()) {
            playB.setVisible(true);

            playB.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    try {
                        Process convtomp3 = Runtime.getRuntime().exec("play " + archivo.getAbsolutePath());
                    } catch (Exception ex) {
                        System.exit(-1);
                    }

                }
            });
        }
        incidenceTable.getSelectionModel().select(0);

    }

    private void updateTable() {
        datos = FXCollections.observableArrayList(misdatos);
        incidenceTable.setItems(datos);
        updateUI();
    }


    public void firestoreAuth() throws Exception {


        Firestore db = FirestoreClient.getFirestore();
        db.collection("badpractises").
                addSnapshotListener(new EventListener<QuerySnapshot>() {

                    @Override
                    public void onEvent(QuerySnapshot snapshots, FirestoreException error) {
                        if (error != null) {
                            System.err.println("Listen failed:" + error);
                            return;
                        }
                        misdatos.clear();
                        for (DocumentSnapshot doc : snapshots) {
                            BadPractise in = doc.toObject(BadPractise.class);
                            misdatos.add(in);
                        }
                        updateTable();

                    }

                });


    }
}
