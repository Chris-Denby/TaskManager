package App;

import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class CustomNotesListviewCell extends ListCell<TaskNote>
{
    //create control objects for cell
    VBox vBox;
    HBox hbox;
    Text dateLabel;
    Text noteBodyField;
    Text authorLabel;

    public CustomNotesListviewCell()
    {
        super();
        dateLabel = new Text();
        noteBodyField = new Text();
        noteBodyField.setWrappingWidth(500);
        authorLabel = new Text();
        hbox = new HBox(dateLabel, authorLabel);
        hbox.setSpacing(10);
        vBox = new VBox(hbox, noteBodyField);
    }

    @Override
    protected void updateItem(TaskNote item, boolean empty)
    {
        super.updateItem(item, empty);
        //change appearance of cell here
        if(item != null && !empty)
        {
            //if the item isnt null and empty
            noteBodyField.setText(item.getNoteBody());
            dateLabel.setText(item.getCreatedDate());
            authorLabel.setText(item.getAuthor());

            //dateLabel.setText("00/00/00 00:00");
            //SET CONTENT TO CELL - BLANK WITHOUT THIS CALL
            setGraphic(vBox);
        }
        else
        {
            setGraphic(null);
        }
    }
}