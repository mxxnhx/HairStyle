# -*- coding:utf-8 -*-
import os
from werkzeug import secure_filename
import sys
reload(sys)
sys.setdefaultencoding('utf8')

from flask import Flask,  request, session, send_from_directory
from sqlalchemy import create_engine, Column, Integer, String, Text, DateTime, ForeignKey, desc
from sqlalchemy.orm import scoped_session, sessionmaker, relationship
from sqlalchemy.ext.declarative import declarative_base

import cv2
import numpy
import random

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
 
   # posts = relationship('Post', backref='user', lazy='dynamic')

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

def init_db():
    Base.metadata.create_all(bind=engine)
init_db();

UPLOAD_FOLDER = '/home/moonhc/workspace/HairStyle/Server/imgs'
REC_FOLDER = '/home/moonhc/workspace/HairStyle/Server/imgs_rec'

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
app.config['REC_FOLDER'] = REC_FOLDER

@app.route('/test')
def test():
    return 'hello world.'

@app.route('/signup', methods=['POST'])
def signup():
    if request.method == 'POST':
        name = request.form['name']
        tel = request.form['tel']
        user = User.query.filter_by(tel = tel).first()
        if(user):
            return "-2" # already exists
        result = engine.execute("select idcode from users order by idcode desc")
        idcode = result.fetchone()
        if(not idcode['idcode']):
            _id = 0
        else:
            _id = idcode['idcode'] + 1
        result.close()
        user = User(name,_id,tel)
        db_session.add(user)
        db_session.commit()
        return str(_id)
    else:
        return "-1"

@app.route('/login', methods=['POST'])
def login():
    if request.method == 'POST':
        _id = request.form['idcode']
        result = engine.execute("select idcode from users where idcode = %s"% _id)
        user = result.fetchone()
        if(user == None):
            return "-2"
        else:
            return "1"
    else:
        return "-1"


@app.route('/rating', methods=['POST'])
def rating():
    if request.method == 'POST':
        rate = request.form['rate']
        return str(rate)
    else:
        return "-1"

@app.route('/upload_file', methods=['POST'])
def upload_file():
    if request.method == 'POST':
        file = request.files['file']
        if file:
            file.save(os.path.join(app.config['UPLOAD_FOLDER'], file.filename))
            print file.filename
            return file.filename
        else:
            return '-2'
    else:
        return '-1'

@app.route('/upload', methods=['POST'])
def upload():
    # Have to add code here for hair process
    if request.method == 'POST':
        idcode = request.form['idcode']
        pn1 = request.form['pathname1']
        file1 = request.form['file1']
        pn2 = request.form['pathname2']
        file2 = request.form['file2']
        pn3 = request.form['pathname3']
        file3 = request.form['file3']

        if file1 and file2 and file3:
            file1.save(os.path.join(app.config['UPLOAD_FOLDER'], pn1))
            file2.save(os.path.join(app.config['UPLOAD_FOLDER'], pn2))
            file3.save(os.path.join(app.config['UPLOAD_FOLDER'], pn3))
        else:
            return "-2"

        result = engine.execute("select albumnum from imgs where idcode = %s", idcode)
        albumnum = result.fetchone()
        if(albumnum == None):
            albumnum = 1
        else:
            albumnum = albumnum['albumnum'] + 1
        imgs = img(idcode, pn1, pn2, pn3, albumnum)
        db_session.add(imgs)
        db_session.commit()
        return str(idcode)
    else:
        return '-1'

@app.route('/sendhome/<filename>', methods=['POST'])
def send_home(filename):
    if os.path.isfile(app.config['UPLOAD_FOLDER']+filename):
        return send_from_directory(app.config['UPLOAD_FOLDER'], filename)
    else:
        return "-2"

@app.route('/sendrec/<filename>', methods=['POST'])
def send_rec(filename):
    if os.path.isfile(app.config['REC_FOLDER']+filename):
        return send_from_directory(app.config['REC_FOLDER'], filename)
    else:
        return "-2"

if __name__ == "__main__":
    app.run(debug=True, host='0.0.0.0')

