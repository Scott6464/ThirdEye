package com.mbcode64.android.thirdeye;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Scott on 2/23/2018.
 */

public class gDrive {

    private static final String TAG = "Google drive";
    String webLink = "insert drive link here";
    String webDateLink = "Date Folder";
    String emailAddress = "email@address";
    private DriveResourceClient mDriveResourceClient;
    private Context c;
    private DriveFolder myDriveFolder;
    private DriveFolder myDateFolder;
    private int daysToSave;
    private int jpgIndex;
    private String appFolder;
    private String activityName;

    public gDrive(Context c, String activityName) {
        this.c = c;
        this.activityName = activityName;
        mDriveResourceClient = Drive.getDriveResourceClient(c, GoogleSignIn.getLastSignedInAccount(c));
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        boolean emailPref = sharedPref.getBoolean("pref_email", true);
        String savePref = sharedPref.getString("pref_save", "6");
        emailAddress = GoogleSignIn.getLastSignedInAccount(c).getEmail();
        Log.i("Email ", emailAddress);
        daysToSave = Integer.parseInt(savePref);
        jpgIndex = 0;
        appFolder = "Third Eye" + "-" + Build.MANUFACTURER + " " + Build.MODEL;
        getAppFolder(appFolder);
        Log.i(TAG, "context name " + c.toString());
    }


    public String getDateFolder(String date) {
        final String folderName = date;
        Log.i("Drive finding date", folderName);
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, folderName))
                .build();
        Task<MetadataBuffer> queryTask = mDriveResourceClient.queryChildren(myDriveFolder, query);
        queryTask
                .addOnSuccessListener(
                        new OnSuccessListener<MetadataBuffer>() {
                            @Override
                            public void onSuccess(MetadataBuffer metadataBuffer) {
                                if (metadataBuffer.getCount() > 0) {
                                    Log.i("Date Drive found", folderName);
                                    Metadata myMetadata = metadataBuffer.get(0); //get the first item found
                                    webDateLink = myMetadata.getAlternateLink();
                                    Log.i("Date Search found", myMetadata.getTitle());
                                    myDateFolder = myMetadata.getDriveId().asDriveFolder();
                                    if (activityName.equals("email")) {
                                        getWebLink(0);
                                    }
                                } else {
                                    Log.i("Date Search failed", folderName);
                                    createDateFolder(folderName);
                                }
                                metadataBuffer.release();
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
        return webDateLink;
    }


    /**
     * Check if folder exists. If not, then create it.
     */

    public void createDateFolder(final String folderName) {
        Log.i("Drive Creating Date", folderName);
        mDriveResourceClient
                .getRootFolder()
                .continueWithTask(new Continuation<DriveFolder, Task<DriveFolder>>() {
                    @Override
                    public Task<DriveFolder> then(@NonNull Task<DriveFolder> task) {
                        DriveFolder parentFolder = task.getResult();
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle(folderName)
                                .setMimeType(DriveFolder.MIME_TYPE)
                                .setStarred(true)
                                .build();
                        return mDriveResourceClient.createFolder(myDriveFolder, changeSet);
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
                                    if (activityName.equals("email")) {
                                        getDateFolder(yesterday());
                                    } else {
                                        getDateFolder(getDate());
                                    } // After App folder is found, create date folder.
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
                                    myDateFolder = myMetadata.getDriveId().asDriveFolder();
                                    saveJpgToDrive();
                                } else {
                                    Log.i("Search failed", folderName);
                                    appFolder = "Third Eye" + "-" + Build.MANUFACTURER + " " + Build.MODEL;
                                    getAppFolder(appFolder);
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
     * Check if folder exists. If not, then create it.
     */

    public void createFolder(final String folderName) {
        Log.i("Drive Creating", folderName);
        mDriveResourceClient
                .getRootFolder()
                .continueWithTask(new Continuation<DriveFolder, Task<DriveFolder>>() {
                    @Override
                    public Task<DriveFolder> then(@NonNull Task<DriveFolder> task) {
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
                .addFilter(Filters.contains(SearchableField.TITLE, "20"))
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, "application/vnd.google-apps.folder"))
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




    public void saveGifToDrive(final int numEvents) {
        getDateFolder(getDate());
        final Task<DriveFolder> appFolderTask = mDriveResourceClient.getRootFolder();
        final Task<DriveContents> createContentsTask = mDriveResourceClient.createContents();
        Tasks.whenAll(appFolderTask, createContentsTask)
                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(@NonNull Task<Void> task) {
                        String title = "MotionEvents.gif";
                        Log.i("Drive upload ", title);
                        DriveContents contents = createContentsTask.getResult();
                        OutputStream outputStream = contents.getOutputStream();
                        fileToBitstream(new File(c.getFilesDir().getAbsolutePath() + "/output.gif"), outputStream);
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setMimeType("image/gif")
                                .setTitle(title)
                                .setStarred(true)
                                .build();
                        return mDriveResourceClient.createFile(myDateFolder, changeSet, contents);
                    }
                })

                .addOnSuccessListener(
                        new OnSuccessListener<DriveFile>() {
                            @Override
                            public void onSuccess(DriveFile driveFile) {
                                searchDestroy();
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
        Date d = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("kk:mm:ss");
        return df.format(d);
    }

    private String getDate() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        return df.format(c);
    }

    private String yesterday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date d = cal.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        return df.format(d);
    }

    public String tomorrow() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        Date d = cal.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        return df.format(d);
    }



    public void saveJpgToDrive() {
        Log.i("Drive", "Writing jpg to drive");
        final Task<DriveFolder> appFolderTask = mDriveResourceClient.getRootFolder();
        final Task<DriveContents> createContentsTask = mDriveResourceClient.createContents();
        Tasks.whenAll(appFolderTask, createContentsTask)
                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(@NonNull Task<Void> task) {
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
                        return mDriveResourceClient.createFile(myDateFolder, changeSet, contents);
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

    //todo one search function search(folder, string)



    public String getWebLink(final int numEvents) {
        Log.i("Drive", "getting weblink");
        Query query = new Query.Builder()
                .addFilter(Filters.contains(SearchableField.TITLE, "Motion"))
                .build();
        if (myDateFolder != null) {
            Task<MetadataBuffer> queryTask = mDriveResourceClient.queryChildren(myDateFolder, query);
            queryTask
                    .addOnSuccessListener(
                            new OnSuccessListener<MetadataBuffer>() {
                                @Override
                                public void onSuccess(final MetadataBuffer metadataBuffer) {
                                    if (metadataBuffer.getCount() > 0) {
                                        Metadata myMetadata = metadataBuffer.get(0);
                                        webLink = myMetadata.getEmbedLink();
                                        Log.i("Drive link", Integer.toString(metadataBuffer.getCount()) + webLink);
                                        sendEmail();
                                    }
                                    metadataBuffer.release();
                                }
                            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("Drive Search not found", "");
                            webLink = "getWebLink failed";

                        }
                    });
        }
        return webLink;
    }


    public void emailGif(final int numEvents) {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    String user = "email";
                    String password = "password";
                    String recipient = emailAddress;
                    String emailBody = "Motion Folder " + webDateLink + "<br><br>" + Integer.toString(numEvents) + " motion events: " + webLink;
                    String pathForAppFiles = c.getFilesDir().getAbsolutePath() + "/output.gif"; //+ STILL_IMAGE_FILE;
                    GMailSender sender = new GMailSender(user, password);
                    sender.sendMail("Third Eye Daily Digest", emailBody, user, recipient, pathForAppFiles);
                    Log.i("Email", "email sent " + recipient);
                } catch (Exception e) {
                    Log.e("SendMail", e.getMessage(), e);
                }
            }
        };
        t.start();
    }


    private void sendEmail() {

        new Thread(new Runnable() {
            public void run() {
                try {
                    URL url = new URL("http://mbcode.net/b.pl?gif=" + webLink + "&folder=" + webDateLink + "&address=" + emailAddress);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    int responseCode = urlConnection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        String server_response = readStream(urlConnection.getInputStream());
                        Log.i("CatalogClient", server_response);
                    }
                    Log.i(TAG, "http success");
                } catch (MalformedURLException ex) {
                    Log.e("httptest", Log.getStackTraceString(ex));
                } catch (IOException ex) {
                    Log.e("httptest", Log.getStackTraceString(ex));
                }
            }
        }).start();
    }


// Converting InputStream to String

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }


}
