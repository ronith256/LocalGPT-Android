package com.lucario.gpt4allandroid;

import android.content.Context;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

// Shamelessly stolen from https://stackoverflow.com/questions/13765371/so-library-from-included-jar-involves-unsatisfiedlinkerror
public class SharedLibraryLoader
{
  private static final String TAG = SharedLibraryLoader.class.getSimpleName();
  private static Context context;
  private static String libDir = "lib";
  private static String shortLibName;
  private static String fullLibName;

  static public boolean loadLibrary(String libName, Context ctx)
  {
    context = ctx;
    shortLibName = libName;
    fullLibName = libName;

    try
    {
      Log.d(TAG, "Trying to load library");
      System.loadLibrary(shortLibName);
      Log.d(TAG, "Library was loaded from default location");
      return true;
    }
    catch(UnsatisfiedLinkError e)
    {
      Log.d(TAG,"Lib wasn't found at default location. Trying to find in application private storage");
      String path = null;
      path = findInAppStorage(fullLibName);
      if(path != null)
      {
        Log.d(TAG,"Lib was found in application private storage. Loading lib...");
        System.load(path);
        return true;
      }
      else
      {
        Log.d(TAG,"Lib was not found in application private storage. Trying to find in apk...");
        path = findInApkAndCopyToAppStorage(fullLibName);

        if(path != null)
        {
          Log.d(TAG,"Lib was found in apk and copied to application private storage. Loading lib...");
          System.load(path);
          return true;
        }
        else
        {
          Log.e(TAG, "FAILED TO LOAD LIBRARY");
          return false;
        }
      }
    }
  }

  static private String findInAppStorage(String libName)
  {

    Log.d(TAG,"enter findInAppStorage()");
    String basePath = context.getApplicationInfo().dataDir;
    File dataDir = new File(basePath);

    String[] listFiles;
    String  lib = null;
    listFiles = dataDir.list();


    for(int i=0; i < listFiles.length; i++)
    {
      lib = findInStorage(basePath + "/" +listFiles[i], libName);

      if(lib != null)
      {
        return lib;
      }
    }

    Log.d(TAG, "Lib wasn't found.");
    return null;
  }

  static private String findInStorage(String path, String nameOfLib)
  {
    File file = new File(path);
    if(file.isDirectory())
    {
      Log.d(TAG,"Strorage__dir: " + path + "/");
      String[]    list = file.list();
      String      target = null;
      for(int i = 0; i < list.length; i++)
      {
        target = findInStorage(path + "/" + list[i], nameOfLib);
        if(target != null)
        {
          return target;
        }
      }
    }
    else
    {
      Log.d(TAG,"Strorage_file: " + path);
      if(path.contains(nameOfLib))
      {
        Log.d(TAG,"Lib was found in: " + path);
        return path;
      }
    }
    return null;
  }

  static private String findInApkAndCopyToAppStorage(String libName)
  {
    Log.d(TAG, "Enter findInApkAndCopyToStorage()");

    // ---------------- ZIP - find path to .so  inside .apk ------------------
    String apkPath = context.getPackageResourcePath();
    Log.d(TAG, String.format("Path to Package resource is: %s", apkPath));

    try
    {
      ZipFile zf = new ZipFile(apkPath);

      Enumeration<ZipEntry> zipFiles = (Enumeration<ZipEntry>) zf.entries();
      ZipEntry    soZipEntry = null;
      ZipEntry    tempZipEntry;
      String      tmpString;
      for ( ; zipFiles.hasMoreElements();)
      {
        tempZipEntry = zipFiles.nextElement();
        tmpString = tempZipEntry.getName();

        Log.d(TAG, "Trying zip file: " + tmpString);

        if(tmpString.contains(libName))
        {
          Log.d(TAG, "Library " + fullLibName + " was found in: " + tmpString);
          soZipEntry = tempZipEntry;
        }
      }

      //----------now copy library---------------

      if(soZipEntry != null)
      {
        InputStream soInputStream = zf.getInputStream(soZipEntry);

        File fileDir;
        File soFile;
        OutputStream outStream;
        fileDir = context.getApplicationContext().getDir(libDir, Context.MODE_PRIVATE); // but "app_lib" was created!
        String fullSoFilePath = fileDir.getAbsolutePath() + "/" + libName;
        Log.d(TAG, "New libpath is "+ fullSoFilePath);
        soFile = new File(fullSoFilePath);

        Log.d(TAG, "Is file already exists? - " + soFile.exists());

        outStream = new BufferedOutputStream(new FileOutputStream(soFile));

        Log.d(TAG, "Start copying library...");
        byte[] byteArray = new byte[256];
        int copiedBytes = 0;

        while((copiedBytes = soInputStream.read(byteArray)) != -1)
        {
          outStream.write(byteArray, 0, copiedBytes);
        }

        Log.d(TAG, "Finish copying library");
        outStream.close();

        soInputStream.close();
        return fullSoFilePath;
      }
      else
      {
        Log.d(TAG, "Library not Found in APK");
        return null;
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
      return null;
    }
  }
}
