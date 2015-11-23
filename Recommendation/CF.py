import sqlite3
import math

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
	return len(item) % NUMBER_OF_CATEGORY

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
		k = [0,0,0,0,0,0,0,0,0,0,0,0]

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

#ADD user's NEW item rating 
def update_rating(user, item, rating) :
	if(is_user(user) and is_item(item)) :
		if(not is_rated(user, item)) :
			cursor = db.cursor()
			cursor.execute("INSERT into rating(user_name , item_name  , rate ) values (?, ?,?)", (user, item, rating))
			cursor.execute("SELECT * FROM user WHERE user_name = ?", (user,))
			k = list(cursor.fetchall()[0])
			k[categorize(item)*2 + 2]+= 1
			k[categorize(item)*2 + 1] += rating
			S = "UPDATE user SET rating%d = ?, num%d = ? WHERE user_name = ?" % (categorize(item), categorize(item))
			cursor.execute(S, (k[categorize(item)*2 + 1],k[categorize(item)*2 + 2],user))
			update_item_used(item)
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
			return rating/num

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

#GET RECOMMANDED ITEM LIST WITH user
#NEED_TO_UPDATE
def recommend(user) :
	if(is_user(user)) :
		cursor = db.cursor()
		cursor.execute("SELECT user_name FROM user WHERE user_name != ?", (user,))
		d = cursor.fetchall()
		sim = [(tup[0], cosine_similarity(tup[0], user)) for tup in d]
		m= max(sim,key=lambda item:item[1])[1]
		max_user = [tup[0] for tup in sim if (tup[1] == m)]
		l = []
		for u in max_user:
			l.extend(get_high_rated_item(u))
		item_list = filter(lambda x : not is_rated(user, x), l)
		print item_list

#PRINT CURRENT DB
def show_DB() :
	cursor = db.cursor()
	print("---item---")
	item_select = cursor.execute("SELECT * from item")
       	for row in item_select :
		print row
	print("---user---")
	user_select = cursor.execute("SELECT * from user")
       	for row in user_select :
		print row
	print("---rating---")
	rating_select = cursor.execute("SELECT * from rating")
       	for row in rating_select :
		print row

"""
very simple test code
"""
add_item('1')
add_item('22')
add_item('333')
add_item('4444')
add_item('555')
add_item('66666')
add_item('777777')
add_item('8')
add_item('9999')
add_item('00')
add_user('A',['1','777777','66666'],[1,2,3])
add_user('B',['22','333','9999'],[4,2,2.5])
add_user('C',['1','333','00'],[1,3.5,2.5])
update_rating('A','333',3.5)
update_rating('C','22',3.5)


recommend('C')
show_DB()