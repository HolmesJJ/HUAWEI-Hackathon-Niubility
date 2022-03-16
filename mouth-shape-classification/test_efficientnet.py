import os
import cv2
import numpy as np
import tensorflow as tf
import matplotlib.pyplot as plt
import efficientnet.tfkeras as efn
from tensorflow import keras

os.environ["CUDA_VISIBLE_DEVICES"] = "-1"

# 申请使用所有显存的50%:
# gpu_options = tf.GPUOptions(per_process_gpu_memory_fraction=0.69)
# sess = tf.Session(config=tf.ConfigProto(gpu_options=gpu_options))

def build_model():
    base_model = efn.EfficientNetB0(
        include_top=False,
        weights='imagenet',
        input_shape=[224, 224, 3],
        pooling='avg'
    )

    model = keras.Sequential()
    model.add(base_model)
    model.add(keras.layers.Dropout(0.5))
    model.add(keras.layers.Dense(512, activation='relu'))
    model.add(keras.layers.Dense(3, activation='softmax'))
    return model

def get_files(path):
    file_list = []
    for maindir, subdir, file_name_list in os.walk(path):
        for filename in file_name_list:
            if ".jpg" in filename:
                apath = os.path.join(maindir,filename)
                file_list.append(apath)
    file_list.sort()
    return file_list

def get_newimage(image, resize_height=224, resize_width=224, normalization=True):
    rgb_image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)  # 将BGR转为RGB
    rgb_image = cv2.resize(rgb_image, (resize_width, resize_height))
    rgb_image = np.asanyarray(rgb_image)
    if normalization:
        rgb_image = rgb_image / 255.
    rgb_image = rgb_image[np.newaxis, :]
    return rgb_image

img_path = 'mouth_data/val/2/normal.150.jpg'

eff_model = build_model()
eff_model.load_weights("models/best_mouth_model_efficientnet.h5")
classes_name_list = ['big', 'normal','small']

image = cv2.imread(img_path)
res = eff_model.predict(get_newimage(image))
print(res)
res = np.argmax(res)
print("类别：", res + 1)


