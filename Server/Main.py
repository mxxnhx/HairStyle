import cv2
import HairAnalyzer
import numpy as np
import random

#path1='suit.jpg'
#path1='man.jpg'
#path1='hurban-1.jpg'
def check_hair_front(filename) :
    try:

        ha = HairAnalyzer.HairAnalyzer(filename)

        img=ha.getImage()

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

        area=ha.getHairArea(face)
        for i in range(img.shape[0]):
            for j in range(img.shape[1]):
                if area[i][j] == 0:
                    img[i][j]=np.array([255,255,255])
        #cv2.imshow('hair_front',img)
        cv2.imwrite(filename+'_a.png',img)
        d = ha.getHairParams(face,eyes,img,area,None,None)
        return d
    except:
        return