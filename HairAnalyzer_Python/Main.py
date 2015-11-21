import cv2
import HairAnalyzer
import numpy as np
import random

def draw_square(item,img_face, color) :
    p1 = (item[0],item[1])
    p2 = (item[0]+item[2],item[1])
    p3 = (item[0],item[1]+item[3])
    p4 = (item[0]+item[2],item[1]+item[3])
    cv2.line(img_face,p1,p2,color,2)
    cv2.line(img_face,p2,p4,color,2)
    cv2.line(img_face,p3,p4,color,2)
    cv2.line(img_face,p1,p3,color,2)
    return img_face

def center(item) :
    return (item[0]+item[2],item[1]+item[3])

def show(img, area,name) :
    img2 = img.copy()
    for i in range(img2.shape[0]):
        for j in range(img2.shape[1]):
            if area[i][j] == 1:
                img2[i][j]=np.array([255,255,255])
    cv2.imshow(name,img2)

def make_front_hair(filename):
    ha = HairAnalyzer.HairAnalyzer(filename)
    img = ha.getImage()

    faces = ha.detectFace()
    eyes = ha.detectEye(faces)
    img_face = img.copy()

    for item in faces:
        img_face = draw_square(item,img_face,(255,0,0))
    for item in eyes:
        img_face = draw_square(item,img_face,(255,255,0))

    cv2.imshow('face',img_face)

    (height,width,color) = img.shape
    for i in range (1) :
        area=ha.getHairArea(faces[0][2]*random.random() + faces[0][0],faces[0][1]*random.random())  
        show(img, area,str(i))

    #print(ha.getHairParams(faces,eyes,img,area,img2,area2))


make_front_hair('asdf6.jpg')




"""
ha2 = HairAnalyzer.HairAnalyzer('me2.jpg')

img2=ha2.getImage()

area2=ha2.getHairArea(200,100) #me2.jpg

for i in range(img2.shape[0]):
    for j in range(img2.shape[1]):
        if area2[i][j] == 0:
            img2[i][j]=np.array([255,255,255])
cv2.imshow('hair_side',img2)
"""

cv2.waitKey(0)
cv2.destroyAllWindows()
