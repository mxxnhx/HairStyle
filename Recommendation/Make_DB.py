import  sqlite3


try:
    db = sqlite3.connect("DB")
    cursor = db.cursor()

    cursor.execute("create table item (item_name text, type integer)")

    cursor.execute("create table user (user_name text, rating0 real, num0 integer, rating1 real, num1 integer, rating2 real, num2 integer)")

    cursor.execute("create table rating (user_name text, item_name text , rate real)")

    
    db.commit()


except sqlite3.Error, e:
    print("ERR")
    if db:
        db.rollback
    else :
        print("ASDFASDF")

finally:
    if db:
        db.close()