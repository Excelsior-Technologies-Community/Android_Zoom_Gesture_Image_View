# **Android Zoom & Gesture Image View Library**


---
A production-ready Android library that provides a custom ImageView with pinch-to-zoom, pan, and double-tap gestures.

---

## ✨ **Features**

- Multi-Touch Gesture Support - All gesture capabilities explained

- Highly Customizable via XML - Easy configuration without coding

- Production-Ready Performance - Technical benefits and optimization

- Easy Integration - Developer experience and compatibility

  ---

# **Preview**
---
<p align="center">
  <img src="https://github.com/S13reya/Android_Zoom_Gesture_Image_View/blob/stages/app/src/main/assets/demovideo.gif" height="320"/>




</p>


## ⚡ **Installation**

**Step 1:** Add JitPack repository to your root build.gradle:

```gradle
maven { url = uri("https://jitpack.io") }
```

**Step 2:** Add the dependency in your app `build.gradle` (example if hosted on JitPack):  

```gradle
dependencies {
	        implementation 'com.github.Excelsior-Technologies-Community:Android_Zoom_Gesture_Image_View:1.0.0'

}
```
## ⚡ **attrs file**

```

<?xml version="1.0" encoding="utf-8"?>
<resources>
    <declare-styleable name="ZoomGestureImageView">
        <!-- Minimum zoom scale (default: 1.0) -->
        <attr name="minZoom" format="float" />

        <!-- Maximum zoom scale (default: 5.0) -->
        <attr name="maxZoom" format="float" />

        <!-- Enable/disable zoom gestures (default: true) -->
        <attr name="enableZoom" format="boolean" />

        <!-- Enable/disable pan gestures (default: true) -->
        <attr name="enablePan" format="boolean" />

        <!-- Enable/disable double tap to zoom (default: true) -->
        <attr name="doubleTapToZoom" format="boolean" />
    </declare-styleable>
</resources>


```

## ⚡ **Usage**

1. Add in XML

```
  <com.ext.android_zoom_gesture_imageview.ZoomGestureImageView
        android:id="@+id/zoomImageView"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:scaleType="matrix"
        android:src="@drawable/sample"
        app:minZoom="1.0"
        app:maxZoom="10.0"
        app:enableZoom="true"
        app:enablePan="true"
        app:doubleTapToZoom="true"
        app:layout_constraintBottom_toTopOf="@+id/controlPanel"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

```





## **📄 License**

**MIT License**  
```
Copyright (c) 2025 Excelsior Technologies

Permission is hereby granted, free of charge, to any person obtaining a copy  
of this software and associated documentation files (the "Software"), to deal  
in the Software without restriction, including without limitation the rights  
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell  
copies of the Software, and to permit persons to whom the Software is  
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all  
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED **"AS IS"**, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR  
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,  
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```



  
