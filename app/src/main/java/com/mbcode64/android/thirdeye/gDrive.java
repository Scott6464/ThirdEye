package com.mbcode64.android.thirdeye;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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

/**
 * Created by Scott on 2/23/2018.
 */

public class gDrive {

    private static final String TAG = "Google drive";
    private static final int REQUEST_CODE_SIGN_IN = 0;
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;
    String webLink = "insert drive link here";
    //private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;
    private Context c;
    private DriveFolder myDriveFolder;
    private int daysToSave;
    private int jpgIndex;

    public gDrive(Context c) {
        this.c = c;
        mDriveResourceClient = Drive.getDriveResourceClient(c, GoogleSignIn.getLastSignedInAccount(c));
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        //boolean emailPref = sharedPref.getBoolean( "pref_email",true);
        String savePref = sharedPref.getString("pref_save", "7");
        //Log.i("Main Pref", savePref);
        daysToSave = Integer.parseInt(savePref);
        jpgIndex = 0;
    }

    public static GoogleSignInClient signInPlay(Context c) {
        Log.i(TAG, "Start sign in");
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE, Drive.SCOPE_APPFOLDER)
                        .build();
        return GoogleSignIn.getClient(c, signInOptions);
    }

    /**
     * Check if folder exists.
     * If not. Create folder.
     *
     * @param folderName
     * @return
     */


    public void getAppFolder(final String folderName) {

        Log.i("Drive finding", folderName);
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, folderName))
                .build();
        Task<MetadataBuffer> queryTask = mDriveResourceClient.query(query);
        queryTask
                .addOnSuccessListener(
                        new OnSuccessListener<MetadataBuffer>() {
                            @Override
                            public void onSuccess(MetadataBuffer metadataBuffer) {
                                if (metadataBuffer.getCount() > 0) {
                                    Log.i("Drive found", folderName);
                                    Metadata myMetadata = metadataBuffer.get(0); //get the first item found
                                    Log.i("Search found", myMetadata.getCreatedDate().toString());
                                    myDriveFolder = myMetadata.getDriveId().asDriveFolder();
                                } else {
                                    Log.i("Search failed", folderName);
                                    createFolder(folderName);
                                }
                                metadataBuffer.release();
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    /**
     * Upload photo on Motion jpg to today's folder
     */

    public void uploadEvent(int jpgIndex) {
        final String folderName = getDate();
        Log.i("Drive finding", folderName);
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, folderName))
                .build();
        Task<MetadataBuffer> queryTask = mDriveResourceClient.query(query);

        queryTask
                .addOnSuccessListener(
                        new OnSuccessListener<MetadataBuffer>() {
                            @Override
                            public void onSuccess(MetadataBuffer metadataBuffer) {
                                if (metadataBuffer.getCount() > 0) {
                                    Log.i("Drive found", folderName);
                                    Metadata myMetadata = metadataBuffer.get(0); //get the first item found
                                    Log.i("Search found", myMetadata.getCreatedDate().toString());
                                    myDriveFolder = myMetadata.getDriveId().asDriveFolder();
                                    saveJpgToDrive();
                                } else {
                                    Log.i("Search failed", folderName);
                                    createFolder(folderName);
                                }
                                metadataBuffer.release();
                                //searchDestroy();


                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    /**
     * Check if folder exists. If not, then create it.
     */

    public void createFolder(final String folderName) {
        Log.i("Drive Creating", folderName);
        mDriveResourceClient
                .getRootFolder()
                .continueWithTask(new Continuation<DriveFolder, Task<DriveFolder>>() {
                    @Override
                    public Task<DriveFolder> then(@NonNull Task<DriveFolder> task)
                            throws Exception {
                        DriveFolder parentFolder = task.getResult();
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle(folderName)
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



    /**
     * No more than 7 gifs allowed in the dir. Delete oldest.
     */

    public void searchDestroy() {
        Query query = new Query.Builder()
                .addFilter(Filters.contains(SearchableField.TITLE, "gif"))
                .build();
        Task<MetadataBuffer> queryTask = mDriveResourceClient.query(query);
        queryTask
                .addOnSuccessListener(
                        new OnSuccessListener<MetadataBuffer>() {
                            @Override
                            public void onSuccess(final MetadataBuffer metadataBuffer) {
                                Log.i("Drive destroy", Integer.toString(metadataBuffer.getCount()));
                                if (metadataBuffer.getCount() > daysToSave) {
                                    Metadata myMetadata = metadataBuffer.get(metadataBuffer.getCount() - 1);
                                    Log.i("Search and destroy", myMetadata.getCreatedDate().toString());
                                    DriveResource myDriveResource = myMetadata.getDriveId().asDriveResource();
                                    mDriveResourceClient.delete(myDriveResource)
                                            .addOnSuccessListener(
                                                    new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.i(TAG, "deleted file");
                                                            if (metadataBuffer.getCount() > daysToSave) {
                                                                searchDestroy();
                                                            }
                                                        }
                                                    })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e(TAG, "Unable to delete file", e);

                                                }
                                            });
                                }
                                metadataBuffer.release();
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Drive Search not found", "");
                    }
                });

    }

    private String getDate() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(c);
    }


    public void saveGifToDrive() {

        final Task<DriveFolder> appFolderTask = mDriveResourceClient.getRootFolder(); //getAppFolder();
        final Task<DriveContents> createContentsTask = mDriveResourceClient.createContents();
        Tasks.whenAll(appFolderTask, createContentsTask)
                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
                        String title = getDate() + ".gif";
                        Log.i("Drive upload ", title);
                        //DriveFolder parent = appFolderTask.getResult();
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
 */
                })

                .addOnSuccessListener(
                        new OnSuccessListener<DriveFile>() {
                            @Override
                            public void onSuccess(DriveFile driveFile) {
                                //showMessage(getString(R.string.file_created,
                                //        driveFile.getDriveId().encodeToString()));
                                //finish();
                                getWebLink();           // get the link to the new gif
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Unable to create file", e);
                        //showMessage(getString(R.string.file_create_error));
                        //finish();
                    }
                });
    }

    private String getTime() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("kk:mm:ss");
        return df.format(c);
    }


    public void saveJpgToDrive() {
        Log.i("Drive", "Writing jpg to drive");
        final Task<DriveFolder> appFolderTask = mDriveResourceClient.getRootFolder(); //getAppFolder();
        final Task<DriveContents> createContentsTask = mDriveResourceClient.createContents();
        Tasks.whenAll(appFolderTask, createContentsTask)
                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
                        String title = getTime() + ".jpg";
                        DriveFolder parent = appFolderTask.getResult();
                        DriveContents contents = createContentsTask.getResult();
                        OutputStream outputStream = contents.getOutputStream();
                        fileToBitstream(new File(c.getFilesDir().getAbsolutePath()
                                + "/" + Integer.toString(jpgIndex) + ".jpg"), outputStream);
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setMimeType("image/jpg")
                                .setTitle(title)
                                .setStarred(true)
                                .build();
                        jpgIndex++;
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
 */
                });
    }


    private void fileToBitstream(File file, OutputStream outputStream) {

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


    public String getWebLink() {
        Log.i("Drive", "getting weblink");
        String date = getDate() + ".gif";
        Query query = new Query.Builder()
                .addFilter(Filters.contains(SearchableField.TITLE, date))
                .build();
        Task<MetadataBuffer> queryTask = mDriveResourceClient.query(query);
        queryTask
                .addOnSuccessListener(
                        new OnSuccessListener<MetadataBuffer>() {
                            @Override
                            public void onSuccess(final MetadataBuffer metadataBuffer) {
                                Metadata myMetadata = metadataBuffer.get(0);
                                webLink = myMetadata.getEmbedLink();//Integer.toString(metadataBuffer.getCount());//getEmbedLink();
                                Log.i("Drive link", Integer.toString(metadataBuffer.getCount())); //webLink);
                                metadataBuffer.release();
                                emailGif();
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Drive Search not found", "");
                        webLink = "getWebLink failed";

                    }
                });
        return webLink;
    }


    public void emailGif() {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    String user = "thirdeye@mbcode.net";
                    String password = "Crouton1!";
                    String recipient = "sengle64@gmail.com";
                    String emailBody = " motion events: " + webLink;
                    String pathForAppFiles = c.getFilesDir().getAbsolutePath() + "/output.gif"; //+ STILL_IMAGE_FILE;
                    GMailSender sender = new GMailSender(user, password);
                    sender.sendMail("Third Eye Daily Digest", emailBody, user, recipient, pathForAppFiles);
                    Log.i("Email", "email sent");
                } catch (Exception e)
                {
                    Log.e("SendMail", e.getMessage(), e);
                }
            }
        };
        t.start();
    }
}
