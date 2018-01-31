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
**1. Start by creating an instance of FilePickUtils and LifeCycleCallBackManager.**<br>

 FilePickUtils filePickUtils = new FilePickUtils(this, onFileChoose);;<br>
 LifeCycleCallBackManager lifeCycleCallBackManager = filePickUtils.getCallBackManager();<br>

**2. Callback listener**<br>

 private FilePickUtils.OnFileChoose onFileChoose = new FilePickUtils.OnFileChoose() {<br>
    @Override public void onFileChoose(String fileUri, int requestCode) {<br>
     // here you will get captured or selected image<br>
    }<br>
  };<br>
  
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
