import java.net.*;
import java.io.*;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.JScrollPane;
import java.awt.Dimension;
import java.applet.*;
import javax.swing.UIManager.*;
public class Chat extends JFrame implements ActionListener{
	//�`���b�g��ʊ֘A
	JTextField textFieldKeyin;//���b�Z�[�W���͗p�e�L�X�g�t�B�[���h
	JTextArea textAreaMain;//�e�L�X�g�G���A
	JScrollPane js;
	String myName;//���O��ۑ�
	JButton bs=new JButton("���M");
	private Container c;
	PrintWriter out;//�o�͗p�̃��C�^�[
	JCheckBox jc;
	AudioClip music;
	char Ban[]={'@',':','��','�F'};
	boolean readText=false;
	BufferedReader text;
	int port=0;
	PrintWriter pw;
	File file;
	public Chat(){


		String ip=null;
		int laF=0;
		try{
			BufferedReader conf= new BufferedReader(new FileReader(new File("conf.properties")));
			laF=Integer.parseInt(conf.readLine().split("=")[1]);
			ip=conf.readLine().split("=")[1];
			port=Integer.parseInt(conf.readLine().split("=")[1]);
			readText=Boolean.valueOf(conf.readLine().split("=")[1]);
			


		}catch(Exception e){
		}
		try{
			file=new File("chat.log");
			file.createNewFile();
			text=new BufferedReader(new FileReader(file));
	//		pw=new PrintWriter(new BufferedWriter(new FileWriter(file)),true);
		}catch(Exception e){
		}
		lookAF(laF);//LookAndFeel�̐ݒ�

		
		//�E�B���h�E���쐬����
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	//	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Client");
		setSize(600,400);
		setResizable(false);
		c = getContentPane();
		c.setLayout(new FlowLayout());//���C�A�E�g�̐ݒ�
		jc=new JCheckBox("�~���[�g");
		//�`���b�g��ʂ��쐬����
		textFieldKeyin = new JTextField("",42);//���͗p�̃e�L�X�g�t�B�[���h���쐬

		textFieldKeyin.addActionListener(this);
		textFieldKeyin.setActionCommand("Enter");
		bs.addActionListener(this);//�{�^�����������Ƃ��̓��삷��悤�ɂ���
		bs.setActionCommand("Send");
		textAreaMain = new JTextArea();//�`���b�g�̏o�͗p�̃t�B�[���h���쐬
		textAreaMain.setFont(new Font(Font.SERIF, Font.PLAIN, 24));
		textAreaMain.setLineWrap(true);
		js=new JScrollPane(textAreaMain);
		js.setPreferredSize(new Dimension(500,300));
		c.add(js);//�R���e�i�ɒǉ�

		c.add(textFieldKeyin);//�R���e�i�ɒǉ�
		c.add(bs);//�{�^�����R���e�i�ɒǉ�
		c.add(jc);//�ʒm���~���[�g�p
		textAreaMain.setEditable(false);//�ҏW�s�ɂ���
		
		music=Applet.newAudioClip(getClass().getResource("music.wav"));//�ʒm����ǂݍ���
		//�T�[�o�ɐڑ�����
		Socket socket = null;
		
	//	ip= JOptionPane.showInputDialog(null,"�ڑ����IP����͂��Ă�������","IP�̓���",JOptionPane.QUESTION_MESSAGE);
		
		try {
			socket = new Socket(ip,port);
		} catch (UnknownHostException e) {
			System.err.println("�z�X�g�� IP �A�h���X������ł��܂���: " + e);
			
		} catch (IOException e) {
			 System.err.println("�G���[���������܂���: " + e);
		}
		reloadLog();
		try{
			pw=new PrintWriter(new BufferedWriter(new FileWriter(file,true)));
		}catch(Exception e){}
		addWindowListener(new WindowClosing());
		Runtime.getRuntime().addShutdownHook( new Shutdown(pw));

		MessegeThread mrt = new MessegeThread(socket);
		mrt.start();
		
	}


	//���b�Z�[�W��M�̂��߂̃X���b�h
	public class MessegeThread extends Thread {
		
		Socket socket;
		String myName;
		
		public MessegeThread(Socket s){
			socket = s;
//			myName = n;
		}
		
		//�ʐM�󋵂��Ď����C��M�f�[�^�ɂ���ē��삷��
		public void run() {
			try{
				InputStreamReader sisr = new InputStreamReader(socket.getInputStream());
				BufferedReader br = new BufferedReader(sisr);
				out = new PrintWriter(socket.getOutputStream(), true);
				String mes="���O����͂��Ă�������";
				while(true){
					myName = JOptionPane.showInputDialog(null,mes,"���O�̓���",JOptionPane.QUESTION_MESSAGE);
					if(myName == null){
						myName = "No name";
					}
					Boolean NameBan =nameCheck(myName);
					if(!NameBan){
						out.println(myName);//�ڑ��̍ŏ��ɖ��O�𑗂�
						if(br.readLine().equals("true")){
							break;
						}
						mes="���O���d�����Ă��܂��B�ʂ̖��O�ɂ��Ă��������B";
					}
					else mes="�֎~�������܂܂�Ă��܂�";					
				}
				setTitle("MyChatClient:"+myName);
				textAreaMain.append(br.readLine()+"\n");
				while(true) {
					String inputLine = br.readLine();
				//	java.awt.Toolkit.getDefaultToolkit().beep();
					String nameC[]=inputLine.split(":",0);
					if(!jc.isSelected()&&!nameC[0].equals(myName)){
						music.play();
					}
					if (inputLine != null) {
						textAreaMain.append(inputLine+"\n");//���b�Z�[�W�̓��e���o�͗p�e�L�X�g�ɒǉ�����
						textAreaMain.setCaretPosition(textAreaMain.getText().length());
						pw.println(inputLine);
					}
					else{
						break;
					}
				}
				socket.close();
			} catch (IOException e) {
				System.err.println("�G���[���������܂���: " + e);
			}
		}
	}
	
	//�A�N�V�������s��ꂽ�Ƃ��̏���
	public void actionPerformed(ActionEvent ae) {
		if(ae.getActionCommand()=="Send"||ae.getActionCommand()=="Enter") {
			String msg = textFieldKeyin.getText();//���͂����e�L�X�g�𓾂�
			textFieldKeyin.setText("");//textFieldKeyin��Text���N���A����
			if(msg.length()>0){//���͂������b�Z�[�W�̒������O�Ŗ�����΁C
				out.println(msg);
				out.flush();
				textAreaMain.setCaretPosition(textAreaMain.getText().length());
			}
		}
  	}
  	
  	public static void main(String[] args) {
	
		Chat cc = new Chat();
		cc.setVisible(true);
	}
	public boolean nameCheck(String ss){
		boolean bb=false;
		for(int i=0;i<Ban.length;i++){
			for(int j=0;j<ss.length();j++){
				if(Ban[i]==ss.charAt(j)){
					bb=true;
				}
			}
		}
		return bb;

	}
	
	public void lookAF(int x){
		try {
			//UIManager.setLookAndFeel("sun.swing.plaf.nimbus.NimbusLookAndFeel");
			if(x==1){
  				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			}
			else if(x==2){
				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if ("Nimbus".equals(info.getName())) {
						UIManager.setLookAndFeel(info.getClassName());
						break;
					}
				}
			}
				
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	public void reloadLog(){
		try{
			String str=text.readLine();
			while(str!=null){

				textAreaMain.append(str+"\n");
				textAreaMain.setCaretPosition(textAreaMain.getText().length());
				str=text.readLine();
			}
		}catch(Exception e){
		}
	}
}
class WindowClosing extends WindowAdapter{
	public void windowClosing(WindowEvent e) {
		int nReturn = JOptionPane.showConfirmDialog(null,"�I�����܂����H", "�I���m�F",JOptionPane.YES_NO_OPTION);
		if(nReturn== JOptionPane.YES_OPTION){
			System.exit(0);
		}
	}
}


class Shutdown extends Thread{
	PrintWriter pw_w;
	Shutdown(PrintWriter pw){
		pw_w=pw;
	}
	public void run(){
	//�����ɁA�A�v���P�[�V�����I�����Ɏ��{���鏈����ǉ����܂�
		pw_w.close();
	}
}