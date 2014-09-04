package files;

import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JTextArea;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;


@SuppressWarnings("serial")
public class Archivos extends JFrame{
	private String _tempStr = "";
	private String _rtaEnvio ;
	private JTextArea _txtEstadoEnvio;
	
	public static int MAX_WIDTH=460;
	//Alto máximo
	public static int MAX_HEIGHT=460;
	
	private int SizeBuffer = 10;
	private String URL; 			
	private String NAMESPACE; 	
	SoapPrimitive response = null;
	private static final String METHOD_NAME	= "UpLoadFotosActas";	
	private static final String SOAP_ACTION	= "UpLoadFotosActas";
	
	private byte[] fotoParcial ;
	
	
	public Archivos(){
		_txtEstadoEnvio	= new JTextArea();
		_txtEstadoEnvio.setEnabled(true);
		Container paneMensajes = getContentPane();
		paneMensajes.setLayout(new GridLayout(1,1));
		paneMensajes.add(_txtEstadoEnvio);		
		setTitle("Log de Envio al Servidor");
        setSize(400,300);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	
	public void ListarArchivos(String _ruta, String _usuario, String _contrasena){
		_txtEstadoEnvio.setText(GetDateTimeHora()+ "   Iniciando escaneo carpeta "+_ruta+" \n");        
		File f = new File(_ruta);
		if (f.exists()){ 
			File[] actas = f.listFiles();
			for (int i=0;i<actas.length;i++){
				if(actas[i].isDirectory()){
					File[] fotos = actas[i].listFiles();
					_tempStr = _txtEstadoEnvio.getText();
					_txtEstadoEnvio.setText(_tempStr+ GetDateTimeHora()+ "   Escaneando carpeta de acta "+actas[i]+" \n");
					for(int j=0;j<fotos.length;j++){						
						copyImage(_ruta +"/"+actas[i].getName()+"/"+fotos[j].getName(),_ruta +"/"+actas[i].getName()+"/small_"+fotos[j].getName());
						_tempStr = _txtEstadoEnvio.getText();
						_txtEstadoEnvio.setText(_tempStr+GetDateTimeHora()+ "   Redimensionando foto "+fotos[j].getName()+" \n");
						
						this.fotoParcial = this.FileToArrayBytes(_ruta +"/"+actas[i].getName()+"/small_"+fotos[j].getName());
						_rtaEnvio = this.WebService(_usuario, _contrasena, actas[i].getName().toString(),this.fotoParcial);
						_tempStr = _txtEstadoEnvio.getText();
						_txtEstadoEnvio.setText(_tempStr+GetDateTimeHora()+ "   Enviando al servidor "+fotos[j].getName()+" \n");						
						
						if(_rtaEnvio==null){
							_tempStr = _txtEstadoEnvio.getText();
							_txtEstadoEnvio.setText(_tempStr+GetDateTimeHora()+ "   No hay conexion con el servidor. \n");
						}else if(_rtaEnvio.isEmpty()){
							_tempStr = _txtEstadoEnvio.getText();
							_txtEstadoEnvio.setText(_tempStr+GetDateTimeHora()+ "   No hay respuesta del servidor. \n");
						}else if(_rtaEnvio.equals("-1")){
							_tempStr = _txtEstadoEnvio.getText();
							_txtEstadoEnvio.setText(_tempStr+GetDateTimeHora()+ "   Usuario y/o contraseña erroneo. \n");
						}else if(_rtaEnvio.equals("-2")){
							_tempStr = _txtEstadoEnvio.getText();
							_txtEstadoEnvio.setText(_tempStr+GetDateTimeHora()+ "   Violacion de llave unica. \n");
						}else if(_rtaEnvio.equals("-3")){
							_tempStr = _txtEstadoEnvio.getText();
							_txtEstadoEnvio.setText(_tempStr+GetDateTimeHora()+ "   Violacion de llave foranea. \n"); 
						}else if(_rtaEnvio.equals("-4")){
							_tempStr = _txtEstadoEnvio.getText();
							_txtEstadoEnvio.setText(_tempStr+GetDateTimeHora()+ "   Violacion de campo no nulo. \n"); 
						}else if(_rtaEnvio.equals("-5")){
							_tempStr = _txtEstadoEnvio.getText();
							_txtEstadoEnvio.setText(_tempStr+GetDateTimeHora()+ "   Error.\n"); 
						}else if(_rtaEnvio.equals("1")){
							_tempStr = _txtEstadoEnvio.getText();
							_txtEstadoEnvio.setText(_tempStr+GetDateTimeHora()+ "   Foto enviada correctamente. \n");
							fotos[j].delete();
							_tempStr = _txtEstadoEnvio.getText();
							_txtEstadoEnvio.setText(_tempStr+GetDateTimeHora()+ "   Foto eliminada en carpeta local.\n");
						}	
						File foto_small = new File(_ruta +"/"+actas[i].getName()+"/small_"+fotos[j].getName());
						foto_small.delete();
					}
					if(actas[i].listFiles().length==0){
						actas[i].delete();
					}
				}
			}
		}else { 
			System.out.println("El directorio no existe.");			
		}
	}
	
	
	private byte[] FileToArrayBytes(String NombreArchivo){
		int len = 0;
		InputStream is 	= null;
		ByteArrayOutputStream os = new ByteArrayOutputStream(1024 * this.SizeBuffer);
		byte[] buffer = new byte[1024*this.SizeBuffer];
		
		try{
			if (NombreArchivo != null) {
				is = new FileInputStream(NombreArchivo);
				try {
					while ((len = is.read(buffer)) >= 0) {
		    			os.write(buffer, 0, len);
					}
				} finally {
					is.close();
				}
			}
		}catch (Exception e){
			e.printStackTrace();
			try {
				throw new IOException("Unable to open R.raw.");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return os.toByteArray();
	}
	
	
	public static void copyImage(String filePath, String copyPath) {
        BufferedImage bimage = loadImage(filePath);
        if(bimage.getHeight()>bimage.getWidth()){
            int heigt = (bimage.getHeight() * MAX_WIDTH) / bimage.getWidth();
            bimage = resize(bimage, MAX_WIDTH, heigt);
            int width = (bimage.getWidth() * MAX_HEIGHT) / bimage.getHeight();
            bimage = resize(bimage, width, MAX_HEIGHT);
        }else{
            int width = (bimage.getWidth() * MAX_HEIGHT) / bimage.getHeight();
            bimage = resize(bimage, width, MAX_HEIGHT);
            int heigt = (bimage.getHeight() * MAX_WIDTH) / bimage.getWidth();
            bimage = resize(bimage, MAX_WIDTH, heigt);
        }
        saveImage(bimage, copyPath);
    }
	
	
	/*
    Este método se utiliza para redimensionar la imagen
    */
    public static BufferedImage resize(BufferedImage bufferedImage, int newW, int newH) {
        int w = bufferedImage.getWidth();
        int h = bufferedImage.getHeight();
        BufferedImage bufim = new BufferedImage(newW, newH, bufferedImage.getType());
        Graphics2D g = bufim.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(bufferedImage, 0, 0, newW, newH, 0, 0, w, h, null);
        g.dispose();
        return bufim;
    }
	
    
    /*
    Este método se utiliza para cargar la imagen de disco
    */
    public static BufferedImage loadImage(String pathName) {
        BufferedImage bimage = null;
        try {
            bimage = ImageIO.read(new File(pathName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bimage;
    }
 
    /*
    Este método se utiliza para almacenar la imagen en disco
    */
    public static void saveImage(BufferedImage bufferedImage, String pathName) {
        try {
            String format = (pathName.endsWith(".png")) ? "png" : "jpg";
            File file =new File(pathName);
            file.getParentFile().mkdirs();
            ImageIO.write(bufferedImage, format, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public String GetDateTimeHora(){
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		String formattedDate = df1.format(c.getTime());
		return formattedDate;
	}
	
	private String WebService(String _username, String _contrasena, String _acta, byte[] _informacion){
		this.URL 			= "http://190.93.133.87:8080/Enerca/WS/JavaWS.php?wsdl";
		this.NAMESPACE 		= "http://190.93.133.87:8080/Enerca/WS";
		try{		
			SoapObject so = new SoapObject(NAMESPACE, METHOD_NAME);
			so.addProperty("usuario", _username);
			so.addProperty("contrasena",_contrasena);
			so.addProperty("acta", _acta);
			so.addProperty("informacion",_informacion);
			
			SoapSerializationEnvelope sse=new SoapSerializationEnvelope(SoapEnvelope.VER11);
			new MarshalBase64().register(sse);
			sse.dotNet=true;
			sse.setOutputSoapObject(so);
			HttpTransportSE htse=new HttpTransportSE(URL);
			htse.call(SOAP_ACTION, sse);
			response=(SoapPrimitive) sse.getResponse();
			
			/*if(response==null){
				this.Respuesta = "-1";
			}else if(response.toString().isEmpty()){
				this.Respuesta = "-2";
			}else{
				this.Respuesta = "1";
			}*/	
		} catch (Exception e) {
			e.toString();
		}	
		return this.response.toString();
	}
}
