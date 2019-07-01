package sample;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.storage.Blob;
import com.google.cloud.firestore.Query.Direction;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient;

import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.print.Collation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.Callback;
import models.*;

public class PlateList implements Initializable {
    private static final int UMBRAL = 0;
    @FXML
    private TableView<Plate> incidenceTable;
    @FXML
    private TableColumn<Plate, String> matriculaColumn;
    @FXML
    private TableColumn<Plate, String> descriptionColumn;
    @FXML
    private TableColumn<Plate, String> badColumn;
    @FXML
    private TableColumn<Plate, String> count;
    @FXML
    private TableColumn<Plate, String> date;
    @FXML
    private TableColumn<Plate, String> latitudColumn;
    @FXML
    private TableColumn<Plate, String> longitudColumn;
    @FXML
    private Button editB;
    @FXML
    private Button historyB;

    private ObservableList<Plate> datos = null; // Colecci�n vinculada a la vista.
    private ArrayList<Plate> misdatos;
    private ArrayList<Incidence> misaudios;
    Map<String, Plate> plateMap = new HashMap<String, Plate>();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO

        incidenceTable.setStyle("-fx-alignment: CENTER-RIGHT;");
        matriculaColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Plate, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Plate, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getMatricula());
            }
        });
        descriptionColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Plate, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Plate, String> cellData) {
                        return new SimpleStringProperty(cellData.getValue().getIncidence().getDescripcion());
                    }
                });
        badColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Plate, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Plate, String> cellData) {
                return new SimpleStringProperty(
                        cellData.getValue().getIncidence().getMala_practica().getTitulo());
            }
        });
        date.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Plate, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Plate, String> cellData) {
                        return new SimpleStringProperty(cellData.getValue().getIncidence().getFecha().toString());
                    }
                });

        count.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Plate, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Plate, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getCount() + "");
            }
        });

        latitudColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Plate, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Plate, String> cellData) {
                        return new SimpleStringProperty(cellData.getValue().getIncidence().getLatitud() + "");
                    }
                });
        longitudColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Plate, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Plate, String> cellData) {
                        return new SimpleStringProperty(cellData.getValue().getIncidence().getLongitud() + "");
                    }
                });

        misdatos = new ArrayList<Plate>();
        misaudios = new ArrayList<Incidence>();
        editB.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/incidenceListAll.fxml"));
                /*
                 * if "fx:controller" is not set in fxml
                 * fxmlLoader.setController(NewWindowController);

                 */
                Parent parent = null;
                try {
                    parent = fxmlLoader.load();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                Scene scene = new Scene(parent, 1165, 642);
                Stage stage = new Stage();
                stage.setTitle("Lista de Incidentes en directo ");
                stage.setScene(scene);
                stage.show();
            }
        });
        historyB.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(getClass().getResource("/incidenceList.fxml"));
                    /*
                     * if "fx:controller" is not set in fxml
                     * fxmlLoader.setController(NewWindowController);

                     */
                    Parent parent = fxmlLoader.load();
                    int pos = incidenceTable.getSelectionModel().getSelectedIndex();
                    HistoryList controller = (HistoryList) fxmlLoader.getController();
                    controller.setPlate(matriculaColumn.getCellData(pos));
                    Scene scene = new Scene(parent, 1165, 642);
                    Stage stage = new Stage();
                    stage.setTitle("Lista de Incidentes de la matrícula: " + matriculaColumn.getCellData(pos));
                    stage.setScene(scene);
                    stage.show();
                } catch (IOException ex) {

                }

            }
        });
        try {
            firestoreAuth();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void updateTable() {
        List<Plate> aux = new ArrayList<Plate>(plateMap.values());
        Collections.sort(aux);
        datos = FXCollections.observableArrayList(aux);
        incidenceTable.setItems(datos);
        incidenceTable.getSelectionModel().select(0);
    }

    public void firestoreAuth() throws Exception {
        InputStream serviceAccount = new FileInputStream("/home/adrian/iatros_matriculas/incidencereport.json");
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
        FirebaseOptions options = new FirebaseOptions.Builder().setCredentials(credentials)
                .setStorageBucket("incidencereport.appspot.com").build();
        FirebaseApp.initializeApp(options);
        final Bucket bucket = StorageClient.getInstance().bucket();

        final Firestore db = FirestoreClient.getFirestore();
        db.collection("incidences").whereEqualTo("matricula", "PROVISIONAL")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {

                    @Override
                    public void onEvent(QuerySnapshot snapshots, FirestoreException error) {
                        if (error != null) {
                            System.err.println("Listen failed:" + error);
                            return;
                        }
                        misaudios.clear();
                        for (DocumentSnapshot doc : snapshots) {
                            Incidence in = doc.toObject(Incidence.class);
                            misaudios.add(in);
                            Blob blob = bucket.get(in.getUrl());
                            Path path = Paths.get("/home/adrian/iatros_matriculas/audios/" + in.getId() + ".3gp");

                            blob.downloadTo(path);
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            AudioRecogniser recogniser = new AudioRecogniser(in.getId());
                            in.setMatricula(recogniser.recogniseAudio().replaceAll("\\s", ""));
                            System.out.println("matricula " + in.getMatricula());
                            db.collection("incidences").document(in.getId()).set(in);

                        }

                    }

                });
        db.collection("incidences")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {

                    @Override
                    public void onEvent(QuerySnapshot snapshots, FirestoreException error) {
                        if (error != null) {
                            System.err.println("Listen failed:" + error);
                            return;
                        }
                        misdatos.clear();
                        plateMap.clear();
                        for (DocumentSnapshot doc : snapshots) {
                            Plate aux;
                            Incidence in = doc.toObject(Incidence.class);
                            if (plateMap.containsKey(in.getMatricula())) {
                                aux = plateMap.get(in.getMatricula());
                                aux.setCount();
                                aux.setIncidence(in);

                            } else {
                                aux = new Plate(in);
                            }
                            plateMap.put(in.getMatricula(), aux);

                        }
                        updateTable();

                    }

                });


    }

}
