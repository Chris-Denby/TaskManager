package App;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class FileHelper
{
    public static String createFolder(String folderName)
    {
        String folderPath = "\\Mail_Task" + "\\" + folderName;

        File directory = new File(folderPath);
        if(!directory.exists())
        {
            //if directory doesnt already exists - create it
            try {
                directory.mkdirs();
            }
            catch (SecurityException e)
            {
                System.out.println("Security Exception: " + e.getMessage());
            }
            System.out.println("directory created -" + directory.getAbsolutePath());
        }
        else
        {
            System.out.println("directory exists -" + directory.getAbsolutePath());
        }
        return directory.getAbsolutePath();
    }

    public static void openFolder(String path)
    {
        String folderPath = "\\Mail_Task" + "\\" + path;
        File directory = new File(folderPath);
        Desktop desktop = Desktop.getDesktop();
        if(directory.exists())
        {
            try
            {
                desktop.open(directory);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static String[] listFilesInFolder(String path)
    {
        String folderPath = "\\Mail_Task" + "\\" + path;
        File directory = new File(folderPath);
        return directory.list();
    }

    public static int copyFiles(List<File> files, String targetPath)
    {
        int numFilesUpload = 0;
        for(File file:files)
        {
            //upload files
            Path sourceFolderPath = Paths.get(file.getAbsolutePath());
            Path targetFolderPath =  Paths.get("\\Mail_Task" + "\\" + targetPath + "\\" + sourceFolderPath.getFileName());
            try
            {
                Files.copy(sourceFolderPath, targetFolderPath, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            numFilesUpload ++;
        }
        return numFilesUpload;
    }

    public static int copyFiles(File file, String targetPath)
    {
        int numFilesUpload = 0;
        //upload files
        Path sourceFolderPath = Paths.get(file.getAbsolutePath());
        Path targetFolderPath =  Paths.get("\\Mail_Task" + "\\" + targetPath + "\\" + sourceFolderPath.getFileName());
        try
        {
            Files.copy(sourceFolderPath, targetFolderPath, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        numFilesUpload ++;

        return numFilesUpload;
    }

    public static Boolean checkDirectoryExists(String path)
    {
        File file = new File("\\Mail_Task" + "\\" + path);
        return file.exists();
    }

    public static void renameFolder(String oldFolderName, String newFolderName)
    {
        //THSI METHOD DOESNT WORK TO RENAME THE FOLDER

        File oldFolder = new File("\\Mail_Task" + "\\" + oldFolderName.trim());
        //make new folder
        createFolder(newFolderName.trim());
        //System.out.println("RENAME - Folder " + newFolderName + " created");
        //copy files from old folder to new
        for(File f:oldFolder.listFiles())
        {
          copyFiles(f, newFolderName.trim());
        }
       deleteFolder(oldFolderName);
    }

    public static void deleteFolder(String path)
    {
        String folderPath = "\\Mail_Task" + "\\" + path;
        File directory = new File(folderPath);

        //get contents of folder and delete
        String [] contents = directory.list();
        for(String file:contents)
        {
            File currentFile = new File(directory.getPath(),file);
            currentFile.delete();
        }
        //delete folder
        directory.delete();
    }
}
