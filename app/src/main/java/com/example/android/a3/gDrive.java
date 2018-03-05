package com.example.android.a3;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

//import com.google.android.gms.drive.Drive;
//import com.google.android.gms.drive.DriveClient;
//import com.google.android.gms.drive.DriveContents;
//import com.google.android.gms.drive.DriveFile;
//import com.google.android.gms.drive.DriveFolder;
//import com.google.android.gms.drive.DriveResourceClient;

/**
 * Created by Scott on 2/23/2018.
 */

public class gDrive {

    private static final String TAG = "Google drive";
    private static final int REQUEST_CODE_SIGN_IN = 0;
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;


    //private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;
    private Context c;
    private DriveFolder myDriveFolder;



    public gDrive(Context c) {
        this.c = c;
        mDriveResourceClient = Drive.getDriveResourceClient(c, GoogleSignIn.getLastSignedInAccount(c));
    }


    public static GoogleSignInClient signInPlay(Context c) {
        Log.i(TAG, "Start sign in");
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE, Drive.SCOPE_APPFOLDER)
                        .build();
        return GoogleSignIn.getClient(c, signInOptions);
    }

    public void getFolder(){

        Log.i("Drive", "Getting folder");
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, "Secure Cam"))
                .build();
        Task<MetadataBuffer> queryTask = mDriveResourceClient.query(query);
        queryTask
                .addOnSuccessListener(
                        new OnSuccessListener<MetadataBuffer>() {
                            @Override
                            public void onSuccess(MetadataBuffer metadataBuffer) {
                                Metadata myMetadata = metadataBuffer.get(0);
                                Log.i("Search",  myMetadata.getCreatedDate().toString());
                                DriveResource myDriveResource = myMetadata.getDriveId().asDriveResource();
                                myDriveFolder = myMetadata.getDriveId().asDriveFolder();
                                metadataBuffer.release();
                                saveToDrive();
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Search",  "Folder not found");
                    }
                });
    }



    public void searchDestroy(){
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, "SecureCam.gif"))
                .build();
        Task<MetadataBuffer> queryTask = mDriveResourceClient.query(query);
        queryTask
                .addOnSuccessListener(
                        new OnSuccessListener<MetadataBuffer>() {
                            @Override
                            public void onSuccess(MetadataBuffer metadataBuffer) {
                                Metadata myMetadata = metadataBuffer.get(0);
                                Log.i("Search",  myMetadata.getCreatedDate().toString());
                                DriveResource myDriveResource = myMetadata.getDriveId().asDriveResource();
                                metadataBuffer.release();
                                //DriveResource driveResource = metadata.getDriveId().asDriveResource();
                                mDriveResourceClient.delete(myDriveResource)
                                        .addOnSuccessListener(
                                        new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.i(TAG, "deleted file");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e(TAG, "Unable to delete file", e);

                                            }
                                        });
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure...
                    }
                });

    }

    private String getDate(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(c);
    }


    public void saveToDrive() {

        final Task<DriveFolder> appFolderTask = mDriveResourceClient.getRootFolder(); //getAppFolder();
        final Task<DriveContents> createContentsTask = mDriveResourceClient.createContents();
        Tasks.whenAll(appFolderTask, createContentsTask)
                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
                        String title = getDate() + ".gif";
                        DriveFolder parent = appFolderTask.getResult();
                        DriveContents contents = createContentsTask.getResult();
                        OutputStream outputStream = contents.getOutputStream();
                        fileToBitstream(new File(c.getFilesDir().getAbsolutePath() + "/output.gif"), outputStream);
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setMimeType("image/gif")
                                .setTitle(title)
                                .setStarred(true)
                                .build();

                        return mDriveResourceClient.createFile(myDriveFolder, changeSet, contents);
                    }
//                })
/*                .addOnSuccessListener(this,
                        new OnSuccessListener<DriveFile>() {
                            @Override
                            public void onSuccess(DriveFile driveFile) {
                                Log.i(TAG, "File Created");
                                Toast.makeText(this, "file created " + driveFile.getDriveId().encodeToString(), Toast.LENGTH_LONG).show();
                                // showMessage(getString(R.string.file_created,
                                //        driveFile.getDriveId().encodeToString()));
                                //finish();
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Unable to create file", e);
                        //showMessage(getString(R.string.file_create_error));
                        finish();
                    }
 */               });
    }


    private void fileToBitstream(File file, OutputStream outputStream){

        FileInputStream fis;
        try {
            fis = new FileInputStream(file.getPath());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n;
            while (-1 != (n = fis.read(buf)))
                baos.write(buf, 0, n);
            byte[] photoBytes = baos.toByteArray();
            outputStream.write(photoBytes);
            outputStream.close();
            fis.close();
            Log.i("Drive", "File written to Drive");
        } catch (FileNotFoundException e) {
            Log.w(TAG, "FileNotFoundException: " + e.getMessage());
        } catch (IOException e1) {
            Log.w(TAG, "Unable to write file contents." + e1.getMessage());
        }
    }


    public void createFolder() {
        mDriveResourceClient
                .getRootFolder()
                .continueWithTask(new Continuation<DriveFolder, Task<DriveFolder>>() {
                    @Override
                    public Task<DriveFolder> then(@NonNull Task<DriveFolder> task)
                            throws Exception {
                        DriveFolder parentFolder = task.getResult();
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle("Secure Cam")
                                .setMimeType(DriveFolder.MIME_TYPE)
                                .setStarred(true)
                                .build();
                        return mDriveResourceClient.createFolder(parentFolder, changeSet);
                    }
                /*})

                .addOnSuccessListener(c,
                        new OnSuccessListener<DriveFolder>() {
                            @Override
                            public void onSuccess(DriveFolder driveFolder) {
                  //              showMessage(getString(R.string.file_created,
                  //                      driveFolder.getDriveId().encodeToString()));
                  //              finish();
                            }
                        })
                .addOnFailureListener(c, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Unable to create file", e);
                //        showMessage(getString(R.string.file_create_error));
                //        finish();
                    }
                  */
                });
    }

}
