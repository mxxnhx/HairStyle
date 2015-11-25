import cv2
import HairAnalyzer
import numpy as np
import random

path1='suit.jpg'
#path1='hurban-1.jpg'
#path1='suit2.jpg'
path1='asdf1.jpg'
#path1='kyeong1.jpg'
ha = HairAnalyzer.HairAnalyzer(path1)


img=ha.getImage()
#cv2.imshow('image',img)

# Face&eye area
face,eyes = ha.detectFace()
img_face = img.copy()
p1 = (face[0],face[1])
p2 = (face[0]+face[2],face[1])
p3 = (face[0],face[1]+face[3])
p4 = (face[0]+face[2],face[1]+face[3])
c = (255*random.random(),255*random.random(),255*random.random())
cv2.line(img_face,p1,p2,c,2)
cv2.line(img_face,p2,p4,c,2)
cv2.line(img_face,p3,p4,c,2)
cv2.line(img_face,p1,p3,c,2)
for item in eyes:
    p1 = (item[0],item[1])
    p2 = (item[0]+item[2],item[1])
    p3 = (item[0],item[1]+item[3])
    p4 = (item[0]+item[2],item[1]+item[3])
    c = (255*random.random(),255*random.random(),255*random.random())
    cv2.line(img_face,p1,p2,c,2)
    cv2.line(img_face,p2,p4,c,2)
    cv2.line(img_face,p3,p4,c,2)
    cv2.line(img_face,p1,p3,c,2)
cv2.imshow('face',img_face)
# Hair area

area=ha.getHairArea(face)
img_hair=img.copy()
for i in range(img_hair.shape[0]):
    for j in range(img_hair.shape[1]):
        if area[i][j] == 0:
            img_hair[i][j]=np.array([255,255,255])
cv2.imshow('hair_front',img_hair)

cv2.waitKey(0)
cv2.destroyAllWindows()
