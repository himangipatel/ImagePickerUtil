# ImagePickerUtil
A Imagepicker which allows to select images or capture from camera with permission


### Developed by
[Himangi Patel](https://www.github.com/himangipatel)


**Features**

Easy to Implement. <br>
No permissions required.  <br>
Allow Crop <br>
Compress image <br>

## Installation

Add repository url and dependency in application module gradle file:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
  	dependencies {
	        compile 'com.github.himangipatel:ImagePickerUtil:0.1.8'
	}

## Usage

**1. Start by creating an instance of FilePickUtils and LifeCycleCallBackManager.**

```java
  FilePickUtils filePickUtils = new FilePickUtils(this, onFileChoose);;
 LifeCycleCallBackManager lifeCycleCallBackManager = filePickUtils.getCallBackManager();
 ```


**2. Callback listener**<br>

```java
  private FilePickUtils.OnFileChoose onFileChoose = new FilePickUtils.OnFileChoose() {
    @Override public void onFileChoose(String fileUri, int requestCode) {
     // here you will get captured or selected image<br>
    }
  };
  
  ```
  
**3. Call below lines on onRequestPermissionsResult and onActivityResult**<br>
  
  ```java
   @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (lifeCycleCallBackManager != null) {
      lifeCycleCallBackManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (lifeCycleCallBackManager != null) {
      lifeCycleCallBackManager.onActivityResult(requestCode, resultCode, data);
    }
  }
 ```
 
  **4. Open Camera picker using** <br>

```java
  filePickUtils.requestImageCamera(CAMERA_PERMISSION, true, true); // pass false if you dont want to allow image crope
 ```
  **5. Open gallery picker using** <br>

 ```java
  filePickUtils.requestImageGallery(STORAGE_PERMISSION_IMAGE, true, true);
 ```
 **6. Add below code to your manifest** <br>
 
 ```java
 <provider
        android:name="android.support.v4.content.FileProvider"
        android:authorities="add your package name"
        android:exported="false"
        android:grantUriPermissions="true">
      <meta-data
          android:name="android.support.FILE_PROVIDER_PATHS"
          android:resource="@xml/provider_paths"/>
    </provider>
 ```
  

 <p align="end">
  <img src="https://img.shields.io/badge/Android%20Arsenal-ImagePickerUtil-green.svg?style=flat" width="250"/>
</p>
