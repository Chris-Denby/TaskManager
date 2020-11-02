package App;

import Graph.GraphAuthHelper;
import Graph.GraphHelper;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.derby.shared.common.error.DerbySQLIntegrityConstraintViolationException;

import javax.mail.Message;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

public class Main extends Application
{
    private static String deviceKey = null;

    Double applicationWidth;
    Double applicationHeight;
    static List<Message> mailList;
    static ObservableList<Task> taskList = FXCollections.observableArrayList();
    static ObservableList<TaskNote> notesList;
    static ObservableList<User> usersList = FXCollections.observableArrayList();

    static IGraphServiceClient graphClient = null;

    Properties applicationProps;
    File appPropertiesFile;

    static GridPane taskGridPane;
    static Task selectedTask = null;
    static ListView taskNotesListView = new ListView();
    private static String accessToken = null;
    static Map cachedTasks = new HashMap();
    public Calendar calendar;
    Scene mainScene;
    //GLOBAL UI FIELDS

    static Dialog tokenDialog;
    Label emailSubjectLabel;
    Label emailReceivedLabel;
    Label emailFromLabel;
    Label emailToLabel;
    Label emailBodyLabel;
    WebView emailBodyWebView = new WebView();
    TextField emailFromField;
    TextField emailToField;
    TextField emailSubjectField;
    TextField emailReceivedField;

    Label customerReferenceLabel;
    TextField customerReferenceField;
    Label internalReferenceLabel;
    TextField internalReferenceField;
    Label taskOwnerLabel;
    Label priorityLabel;
    Label taskStatusLabel;
    TextField taskStatusField;
    Label taskReceivedLabel;
    DatePicker taskReceivedPicker;
    DatePicker taskRespondedPicker;
    Label taskRespondedLabel;
    Label notesLabel;
    Button addNoteButton;
    Button openFolderButton;
    Button deleteTaskButton;
    TextField searchBar;
    Button refreshListButton;
    static Button signInExchangeButton;
    static Button getMailButton;
    Button uploadToSharepointButton;
    Button increaseOrderButton;
    Button decreaseOrderButton;
    TextArea notesField;
    Label noteLengthLabel;
    Label taskDocumentsLabel;
    TextArea taskDocumentsField;
    ComboBox ownerSelection;
    ComboBox prioritySelection;
    ListView<Task> tasksListView;

    static Label databaseStatusLabel = new Label("DB STATUS");
    Label networkStatusLabel = new Label("NET STATUS");
    Label processStatusLabel = new Label("PROCESS STATUS");
    Label currentUserLabel = new Label("CURRENT USER");

    public static void main(String[] args)
    {
        //obtainAuthToken();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        //###################### CREATE & LOAD CONFIG FILE ##############################
        //check if properties files exist, if not create them
        FileHelper.createFolder("");
        appPropertiesFile = new File("\\Mail_Task\\" + "app.Properties");
        File defaultPropertiesFile = new File("\\Mail_Task\\" + "default.Properties");
        try {
            appPropertiesFile.createNewFile(); // if file already exists will do nothing
            defaultPropertiesFile.createNewFile(); // if file already exists will do nothing
        } catch (IOException e) {
            e.printStackTrace();
        }

        //create and load default properties
        //application will use these properties if values are not explicitly set elsewhere.
        FileInputStream inputStream = null;
        Properties defaultProperties = new Properties();
        try {
            inputStream = new FileInputStream("\\Mail_Task\\" + "default.Properties");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            defaultProperties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //create application prioerties with default
        //these come into play when a property is being retrieved. If the property can't be found in applicationPropers, the defaults will be used.
        applicationProps = new Properties(defaultProperties);

        //now load the properties from last invocation
        //the properties in this faile are those that were saved from the last time the application was invoked.
        try {
            inputStream = new FileInputStream("\\Mail_Task\\" + "app.Properties");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            applicationProps.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //##########################################################################

        //https://docs.oracle.com/javafx/2/ui_controls/list-view.htm
        tasksListView = new ListView<Task>();
        tasksListView.setOrientation(Orientation.VERTICAL);
        //listView.setPrefWidth(applicationWidth/3);
        tasksListView.setMinWidth(600);

        TabPane tabPane = new TabPane();

        //TOP MENU BAR
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");

        Menu settingsMenu = new Menu("Settings");
        MenuItem setupUsersSettings = new MenuItem("Setup Users");
        MenuItem propertiesSettings = new MenuItem("Properties");
        MenuItem businessRulesSettings = new MenuItem("Business Rules");
        settingsMenu.getItems().addAll(setupUsersSettings, businessRulesSettings, propertiesSettings);

        setupUsersSettings.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
                //setup users
                showUserSettings(mainScene.getRoot(), primaryStage);
            }
        });

        businessRulesSettings.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showBusinessRulesSettings(mainScene.getRoot(), primaryStage);
            }
        });

        propertiesSettings.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
                //configure properties
                showPropertiesSettings(mainScene.getRoot(), primaryStage);
            }
        });

        Menu aboutMenu = new Menu("About");

        menuBar.getMenus().addAll(fileMenu,settingsMenu, aboutMenu);

        //#################### MESSAGE TAB ####################
        GridPane messageGridPane = new GridPane();
        //position the grid in the centre of the scene
        //messageGridPane.setAlignment(Pos.CENTER);
        //set horizontal and vertical gap between cells
        messageGridPane.setHgap(10);
        messageGridPane.setVgap(10);
        messageGridPane.setPadding(new Insets(25,25,25,25));
        emailSubjectLabel = new Label("Subject");
        emailReceivedLabel = new Label("Received");
        emailFromLabel = new Label("From");
        emailToLabel = new Label("To");
        emailBodyLabel = new Label("Message");
        emailBodyWebView = new WebView();
        emailFromField = new TextField();
        emailToField = new TextField();
        emailSubjectField = new TextField();
        emailReceivedField = new TextField();
        emailSubjectField.setEditable(false);
        emailSubjectField.setPrefHeight(5);
        emailSubjectField.setMaxWidth(300);
        emailReceivedField.setEditable(false);
        emailReceivedField.setPrefHeight(5);
        emailReceivedField.setMaxWidth(300);
        emailFromField.setEditable(false);
        emailFromField.setPrefHeight(5);
        emailFromField.setMaxWidth(300);
        emailToField.setEditable(false);
        emailToField.setPrefHeight(5);
        emailToField.setMaxWidth(300);
        emailBodyWebView.setMaxWidth(600);
        messageGridPane.add(emailSubjectLabel,0,0);
        messageGridPane.add(emailSubjectField,1,0,2,1);
        messageGridPane.add(emailFromLabel,0,1);
        messageGridPane.add(emailFromField, 1,1,2,1);
        messageGridPane.add(emailToLabel,0,2);
        messageGridPane.add(emailToField,1,2,2,1);
        messageGridPane.add(emailReceivedLabel, 0, 3,2,1);
        messageGridPane.add(emailReceivedField, 1, 3);
        messageGridPane.add(emailBodyLabel,0,4);
        messageGridPane.add(emailBodyWebView, 0,5, 2, 1);

        //#################### TASK TAB ####################
        taskGridPane = new GridPane();
        taskGridPane.setHgap(10);
        taskGridPane.setVgap(10);
        taskGridPane.setPadding(new Insets(25,25,25,25));
        taskGridPane.setVisible(false);
        customerReferenceLabel = new Label("Customer Reference");
        customerReferenceField = new TextField();
        customerReferenceField.setPrefHeight(5);
        customerReferenceField.setEditable(false);
        customerReferenceField.setMaxWidth(300);
        internalReferenceLabel = new Label("Internal Reference");
        internalReferenceField = new TextField();
        internalReferenceField.setPrefHeight(5);
        internalReferenceField.setEditable(false);
        internalReferenceField.setMaxWidth(300);
        taskOwnerLabel = new Label("Owner");
        priorityLabel = new Label("Priority");
        taskStatusLabel = new Label("Status");
        taskStatusField = new TextField();
        taskStatusField.setMaxWidth(300);
        taskStatusField.setPrefHeight(5);

        taskReceivedLabel = new Label("Received Date/Time:");
        taskRespondedLabel = new Label("Responded Date/Time");
        taskReceivedPicker = new DatePicker(LocalDate.now());
        taskReceivedPicker.setMaxWidth(300);
        taskReceivedPicker.setPrefHeight(5);
        taskReceivedPicker.setEditable(false);
        taskRespondedPicker = new DatePicker(LocalDate.now());
        taskRespondedPicker.setMaxWidth(300);
        taskRespondedPicker.setEditable(false);

        notesLabel = new Label("Current Notes");
        addNoteButton = new Button ("ADD NOTE");
        taskGridPane.setHalignment(addNoteButton, HPos.RIGHT);
        noteLengthLabel = new Label("");
        notesField = new TextArea();
        notesField.setMaxWidth(600);
        notesField.setEditable(false);
        notesField.setWrapText(true);
        taskDocumentsLabel = new Label("Documents");
        taskDocumentsField = new TextArea();
        taskDocumentsField.setMaxWidth(600);
        taskDocumentsField.setEditable(false);

        notesField.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event)
            {
                System.out.println("Note key pressed");
                int noteLength = notesField.getText().length();
                noteLengthLabel.setText(noteLength +"/500");
                if(noteLength >= 500)
                {
                    notesField.setText(notesField.getText().substring(0,499));
                }
            }
        });

        taskDocumentsField.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event)
            {
                //DO ON DRAG OVER
                if(event.getGestureSource() != taskDocumentsField && event.getDragboard().hasFiles())
                {
                    //IF BEING DRAGGED FROM OUTSIDE TARGET AND CONTAINS FILES
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    event.consume();
                }
            }
        });
        taskDocumentsField.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event)
            {
                //DO ON DRAG & DROP
                Dragboard dragBoard = event.getDragboard();
                boolean success = false;
                //check if directory exists for selected task
                if(!FileHelper.checkDirectoryExists(selectedTask.getInternalReference()))
                {
                    //IF DIRECTORY DOESNT EXIST - CREATE IT
                    FileHelper.createFolder(selectedTask.getInternalReference());
                }
                System.out.println(FileHelper.copyFiles(dragBoard.getFiles(),selectedTask.getInternalReference()) + " files copied");
                success = true;
                event.setDropCompleted(success);
                event.consume();
                //list new folder contents in documents field
                taskDocumentsField.setText(Arrays.toString(FileHelper.listFilesInFolder(selectedTask.getInternalReference())));
            }
        });

        openFolderButton = new Button("Open Documents Folder");
        openFolderButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
                //DO ON CLICK
                if(selectedTask != null)
                {
                    FileHelper.openFolder(selectedTask.getInternalReference());
                }
            }
        });

        deleteTaskButton= new Button("Delete Task");
        deleteTaskButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
                //DO ON CLICK
                if(selectedTask != null)
                {
                    System.out.println("DELETE TASK - " + selectedTask.getInternalReference());
                    try {
                        if(FileHelper.checkDirectoryExists(selectedTask.getInternalReference()))
                        {
                            System.out.println("Delete Folder - Selcted Task: " + selectedTask.getInternalReference());
                            FileHelper.deleteFolder(selectedTask.getInternalReference());
                            //emailListView.getSelectionModel().clearSelection();
                            //selectedTask=null;
                        }
                        //delete task from TB and list view
                        DBHelper.deleteTask(selectedTask.getPrimaryKey());
                        taskList.remove(selectedTask);
                        for(Task t:taskList)
                        {
                            t.setOrder(taskList.indexOf(t));
                        }
                        tasksListView.refresh();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }

            }
        });

        uploadToSharepointButton = new Button("Upload to SP");
        uploadToSharepointButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
                //uploadToSharepoint();
            }
        });


        customerReferenceField.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                customerReferenceField.setEditable(true);
            }
        });

        customerReferenceField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event)
            {
                if(event.getCode() == KeyCode.ENTER)
                {
                    //SET NEW TASK REFERENCE
                    selectedTask.setCustomerReference(customerReferenceField.getText().trim());
                    //UPDATE TEXT BOX
                    customerReferenceField.setText(selectedTask.getCustomerReference().trim());
                    customerReferenceField.setEditable(false);
                    tasksListView.refresh();
                }

            }
        });

        internalReferenceField.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                internalReferenceField.setEditable(true);
            }
        });

        internalReferenceField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event)
            {
                if(event.getCode() == KeyCode.ENTER)
                {
                    //STORE ORIGINAL TASK REFERENCE
                    selectedTask.setOldInternalTaskReference(selectedTask.getInternalReference().trim());
                    //SET NEW TASK REFERENCE
                    selectedTask.setInternalReference(internalReferenceField.getText().trim());
                    //UPDATE TEXT BOX
                    internalReferenceField.setText(selectedTask.getInternalReference().trim());
                    internalReferenceField.setEditable(false);
                    tasksListView.refresh();
                    //MOVE FOLDER
                    try {
                        FileHelper.renameFolder(selectedTask.getOldInternalTaskReference(), selectedTask.getInternalReference());
                    }
                    catch(NullPointerException e)
                    {
                        FileHelper.renameFolder(selectedTask.getOldInternalTaskReference(), selectedTask.getInternalReference());
                    }
                    //UPDATE DOCUMENTS FIELD
                    taskDocumentsField.setText(Arrays.toString(FileHelper.listFilesInFolder(selectedTask.getInternalReference())));
                }
            }
        });

        ObservableList<User> ownerOptionsList = FXCollections.observableArrayList(usersList);
        ownerSelection = new ComboBox(ownerOptionsList);
        ownerSelection.setItems(usersList);


        ownerSelection.setOnHidden(new EventHandler<Event>() {
            @Override
            public void handle(Event event)
            {
                selectedTask.setOwner((User)ownerSelection.getValue());
                tasksListView.refresh();
                System.out.println(selectedTask.getOwner().toString());
            }
        });

        ObservableList<String> priorityOptionsList = FXCollections.observableArrayList("BAU", "HIGH", "IMMEDIATE");
        prioritySelection = new ComboBox(priorityOptionsList);
        prioritySelection.getSelectionModel().selectedItemProperty().addListener(new ChangeListener()
        {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue)
            {
                selectedTask.setPriority(prioritySelection.getSelectionModel().getSelectedIndex());
                tasksListView.refresh();
            }
        });

        addNoteButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                //DO ON BUTTON CLICK
                //IF A TASK IS SELECTED
                if(selectedTask != null)
                {
                    if(addNoteButton.getText().equals("ADD NOTE"))
                    {
                        //set to save mode
                        addNoteButton.setText("SAVE");
                        //make notes field editable and clear text
                        notesField.setEditable(true);
                        notesField.clear();
                        notesField.setStyle("-fx-background-color: white");
                    }
                    else if(addNoteButton.getText().equals("SAVE"))
                    {
                        //save the note to the selected appl.Task
                        if(!notesField.getText().equals(""))
                        {
                            TaskNote newNote = new TaskNote();
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                            LocalDateTime now = LocalDateTime.now();
                            String dateTime= dtf.format(now);
                            newNote.setNoteBody(notesField.getText());
                            newNote.setAuthor(applicationProps.getProperty("userFirstName") + " " + applicationProps.getProperty("userLastName"));
                            newNote.setCreatedDate(dateTime);
                            newNote.setTaskId(selectedTask.getPrimaryKey());
                            newNote.setPersist(true);
                            selectedTask.addTaskNote(newNote);
                            noteLengthLabel.setVisible(false);

                            //set to add mode
                            String text = "["+selectedTask.getLastTaskNote().getCreatedDate() + "] [" + selectedTask.getLastTaskNote().getAuthor().trim() + "]"
                                    +  "%n"
                                    +  "%n"
                                    + selectedTask.getLastTaskNote().getNoteBody();

                            notesField.setText(String.format(text));

                            addNoteButton.setText("ADD NOTE");
                            notesField.setEditable(false);
                            notesField.setStyle("-fx-background-color: #f5f5f5");

                            //NOTES TAB
                            notesList = FXCollections.observableArrayList(selectedTask.getAllTaskNotes());
                            taskNotesListView.setItems(notesList);
                            taskNotesListView.refresh();
                        }
                    }
                }
                else
                {
                    System.out.println("No task selected");
                }
            }
        });

        taskReceivedPicker.setOnHidden(new EventHandler<Event>() {
            @Override
            public void handle(Event event)
            {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String date = taskReceivedPicker.getValue().format(formatter);
                selectedTask.setReceivedDate(date);
                checkTaskSLA(selectedTask);
                tasksListView.refresh();
            }
        });

        taskRespondedPicker.setOnHidden(new EventHandler<Event>() {
            @Override
            public void handle(Event event)
            {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String date = taskRespondedPicker.getValue().format(formatter);
                selectedTask.setRespondedDate(date);
                tasksListView.refresh();
            }
        });

        taskGridPane.add(customerReferenceLabel,0,0);
        taskGridPane.add(customerReferenceField,1,0,3,1);
        taskGridPane.add(internalReferenceLabel,0,1);
        taskGridPane.add(internalReferenceField,1,1,3,1);
        taskGridPane.add(taskOwnerLabel,0,2);
        taskGridPane.add(ownerSelection, 1, 2);
        taskGridPane.add(priorityLabel,0,3);
        taskGridPane.add(prioritySelection,1,3);

        taskGridPane.add(taskStatusLabel,0,4);
        taskGridPane.add(taskStatusField,1,4,2,1);

        taskGridPane.add(taskReceivedLabel,0,5);
        taskGridPane.add(taskReceivedPicker,1,5,1,1);

        taskGridPane.add(taskRespondedLabel,0,6);
        taskGridPane.add(taskRespondedPicker,1,6,1,1);

        taskGridPane.add(notesLabel,0,7);
        taskGridPane.add(noteLengthLabel, 2, 7);
        taskGridPane.add(addNoteButton,1,7);
        taskGridPane.add(notesField,0,8,3,1);

        taskGridPane.add(taskDocumentsLabel,0,9);
        taskGridPane.add(taskDocumentsField,0,10,3,1);

        taskGridPane.add(openFolderButton,0,11);
        taskGridPane.add(deleteTaskButton, 1, 11);
        taskGridPane.add(uploadToSharepointButton, 2, 11);


        //#################### NOTES TAB ####################
        GridPane notesGridPane = new GridPane();
        notesGridPane.setHgap(10);
        notesGridPane.setVgap(10);
        notesGridPane.setPadding(new Insets(25,25,25,25));
        notesGridPane.add(taskNotesListView,0,0);
        taskNotesListView.setOrientation(Orientation.VERTICAL);
        taskNotesListView.setPrefWidth(600);
        taskNotesListView.setPrefHeight(600);
        taskNotesListView.setMaxHeight(Double.MAX_VALUE);
        //add cell factory and event listener to email listview
        taskNotesListView.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                if(newValue.intValue() == -1)
                {

                }
            }
        });

        //#################### LOGIN BAR ####################
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(10,10,10,10));
        hbox.setSpacing(10);
        hbox.setStyle("fx-background-color: #336699;");

        signInExchangeButton = new Button("Exchange Sign In");
        signInExchangeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                obtainAuthToken();
            }
        });

        getMailButton = new Button("Get Exchange Mail");
        getMailButton.setDisable(true);
        getMailButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                listGraphEmails();
                //GraphHelper.getEmailFolders(graphClient);
            }
        });

        Button createTaskButton = new Button("Create Task");
        createTaskButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {

                //do on button click
                createNewTask("NEW TASK", "NEW TASK");
            }
        });

        increaseOrderButton = new Button("+");
        increaseOrderButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
                increaseOrder(selectedTask);
            }
        });

        decreaseOrderButton = new Button("-");
        decreaseOrderButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                decreaseOrder(selectedTask);
            }
        });
        
        searchBar = new TextField("Search");
        searchBar.setPrefWidth(100);
        searchBar.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                searchBar.clear();
            }
        });
        searchBar.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event)
            {
                if(event.getCode() == KeyCode.ENTER)
                {
                    //DO SEARCH FUNCTION
                    ObservableList tempList = FXCollections.observableArrayList();
                    //convert to upper case to care is comparable
                    String searchString = searchBar.getText().toUpperCase();
                    for(Task t:taskList)
                    {
                        //convert to upper case to care is comparable
                        String cref = t.getCustomerReference().trim().toUpperCase();
                        String iref = t.getInternalReference().trim().toUpperCase();
                        if(cref.contains(searchString) || iref.contains(searchString))
                        {
                            tempList.add(t);
                        }
                        else
                        {

                        }
                    }
                    tasksListView.setItems(tempList);
                    tasksListView.getSelectionModel().select(0);
                    tasksListView.refresh();
                }
            }
        });

        refreshListButton = new Button("Refresh");
        refreshListButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
                searchBar.setText("Search");
                tasksListView.setItems(taskList);
                tasksListView.refresh();
            }
        });


        hbox.getChildren().addAll(signInExchangeButton,getMailButton, createTaskButton, decreaseOrderButton,increaseOrderButton, searchBar, refreshListButton);

        //add cell factory and event listener to email listview
        tasksListView.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {;
            //DO ON LIST ITEM SELECTION
                //get current list bound to ListView
                ObservableList<Task> activeList = tasksListView.getItems();

                if(activeList.size() == 0)
                {
                    taskGridPane.setVisible(false);
                    taskGridPane.setVisible(false);
                    customerReferenceField.setText("");
                    internalReferenceField.setText("");
                    taskStatusField.setText("");
                    taskDocumentsField.setText("");
                    notesField.setText("");
                }
                else
                {
                    try
                    {
                        selectedTask = activeList.get(newValue.intValue());
                    }
                    catch(IndexOutOfBoundsException e)
                    {
                        System.out.println("Index out of bounds exception when assigning selected task - taskListView change listener");
                    }


                    if(selectedTask == null)
                    {
                        taskGridPane.setVisible(false);
                    }
                    taskGridPane.setVisible(true);


                    //UPDATE APPEARANCE OF TAB FIELDS HERE
                    //MESSAGE TAB
                    if(selectedTask.getEmail() != null)
                    {
                        emailSubjectField.setText(selectedTask.getEmail().subject);
                        emailFromField.setText(selectedTask.getEmail().from.toString());
                        emailToField.setText(selectedTask.getEmail().toRecipients.toString());
                        emailReceivedField.setText(selectedTask.getEmail().receivedDateTime.toString());
                        emailBodyWebView.getEngine().loadContent(selectedTask.getEmail().body.content, "text/html");
                    }

                    //TASK TAB
                    if(selectedTask.getOwner() != null)
                    {
                        ownerSelection.getSelectionModel().select(selectedTask.getOwner());
                    }

                    prioritySelection.getSelectionModel().select(selectedTask.getPriority());

                    if(selectedTask.getLastTaskNote() != null)
                    {
                        String text = "["+selectedTask.getLastTaskNote().getCreatedDate() + "] [" + selectedTask.getLastTaskNote().getAuthor().trim() + "]"
                                +  "%n"
                                +  "%n"
                                + selectedTask.getLastTaskNote().getNoteBody();

                        notesField.setText(String.format(text));
                    }
                    else
                    {
                        notesField.setText("No notes recorded");
                    }
                    customerReferenceField.setText(selectedTask.getCustomerReference());
                    internalReferenceField.setText(selectedTask.getInternalReference());

                    if(!selectedTask.getReceivedDate().equals("nil"))
                    {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        LocalDate d = LocalDate.parse(selectedTask.getReceivedDate(), formatter);
                        taskReceivedPicker.setValue(d);
                    }
                    else
                    {
                        taskReceivedPicker.setValue(null);
                    }

                    if(!selectedTask.getRespondedDate().equals("nil"))
                    {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        LocalDate d = LocalDate.parse(selectedTask.getRespondedDate(), formatter);
                        taskRespondedPicker.setValue(d);
                    }
                    else
                    {
                        taskRespondedPicker.setValue(null);
                    }


                    taskDocumentsField.setText(Arrays.toString(FileHelper.listFilesInFolder(selectedTask.getInternalReference())));

                    //NOTES TAB
                    notesList = FXCollections.observableArrayList(selectedTask.getAllTaskNotes());
                    taskNotesListView.setCellFactory(new Callback<ListView<TaskNote>, ListCell<TaskNote>>()
                    {
                        @Override
                        public ListCell<TaskNote> call(ListView<TaskNote> listView)
                        {
                            return new CustomNotesListviewCell();
                        }
                    });
                    taskNotesListView.setItems(notesList);
                }
            }
        });

        tasksListView.setCellFactory(new Callback<ListView<Task>, ListCell<Task>>()
        {
            @Override
            public ListCell<Task> call(ListView<Task> listView)
            {
                return new CustomEmailListviewCell(applicationProps);
            }
        });
        tasksListView.setItems(taskList);
        taskList.addListener(new ListChangeListener<Task>() {
            @Override
            public void onChanged(Change<? extends Task> c)
            {
                if(taskList.size() == 0)
                {

                }
            }
        });

        tasksListView.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event)
            {
                //DO ON DRAG OVER
                if(event.getGestureSource() != taskDocumentsField && event.getDragboard().hasFiles())
                {
                    //IF BEING DRAGGED FROM OUTSIDE TARGET AND CONTAINS FILES
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    event.consume();
                }
            }
        });
        tasksListView.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event)
            {
                //DO ON DRAG & DROP
                Dragboard dragBoard = event.getDragboard();
                boolean success = false;
                //check if directory exists for selected task
                for(File f:dragBoard.getFiles())
                {
                    //truncate filename to remove extension
                    String fileName = f.getName().substring(0, f.getName().lastIndexOf(".", f.getName().length()-1));
                    //if filename is beyond 40 characters, truncate to fit database
                    if(fileName.length()>40)
                    {
                        fileName = fileName.substring(0,40).trim();
                    }
                    createNewTask(fileName, fileName);

                    if(!FileHelper.checkDirectoryExists(fileName))
                    {
                        //IF DIRECTORY DOESNT EXIST - CREATE IT
                        FileHelper.createFolder(fileName);
                        FileHelper.copyFiles(f, fileName);
                        taskDocumentsField.setText(Arrays.toString(FileHelper.listFilesInFolder(fileName)));
                    }
                }
                success = true;
                event.setDropCompleted(success);
                event.consume();
            }
        });

        //#################### BOTTOM BAR ##############################
        GridPane bottomBarGrid = new GridPane();
        bottomBarGrid.setHgap(50);
        bottomBarGrid.add(currentUserLabel,0,0);
        bottomBarGrid.add(databaseStatusLabel,1,0);
        bottomBarGrid.add(networkStatusLabel,2,0);
        bottomBarGrid.add(processStatusLabel,3,0,2,1);
        HBox bottomBar = new HBox();
        bottomBar.setPadding(new Insets(5,5,5,5));
        bottomBar.setSpacing(10);
        bottomBar.getChildren().addAll(bottomBarGrid);

        //#################### POPULATE BORDER PANE ####################
        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(10,10,10,10));
        borderPane.setTop(hbox);
        borderPane.setLeft(tasksListView);
        Tab messageTab = new Tab("Message");
        messageTab.setContent(messageGridPane);
        messageTab.setClosable(false);
        Tab taskTab = new Tab("Task");
        taskTab.setContent(taskGridPane);
        taskTab.setClosable(false);
        Tab notesTab = new Tab("Notes");
        notesTab.setContent(notesGridPane);
        notesTab.setClosable(false);
        tabPane.getTabs().add(taskTab);
        tabPane.getTabs().add(notesTab);
        tabPane.getTabs().add(messageTab);
        borderPane.setCenter(tabPane);
        borderPane.setBottom(bottomBar);

        VBox vBox = new VBox();
        vBox.getChildren().addAll(menuBar, borderPane);
        mainScene = new Scene(vBox);
        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Application Title");
        primaryStage.show();

        DBHelper.connectDB();
        //dropTables();
        populateUsers();
        populateTasks();
        //dropTables();
        //closeTokenDialog();

        int delay = 1 * 10 * 1000; //miliseconds
        Timer cacheTimer = new Timer();
        //time to execute task after set delay
        cacheTimer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                persistAllTasks();
                System.out.println("CACHE PERSISTED TO DB");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        databaseStatusLabel.setText("CACHE SAVED");
                    }
                });
            }
        }, delay, delay);
    }

    @Override
    public void stop() throws Exception
    {
        persistAllTasks();
        try {
            DBHelper.closeDB();
        }
        catch (SQLException e)
        {
            databaseStatusLabel.setText("FAILED TO CLOSE DB");
        }
        super.stop();

    }

    private static void obtainAuthToken()
    {
            // Load OAuth settings
            final Properties oAuthProperties = new Properties();
            try
            {
                oAuthProperties.load(App.Main.class.getResourceAsStream("App/oAuth.properties"));
            }
            catch (IOException e) {

                System.out.println("Unable to read OAuth configuration. Make sure you have a properly formatted oAuth.properties file. See README for details.");
                return;
            }

            final String appId = oAuthProperties.getProperty("app.id");
            final String[] appScopes = oAuthProperties.getProperty("app.scopes").split(",");

            // Get an access token asynchronously
            Runnable runnable = new Runnable() {
                @Override
                public void run()
                {
                    GraphAuthHelper.initialize(appId);
                    accessToken = GraphAuthHelper.getUserAccessToken(appScopes);
                    System.out.println("ACCESS TOKEN: " + accessToken);
                    graphClient = GraphHelper.getGraphClient(accessToken);
                    getMailButton.setDisable(false);
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
    }

    private void listGraphEmails()
    {
        //graphClient = GraphHelper.getGraphClient(accessToken);
        List<com.microsoft.graph.models.extensions.Message> messages = GraphHelper.getEmail(applicationProps.getProperty("userMonitoredMailbox"), graphClient);
        for(com.microsoft.graph.models.extensions.Message m: messages)
        {
            //filter email by categorie - add to task list if catagory matches filter option
            //for(String cat:m.categories)
            //{
                //if (cat.equals(applicationProps.getProperty("userEmailFlag")))
                //{
                    //compare messageID to those in task list - only add if the same ID doesnt exist in list
                    /**
                    for(Task t:taskList)
                    {
                        if(m.id.length()>40) {
                            String id = m.id.substring(0,40);
                        }

                        if(!t.getMessageId().equals(m.id))
                        {
                            //add to list
                            System.out.println("Unique - add to list");
                        }
                        else
                        {
                            System.out.println("Duplicate");
                        }
                    }
                     **/

                    Task task = new Task();
                    task.setEmail(m);
                    //task.setReceivedDate(m.receivedDateTime.getTime().toString());
                    task.setOrder(taskList.size() + 1);

                    //if strings are longer than 40 char, truncate so they fit in DB
                    String cusRef = m.subject;
                    if(m.subject.length() >= 40)
                    {
                        cusRef = m.subject.substring(0,40);
                    }
                    task.setCustomerReference(cusRef);

                    String messageID = m.id;
                    if(m.id.length() >= 40)
                    {
                        messageID = messageID.substring(0,40);
                    }
                    task.setMessageId(messageID);

                    if(!taskList.contains(task))
                    {
                        taskList.add(task);
                    }
                    addTaskToDB(task);
                //}
            //}
            //TO DO - SAVE EMAIL TO FOLDER
            /**
             *
             * IGraphServiceClient graphClient = GraphServiceClient.builder().authenticationProvider( authProvider ).buildClient();
             *
             * Stream stream = graphClient.customRequest("/me/drive/items/{item-id}/content", Stream.class)
             * 	.buildRequest()
             * 	.get();
             *
            try
            {
                ByteArrayInputStream bytes = new ByteArrayInputStream(Base64.getDecoder().decode(m.getRawObject().get("contentBytes").getAsString()));
                FileOutputStream stream = new FileOutputStream("C:\\Source\\test.png");
                //stream.write();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
             **/
        }
        tasksListView.refresh();
    }

    private static void getGraphUser()
    {
        com.microsoft.graph.models.extensions.User user = GraphHelper.getUser(graphClient);
        System.out.println(user.aboutMe);
    }

    public static void displayTokenDialog(String code)
    {
        copyToClipboard(code);
        showBrowser();
        tokenDialog = new Dialog();
        tokenDialog.setHeaderText("Auth Code");
        tokenDialog.setContentText("Authorisation Code: " + code);
        tokenDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK,ButtonType.CANCEL);
        Optional<ButtonType> result = tokenDialog.showAndWait();
        if(!result.isPresent())
        {
            //dialog has been exited
        }
        else if(result.get() == ButtonType.CANCEL)
        {
            //OK pressed
            //Platform.exit();
            //System.exit(0);
        }

    }

    public static void closeTokenDialog()
    {
        if(tokenDialog.isShowing())
        {
            //tokenDialog.close();
        }
    }

    public static void copyToClipboard(String content)
    {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent clipboardContent = new ClipboardContent();
        //add code as plain text
        clipboardContent.putString(content);
        //add code as HTML
        clipboardContent.putHtml("<b>"+content+"<b>");
        clipboard.setContent(clipboardContent);
    }

    public static void showBrowser()
    {
        if(Desktop.isDesktopSupported())
        {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI("https://login.microsoftonline.com/common/oauth2/deviceauth"));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            //If platform supports the desktop function

        }
    }

    public static void addTaskToDB(Task task)
    {
        try
        {
            task.setPrimaryKey(DBHelper.insertTask(task));
            databaseStatusLabel.setText("Added to DB");
            System.out.println("Added task to DB - " + task.getPrimaryKey());
        }
        catch(DerbySQLIntegrityConstraintViolationException e)
        {
            databaseStatusLabel.setText("Duplicate insert - aborted");
        }
        catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
    }

    public void populateTasks()
    {
        taskList.clear();
        try {
            for(Task t: DBHelper.getAllTasks())
            {
                //if the task has an owner, assign the owner object to it if it exists in the userList
                if(!t.getPreparedBy().equals("Unassigned"))
                {
                    for(User u:usersList)
                    {
                       if(t.getPreparedBy().equals(u.toString()))
                       {
                           t.setOwner(u);
                       }
                    }
                }
                //calculate gap between task received and SLA
                checkTaskSLA(t);
                //add to array
                taskList.add(t);
                Collections.sort(taskList);
                databaseStatusLabel.setText(taskList.size() + " tasks added to cache");
            }
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
        if(!taskList.isEmpty())
        {
            taskGridPane.setVisible(true);
        }
    }

    public static void populateUsers()
    {
        //add user for Unassigned tasks
        usersList.add(new User("Unassigned", "", "", ""));
        try {
            for(User u: DBHelper.getAllUsers())
            {
                usersList.add(u);
            }
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
        if(!usersList.isEmpty())
        {

        }
    }

    public static void persistAllTasks()
    {
        if(!taskList.isEmpty())
        {
            int count = 0;
            ArrayList tempArray = new ArrayList<Task>();
            for(Task task : taskList)
            {
                tempArray.add(task);
                count ++;
            }
            try
            {
                DBHelper.persistTaskArray(tempArray);
            }
            catch (SQLException throwables)
            {
                //databaseStatusLabel.setText("CACHE SAVE FAILED");
                throwables.printStackTrace();
            }
            System.out.println("Cached items saved: " + count);
        }
    }

    public static void updateTask(String oldTaskRef, Task task)
    {
        try
        {
            DBHelper.overwriteTask(oldTaskRef, task);
            databaseStatusLabel.setText("DB UPDATED");
        }
        catch (SQLException throwables)
        {
            databaseStatusLabel.setText("DB UPDATE FAILED");
            throwables.printStackTrace();
        }
    }

    private void dropTables()
    {
         try {
         DBHelper.dropTables();
         } catch (SQLException throwables) {
         throwables.printStackTrace();
         }
    }

    private void createNewTask(String internalReference, String customerReference)
    {
        Task task = new Task();
        if(!taskList.contains(task))
        {
            //create new strings to truncate if required
            String intRef = new String(internalReference);
            String cusRef = new String(customerReference);
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dateTime = now.format(dtf);
            task.setReceivedDate(dateTime);
            task.setOrder(taskList.size() + 1);
            //if strings are longer than 40 char, truncate so they fit in DB
            if(internalReference.length() >= 40)
            {
                intRef = intRef.substring(0,40);
            }
            if(customerReference.length() >= 40)
            {
                cusRef = intRef.substring(0,40);
            }
            task.setInternalReference(intRef);
            task.setCustomerReference(cusRef);
            taskList.add(task);
            addTaskToDB(task);
            tasksListView.refresh();
        }
    }

    public void increaseOrder(Task selectedTask)
    {
        //get index of target object
        int selectedIndex = taskList.indexOf(selectedTask);
        if(taskList.indexOf(selectedTask) != taskList.size())
        {
                    //increase order of selected tasks - by position +1 past next neighbour
                    //taskList.get(selectedIndex).setOrder(taskList.get(selectedIndex+1).getOrder()+1);


            //decrease order of next neighbour
            taskList.get(selectedIndex + 1).setOrder(taskList.get(selectedIndex+1).getOrder()-1);
            //increase order of selected task
            selectedTask.setOrder(selectedTask.getOrder()+1);

            //keep item selected
            tasksListView.getSelectionModel().select(selectedTask);
            Collections.sort(taskList);
            tasksListView.refresh();
        }
    }

    public void decreaseOrder(Task selectedTask)
    {
        //get index of target object
        int selectedIndex = taskList.indexOf(selectedTask);
        if(taskList.indexOf(selectedTask) != 0)
        {
                    //decrease order of selected tasks - by position -1 before previous neighbour
                    //taskList.get(selectedIndex).setOrder(taskList.get(selectedIndex-1).getOrder()-1);

            //increase order of previous neighbour
            taskList.get(selectedIndex - 1).setOrder(taskList.get(selectedIndex-1).getOrder()+1);
            //decrease order of selected task
            selectedTask.setOrder(selectedTask.getOrder()-1);

            //keep item selected
            tasksListView.getSelectionModel().select(selectedTask);
            Collections.sort(taskList);
            tasksListView.refresh();
        }
    }

    public void showUserSettings(Parent parent, Stage owner)
    {
        //UI layout
        Stage settingsStage = new Stage();
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(20));
        vBox.setSpacing(10);
        Scene settingsScene = new Scene(vBox);
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        ListView usersListView = new ListView();

        Label firstNameLabel = new Label("First Name");
        TextField firstNameField = new TextField();
        Label lastNameLabel = new Label("Last Name");
        TextField lastNameField = new TextField();
        Label usernameLabel = new Label("Username");
        TextField usernameField = new TextField();
        Label emailLabel = new Label("Email");
        TextField emailField = new TextField();

        Button addUserButton = new Button("Add User");
        Button removeUserButton = new Button("Remove User");
        Button closeButton = new Button("Close");

        gridPane.add(firstNameLabel, 0,0);
        gridPane.add(firstNameField, 2,0);
        gridPane.add(lastNameLabel, 0,1);
        gridPane.add(lastNameField, 2,1);
        gridPane.add(usernameLabel, 0,2);
        gridPane.add(usernameField, 2,2);
        gridPane.add(emailLabel, 0,3);
        gridPane.add(emailField, 2,3);

        HBox buttonBox = new HBox();
        buttonBox.getChildren().addAll(addUserButton, removeUserButton, closeButton);
        buttonBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(usersListView, gridPane, buttonBox);
        usersListView.setItems(usersList);

        addUserButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                if(!firstNameField.getText().isEmpty() & !lastNameField.getText().isEmpty() & !usernameField.getText().isEmpty() & !emailField.getText().isEmpty())
                {
                    try
                    {
                        User newUser = new User(firstNameField.getText().trim(), lastNameField.getText().trim(), usernameField.getText().trim(), emailField.getText().trim());
                        DBHelper.insertUser(newUser);
                        firstNameField.clear();
                        lastNameField.clear();
                        usernameField.clear();
                        emailField.clear();
                        usersList.add(newUser);
                        usersListView.refresh();
                        ownerSelection.setItems(usersList);
                    }
                    catch (SQLException throwables)
                    {
                        throwables.printStackTrace();
                    }
                }
            }
        });

        removeUserButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                User user = (User) usersListView.getSelectionModel().getSelectedItem();
                if(!user.getFirstName().equals("Unassigned"))
                {
                    try
                    {
                        DBHelper.deleteUser(user.getUsername());
                        usersList.remove(usersList.indexOf(user));
                        usersListView.refresh();
                    }
                    catch (SQLException throwables)
                    {
                        throwables.printStackTrace();
                    }
                    usersListView.refresh();
                    ownerSelection.setItems(usersList);
                }
            }
        });

        closeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
                settingsStage.close();
            }
        });


        //window
        settingsStage.setTitle("User Settings");
        settingsStage.setScene(settingsScene);
        settingsStage.sizeToScene();
        settingsStage.setMinWidth(settingsStage.getWidth());
        settingsStage.setMinHeight(settingsScene.getHeight());
        settingsStage.initOwner(owner);
        settingsStage.show();
    }

    public void showPropertiesSettings(Parent parent, Stage owner)
    {
        //UI layout
        Stage settingsStage = new Stage();
        VBox vBox = new VBox();
        HBox buttonBox = new HBox();
        Scene settingsScene = new Scene(vBox);
        GridPane gridPane = new GridPane();
        vBox.setPadding(new Insets(20));
        buttonBox.setPadding(new Insets(10,0,0,0));

        //create elements here
        Label userFirstNameLabel = new Label("User first name");
        TextField userFirstNameField = new TextField();
        Label userLastNameLabel = new Label("User last name");
        TextField userLastNameField = new TextField();
        userFirstNameField.setPrefWidth(300);
        Label emailAddressLabel = new Label("Email address");
        TextField emaillAddressField = new TextField();
        Label emailPasswordLabel = new Label("Email password");
        TextField emailPasswordField = new TextField();
        Label emailServerAddressLabel = new Label("Email server");
        TextField emailServerAddressField = new TextField();
        Label monitoredMailboxLabel = new Label("Mailbox to monitor");
        TextField monitoredMailboxField = new TextField();
        Label mailFlagLabel = new Label("Mail flagged by");
        TextField mailFlagField = new TextField();
        Label clientIPAddressLabel = new Label("Client IP address");
        TextField clientIPAddressField = new TextField();
        Label peerIPAddressLabel = new Label("Peer client IP address");
        TextField peerIPAddressField = new TextField();
        Button applyButton = new Button("Apply");
        Button closeButton = new Button("Close");

        gridPane.add(userFirstNameLabel,0,0);
        gridPane.add(userFirstNameField,0,1);
        gridPane.add(userLastNameLabel,0,2);
        gridPane.add(userLastNameField,0,3);
        gridPane.add(emailAddressLabel,0,4);
        gridPane.add(emaillAddressField,0,5);
        gridPane.add(emailPasswordLabel,0,6);
        gridPane.add(emailPasswordField,0,7);
        gridPane.add(emailServerAddressLabel,0,8);
        gridPane.add(emailServerAddressField,0,9);
        gridPane.add(monitoredMailboxLabel,0,10);
        gridPane.add(monitoredMailboxField,0,11);
        gridPane.add(mailFlagLabel,0,12);
        gridPane.add(mailFlagField,0,13);
        gridPane.add(clientIPAddressLabel, 0,14);
        gridPane.add(clientIPAddressField, 0,15);
        gridPane.add(peerIPAddressLabel,0,16);
        gridPane.add(peerIPAddressField,0,17);

        //add elements to parent
        buttonBox.getChildren().addAll(applyButton, closeButton);
        vBox.getChildren().addAll(gridPane, buttonBox);

        //set on actions
        applyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
                //apply changes to properties values

                //populate fields from properties
                if(!userFirstNameField.getText().equals(""))
                {
                    applicationProps.setProperty("userFirstName", userFirstNameField.getText());
                }
                if(!userLastNameField.getText().equals(""))
                {
                    applicationProps.setProperty("userLastName", userLastNameField.getText());
                }
                if(!emaillAddressField.getText().equals(""))
                {
                    applicationProps.setProperty("userEmailAddress", emaillAddressField.getText());
                }
                if(!emailPasswordField.getText().equals(""))
                {
                    applicationProps.setProperty("userEmailPassword", emailPasswordField.getText());
                }
                if(!emailServerAddressField.getText().equals(""))
                {
                    applicationProps.setProperty("userEmailServer", emailServerAddressField.getText());
                }
                if(!monitoredMailboxField.getText().equals(""))
                {
                    applicationProps.setProperty("userMonitoredMailbox", monitoredMailboxField.getText());
                }
                if(!mailFlagField.getText().equals(""))
                {
                    applicationProps.setProperty("userEmailFlag", mailFlagField.getText());
                }
                if(!clientIPAddressField.getText().equals(""))
                {
                    applicationProps.setProperty("clientIPAddress", clientIPAddressField.getText());
                }
                if(!peerIPAddressField.getText().equals(""))
                {
                    applicationProps.setProperty("peerIPAddress", peerIPAddressField.getText());
                }


                //save properties file
                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(appPropertiesFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    applicationProps.store(outputStream, "---Properies saved---");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                settingsStage.close();
            }
        });

        closeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
                settingsStage.close();
            }
        });


        //populate fields from properties
        if(applicationProps.containsKey("userFirstName"))
        {
            userFirstNameField.setText(applicationProps.getProperty("userFirstName", ""));
        }
        if(applicationProps.containsKey("userLastName"))
        {
            userLastNameField.setText(applicationProps.getProperty("userLastName", ""));
        }
        if(applicationProps.containsKey("userEmailAddress"))
        {
            emaillAddressField.setText(applicationProps.getProperty("userEmailAddress", ""));
        }
        if(applicationProps.containsKey("userEmailPassword"))
        {
            emailPasswordField.setText(applicationProps.getProperty("userEmailPassword", ""));
        }
        if(applicationProps.containsKey("userEmailServer"))
        {
            emailServerAddressField.setText(applicationProps.getProperty("userEmailServer", ""));
        }
        if(applicationProps.containsKey("userMonitoredMailbox"))
        {
            monitoredMailboxField.setText(applicationProps.getProperty("userMonitoredMailbox", ""));
        }
        if(applicationProps.containsKey("userEmailFlag"))
        {
            mailFlagField.setText(applicationProps.getProperty("userEmailFlag", ""));
        }
        if(applicationProps.containsKey("clientIPAddress"))
        {
            clientIPAddressField.setText(applicationProps.getProperty("clientIPAddress", ""));
        }
        if(applicationProps.containsKey("peerIPAddress"))
        {
            peerIPAddressField.setText(applicationProps.getProperty("peerIPAddress", ""));
        }

        //window properties
        settingsStage.setTitle("Application Properties");
        settingsStage.setScene(settingsScene);
        settingsStage.sizeToScene();
        settingsStage.setMinWidth(settingsStage.getWidth() );
        settingsStage.setMinHeight(settingsStage.getHeight());
        settingsStage.initOwner(owner);
        settingsStage.show();
    }

    public void showBusinessRulesSettings(Parent parent, Stage owner)
    {
        //UI layout
        Stage settingsStage = new Stage();
        VBox vBox = new VBox();
        HBox buttonBox = new HBox();
        Scene settingsScene = new Scene(vBox);
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        vBox.setPadding(new Insets(20));
        buttonBox.setPadding(new Insets(10,0,0,0));
        ListView excludedDatesListView = new ListView();
        excludedDatesListView.setEditable(true);
        ObservableList<LocalDate> excludedDatesList = FXCollections.observableArrayList();
        excludedDatesListView.setItems(excludedDatesList);

        //create elements here
        Label publicHolidaysLabel = new Label("Public Holidays");
        DatePicker publicHolidayPicker = new DatePicker(LocalDate.now());
        Button addDateButton = new Button("Add Date");
        Button removeDateButton = new Button("Remove Date");

        Label slaLabel = new Label("Task SLA (Days)");
        TextField slaField = new TextField();
        Label excludeWeekendsLabel = new Label("Exclude Weekends");
        CheckBox excludeWeekendsCheck = new CheckBox();

        Button applyButton = new Button("Apply");
        Button closeButton = new Button("Close");

        gridPane.add(publicHolidaysLabel,0,0);
        gridPane.add(publicHolidayPicker,0,1);
        gridPane.add(addDateButton,1,1);
        gridPane.add(removeDateButton,  2,1);
        gridPane.add(slaLabel,0,2);
        gridPane.add(slaField,0,3);
        gridPane.add(excludeWeekendsLabel,1,3);
        gridPane.add(excludeWeekendsCheck,2,3);


        //add elements to parent
        buttonBox.getChildren().addAll(applyButton, closeButton);
        vBox.getChildren().addAll(excludedDatesListView, gridPane, buttonBox);

        //set on actions
        applyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
                //apply changes to properties values

                //populate fields from properties
                if(!slaField.getText().equals(""))
                {
                    applicationProps.setProperty("taskSLA", slaField.getText());
                }

                //saving list of dates as a comma seperated string, removing the leading and trailing [ and ]
                applicationProps.setProperty("excludedDates", excludedDatesList.toString());

                applicationProps.setProperty("excludeWeekends", excludeWeekendsCheck.selectedProperty().getValue().toString());

                //save properties file
                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(appPropertiesFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    applicationProps.store(outputStream, "---Properies saved---");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                settingsStage.close();
                for(Task t:taskList)
                {
                    checkTaskSLA(t);
                }
                tasksListView.refresh();
            }
        });

        closeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
                settingsStage.close();
            }
        });
        
        addDateButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
                if(publicHolidayPicker.getValue() != null && !excludedDatesList.contains(publicHolidayPicker.getValue()))
                {
                    excludedDatesList.add(publicHolidayPicker.getValue());
                    excludedDatesListView.refresh();
                }
            }
        });

        removeDateButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
                excludedDatesList.remove(excludedDatesListView.getSelectionModel().getSelectedItem());
                excludedDatesListView.refresh();
            }
        });

        //populate fields from properties
        if(applicationProps.containsKey("taskSLA"))
        {
            slaField.setText(applicationProps.getProperty("taskSLA", ""));
        }
        if(applicationProps.containsKey("excludedDates") && !applicationProps.getProperty("excludedDates").equals("[]"))
        {
            //excludedDatesList.clear();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date;
            for(String s:applicationProps.getProperty("excludedDates").substring(1,applicationProps.getProperty("excludedDates").length()-1).split(","))
            {
                date = LocalDate.parse(s.trim(), formatter);
                excludedDatesList.add(date);
            }
            excludedDatesListView.refresh();
        }
        if(applicationProps.containsKey("excludeWeekends"))
        {
            excludeWeekendsCheck.selectedProperty().setValue(Boolean.parseBoolean(applicationProps.getProperty("excludeWeekends").trim()));
        }

        //window properties
        settingsStage.setTitle("Application Properties");
        settingsStage.setScene(settingsScene);
        settingsStage.sizeToScene();
        settingsStage.setMinWidth(settingsStage.getWidth() );
        settingsStage.setMinHeight(settingsStage.getHeight());
        settingsStage.initOwner(owner);
        settingsStage.show();
    }

    public void checkTaskSLA(Task t)
    {
        //task received as LocalDate object

        if(!t.getReceivedDate().equals("nil"))
        {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate taskReceivedDate = LocalDate.parse(t.getReceivedDate(), formatter);
            //NOW as LocalDate object
            LocalDate now = LocalDate.now();
            String[] exludedDatesList = null;
            if(applicationProps.containsKey("excludedDates"))
            {
                if(applicationProps.getProperty("excludedDates").contains(","))
                {
                    exludedDatesList = applicationProps.getProperty("excludedDates").substring(1,applicationProps.getProperty("excludedDates").length()-1).split(",");
                }
                else
                {
                    exludedDatesList = new String[1];
                    exludedDatesList[0] = applicationProps.getProperty("excludedDates").substring(1, applicationProps.getProperty("excludedDates").length()-1);
                }

            }
            int gap = now.compareTo(taskReceivedDate);
            int gapWithExclusions = 0;
            Boolean excludeWeekends = false;

            //add checkbox propertie to business rules - SLA includes weekends
            if(applicationProps.containsKey("excludeWeekends") && applicationProps.getProperty("excludeWeekends").equals("true"))
            {
                excludeWeekends = true;
            }

            //for each day between NOW and the task received date, check how many business days and add them to "gapWithExclusions" count.
            for(int x=0; x<=gap;x++)
            {
                boolean addDay = true;
                if(taskReceivedDate.plusDays(x).getDayOfWeek().toString().equalsIgnoreCase("SATURDAY") || taskReceivedDate.plusDays(x).getDayOfWeek().toString().equalsIgnoreCase("SUNDAY"))
                {
                    if(excludeWeekends)
                        addDay = false;
                }
                if(addDay)
                {
                    //check dates in array for an excluded date from properties, if one is encountered add 1 day to gap
                    //substring to remove brackets from array string
                    if(exludedDatesList != null)
                    {
                        for(String s: exludedDatesList)
                        {
                            //trim string to exclude leading spaces
                            //LocalDate date = LocalDate.parse(s.trim(), backwardsrmatter);
                            if(taskReceivedDate.plusDays(x).toString().equals(s.trim()))
                            {
                                //if incremented doesnt match an excluded date, incrememnt gap count
                                addDay = false;
                            }
                        }
                    }
                }
                if(addDay)
                    gapWithExclusions ++;
            }

            //SLA gap should then exclude weekends and excluded days
            t.setSlaGap(gapWithExclusions);
        }

    }
}
