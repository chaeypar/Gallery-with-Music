import numpy as np
import pandas as pd 
from pathlib import Path
import tensorflow
from tensorflow.keras.preprocessing import image
from tensorflow.keras.layers import Conv2D,MaxPooling2D,Dense,Input,Dropout,Flatten,concatenate,AveragePooling2D,BatchNormalization
from tensorflow.keras.utils import to_categorical,plot_model
from tensorflow.keras.models import Model
from tensorflow.keras.preprocessing import image
from sklearn.preprocessing import LabelEncoder
import matplotlib.pyplot as plt
import tensorflow as tf
import warnings
warnings.filterwarnings(action = 'ignore')

folder_path = "./fer2013"
picture_size  = 128
batch_size = 128
datagen_train = image.ImageDataGenerator(rescale = 1./255,shear_range = 0.2)
datagen_test = image.ImageDataGenerator(rescale = 1./255,shear_range = 0.2)

train_set = datagen_train.flow_from_directory(folder_path+"/train",target_size = (picture_size,picture_size),
                                             color_mode = 'grayscale',batch_size = batch_size,class_mode = 'categorical',shuffle = True,
                                             )

test_set = datagen_test.flow_from_directory(folder_path+"/test",target_size = (picture_size,picture_size),
                                             color_mode = 'grayscale',batch_size = batch_size,class_mode = 'categorical',shuffle = True)

def plot_function(history):
    fig,ax = plt.subplots(1,2,figsize = (25,5))
    ax[0].plot(history.history['loss'],color = 'red',label = 'train_loss')
    ax[0].set_title('Loss and val_loss')
    ax[0].plot(history.history['val_loss'],color = 'green',label = 'val_loss')
    ax[0].legend()
    ax[1].plot(history.history['accuracy'],color = 'orange',label = 'train_accuracy')
    ax[1].set_title('accuracy and val_accuracy')
    ax[1].plot(history.history['val_accuracy'],color = 'black',label = 'val_accuarcy')
    ax[1].legend()
    
inputs = Input(shape = (128,128,1))
x = Conv2D(128,2,strides = 1,padding = 'same',activation = 'relu')(inputs)
x = BatchNormalization()(x)
x = MaxPooling2D(pool_size = (2,2))(x)
x = Dropout(0.55)(x)


x = Conv2D(256,2,strides = 1,padding = 'same',activation = 'relu')(x)
x = BatchNormalization()(x)
x = MaxPooling2D(pool_size = (2,2))(x)
x = Dropout(0.55)(x)

x = Conv2D(512,2,strides = 1,padding = 'same',activation = 'relu')(x)
x = BatchNormalization()(x)
x = MaxPooling2D(pool_size = (2,2))(x)
x = Dropout(0.55)(x)

x = Conv2D(256,5,strides = 2,padding = 'same',activation = 'relu')(x)
x = BatchNormalization()(x)
x = MaxPooling2D(pool_size = (2,2))(x)
x = Dropout(0.55)(x)

x = Conv2D(512,5,strides = 3,padding = 'same',activation = 'relu')(x)
x = BatchNormalization()(x)
x = MaxPooling2D(pool_size = (2,2))(x)
x = Dropout(0.55)(x)


x = Conv2D(128,2,strides = 1,padding = 'same',activation = 'relu')(x)
x = BatchNormalization()(x)
x = Flatten()(x)

x = Dense(512,activation = 'relu')(x)
x = BatchNormalization()(x)
x = Dropout(0.45)(x)

x = Dense(256,activation = 'linear')(x)
x = BatchNormalization()(x)
x = Dropout(0.45)(x)

outputs = Dense(7,activation = 'softmax')(x)
model3 = Model(inputs,outputs)

from keras.callbacks import ModelCheckpoint,EarlyStopping,ReduceLROnPlateau
checkpoint = ModelCheckpoint("./model.h5",monitor = 'val_acc',verbose = 1,save_best_only = True)
reduce_learning_rate = ReduceLROnPlateau(monitor = 'val_loss',factor = 0.02,patience = 3)
callback = [checkpoint,reduce_learning_rate]
#optimizer = tensorflow.keras.optimizers.Adam(learning_rate = 0.001,decay = 6e-5)
model3.compile(loss = 'categorical_crossentropy',optimizer ='sgd',metrics = ['accuracy'])
model3.summary()

history4 = model3.fit(train_set,epochs = 30,validation_data = test_set,callbacks = callback)
model3.evaluate(test_set)
plot_function(history4)
model3.save('Model.h5')