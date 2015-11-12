-참고자료:http://vgg.fiit.stuba.sk/2013-04/hair-segmentation/
-설치해야 할 것
1)Python 2.7
2)opencv
http://opencvpython.blogspot.kr/2012/05/install-opencv-in-windows-for-python.html (Windows)
https://jjyap.wordpress.com/2014/05/24/installing-opencv-2-4-9-on-mac-osx-with-python-support/ (OS X)
3)pymeanshift : 영역 나누는 알고리즘
https://github.com/fjean/pymeanshift/wiki/Install
-Todo
new compare function (현재는 흑백 사진에서 gray 값 차이로 결정. 참고자료 보면 색 히스토그램 뽑아서 비교하라는 것 같은데…)
pms.segment parameter 값 조절?
-그 외
사진이 너무 고화질이면(=사이즈가 너무 크면) 처리가 오래 걸림. 서버에서 돌릴땐 downscaling이 필요할 듯?
나중에 분석 쪽이랑 합쳐야 되니 branch 새로 파서 작업할 것. git checkout -b (branch_name)