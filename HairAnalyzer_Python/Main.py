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
        #cv2.imshow('face',img_face)
        # Hair area

        #area=ha.getHairArea(424,94) # suit.jpg
        #area=ha.getHairArea(170,100) # man.jpeg
        #area=ha.getHairArea(78,37) # hurban-1.jpg
        #area=ha.getHairArea(200,100) # me.jpg & suit2.jpg
        #area=ha.getHairArea(175,60) # asdf1&2.jpg
        #area=ha.getHairArea(251,71) # asdf3.jpg
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



"""
path1='me.jpg'
#path1='suit2.jpg'
#path1='asdf1.jpg'
path2='me2.jpg'
ha = HairAnalyzer.HairAnalyzer(path1)
ha2 = HairAnalyzer.HairAnalyzer(path2)


img=ha.getImage()
img2=ha2.getImage()
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

#area=ha.getHairArea(424,94) # suit.jpg
#area=ha.getHairArea(170,100) # man.jpeg
#area=ha.getHairArea(78,37) # hurban-1.jpg
#area=ha.getHairArea(200,100) # me.jpg & suit2.jpg
#area=ha.getHairArea(175,60) # asdf1&2.jpg
#area=ha.getHairArea(251,71) # asdf3.jpg
area=ha.getHairArea(face)
for i in range(img.shape[0]):
    for j in range(img.shape[1]):
        if area[i][j] == 0:
            img[i][j]=np.array([255,255,255])
cv2.imshow('hair_front',img)

area2=ha2.getHairArea_side(face,img) #me2.jpg

for i in range(img2.shape[0]):
    for j in range(img2.shape[1]):
        if area2[i][j] == 0:
            img2[i][j]=np.array([255,255,255])
cv2.imshow('hair_side',img2)


print(ha.getHairParams(face,eyes,img,area,img2,None))

cv2.waitKey(0)
cv2.destroyAllWindows()
    """
"""
L = [1,4,5,6,14,17,19,22,23,24,28,35,36,40,43,46,48,50,51,56,57,58,60,61,62,63,64,68,69,70,74,75,77,78,80,83,84,85]

for i in L :
    string ="hair/t%d.jpg"%(i)
    check_hair_front(string)
"""