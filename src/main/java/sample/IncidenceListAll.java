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
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.Callback;
import models.Incidence;
import models.Plate;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class IncidenceListAll implements Initializable {
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
                        return new SimpleStringProperty(cellData.getValue().getFecha() +"");
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
        try {
            firestoreAuth();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateTable() {
        Collections.sort(misdatos);
        datos = FXCollections.observableArrayList(misdatos);
        incidenceTable.setItems(datos);
        incidenceTable.getSelectionModel().select(0);

    }


    public void firestoreAuth() throws Exception {


        Firestore db = FirestoreClient.getFirestore();
        db.collection("incidences").
                addSnapshotListener(new EventListener<QuerySnapshot>() {

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


    }
}
