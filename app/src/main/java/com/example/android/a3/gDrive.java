package com.example.android.a3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.drive.CreateFileActivityOptions;
//import com.google.android.gms.drive.Drive;
//import com.google.android.gms.drive.DriveClient;
//import com.google.android.gms.drive.DriveContents;
//import com.google.android.gms.drive.DriveFile;
//import com.google.android.gms.drive.DriveFolder;
//import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.*;
import com.google.android.gms.drive.MetadataChangeSet;
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
import java.io.OutputStreamWriter;
import java.io.Writer;
/**
 * Created by Scott on 2/23/2018.
 */

public class gDrive {

    private static final String TAG = "drive-quickstart";
    private static final int REQUEST_CODE_SIGN_IN = 0;
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;


    //private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;
    private Context c;

    public gDrive(Context c) {
        this.c = c;
    }


    public void saveToDrive() {
        //mDriveClient = Drive.getDriveClient(c, GoogleSignIn.getLastSignedInAccount(c));
        mDriveResourceClient = Drive.getDriveResourceClient(c, GoogleSignIn.getLastSignedInAccount(c));
        createFileInAppFolder();
    }


    private void createFileInAppFolder() {
        final Task<DriveFolder> appFolderTask = mDriveResourceClient.getRootFolder(); //getAppFolder();
        final Task<DriveContents> createContentsTask = mDriveResourceClient.createContents();
        Tasks.whenAll(appFolderTask, createContentsTask)
                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
                        DriveFolder parent = appFolderTask.getResult();
                        DriveContents contents = createContentsTask.getResult();
                        OutputStream outputStream = contents.getOutputStream();
                        fileToBitstream(new File(c.getFilesDir().getAbsolutePath() + "/output.gif"), outputStream);
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setMimeType("image/gif")
                                .setTitle("SecureCam.gif")
                                .setStarred(true)
                                .build();
                        return mDriveResourceClient.createFile(parent, changeSet, contents);
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

}
