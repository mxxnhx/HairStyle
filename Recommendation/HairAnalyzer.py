import numpy as np
import cv2
import pymeanshift as pms
import sqlite3

db=sqlite3.connect("DB")
# parameter for compare function
diff=30
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
    d1=abs(int(n1[0])-int(n2[0]))
    d2=abs(int(n1[1])-int(n2[1]))
    d3=abs(int(n1[2])-int(n2[2]))
    return d1*d1+d2*d2+d3*d3<diff*diff*2

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
        cc = cv2.CascadeClassifier('haarcascade_frontalface_alt.xml')
        if(cc.empty()):
            print("!")
        gray = cv2.cvtColor(self.img,cv2.COLOR_RGB2GRAY)
        faces = cc.detectMultiScale(gray,1.3,5)
        for face in faces:
            eyes=self.detectEye(face)
            if len(eyes)>1:
                return face,eyes
        if len(faces)!=0:
            return faces[len(faces)-1],np.asarray([])           
        print('No face region found')
        return [0,0,0,0],[]

    def detectEye(self,face):
        img_face=self.img[face[1]:face[1]+face[3],face[0]:face[0]+face[2]]
        cc = cv2.CascadeClassifier('haarcascade_eye.xml')
        gray = cv2.cvtColor(img_face,cv2.COLOR_RGB2GRAY)
        eyes_candidate = cc.detectMultiScale(gray)
        eyes=[]
        for eye in eyes_candidate:
            if eye[1]<=face[3]/2:
                eye[0]+=face[0]
                eye[1]+=face[1]
                eyes.append(eye)
        return np.asarray(eyes)

    # Returns hair area from an image.
    # area_hair[i][j]=1 if img[i][j] is a part of hair area, 0 otherwise
    def getHairArea(self,face):
        scale=float(self.img.shape[1])/400
        (segmented,labels,n)=pms.segment(self.img,spatial_radius=int(scale*6),
                              range_radius=scale*5, min_density=300)
        #cv2.imshow('segmented',segmented)
        mv = cv2.split(self.img)
        hair = []
        x=face[0]+face[2]/2
        y=face[1]-self.img.shape[1]/8
        hair_new = [labels[y][x]]
        not_hair = range(n)
        not_hair.remove(labels[y][x])
        neighbor=self.getNeighbor(labels,n)
        factors=self.getFactor(segmented,labels,n)
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
        #print('making array of hair area...')
        for i in range(area_hair.shape[0]):
            for j in range(area_hair.shape[1]):
                if labels[i][j] in hair:
                    area_hair[i][j]=1
        return area_hair

    def getHairArea_side(self,face_front,img_front):
        xp=face_front[0]+face_front[2]/2
        yp=face_front[1]-img_front.shape[0]/8
        c=img_front[yp][xp]
        x=self.img.shape[1]/2
        h=self.img.shape[0]/8
        while not compare(self.img[h][img_front.shape[1]/2],c):
            h=h+5
        face_side=[x,h+20+self.img.shape[1]/8,0,0]
        #print(x,h)
        return self.getHairArea(face_side)

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
    def getHairParams(self,face,eyes,img_front,hair_front,img_side,hair_side):
        face=None
        dic={}
        d_eye=abs((eyes[1][0]-eyes[0][0])+(eyes[1][2]-eyes[1][2])/2)
        dic['d_eye']=d_eye
        #hair width for front hair
        xleft=-1
        for i in range(hair_front.shape[1]):
            for h in range(hair_front.shape[0]):
                if hair_front[h][i]==1:
                    xleft=i
                    break
            if xleft>=0:
                break
        xright=-1
        for i in range(hair_front.shape[1]-1,0,-1):
            for h in range(hair_front.shape[0]):
                if hair_front[h][i]==1:
                    xright=i
                    break
            if xright>=0:
                break
        width_front=xright-xleft
        dic['width_front']=width_front
        
        #first hair point for front hair
        xff=0
        yff=-1
        for h in range(hair_front.shape[0]):
            for i in range(hair_front.shape[1]):
                if hair_front[h][i]==1:
                    xff=i
                    yff=h
                    break
            if yff!=-1:
                break
        #forehair
        h_forehair_front=yff
        #for loop continues until passing hair region twice
        for h in range(yff,hair_front.shape[0]):
            change_point=0
            flag=0
            for i in range(hair_front.shape[1]-1):
                if hair_front[h][i]==0 and hair_front[h][i+1]==1:
                    if flag==1 and i-change_point>=hair_front.shape[1]/4:
                        #print(i,change_point,h)
                        h_forehair_front=h
                    else:
                        flag=0
                elif hair_front[h][i]==1 and hair_front[h][i+1]==0:
                    if flag!=1:
                        change_point=i
                    flag=1
            if h_forehair_front!=yff:
                break

        #sidehair
        h_sidehair_front=h_forehair_front
        for h in range(h_forehair_front,hair_front.shape[0]):
            flag=0
            for i in range(hair_front.shape[1]):
                if hair_front[h][i]==1:
                    flag=1
                    break
            if flag==0:
                h_sidehair_front=h
                break
        #print(yff,h_forehair_front,h_sidehair_front)
        dic['l_forehair']=(float) (h_forehair_front-yff)/2 
        dic['l_sidehair']=(float) (h_sidehair_front-h_forehair_front)
        
        #first hair point for side hair
        if(hair_side != None):
            xfs=hair_side.shape[1]-1
            yfs=0
            for i in range(hair_side.shape[1]-1,0,-1):
                for h in range(hair_side.shape[0]):
                    if hair_side[h][i]==1:
                        xfs=i
                        yfs=h
                        break
                if yfs!=0:
                    break
        
            #forehair
            h_forehair=0
            for i in range(xfs,xfs-20,-5):
                for h in range(yfs,hair_side.shape[0]):
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
            while i<xfs:
                while hair_side[h][i]==0:
                    h+=1
                while hair_side[h][i]==1:
                    h-=1
                if h<h_head:
                    h_head=h
                i=i+5
            #print('h_head',h_head)
            h_forehead=(h_forehair+h_head)/2
            #dic['l_forehair']=h_forehair-h_forehead
            #dic['l_sidehair']=h_sidehair-h_forehair
            dic['l_rearhair']=h_rearhair-h_sidehair

        v=0
        for h in range(yff,h_sidehair_front):
            for i in range(hair_front.shape[1]):
                if hair_front[h][i]==1:
                    v=v+1

        dic['volume']=round(float(v)/d_eye/d_eye,2)
        v=0
        for h in range( h_forehair_front,h_sidehair_front):
            for i in range(hair_front.shape[1]):
                if hair_front[h][i]==1:
                    v=v+1
        dic['side_volume']=round(float(v)/d_eye/d_eye,2)
        
        
            
        h_eye=0
        for eye in eyes:
            h_eye+=eye[1]
            h_eye+=eye[3]/2
        h_eye/=len(eyes)
            #print(h_eye,h_forehair,h_forehead)
        e_forehead=float(h_eye-h_forehair_front)/(h_eye-(h_forehair_front+yff)/2)
        dic['l_forehair']=dic['l_forehair']/d_eye
        dic['l_sidehair']=dic['l_sidehair']/d_eye
        dic['e_forehead']=round(e_forehead,2)
        dic['e_ear']=1
        return dic
