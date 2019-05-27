# QC DashCam

The project is designed to utilize the Qualcomm Neural Processing SDK, a deep learning software from Qualcomm Snapdragon Platforms for Object Detection in Android platform. The Android Application  uses any built-in/connected  camera to capture the objects on roads and use Machine Learning model to get the prediction/inference and location of the respective objects.

## Pre-requisites
* Before starting the Android application, please follow the instructions for setting up SNPE using the link provided.
	https://developer.qualcomm.com/docs/snpe/setup.html. 
* Android device 6.0 and above which uses below mentioned Snapdragon processors/Snapdragon HDK with display can be used to test the application

## List of Supported Snapdragon Devices

- Qualcomm Snapdragon 855
- Qualcomm Snapdragon 845
- Qualcomm Snapdragon 835
- Qualcomm Snapdragon 821
- Qualcomm Snapdragon 820
- Qualcomm Snapdragon 710
- Qualcomm Snapdragon 660
- Qualcomm Snapdragon 652
- Qualcomm Snapdragon 636
- Qualcomm Snapdragon 630
- Qualcomm Snapdragon 625
- Qualcomm Snapdragon 605
- Qualcomm Snapdragon 450

The above list supports the application with CPU and GPU.For more information on the supported devices, please follow this link https://developer.qualcomm.com/docs/snpe/overview.html

## Components
Below are the items used in the project.
1. Mobile Display with QC Dash Cam app
2. HDK Snapdragon board with GPU enabled
3. USB type – C cable
4. External camera setup
5. Power Cable

## Hardware Setup

![Qualcomm Snapdragon HDK image](https://github.com/globaledgesoft/AIML-DashCam-App/blob/master/app/src/main/res/drawable/snapdragon_hdk.jpg)

## How does it work?
QC_DashCam application opens a camera preview, collects all the frames and converts them to bitmap. The network is built via  Neural Network builder by passing caffe_mobilenet.dlc as the input. The bitmap is then given to the model for inference, which returns object prediction and localization of the respective object.
The application is customized for detection of bicycle, motorbike, bus, car and person only. 

## Instructions to install the application on Board
Below are the items used in the project.
(The appication can be built from the scratch or can use the apk in 


## Steps to Install and Run the Application
* Firstly set up the hardware as shown above in the Hardware Setup section
* Power on the Snapdragon HDK board
* Connect the Dev-Board/Android phone via usb to the device
* Switch on the display and choose the USB connection option to File Transfer
* Check if ABD is installed in the windows/linux device, if not follow the below instructions in the below link to install
	https://developer.android.com/studio/command-line/adb.html.
* Use the below command to install application apk in the connected device with help of abd. [Download APK(Debug)](https://github.com/globaledgesoft/AIML-DashCam-App/blob/master/app/build/outputs/apk/debug)

	$ adb install app-debug.apk
* Search the GESL_DashCam in the app menu and launch the application

## Screenshot of the application
<a href="url"><img src="https://github.com/globaledgesoft/AIML-DashCam-App/blob/master/app/src/main/res/drawable/sample_predic_img.png" align="left" height="640" width="360" ></a>
