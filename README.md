# Gallery with Music

I mainly used kotlin for the application and python for my tflite model.

## What is Gallery with Music?

Gallery with Music is a kind of gallery app which added the music recommendation function based on face emotion recognition to the basic gallery. 

## File structure of the project

There are 2 folders in the top-level, 'GallerywithMusic' and 'Model'

### GallerywithMusic

It is for my application. I used kotlin for developing the application. In the main/assets folder, there are 4 tflite files including 'fer_model.tflite', 'reduced_fer_model.tflite', 'pruned_fer_model.tflite' and 'reduced_pruned_fer_model.tflite'. Among them, 'pruned_fer_model.tflite' is set for default. If you want, you can change it in 'main/java/com/example/gallerywithmusic/FaceRecognition.kt'

### Model

There are two sub folders 'Original' and 'Reduced_Category'. 'Original' is for my tflite model which was trained with all categories of 'FER2013' data while 'Reduced_Category' is for the one which was trained only with 4 categories including 'angry', 'happy', 'sad' and 'surprise'. (I deleted 3 out of 7 categories for training and test to improve the accuracy of the model in 'Reduced_Category')

In each of these folders, there is 'Optimized' folder which contains the model optimized with pruning and quantization. They include tflite, h5 and ipynb file.

## Train and Test Data

I used 'FER 2013' data. You can refer to the following link: https://www.kaggle.com/datasets/msambare/fer2013

## Bugs and version update

If there are any updates related to unexpected bugs, I will let you know here. 

## The contributor of 'Gallery with Music'

Chaeyeon Park, Seoul National University, Department of Mathematical Sciences