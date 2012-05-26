LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_C_INCLUDES += $(LOCAL_PATH)/src

LOCAL_SRC_FILES := \
    com_android_inputmethod_norwegian_BinaryDictionary.cpp \
    src/dictionary.cpp \
    src/char_utils.cpp

LOCAL_C_INCLUDES += \
    external/icu4c/common \
    $(JNI_H_INCLUDE)

LOCAL_LDLIBS := -lm -llog -landroid

LOCAL_PRELINK_MODULE := false

LOCAL_SHARED_LIBRARIES := \
    libandroid_runtime \
    libcutils \
    libutils \
    libicuuc

LOCAL_MODULE := libjni_norwegianime_ics

LOCAL_MODULE_TAGS := user

include $(BUILD_SHARED_LIBRARY)
