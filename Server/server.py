#-*- coding:utf-8 -*-
import os
from werkzeug import secure_filename
import sys
reload(sys)
sys.setdefaultencoding('utf8')

from flask import Flask,  request, session, send_from_directory, make_response
from sqlalchemy import create_engine, Column, Integer, String, Text, DateTime, ForeignKey, desc
from sqlalchemy.orm import scoped_session, sessionmaker, relationship
from sqlalchemy.ext.declarative import declarative_base

import cv2
import HairAnalyzer
import numpy
import random
import CF

engine = create_engine('mysql://root:qlqjs1@127.0.0.1/hairstyle?charset=utf8',
        convert_unicode=True)
db_session = scoped_session(
        sessionmaker(autocommit=False, autoflush=False, bind=engine))

Base = declarative_base()
Base.query = db_session.query_property()

class User(Base):
    __tablename__ = 'users'
    id = Column(Integer, primary_key=True)
    name = Column(String(50), unique=False)
    idcode = Column(Integer, unique=True, nullable=False)
    tel = Column(String(100), unique=True)

    def __init__(self, name=None, idcode=None, tel=None):
        self.name = name
        self.idcode = idcode
        self.tel = tel

class img(Base):
    __tablename__ = 'imgs'
    id = Column(Integer, primary_key=True)
    idcode = Column(Integer, unique=False, nullable=False)
    pathname1 = Column(String(100), unique=True)
    pathname2 = Column(String(100), unique=True)
    pathname3 = Column(String(100), unique=True)
    albumnum = Column(Integer, unique=False)

    def __init__(self, idcode=None, pathname1=None, pathname2=None, pathname3=None, albumnum=None):
        self.idcode = idcode
        self.pathname1 = pathname1
        self.pathname2 = pathname2
        self.pathname3 = pathname3
        self.albumnum = albumnum

class img_rec(Base):
    __tablename__ = 'imgs_rec'
    id = Column(Integer, primary_key=True)
    idcode = Column(Integer, unique=False, nullable=False)

    pathname = Column(String(100), unique=True)
    
    def __init__(self, idcode=None, pathname=None):
        self.idcode = idcode
        self.pathname = pathname

class img_face(Base):
    __tablename__ = 'face'
    id = Column(Integer, primary_key=True)
    idcode = Column(Integer, unique=True, nullable=False)
    pathname = Column(String(100), unique=True)

    def __init__(self, idcode=None, pathname=None):
        self.idcode = idcode
        self.pathname = pathname

def init_db():
    Base.metadata.create_all(bind=engine)
init_db();

UPLOAD_FOLDER = '/home/moonhc/workspace/HairStyle/Server/imgs'
REC_FOLDER = '/home/moonhc/workspace/HairStyle/Server/imgs_rec'
FACE_FOLDER = '/home/moonhc/workspace/HairStyle/Server/imgs_face'

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
app.config['REC_FOLDER'] = REC_FOLDER
app.config['FACE_FOLDER'] = FACE_FOLDER

@app.route('/test')
def test():
    return 'hello world.'

@app.route('/signup', methods=['POST'])
def signup():
    if request.method == 'POST':
        name = request.form['name']
        tel = request.form['tel']
        face = request.files['face']
        user = User.query.filter_by(tel = tel).first()
        if(user):
            return "-2" # already exists
        result = engine.execute("select idcode from users order by idcode desc")
        idcode = result.fetchone()
        if(idcode == None):
            _id = 0
        else:
            _id = idcode['idcode'] + 1
        result.close()
        
        pn = os.path.join(app.config['FACE_FOLDER'], str(_id) + ".jpg")
        face.save(pn)
        ha = HairAnalyzer.HairAnalyzer(pn)
        img = ha.getImage()
        print(img.shape)
        face, eyes = ha.detectFace()
        if (len(eyes)<2):
            os.remove(pn)
            return "-3"

        area = ha.getHairArea(face)
        for i in range(img.shape[0]):
            for j in range(img.shape[1]):
                if area[i][j] == 1:
                    img[i][j]=numpy.array([255,255,255])
        cv2.imwrite(os.path.join(app.config['FACE_FOLDER'], str(_id) + '_face.jpg'), img)

        facedata = img_face(_id, str(_id) +"_face.jpg")
        user = User(name,_id,tel)
        db_session.add(user)
        db_session.add(facedata)
        db_session.commit() 
        CF.add_user(str(_id), [], [])
        CF.show_DB()
        return str(_id)
    else:
        return "-1"

@app.route('/login', methods=['POST'])
def login():
    if request.method == 'POST':
        _id = request.form['idcode']
        result = engine.execute("select idcode from users where idcode = %s", _id)
        user = result.fetchone()
        result.close()
        if(user == None):
            return "-2"
        else:
            result = engine.execute("select * from imgs where idcode = %s order by albumnum desc", _id)
            album = result.fetchone()
            if (album==None):
                return "0"
            else:
                return str(album['albumnum'])
    else:
        return "-1"

@app.route('/rating_signup', methods=['POST'])
def rating_signup():
    if request.method == 'POST':
        idcode = request.form['idcode']
        rate = request.form['rate']
        filename = request.form['filename']
        CF.update_rating(idcode, filename, float(rate))
        CF.show_DB()
        return "1"
    else:
        return "-1"

@app.route('/rating_rec', methods=['POST'])
def rating_rec():
    if request.method == 'POST':
        idcode = request.form['idcode']
        rate = request.form['rate']
        filename = request.form['filename']
        CF.update_rating(idcode, filename, float(rate))
        CF.show_DB()
        return "1"
    else:
        return "-1"

@app.route('/upload', methods=['POST'])
def upload():
    # Have to add code here for hair process
    if request.method == 'POST':
        idcode = request.form['idcode']
        pn1 = request.form['pathname1']
        file1 = request.files['file1']
        pn2 = request.form['pathname2']
        file2 = request.files['file2']
        pn3 = request.form['pathname3']
        file3 = request.files['file3']
        
        result = engine.execute("select idcode from users where idcode = %s", idcode)
        _id = result.fetchone()
        result.close()
        if (_id == None):
            return "-2"

        if file1 and file2 and file3:
            file1.save(os.path.join(app.config['UPLOAD_FOLDER'], pn1))
            file2.save(os.path.join(app.config['UPLOAD_FOLDER'], pn2))
            file3.save(os.path.join(app.config['UPLOAD_FOLDER'], pn3))
        else:
            return "-2"

        result = engine.execute("select albumnum from imgs where idcode = %s", idcode)
        albumnum = result.fetchone()
        result.close()
        if(albumnum == None):
            albumnum = 1
        else:
            albumnum = albumnum['albumnum'] + 1
        imgs = img(idcode, pn1, pn2, pn3, albumnum)
        db_session.add(imgs)
        db_session.commit()
        
        ha1 = HairAnalyzer.HairAnalyzer(os.path.join(app.config['UPLOAD_FOLDER'], pn1))
        ha2 = HairAnalyzer.HairAnalyzer(os.path.join(app.config['UPLOAD_FOLDER'], pn2))
        img1 = ha1.getImage()
        img2 = ha2.getImage()

        face, eyes = ha1.detectFace()
        area1 = ha1.getHairArea(face)
        area2 = ha2.getHairArea_side(face, img1)
        
        for i in range(img1.shape[0]):
            for j in range(img1.shape[1]):
                if area1[i][j] == 0:
                    img1[i][j]=numpy.array([255,255,255])
        
        cv2.imwrite(os.path.join(app.config['REC_FOLDER'], pn1)) 
        # To add recomend code
        return str(idcode)
    else:
        return '-1'

@app.route('/sendhome/<filename>', methods=['GET', 'POST'])
def send_home(filename):
    if os.path.isfile(os.path.join(app.config['UPLOAD_FOLDER'], filename)):
        return send_from_directory(app.config['UPLOAD_FOLDER'], filename)
    else:
        return "-2"

@app.route('/sendtest/<catenum>', methods=['GET', 'POST'])
def send_test(catenum):
    filename = CF.select_random_item_with_category(int(catenum))
    if os.path.isfile(os.path.join(app.config['REC_FOLDER'], filename)):
        response = make_response(send_from_directory(app.config['REC_FOLDER'], filename))
        response.headers['Content-Disposition'] = "attachment; filename=" + filename
        return response
    else:
        return "-2"

@app.route('/sendrec/<idcode>', methods=['GET', 'POST'])
def send_rec(idcode):
    filename = CF.recommend(idcode)
    filename = filename + "_a.png"
    if os.path.isfile(os.path.join(app.config['REC_FOLDER'], filename)):
        response = make_response(send_from_directory(app.config['REC_FOLDER'], filename))
        response.headers['Content-Disposition'] = "attachment; filename=" + filename
        return response
    else:
        return "-2"

@app.route('/sendface/<idcode>', methods=['GET', 'POST'])
def send_face(idcode):
    result = engine.execute("select pathname from face where idcode = %s", idcode)
    user = result.fetchone()
    result.close()
    if user == None:
        return "-1"
    pathname = user['pathname']
    if os.path.isfile(os.path.join(app.config['FACE_FOLDER'], pathname)):
        return send_from_directory(app.config['FACE_FOLDER'], pathname)
    else:
        return "-2"



if __name__ == "__main__":
    app.run(debug=True, host='0.0.0.0')

