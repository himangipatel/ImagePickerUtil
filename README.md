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
	        compile 'com.github.himangipatel:ImagePickerUtil:0.1.0'
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
  
  @Override<br>
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {<br>
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);<br>
    if (lifeCycleCallBackManager != null) {<br>
      lifeCycleCallBackManager.onRequestPermissionsResult(requestCode, permissions, grantResults);<br>
    }<br>
  }<br>

  @Override<br>
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {<br>
    super.onActivityResult(requestCode, resultCode, data);<br>
    if (lifeCycleCallBackManager != null) {<br>
      lifeCycleCallBackManager.onActivityResult(requestCode, resultCode, data);<br>
    }<br>
  }<br>
  
  **4. Open Camera picker using** <br>

 filePickUtils.requestImageCamera(CAMERA_PERMISSION, true, true); // pass false if you dont want to allow image crope

  **5. Open gallery picker using** <br>
  
filePickUtils.requestImageGallery(STORAGE_PERMISSION_IMAGE, true, true);
 
