import numpy as np
import cv2
import pymeanshift as pms

# parameter for compare function
diff=17

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
    def detectEye(self,faces):
        array_eyes=[]
        for face in faces:
            img_face=self.img[face[1]:face[1]+face[3],face[0]:face[0]+face[2]]
            cc = cv2.CascadeClassifier('haarcascade_eye.xml')
            gray = cv2.cvtColor(img_face,cv2.COLOR_RGB2GRAY)
            eyes = cc.detectMultiScale(gray)
            for eye in eyes:
                eye[0]+=face[0]
                eye[1]+=face[1]
            array_eyes.extend(eyes)
        return array_eyes

    # Returns hair area from an image.
    # area_hair[i][j]=1 if img[i][j] is a part of hair area, 0 otherwise
    # x,y : coordinate of the first hair area
    def getHairArea(self,x,y):
        (segmented,labels,n)=pms.segment(self.img,spatial_radius=6,range_radius=4.5, min_density=50)
        cv2.imshow('segmented',segmented)
        mv = cv2.split(self.img)
        gray = cv2.cvtColor(segmented,cv2.COLOR_RGB2GRAY)
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
        area_hair=np.zeros(shape=labels.shape,dtype=np.int)
        print('making array of hair area...')
        for i in range(area_hair.shape[0]):
            for j in range(area_hair.shape[1]):
                if labels[i][j] in hair:
                    area_hair[i][j]=1
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

    # Returns list of hair parameters from hair region
    def getHairParams(self,faces,eyes,img_front,hair_front,img_side,hair_side):
        face=None
        dic={}
        #first hair point
        x_firsthair=hair_side.shape[1]-1
        y_firsthair=0
        for i in range(hair_side.shape[1]-1,0,-1):
            for h in range(hair_side.shape[0]):
                if hair_side[h][i]==1:
                    x_firsthair=i
                    y_firsthair=h
                    break
            if y_firsthair!=0:
                break
    
        #forehair
        h_forehair=0
        for i in range(x_firsthair,x_firsthair-20,-5):
            for h in range(y_firsthair,hair_side.shape[0]):
                if hair_side[h][i]==1:
                    h_forehair=h
                else:
                    break
        #print('h_forehair',h_forehair)

        #sidehair
        h_sidehair=h_forehair
        x_sidehair=0
        h=h_sidehair
        while h-h_sidehair>-55 or h_sidehair==h_forehair:
            while hair_side[h][i]==0:
                h-=1
            while hair_side[h][i]==1:
                h+=1
            if h>h_sidehair:
                h_sidehair=h
                x_sidehair=i
            i=i-5
            if i<0:
                break
        #print('h_sidehair',h_sidehair)

        #rearhair
        h=h_sidehair
        h_rearhair=h
        while i>0:
            while hair_side[h][i]==0 and h>0:
                h-=1
            while hair_side[h][i]==1:
                h+=1
            if h>h_rearhair:
                h_rearhair=h
            elif h==0:
                break
            i=i-5
        #print('h_rearhair',h_rearhair)

        #head
        i=i+5
        x_lasthair=i
        h=h_rearhair
        while hair_side[h][i]==0:
            h-=1
        h_head=h
        while i<x_firsthair:
            while hair_side[h][i]==0:
                h+=1
            while hair_side[h][i]==1:
                h-=1
            if h<h_head:
                h_head=h
            i=i+5
        #print('h_head',h_head)
        h_forehead=(h_forehair+h_head)/2
        dic['l_forehair']=h_forehair-h_forehead
        dic['l_sidehair']=h_sidehair-h_forehair
        dic['l_rearhair']=h_rearhair-h_sidehair

        v=0
        c=np.array([0,0,0])
        for i in range(x_lasthair,x_firsthair):
            for h in range(h_head,h_rearhair):
                if hair_side[h][i]==1:
                    v=v+1
                    c=c+img_side[h][i]
        dic['volume']=v
        dic['color']=c/v
        
            
        for item in faces:
            if eyes[0][1]>item[1] and eyes[0][1]<item[1]+item[3]\
               and eyes[0][0]>item[0] and eyes[0][0]<item[0]+item[2]:
                face=item
        
            
        h_eye=0
        if len(eyes)!=0:
            for eye in eyes:
                h_eye+=eye[1]
            h_eye/=len(eyes)
            print(h_eye,h_forehair,h_forehead)
            e_forehead=float(h_eye-h_forehair)/(h_eye-h_forehead)
        dic['e_forehead']=round(e_forehead,2)
        dic['e_ear']=1
        return dic
