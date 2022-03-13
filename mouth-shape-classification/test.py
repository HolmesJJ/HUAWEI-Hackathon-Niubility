import os
import cv2
import numpy as np
from keras.models import Model, load_model
from keras.applications.mobilenet import MobileNet, relu6, DepthwiseConv2D
import tensorflow as tf
from keras.layers import GlobalAveragePooling2D, Dense, Dropout
from keras.preprocessing.image import ImageDataGenerator
from keras.callbacks import ModelCheckpoint
import matplotlib.pyplot as plt

os.environ["CUDA_VISIBLE_DEVICES"] = "-1"

# 申请使用所有显存的50%:
# gpu_options = tf.GPUOptions(per_process_gpu_memory_fraction=0.69)
# sess = tf.Session(config=tf.ConfigProto(gpu_options=gpu_options))

def get_newimage(image, resize_height=224, resize_width=224, normalization=True):
    rgb_image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)  # 将BGR转为RGB
    rgb_image = cv2.resize(rgb_image, (resize_width, resize_height))
    rgb_image = np.asanyarray(rgb_image)
    if normalization:
        rgb_image = rgb_image / 255.
    rgb_image = rgb_image[np.newaxis, :]
    return rgb_image

test_path = 'data/val/3/23891005905_17ce9e6936.jpg'

file_model = load_model('models/best_model.h5', custom_objects={'relu6': relu6,'DepthwiseConv2D': DepthwiseConv2D})
#file_model = load_model('models/best_model.h5')

image = cv2.imread(test_path)
res = file_model.predict(get_newimage(image))
res = np.argmax(res)
print("类别：",res + 1)
