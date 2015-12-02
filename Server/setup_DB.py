import Make_DB
import CF
import Main

Make_DB.make_DB()
L = [1,4,5,6,14,17,19,22,23,24,28,35,36,40,43,46,48,50,51,56,57,58,60,61,62,63,64,68,69,70,74,75,77,78,80,83,84,85]

for i in L:
    d = Main.check_hair_front("imgs_rec/" + "t" + str(i) +".jpg")
    print d
    CF.add_item("t"+ str(i) + ".jpg", d)

CF.show_DB()
