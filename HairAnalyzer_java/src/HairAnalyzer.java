import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import java.util.ArrayList;
 
public class HairAnalyzer{
	
	private CascadeClassifier face_cascade;
	private Mat img;
	private Mat img_reduced;
	
	public HairAnalyzer(){
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		face_cascade = new CascadeClassifier("res/haarcascade_frontalface_default.xml");
		
	}
	public HairAnalyzer(String path){
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		face_cascade = new CascadeClassifier("res/haarcascade_frontalface_default.xml");
		loadImage(path);
	}
	
	/** Load image from path. **/
	public void loadImage(String path){
		img = Imgcodecs.imread(path);
	}
	
	/** Detect face from the image. 
	 * Possible areas are returned with the form of (x,y,w,h). **/
	public MatOfRect detectFace(){
		Mat gray = new Mat();
		Imgproc.cvtColor(img, gray, Imgproc.COLOR_RGB2GRAY);
		MatOfRect faces = new MatOfRect();
		face_cascade.detectMultiScale(gray, faces);
		System.out.println( "mat = " + faces.dump());
		return faces;
	}
	/** Apply mean-shift segmentation to the image.
	 * Each element has distinct number for the region. **/
	public Mat meanshift(){
		//color reducing for successful meanshift
		img_reduced = colorReduce();
		Mat gray = new Mat();
		Imgproc.cvtColor(img_reduced, gray, Imgproc.COLOR_RGB2GRAY);
		Mat thresh = new Mat();
		Imgproc.threshold(gray, thresh, 0, 255, Imgproc.THRESH_BINARY_INV+Imgproc.THRESH_OTSU);
		
		Mat kernel = Mat.ones(3,3,CvType.CV_8U);
		Mat opening = new Mat();
		Imgproc.morphologyEx(thresh, opening, Imgproc.MORPH_OPEN, kernel, new Point(-1,-1), 2);
		
		Mat sure_bg = new Mat();
		Imgproc.dilate(opening, sure_bg, kernel,new Point(-1,-1),3);
		
		Mat dist_transform = new Mat();
		Imgproc.distanceTransform(opening, dist_transform, Imgproc.DIST_L2, 5);
		Mat sure_fg = new Mat();
		Imgproc.threshold(dist_transform, sure_fg, 0.7*Core.minMaxLoc(dist_transform).maxVal,255,0);
		
		
		
		Mat unknown = new Mat();
		Mat sure_fg2 = new Mat();
		sure_fg.convertTo(sure_fg2, CvType.CV_8U);
		System.out.println(sure_bg.type());
		System.out.println(sure_fg2.type());
//		for(int i=0;i<sure_fg.rows();i++){
//			for(int j=0;j<sure_fg.cols();j++){
//				sure_fg.put(i,j,(int)sure_fg.get(i, j)[0]);
//			}
//		}
		Core.subtract(sure_bg, sure_fg2, unknown);
		
		//Mat marker = new Mat(img.rows(),img.cols(),CvType.CV_32SC1);
		
		Mat marker = new Mat();
		Imgproc.connectedComponents(sure_fg2, marker);
		for(int i=0;i<img.rows();i++){
			for(int j=0;j<img.cols();j++){
				if(unknown.get(i, j)[0] == 255)
					marker.put(i,j,0);
				else marker.put(i, j, marker.get(i, j)[0]+1);
			}
		}
		
		Imgproc.watershed(img_reduced, marker);
		return marker;
	}
	
	/** Reduce the color of loaded image. **/
	public Mat colorReduce()
	{    
		int div = 32;
		Mat img_reduced = img.clone();
	    int nl = img_reduced.rows(); // number of lines
	    int nc = img_reduced.cols(); // number of elements per line

	    for (int j = 0; j < nl; j++)
	    {
	        for (int i = 0; i < nc; i++)
	        {
	            // process each pixel
	        	double[] data = img_reduced.get(j, i);
	        	for(int n=0;n<data.length;n++)
	        		data[n] = (int)data[n] / div * div + div / 2;
	        	img_reduced.put(j, i, data);       
	        }
	    }
	    return img_reduced;
	}
	
	public int[][] getneighbor(Mat segmented){
		int n = (int)Core.minMaxLoc(segmented).maxVal;
		int[][] neighbor = new int[n][n];
		int n1,n2;
		for(int i=0;i<segmented.rows();i++){
			for(int j=0;j<segmented.cols();j++){
				int mark = (int)segmented.get(i, j)[0];
				if(mark == -1){
					if(i>0 && i<segmented.rows()-1){
						n1 = (int)segmented.get(i+1, j)[0];
						n2 = (int)segmented.get(i-1, j)[0];
						if(n1 != -1 && n2 != -1){
							neighbor[n1-1][n2-1] = 1;
							neighbor[n2-1][n1-1] = 1;
						}
					}
					if(j>0 && j<segmented.cols()-1){
						n1 = (int)segmented.get(i, j+1)[0];
						n2 = (int)segmented.get(i, j-1)[0];
						if(n1 != -1 && n2 != -1){
							neighbor[n1-1][n2-1] = 1;
							neighbor[n2-1][n1-1] = 1;
						}
					}
				}
			}
		}
		return neighbor;
	}
	
	
	/** Find the hair area, based on the first region selection. **/
	public Mat getHairArea(int x, int y){
		Mat segmented = meanshift();
		int[][] neighbor = getneighbor(segmented);
		Mat gray = new Mat();
		Imgproc.cvtColor(img_reduced, gray, Imgproc.COLOR_RGB2GRAY);
		
		double[] vals = new double[neighbor.length];
		int[] count = new int[neighbor.length];
		int n;
		for(int i=0;i<segmented.rows();i++){
			for(int j=0;j<segmented.cols();j++){
				n = (int)segmented.get(i, j)[0];
				if(n != -1){
					vals[n-1]+= img_reduced.get(i, j)[0];
					count[n-1]++;
				}
			}
		}
		for(int i=0;i<vals.length;i++)
			vals[i] /= count[i];
		
		ArrayList<Integer> areas = new ArrayList<Integer>();
		int firstarea = (int)segmented.get(y, x)[0]-1;
		areas.add(firstarea);
		boolean renewal = true;
		while(renewal){
			renewal = false;
			for(int i=0;i<areas.size();i++){
				int area = areas.get(i);
				for(int j=0;j<vals.length;j++){
					if(neighbor[area][j] == 1 && !areas.contains(j) && Math.abs(vals[area]-vals[j]) < 10){
						renewal = true;
						areas.add(j);
					}
				}
			}
		}
		System.out.print("selected regions : ");
		for(int i=0;i<areas.size();i++)
			System.out.print(areas.get(i)+" ");
		System.out.println();
		Mat hair = Mat.zeros(img.rows(), img.cols(),CvType.CV_32S);
		hair.put(0, 0, 1);
		for(int i=0;i<segmented.rows();i++){
			for(int j=0;j<segmented.cols();j++){
				if(areas.contains((int)segmented.get(i, j)[0]-1))
					hair.put(i, j, 1);
			}
		}
		
		return hair;
	}
	
	public Mat getImage(){
		return img;
	}
	/** 첫 hair region을 잡아주면 그 값을 바탕으로 머리 영역 추출 **/
	public void func(int x, int y){
		
	}
}