import cv2
import HairAnalyzer
import numpy as np
import random

#ha = HairAnalyzer.HairAnalyzer('suit.jpg')
ha = HairAnalyzer.HairAnalyzer('man.jpeg')
#ha = HairAnalyzer.HairAnalyzer('hurban-1.jpg')

img=ha.getImage()
cv2.imshow('image',img)

# Face area
faces = ha.detectFace()
img_face = img.copy()
for item in faces:
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

#area=ha.getHairArea(424,94) # suit.jpg
area=ha.getHairArea(170,34) # man.jpeg
#area=ha.getHairArea(78,37) # hurban-1.jpg

for i in range(img.shape[0]):
    for j in range(img.shape[1]):
        if area[i][j] == 0:
            img[i][j]=np.array([255,255,255])
cv2.imshow('hair',img)

cv2.waitKey(0)
cv2.destroyAllWindows()
