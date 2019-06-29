package sample;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.Query.Direction;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient;

import javafx.application.Application;
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
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.Callback;
import models.Incidence;
import models.Plate;

public class HistoryList implements Initializable{
    private static final int UMBRAL = 0;
    @FXML
    private TableView<Incidence> incidenceTable;
    @FXML
    private TableColumn<Incidence, String> matriculaColumn;
    @FXML
    private TableColumn<Incidence, String> descriptionColumn;
    @FXML
    private TableColumn<Incidence, String> badColumn;
    @FXML
    private TableColumn<Incidence, String> date;
    @FXML
    private TableColumn<Incidence, String> latitudColumn;
    @FXML
    private TableColumn<Incidence, String> longitudColumn;
    @FXML
    private Button editB;
    @FXML
    private Button historyB;
    private String matricula;
    private ObservableList<Incidence> datos = null; // Colecci�n vinculada a la vista.
    private ArrayList<Incidence> misdatos;
    private ArrayList<Incidence> misaudios;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        matriculaColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Incidence, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Incidence, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getMatricula());
            }
        });
        descriptionColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Incidence, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Incidence, String> cellData) {
                        return new SimpleStringProperty(cellData.getValue().getDescripcion());
                    }
                });
        badColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Incidence, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Incidence, String> cellData) {
                return new SimpleStringProperty(
                        cellData.getValue().getMala_practica().getTitulo());
            }
        });
        date.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Incidence, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Incidence, String> cellData) {
                        return new SimpleStringProperty(cellData.getValue().getFecha().toString());
                    }
                });
        latitudColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Incidence, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Incidence, String> cellData) {
                        return new SimpleStringProperty(cellData.getValue().getLatitud() + "");
                    }
                });
        longitudColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Incidence, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Incidence, String> cellData) {
                        return new SimpleStringProperty(cellData.getValue().getLongitud() + "");
                    }
                });

        misdatos = new ArrayList<Incidence>();
        misaudios = new ArrayList<Incidence>();
        editB.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {

            }
        });


        editB.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(getClass().getResource("/editincidence.fxml"));
                    /*
                     * if "fx:controller" is not set in fxml
                     * fxmlLoader.setController(NewWindowController);

                     */
                    Parent parent = fxmlLoader.load();
                    EditIncidenceController controller = (EditIncidenceController) fxmlLoader.getController();
                    controller.setIncidence(incidenceTable.getSelectionModel().getSelectedItem());
                    Scene scene = new Scene(parent, 1165, 642);
                    Stage stage = new Stage();
                    stage.setTitle("Editar Incidencia");
                    stage.setScene(scene);
                    stage.show();
                } catch (IOException ex) {

                }

            }
        });

    }

    private void updateTable() {
        Collections.sort(misdatos);
        datos = FXCollections.observableArrayList(misdatos);
        incidenceTable.setItems(datos);
        incidenceTable.getSelectionModel().select(0);

    }

    public void setPlate(String plate){
        matricula=plate;
        try {
            firestoreAuth();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public void firestoreAuth() throws Exception {


        Firestore db = FirestoreClient.getFirestore();
        System.out.println(matricula);
        db.collection("incidences").whereEqualTo("matricula", matricula)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {

                    @Override
                    public void onEvent(QuerySnapshot snapshots, FirestoreException error) {
                        if (error != null) {
                            System.err.println("Listen failed:" + error);
                            return;
                        }
                        misdatos.clear();
                        for (DocumentSnapshot doc : snapshots) {
                            Incidence in = doc.toObject(Incidence.class);
                            misdatos.add(in);
                        }
                        updateTable();

                    }

                });
// ...
// query.get() blocks on response

    }}
