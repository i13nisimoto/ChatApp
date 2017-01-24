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
	//チャット画面関連
	JTextField textFieldKeyin;//メッセージ入力用テキストフィールド
	JTextArea textAreaMain;//テキストエリア
	JScrollPane js;
	String myName;//名前を保存
	JButton bs=new JButton("送信");
	private Container c;
	PrintWriter out;//出力用のライター
	JCheckBox jc;
	AudioClip music;
	char Ban[]={'@',':','＠','：'};
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
		lookAF(laF);//LookAndFeelの設定

		
		//ウィンドウを作成する
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	//	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Client");
		setSize(600,400);
		setResizable(false);
		c = getContentPane();
		c.setLayout(new FlowLayout());//レイアウトの設定
		jc=new JCheckBox("ミュート");
		//チャット画面を作成する
		textFieldKeyin = new JTextField("",42);//入力用のテキストフィールドを作成

		textFieldKeyin.addActionListener(this);
		textFieldKeyin.setActionCommand("Enter");
		bs.addActionListener(this);//ボタンを押したときの動作するようにする
		bs.setActionCommand("Send");
		textAreaMain = new JTextArea();//チャットの出力用のフィールドを作成
		textAreaMain.setFont(new Font(Font.SERIF, Font.PLAIN, 24));
		textAreaMain.setLineWrap(true);
		js=new JScrollPane(textAreaMain);
		js.setPreferredSize(new Dimension(500,300));
		c.add(js);//コンテナに追加

		c.add(textFieldKeyin);//コンテナに追加
		c.add(bs);//ボタンをコンテナに追加
		c.add(jc);//通知音ミュート用
		textAreaMain.setEditable(false);//編集不可にする
		
		music=Applet.newAudioClip(getClass().getResource("music.wav"));//通知音を読み込む
		//サーバに接続する
		Socket socket = null;
		
	//	ip= JOptionPane.showInputDialog(null,"接続先のIPを入力してください","IPの入力",JOptionPane.QUESTION_MESSAGE);
		
		try {
			socket = new Socket(ip,port);
		} catch (UnknownHostException e) {
			System.err.println("ホストの IP アドレスが判定できません: " + e);
			
		} catch (IOException e) {
			 System.err.println("エラーが発生しました: " + e);
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


	//メッセージ受信のためのスレッド
	public class MessegeThread extends Thread {
		
		Socket socket;
		String myName;
		
		public MessegeThread(Socket s){
			socket = s;
//			myName = n;
		}
		
		//通信状況を監視し，受信データによって動作する
		public void run() {
			try{
				InputStreamReader sisr = new InputStreamReader(socket.getInputStream());
				BufferedReader br = new BufferedReader(sisr);
				out = new PrintWriter(socket.getOutputStream(), true);
				String mes="名前を入力してください";
				while(true){
					myName = JOptionPane.showInputDialog(null,mes,"名前の入力",JOptionPane.QUESTION_MESSAGE);
					if(myName == null){
						myName = "No name";
					}
					Boolean NameBan =nameCheck(myName);
					if(!NameBan){
						out.println(myName);//接続の最初に名前を送る
						if(br.readLine().equals("true")){
							break;
						}
						mes="名前が重複しています。別の名前にしてください。";
					}
					else mes="禁止文字が含まれています";					
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
						textAreaMain.append(inputLine+"\n");//メッセージの内容を出力用テキストに追加する
						textAreaMain.setCaretPosition(textAreaMain.getText().length());
						pw.println(inputLine);
					}
					else{
						break;
					}
				}
				socket.close();
			} catch (IOException e) {
				System.err.println("エラーが発生しました: " + e);
			}
		}
	}
	
	//アクションが行われたときの処理
	public void actionPerformed(ActionEvent ae) {
		if(ae.getActionCommand()=="Send"||ae.getActionCommand()=="Enter") {
			String msg = textFieldKeyin.getText();//入力したテキストを得る
			textFieldKeyin.setText("");//textFieldKeyinのTextをクリアする
			if(msg.length()>0){//入力したメッセージの長さが０で無ければ，
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
		int nReturn = JOptionPane.showConfirmDialog(null,"終了しますか？", "終了確認",JOptionPane.YES_NO_OPTION);
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
	//ここに、アプリケーション終了時に実施する処理を追加します
		pw_w.close();
	}
}