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

![Qualcomm Snapdragon HDK image](https://git.globaledgesoft.com/root/QDN_AIML_AndroidApplication/raw/develop/QC_DashCam/app/src/main/res/drawable/snapdragon_hdk.jpg)


## Steps to Run the Application
* Firstly set up the hardware as shown in the image below
* Power on the Snapdragon HDK board
* Connect  board to your development machine with a USB cable
* Switch on the display and choose the USB connection option to File Transfer
* Open terminal and install QC_DashCam.apk using adb command- adb install qc_dashCam.apk

## How does it work?
QC_DashCam application opens a camera preview, collects all the frames and converts them to bitmap. The network is built via  Neural Network builder by passing caffe_mobilenet.dlc as the input. The bitmap is then given to the model for inference, which returns object prediction and localization of the respective object.
The application is customized for detection of bicycle, motorbike, bus, car and person only. 

## Screenshot of the application
<img src="https://git.globaledgesoft.com/root/QDN_AIML_AndroidApplication/raw/542efbd36bad1d54e9c6f857f10b7e91eb2bad44/QC_DashCam/app/src/main/res/drawable/sample_predic_img.png" width="250">


