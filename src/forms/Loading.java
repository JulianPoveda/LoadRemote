package forms;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;



import files.Archivos;

@SuppressWarnings("serial")
public class Loading extends JFrame{
	private String _rutaCarpeta;
	
	private Archivos archivos = new Archivos();
	private JFileChooser rutaCarpeta = new JFileChooser();
	
	private JLabel	_lblUsuario, _lblContrasena;
	private JButton _btnDetener, _btnIniciar;
	private JTextField _txtUsuario;
	private JPasswordField _txtContrasena;
	
	private BtnIniciarHandler _btnIniciarHandler;
	private BtnDetenerHandler _btnDetenerHandler;
	
	
	
	public Loading(){
		rutaCarpeta.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		_lblUsuario 	= new JLabel("Usuario");
		_lblContrasena 	= new JLabel("Contraseña");
		
		_txtUsuario		= new JTextField(50);
		_txtContrasena	= new JPasswordField(50);
		
		_btnIniciar 	= new JButton("Iniciar Envio");
		_btnDetener 	= new JButton("Buscar Carpeta");
		
		_btnIniciarHandler = new BtnIniciarHandler();
		_btnIniciar.addActionListener(_btnIniciarHandler);
		
		_btnIniciar.setEnabled(true);
		_btnDetener.setEnabled(true);
		
		_btnDetenerHandler = new BtnDetenerHandler();
		_btnDetener.addActionListener(_btnDetenerHandler);
		
		 Container pane = getContentPane();
         pane.setLayout(new GridLayout(3,2));
		
         pane.add(_lblUsuario);
         pane.add(_txtUsuario);
         pane.add(_lblContrasena);
         pane.add(_txtContrasena);
         pane.add(_btnIniciar);
         pane.add(_btnDetener);
         
         setTitle("Envio de Archivos Remotos");
         setSize(400,100);
         setVisible(true);
         setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	
	public static void main(String[] arg){
		@SuppressWarnings("unused")
		Loading miAplicacion = new Loading();
	}
	
	private class BtnIniciarHandler implements ActionListener{
		public void actionPerformed(ActionEvent e){
			_txtUsuario.setEnabled(true);
			_txtContrasena.setEnabled(true);
			_btnIniciar.setEnabled(true);
			_btnDetener.setEnabled(true);
			archivos.ListarArchivos(_rutaCarpeta, _txtUsuario.getText(), new String(_txtContrasena.getPassword()));
		}
    }
	
	private class BtnDetenerHandler implements ActionListener{
		public void actionPerformed(ActionEvent e){
			int returnVal = rutaCarpeta.showOpenDialog((Component)e.getSource());
		    if (returnVal == JFileChooser.APPROVE_OPTION){
		        try {
		        	_rutaCarpeta = rutaCarpeta.getSelectedFile().toString();
		        } catch (Exception ex) {
		          System.out.println("problem accessing file "+_rutaCarpeta);
		        }
		    } 
		    else {
		        System.out.println("File access cancelled by user.");
		    }   
		}
    }
}
