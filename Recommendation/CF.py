import sqlite3
import math
import itertools
import operator
import random
from Make_DB import make_DB

#NUMBER OF CATEGORY
NUMBER_OF_CATEGORY = 6
#OUR DB
db = sqlite3.connect("DB")

#CHECK THE item IS ON THE TABLE
def is_item(item) :
	cursor = db.cursor()
	cursor.execute("SELECT item_name FROM item WHERE item_name = ?", (item,))
	data=cursor.fetchall()
	if len(data) == 0 :
		return False;
	else :
		return True;

#CHECK THE user IS ON THE TABLE
def is_user(user) :
	cursor = db.cursor()
	cursor.execute("SELECT user_name FROM user WHERE user_name = ?", (user,))
	data=cursor.fetchall()
	if len(data) == 0 :
		return False;
	else :
		return True;

#CHECK THE item IS RATED BY THE user
def is_rated(user, item) :
	cursor = db.cursor()
	cursor.execute("SELECT rate FROM rating WHERE user_name = ? And item_name = ?", (user, item))
	data=cursor.fetchall()
	if len(data) == 0 :
		return False;
	else :
		return True;

	return True

#ADD item ON THE TABLE
def add_item(item) :
	if(not is_item(item)) :
		cursor = db.cursor()
		cursor.execute("INSERT into item(item_name, type, used) values(?, ?, ?)", (item, categorize(item), 0))
     	db.commit()

#GET item's CATEGORY
#NEED_TO_UPDATE
def categorize(item) : 
	return int(item) % NUMBER_OF_CATEGORY

#INCREASE item's USED COUNT
def update_item_used(item) :
	if is_item(item) : 
			cursor = db.cursor()
			cursor.execute("SELECT used FROM item WHERE item_name = ?", (item,))
			data = cursor.fetchall()
			cursor.execute("UPDATE item SET used = ? WHERE item_name = ?", (data[0][0]+1,item))
			db.commit()

#ADD user ON THE TABLE WITH RATED ITEMS AND RATINGS
def add_user(user, list_of_item, list_of_rating) :
	if(not is_user(user)) :
		cursor = db.cursor()
		k = [0,0] * NUMBER_OF_CATEGORY

		for (item, rating) in zip(list_of_item, list_of_rating) :
			if(is_item(item)) :
				if(not is_rated(user, item)) :
					cursor.execute("INSERT into rating(user_name , item_name  , rate ) values (?,?,?)", (user, item, rating))
					update_item_used(item)
					k[categorize(item)*2 + 1]+= 1
					k[categorize(item)*2] += rating

		cursor.execute("INSERT into user(user_name) values (?)", (user,))
		db.commit()
		for i in range(NUMBER_OF_CATEGORY) :
			S = "UPDATE user SET rating%d = ?, num%d = ? WHERE user_name = ?" % (i,i)
			cursor.execute(S, (k[i*2],k[i*2+1],user))

		db.commit()

#ADD OR UPDATE user's item rating 
def update_rating(user, item, rating) :
	if(is_user(user) and is_item(item)) :
		cursor = db.cursor()
		if(not is_rated(user, item)) :
			cursor.execute("INSERT into rating(user_name , item_name  , rate ) values (?, ?,?)", (user, item, rating))
			cursor.execute("SELECT * FROM user WHERE user_name = ?", (user,))
			k = list(cursor.fetchall()[0])
			k[categorize(item)*2 + 2]+= 1
			k[categorize(item)*2 + 1] += rating
			S = "UPDATE user SET rating%d = ?, num%d = ? WHERE user_name = ?" % (categorize(item), categorize(item))
			cursor.execute(S, (k[categorize(item)*2 + 1],k[categorize(item)*2 + 2],user))
			update_item_used(item)
			db.commit()
		else :
			cursor.execute("SELECT rate FROM rating WHERE item_name = ? AND user_name = ?", (item, user))
			previous_rating = cursor.fetchall()[0][0]
			print previous_rating

			cursor.execute("UPDATE rating SET rate = ? WHERE item_name = ? AND user_name = ?", (rating, item, user))

			category = categorize(item)
			S = "SELECT rating%s FROM user WHERE user_name = ?" %(categorize(item))
			cursor.execute(S, (user,))
			current_rating=cursor.fetchall()[0][0]

			S = "UPDATE user SET rating%d = ? WHERE user_name = ?" % (categorize(item))
			cursor.execute(S, (current_rating - previous_rating + rating , user))
			db.commit()

#GET user's RATING AT category
def get_categoey_rating(user, category) :
	if(is_user(user) and category < NUMBER_OF_CATEGORY) :
		cursor = db.cursor()
		S = "SELECT num%s, rating%s FROM user WHERE user_name = ?" %(category, category)
		cursor.execute(S, (user,))
		(num, rating)=(cursor.fetchall()[0])
		if(num == 0) :
			return 0
		else :
			return rating / num

#GET user's RATING LIST
def get_rating(user) :
	if(is_user(user)) :
		l = []
		for i in range(NUMBER_OF_CATEGORY) :
			l.append(get_categoey_rating(user, i))
		return l

#GET user's ITEM LIST WITH HIGHEST RATING
def get_high_rated_item(user) :
	if(is_user(user)) :
		cursor = db.cursor()
		cursor.execute("SELECT item_name, rate FROM rating WHERE user_name = ?", (user,))
		d = cursor.fetchall()
		if(len(d) == 0) :
			return []
		m= max(d,key=lambda item:item[1])[1]
		return [tup[0] for tup in d if (tup[1] == m)]

#GET COSINE SIMILARITY BETWEEN user1 AND user2
def cosine_similarity(user1, user2) :
	if(is_user(user1) and is_user(user2)) :
		l1 = get_rating(user1)
		l2 = get_rating(user2)
		sum1 = sum(map(lambda x,y : x*y, l1, l2))
		sum2 = math.sqrt(sum(map(lambda x : x*x, l1)) * sum(map(lambda x : x*x, l2))) 
		if sum2 == 0: 
			return -2
		else :
			return sum1/sum2

#GET ITEM LIST
def get_item_list() :
	cursor = db.cursor()
	cursor.execute("SELECT item_name FROM item")
	return [tup[0] for tup in cursor.fetchall()]

#GET RECOMMANDED ITEM LIST WITH user
#NEED_TO_UPDATE
def recommend(user) :
	if(is_user(user)) :
		cursor = db.cursor()
		cursor.execute("SELECT user_name FROM user WHERE user_name != ?", (user,))
		#user_list
		d = cursor.fetchall() 

		#(user_name, similarity) sorted list
		sim = [(tup[0], cosine_similarity(tup[0], user)) for tup in d]
		sim.sort(key = lambda item:item[1], reverse = True)
		print "Similiarity : ", sim

		#grouping by similarity
		it = itertools.groupby(sim, operator.itemgetter(1))
		
		#check recommand value with each element
		for i in it :
			l = []
			#get item with highest rating
			for (u,v) in i[1]:
				l.extend(get_high_rated_item(u))
			#filter user used item 
			item_list = filter(lambda x : not is_rated(user, x), l)
			print "Highest Rated item :",item_list
			#if there are no item, do again
			if (len(item_list) == 0) :
				continue
			#pick item with highest rating
			return pick_highest_rating_item(item_list)

		#CANNOT RECOMMEND! -> RANDOM PICK
		return pick_less_item(filter(lambda x : not is_rated(user, x), get_item_list()))

#PICK SOME ITEM WITH SMALLEST used in item_list
def pick_less_item(item_list) :
	it = "("+",".join(item_list)+")"
	cursor = db.cursor()
	cursor.execute("SELECT item_name,used FROM item WHERE item_name IN %s" % (it))
	d = cursor.fetchall()
	d.sort(key = lambda item:item[1])
	f = filter(lambda x : x[1] == d[0][1], d)
	return random.choice(f)[0]

#RETURM item's AVERAGE RATING FOR ALL USER
def item_rating(item) :
	if (is_item(item)) :
		cursor = db.cursor()
		d = map(lambda x : x[0], cursor.execute("SELECT  rate FROM rating WHERE item_name == ?", (item,)).fetchall())
		if(len(d) == 0) :
			return 0;
		return sum(d) / len(d)

#RETURN HIGHEST RATING ITEM IN item_list
def pick_highest_rating_item(item_list) :
	l = map(lambda x : (x, item_rating(x)), item_list)
	l.sort(key = lambda item:item[1], reverse = True)
	f = filter(lambda x : x[1] == l[0][1], l)
	return random.choice(f)[0]

#PRINT CURRENT DB
def show_DB() :
	print("----------DB----------")
	cursor = db.cursor()
	print("| --- item ---")
	item_select = cursor.execute("SELECT * from item")
       	for row in item_select :
		print "|   ",row
	print("| --- user ---")
	user_select = cursor.execute("SELECT * from user")
       	for row in user_select :
		print "|   ",row
	print("| ---rating---")
	rating_select = cursor.execute("SELECT * from rating")
       	for row in rating_select :
		print "|   ",row
	print("--------END DB--------")


"""
very simple test code
"""
make_DB()
R = [0,0.5,1,1.5,2,2.5,3,3.5,4,4.5,5]
L = map(lambda x : str(x), range(50))

def select_random(lis, num) :
	l = []
	for i in range(num) : l.append(random.choice(lis))
	return l
	
for i in L :
	add_item(i)

add_user('A',select_random(L,20),select_random(R,20))
add_user('B',select_random(L,20),select_random(R,20))
add_user('C',select_random(L,20),select_random(R,20))
add_user('D',select_random(L,20),select_random(R,20))
add_user('E',select_random(L,20),select_random(R,20))
add_user('F',select_random(L,20),select_random(R,20))
add_user('G',select_random(L,20),select_random(R,20))
add_user('H',select_random(L,20),select_random(R,20))
add_user('I',select_random(L,20),select_random(R,20))
add_user('J',select_random(L,20),select_random(R,20))

pick_highest_rating_item(L)

print(recommend('C'))

show_DB()