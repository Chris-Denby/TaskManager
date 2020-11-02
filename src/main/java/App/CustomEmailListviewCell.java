package App;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.Properties;

public class CustomEmailListviewCell extends ListCell<Task>
{
    Properties applicationProps;
    //create control objects for cell
    VBox vBox = new VBox();
    HBox hBox = new HBox();
    HBox subHbox = new HBox();
    BorderPane bPane = new BorderPane();
    Text taskInternalReferenceLabel;
    Text taskCustomerReferenceLabel;
    Text taskPriorityLabel;
    Text taskOwnerLabel;
    Text dateLabel;
    Text orderLabel;
    int charLimit = 50;
    Circle priorityCircle;
    Color yellow = Color.YELLOW;
    Label slaLabel;


    public CustomEmailListviewCell(Properties applicationProps)
    {
        super();
        this.applicationProps = applicationProps;
        taskInternalReferenceLabel = new Text("");
        taskCustomerReferenceLabel = new Text("");
        taskCustomerReferenceLabel.setFont(Font.font("Calibri", FontWeight.NORMAL, 18));
        taskPriorityLabel = new Text("");
        taskOwnerLabel = new Text("");
        dateLabel = new Text("");
        orderLabel = new Text("");
        subHbox.setSpacing(10);
        priorityCircle = new Circle();
        priorityCircle.setRadius(10);
        slaLabel = new Label("0");
        slaLabel.setFont(Font.font("Calibri", FontWeight.BOLD, 18));
        slaLabel.setTextFill(Color.BLACK);
        vBox.setPadding(new Insets(2,0,4,0));
        subHbox.getChildren().addAll(taskInternalReferenceLabel ,taskOwnerLabel, dateLabel, taskPriorityLabel);
        vBox.getChildren().addAll(taskCustomerReferenceLabel, subHbox);
        //hBox.getChildren().addAll(priorityCircle, vBox, slaLabel);
        bPane.setLeft(priorityCircle);
        BorderPane.setAlignment(priorityCircle, Pos.CENTER);
        BorderPane.setMargin(priorityCircle, new Insets(0,10,0,0));
        bPane.setCenter(vBox);
        BorderPane.setAlignment(vBox, Pos.CENTER);
        bPane.setRight(slaLabel);
        BorderPane.setAlignment(slaLabel, Pos.CENTER);
        BorderPane.setMargin(slaLabel, new Insets(0,10,0,10));

    }

    @Override
    protected void updateItem(Task item, boolean empty)
    {
        super.updateItem(item, empty);
        //change appearance of cell here
        if(item != null && !empty)
        {
            //if the item isnt null and empty
            //taskCustomerReferenceLabel.setText("[" + item.getOrder() + "]" + item.getCustomerReference());
            taskCustomerReferenceLabel.setText(item.getCustomerReference());
            taskPriorityLabel.setFill(Color.ROYALBLUE);

            switch(item.getPriority())
            {
                case 0:
                    taskPriorityLabel.setText("BAU");
                    priorityCircle.setFill(Color.ROYALBLUE);
                    break;

                case 1:
                    taskPriorityLabel.setText("HIGH");
                    priorityCircle.setFill(Color.ORANGE);
                    break;

                case 2:
                    taskPriorityLabel.setText("IMMEDIATE");
                    priorityCircle.setFill(Color.YELLOW);
                    break;
            }

            if(item.getSlaGap() > Integer.parseInt(applicationProps.getProperty("taskSLA")))
            {
                slaLabel.setTextFill(Color.RED);
            }
            else
            {
                slaLabel.setTextFill(Color.BLACK);
            }

            taskInternalReferenceLabel.setText(item.getInternalReference());
            taskOwnerLabel.setText(item.getPreparedBy());
            orderLabel.setText(item.getOrder()+ "");
            dateLabel.setText(item.getReceivedDate());
            slaLabel.setText(item.getSlaGap()+ "");
            //SET CONTENT TO CELL - BLANK WITHOUT THIS CALL
            setGraphic(bPane);
        }
        else
        {
            setGraphic(null);
        }
    }
}