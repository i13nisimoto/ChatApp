import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
//�X���b�h���i�e�N���C�A���g�ɉ����āj
class ClientProcThread extends Thread {
	public int number;//�����̃C���X�^���X�̔ԍ�
	public Socket incoming;
	public InputStreamReader myIsr;
	public BufferedReader myIn;
	public PrintWriter myOut;
	public String myName;//�ڑ��҂̖��O

	public ClientProcThread(int n, Socket i, InputStreamReader isr, BufferedReader in, PrintWriter out) {
		number = n;
		incoming = i;
		myIsr = isr;
		myIn = in;
		myOut = out;
	}

	public void run() {
		try {
		//	myOut.println("Hello, client No." + number + "! Enter 'Bye' to exit.");//���񂾂��Ă΂��
			while(true){
				boolean bool=false;
				myName = myIn.readLine();//���߂Đڑ������Ƃ��̈�s�ڂ͖��O�B���O�̂��Ԃ肪�Ȃ��悤�ɔ��ʂ���
				for(int i=1;i<=Network.member;i++){
					if(myName.equals(Network.menber_name[i])&&Network.flag[i]){
						bool=true;
						
					}
				}
				if(bool==false){
					Network.judName("true",number);
					break;
				}
				else Network.judName("false",number);
			}
			Network.SendAll(myName+"���񂪐ڑ����܂���",number);
			Network.setName(myName,number);
			Network.SendOnly(number);
			while (true) {//�������[�v�A���͑҂����
				String str = myIn.readLine();
				System.out.println("Received from client No."+number+"("+myName+"), Messages: "+str);
				if (str != null) {//���̃\�P�b�g�ɓ��͂����邩���`�F�b�N
					if (str.toUpperCase().equals("BYE")) {
						myOut.println("Good bye!");
						System.out.println("Disconnect from client No."+number+"("+myName+")");
						Network.SetFlag(number, false);//�ڑ����؂ꂽ�̂Ńt���O��false
						Network.SendAll(myName+"����͐ؒf���܂���",number);
						break;
					}
					str=str.replaceAll("��","@");
					str=str.replaceAll("�@"," ");
					if(str.charAt(0)=='@'){
						String ss[]=str.split(" ");
						String st=ss[0].replaceAll("@","");
						
						for(int i=1;i<=Network.member;i++){
							System.out.println(Network.menber_name[i]);
							if(Network.menber_name[i].equals(st)){

								Network.sendOnly(myName+":"+str,i,number);
								break;
							}

						}
					}
					else{
						Network.SendAll(str, myName);//Send All
					}
				}
			}
		} catch (Exception e) {
			//�ڑ��I����������
			System.out.println("Disconnect from client No."+number+"("+myName+")");
			Network.SetFlag(number, false);//�ڑ����؂ꂽ�̂Ńt���O��������
			Network.SendAll(myName+"����͐ؒf���܂���",number);
		}
	}
}

class Network{
	
	public static int maxConnection=100;//�ő�ڑ���
	public static Socket[] incoming;//��t�p�̃\�P�b�g
	public static boolean[] flag;//�ڑ������ǂ����̃t���O
	public static InputStreamReader[] isr;//���̓X�g���[���p�̔z��
	public static BufferedReader[] in;//�o�b�t�@�����O���ɂ��e�L�X�g�ǂݍ��ݗp�̔z��
	public static PrintWriter[] out;//�o�̓X�g���[���p�̔z��
	public static ClientProcThread[] myClientProcThread;//�X���b�h�p�̔z��
	public static int member;//�ڑ����Ă��郁���o�[�̐�
	public static String[] menber_name=new String[100];

	static void setName(String name,int number){
		menber_name[number]=name;
	}
	//�S���Ƀ��b�Z�[�W�𑗂�
	public static void SendAll(String str, String myName){
		//����ꂽ�������b�Z�[�W��ڑ����Ă���S���ɔz��
		for(int i=1;i<=member;i++){
			if(flag[i] == true){
				out[i].println(myName+":"+str);
				out[i].flush();//�o�b�t�@���͂��o�������o�b�t�@�ɂ���S�Ẵf�[�^�������ɑ��M����
			}
		}	
	}

	public static void SendAll(String str,int number){
		//����ꂽ�������b�Z�[�W��ڑ����Ă���S���ɔz��i�����͏����j
		for(int i=1;i<=member;i++){
			if(flag[i] == true&&i!=number){
				out[i].println(str);
				out[i].flush();
			}
		}	
	}
	public static void SendOnly(int number){
		boolean stayM=false;
		for(int i=1;i<number;i++){
			if(flag[i]){
				stayM=true;
				break;
			}
		}
		if(stayM){
			out[number].print("���ݐڑ����Ă���̂�");
			for(int i=1;i<number;i++){
				if(flag[i]){
					out[number].print(menber_name[i]+"����");
					if(i+1!=number)out[number].print(":");
					out[number].flush();
				}
			}
			out[number].println("�ł�");
			out[number].flush();
		}
		else {
			out[number].println("���ݒN���ڑ����Ă��܂���");
			out[number].flush();
		}

	}

	public static void sendOnly(String str,int number,int me){
		//����ꂽ�������b�Z�[�W���w�肳�ꂽ�l�ɑ���
			out[number].println(str);
			out[number].flush();
			out[me].println(str);
			out[me].flush();

	}
	public static void judName(String str,int i){
		out[i].println(str);
		out[i].flush();
	}


	//�t���O�̐ݒ���s��
	public static void SetFlag(int n, boolean value){	
		flag[n] = value;
	}
	
	//main�v���O����
	public static void main(String[] args) {
		//�K�v�Ȕz����m�ۂ���
		incoming = new Socket[maxConnection];
		flag = new boolean[maxConnection];
		isr = new InputStreamReader[maxConnection];
		in = new BufferedReader[maxConnection];
		out = new PrintWriter[maxConnection];
		myClientProcThread = new ClientProcThread[maxConnection];
		
		int n = 1;
		member = 0;//�N���ڑ����Ă��Ȃ��̂Ń����o�[���͂O

		try {
			System.out.println("The server has launched!");
			ServerSocket server = new ServerSocket(25566);//10000�ԃ|�[�g�𗘗p����
			while (true) {
				incoming[n] = server.accept();
				flag[n] = true;
				System.out.println("Accept client No." + n);
				//�K�v�ȓ��o�̓X�g���[�����쐬����
				isr[n] = new InputStreamReader(incoming[n].getInputStream());
				in[n] = new BufferedReader(isr[n]);
				out[n] = new PrintWriter(incoming[n].getOutputStream(), true);
				
				myClientProcThread[n] = new ClientProcThread(n, incoming[n], isr[n], in[n], out[n]);//�K�v�ȏ���n���X���b�h���쐬
				myClientProcThread[n] .start();//�X���b�h���J�n����
				member = n;//�����o�[�̐����X�V����
				n++;
			}
		} catch (Exception e) {
			System.err.println("�\�P�b�g�쐬���s: " + e);
		}
	}
}
