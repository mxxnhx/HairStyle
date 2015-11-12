import numpy as np
import cv2
import pymeanshift as pms

# parameter for compare function
diff=22 

# Get first index of an integer from matrix.
# m : matrix
# n : an integer
def index_mat(m,n):
    for i in range(len(m)):
        if n in m[i]:
            return i,np.where(m[i]==n)[0][0]

# Compare function between factors.
# Used for choosing neighboring area.
def compare(n1,n2):
    return abs(int(n1)-int(n2))<=diff

class HairAnalyzer:
    # path : path of an image file
    def __init__(self,path):
        self.img = cv2.imread(path)
    def loadImage(self,path):
        self.img = cv2.imread(path)
    def getImage(self):
        return self.img
        
    # Detects face from an image.
    # elements in faces : [x,y,width,height]
    def detectFace(self):
        cc = cv2.CascadeClassifier('haarcascade_frontalface_default.xml')
        gray = cv2.cvtColor(self.img,cv2.COLOR_RGB2GRAY)
        faces = cc.detectMultiScale(gray)
        return faces

    # Returns hair area from an image.
    # area_hair[i][j]=1 if img[i][j] is a part of hair area, 0 otherwise
    # x,y : coordinate of the first hair area
    def getHairArea(self,x,y):
        (segmented,labels,n)=pms.segment(self.img,spatial_radius=6,
                              range_radius=4.5, min_density=200)
        mv = cv2.split(self.img)
        gray = cv2.cvtColor(segmented,cv2.COLOR_RGB2GRAY)
        #cv2.imshow('segmented',segmented)
        hair = []
        hair_new = [labels[y][x]]
        not_hair = range(n)
        not_hair.remove(labels[y][x])
        neighbor=self.getNeighbor(labels,n)
        factors=self.getFactor(gray,labels,n)
        # Run until new hair area is not detected
        while(hair!=hair_new):
            hair=hair_new[:]
            for i in hair:
                for j in not_hair:
                    if neighbor[i][j]==1 and compare(factors[i],factors[j]):
                        hair_new.append(j)
                        not_hair.remove(j)
        #print(hair)
 
        area_hair=labels.copy()
        for i in range(area_hair.shape[0]):
            for j in range(area_hair.shape[1]):
                if labels[i][j] in hair:
                    area_hair[i][j]=1
                else:
                    area_hair[i][j]=0
        return area_hair

    # Returns matrix of neighbor relations between segmented areas.
    # neighbor[i][j]=1 if area i and j neighbors, 0 otherwise.
    def getNeighbor(self,label,n):
        neighbor=[[0 for x in range(n)] for x in range(n)]
        for i in range(len(label)-1):
            for j in range(len(label[0])):
                if label[i][j]!=label[i+1][j]:
                    n1=label[i][j]
                    n2=label[i+1][j]
                    neighbor[n1][n2]=1
                    neighbor[n2][n1]=1
        for i in range(len(label)):
            for j in range(len(label[0])-1):
                if label[i][j]!=label[i][j+1]:
                    n1=label[i][j]
                    n2=label[i][j+1]
                    neighbor[n1][n2]=1
                    neighbor[n2][n1]=1
        return neighbor
    
    # Returns list of factors for each segmented areas.
    # these factors are used to determine hair area.
    def getFactor(self,img,label,n):
        factors=[]
        for i in range(n):
            x,y=index_mat(label,i)
            factors.append(img[x][y])
        return factors
