import pymeanshift as pms
import cv2
import numpy as np

img=cv2.imread('me.jpg')
for i in range(4):
        (segmented,labels,n)=pms.segment(img,spatial_radius=6,
                              range_radius=1.5*(i+1), min_density=200)
        cv2.imshow('spatial_radius=%s' % str(50+50*i),segmented)
