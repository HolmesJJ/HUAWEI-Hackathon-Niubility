import os
from keras.models import Model, load_model
# from keras.applications.resnet_v2 import ResNet50V2
from keras.applications.mobilenet import MobileNet
# from keras.applications import EfficientNetB0
# from keras.applications.resnet import ResNet50
from keras.applications.vgg19 import VGG19
from keras.optimizers import Adam
import tensorflow as tf
from keras.layers import GlobalAveragePooling2D, Dense, Dropout
from keras.preprocessing.image import ImageDataGenerator
from keras.callbacks import ModelCheckpoint
import matplotlib.pyplot as plt

os.environ["CUDA_VISIBLE_DEVICES"] = "1"

# 申请使用所有显存的50%:
gpu_options = tf.GPUOptions(per_process_gpu_memory_fraction=0.8)
sess = tf.Session(config=tf.ConfigProto(gpu_options=gpu_options))
'''
base_model = ResNet50V2(
    include_top=False,
    weights='imagenet',
    input_shape=(224,224,3),
)
'''
'''
base_model = ResNet50(
    include_top=False,
    weights='imagenet',
    input_shape=(224,224,3),
)
'''

base_model = VGG19(
        include_top=False,
        weights='imagenet',
        input_shape=[224,224,3]
        )

# base_model = MobileNet(input_shape=(224,224,3), alpha=1, depth_multiplier=1,dropout=1e-3,include_top=False,weights='imagenet',input_tensor=None,pooling=None)

x = base_model.output
x = GlobalAveragePooling2D()(x)
x = Dense(3, activation='softmax', name='predictions')(x)
# 输出空间维度、激活函数


model = Model(base_model.input, x)

# model = load_model('final.h5', custom_objects={'relu6': relu6,'DepthwiseConv2D': DepthwiseConv2D})

model.summary()
# model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=[top_k_categorical_accuracy, 'acc'])
adam = Adam(lr=0.0001)
model.compile(optimizer=adam, loss='categorical_crossentropy', metrics=['acc'])


model_checkpoint = ModelCheckpoint('./models/best_mouth_model_new_vgg19.h5', monitor='val_acc',
                                   verbose=1, save_best_only=True)
# 保存模型的路径、被检测的数据、详细信息模式、被监测数据最佳模型是否会被覆盖

train_datagen = ImageDataGenerator(rescale=1. / 255,
                   rotation_range=20,
                   width_shift_range=0.2,
                   height_shift_range=0.2,
                   shear_range=0.2,
                   zoom_range=0.2,
                   channel_shift_range=0,
                   horizontal_flip=True)

test_datagen = ImageDataGenerator(rescale=1. / 255,
                  rotation_range=20,
                  width_shift_range=0.2,
                  height_shift_range=0.2,
                  shear_range=0.2,
                  zoom_range=0.2,
                  channel_shift_range=0,
                  horizontal_flip=True)

train_dir = './mouth_data/train'
validation_dir = './mouth_data/val'
# 我们使用train_datagen生成训练数据，batch_size=20
train_generator = train_datagen.flow_from_directory(
    train_dir,  # train_dir = os.path.join(‘./cats_and_dogs_filtered', 'train')
    target_size=(224, 224),  # All images will be resized to 150x150
    batch_size=32,
    classes=['1','2','3'],
    class_mode='categorical',
    shuffle=True)

# 我们使用test_datagen生成验证数据，batch_size=20
validation_generator = test_datagen.flow_from_directory(
    validation_dir,
    target_size=(224, 224),
    batch_size=32,
    classes=['1','2','3'],
    class_mode='categorical',
    shuffle=True)

history = model.fit_generator(generator=train_generator,
                              steps_per_epoch=train_generator.samples // 32,
                              epochs=20,
                              verbose=1,
                              callbacks=[model_checkpoint],
                              validation_data=validation_generator,
                              validation_steps=validation_generator.samples // 32,
                              shuffle=True)


def show_train_history(train_history,train_metrics,validation_metrics):
    plt.plot(train_history.history[train_metrics])
    plt.plot(train_history.history[validation_metrics])
    plt.title('Train History')
    plt.ylabel(train_metrics)
    plt.xlabel('Epoch')
    plt.legend(['train','validation'],loc='upper left')

def plot(history):
    plt.figure(figsize=(12,4))
    plt.subplot(1,2,1)
    show_train_history(history,'acc','val_acc')
    plt.subplot(1,2,2)
    show_train_history(history,'loss','val_loss')
    plt.savefig('./models/mouth_loss_acc.png')
    # plt.show()

plot(history)



