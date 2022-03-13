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

mobilenet_model = load_model('models/best_mouth_model.h5', custom_objects={'relu6': relu6,'DepthwiseConv2D': DepthwiseConv2D})
vgg_model = load_model('models/best_mouth_model_new_vgg19.h5')
resnet_model = load_model('models/best_mouth_model_new_resnet50.h5')

classes_name_list = ['big', 'normal','small']

# all_path = get_files("E:\MyProjects\Mouth\EfficientNet-classification-keras-main\efficient_classify\data/test/3/")
# true_list = list()
# for img_path in all_path:
#     image = cv2.imread(img_path)
#     new_img = get_newimage(image)
#     mobilenet_res = mobilenet_model.predict(new_img)
#     vgg_res = vgg_model.predict(new_img)
#     resnet_res = resnet_model.predict(new_img)
#
#     mobilenet_cls = np.argmax(mobilenet_res)
#     print(mobilenet_cls)
#     vgg_cls = np.argmax(vgg_res)
#     print(vgg_cls)
#     resnet_cls = np.argmax(resnet_res)
#     print(resnet_cls)
#     best_cls = None
#
#     if mobilenet_cls == vgg_cls or mobilenet_cls == resnet_cls:
#         best_cls = mobilenet_cls
#     elif vgg_cls == resnet_cls:
#         best_cls = vgg_cls
#
#     print(best_cls)
#     print("类别：", classes_name_list[best_cls])
#     if best_cls == 2:
#         true_list.append(2)
#
# print(len(all_path))
# print(len(true_list))
#

img_path = 'E:\MyProjects\Mouth\EfficientNet-classification-keras-main\efficient_classify\data/test/2/normal.150.jpg'
image = cv2.imread(img_path)
new_img = get_newimage(image)
mobilenet_res = mobilenet_model.predict(new_img)
vgg_res = vgg_model.predict(new_img)
resnet_res = resnet_model.predict(new_img)

mobilenet_cls = np.argmax(mobilenet_res)
vgg_cls = np.argmax(vgg_res)
resnet_cls = np.argmax(resnet_res)
best_cls = None

if mobilenet_cls == vgg_cls or mobilenet_cls == resnet_cls:
    best_cls = mobilenet_cls
elif vgg_cls == resnet_cls:
    best_cls = vgg_cls

print(best_cls)
print("类别：", classes_name_list[best_cls])