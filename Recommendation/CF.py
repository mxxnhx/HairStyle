import  sqlite3
import math

db = sqlite3.connect("DB")

def is_item(item) :
	cursor = db.cursor()
	cursor.execute("SELECT item_name FROM item WHERE item_name = ?", (item,))
	data=cursor.fetchall()
	if len(data) == 0 :
		return False;
	else :
		return True;

def is_user(user) :
	cursor = db.cursor()
	cursor.execute("SELECT user_name FROM user WHERE user_name = ?", (user,))
	data=cursor.fetchall()
	if len(data) == 0 :
		return False;
	else :
		return True;

def is_rated(user, item) :
	cursor = db.cursor()
	cursor.execute("SELECT rate FROM rating WHERE user_name = ? And item_name = ?", (user, item))
	data=cursor.fetchall()
	if len(data) == 0 :
		return False;
	else :
		return True;

	return True

def add_item(item) :
	if(not is_item(item)) :
		cursor = db.cursor()
		cursor.execute("INSERT into item(item_name, type) values(?, ?)", (item, categorize(item)))
     		db.commit()

def categorize(item) : 
	return len(item) % 3

def add_user(user, list_of_item, list_of_rating) :
	if(not is_user(user)) :
		cursor = db.cursor()
		k = [user, 0,0,0,0,0,0]

		for (item, rating) in zip(list_of_item, list_of_rating) :
			if(is_item(item)) :
				if(not is_rated(user, item)) :
					cursor.execute("INSERT into rating(user_name , item_name  , rate ) values (?, ?,?)", (user, item, rating))
					k[categorize(item)*2 + 2]+= 1
					k[categorize(item)*2 + 1] += rating

		cursor.execute("INSERT into user(user_name, rating0, num0, rating1 , num1, rating2, num2 ) values (?, ?,?,?,?,?,?)", tuple(k))
		db.commit()

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
			db.commit()

def get_categoey_rating(user, category) :
	if(is_user(user) and category < 3) :
		cursor = db.cursor()
		S = "SELECT num%s, rating%s FROM user WHERE user_name = ?" %(category, category)
		cursor.execute(S, (user,))
		(num, rating)=(cursor.fetchall()[0])
		if(num == 0) :
			return 0
		else :
			return rating/num

def get_rating(user) :
	if(is_user(user)) :
		l = []
		for i in range(3) :
			l.append(get_categoey_rating(user, i))
		return l

def get_high_rated_item(user) :
	if(is_user(user)) :
		cursor = db.cursor()
		cursor.execute("SELECT item_name, rate FROM rating WHERE user_name = ?", (user,))
		d = cursor.fetchall()
		m= max(d,key=lambda item:item[1])[1]
		return [tup[0] for tup in d if (tup[1] == m)]


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

add_user('A',['1','777777','666666'],[1,2,3])
add_user('B',['22','333','9999'],[4,2,2.5])
add_user('C',['1','333','00'],[1,3.5,2.5])


recommend('C')
show_DB()