import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
//スレッド部（各クライアントに応じて）
class ClientProcThread extends Thread {
	public int number;//自分のインスタンスの番号
	public Socket incoming;
	public InputStreamReader myIsr;
	public BufferedReader myIn;
	public PrintWriter myOut;
	public String myName;//接続者の名前

	public ClientProcThread(int n, Socket i, InputStreamReader isr, BufferedReader in, PrintWriter out) {
		number = n;
		incoming = i;
		myIsr = isr;
		myIn = in;
		myOut = out;
	}

	public void run() {
		try {
		//	myOut.println("Hello, client No." + number + "! Enter 'Bye' to exit.");//初回だけ呼ばれる
			while(true){
				boolean bool=false;
				myName = myIn.readLine();//初めて接続したときの一行目は名前。名前のかぶりがないように判別する
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
			Network.SendAll(myName+"さんが接続しました",number);
			Network.setName(myName,number);
			Network.SendOnly(number);
			while (true) {//無限ループ、入力待ち状態
				String str = myIn.readLine();
				System.out.println("Received from client No."+number+"("+myName+"), Messages: "+str);
				if (str != null) {//このソケットに入力があるかをチェック
					if (str.toUpperCase().equals("BYE")) {
						myOut.println("Good bye!");
						System.out.println("Disconnect from client No."+number+"("+myName+")");
						Network.SetFlag(number, false);//接続が切れたのでフラグをfalse
						Network.SendAll(myName+"さんは切断しました",number);
						break;
					}
					str=str.replaceAll("＠","@");
					str=str.replaceAll("　"," ");
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
			//接続終了時ここに
			System.out.println("Disconnect from client No."+number+"("+myName+")");
			Network.SetFlag(number, false);//接続が切れたのでフラグを下げる
			Network.SendAll(myName+"さんは切断しました",number);
		}
	}
}

class Network{
	
	public static int maxConnection=100;//最大接続数
	public static Socket[] incoming;//受付用のソケット
	public static boolean[] flag;//接続中かどうかのフラグ
	public static InputStreamReader[] isr;//入力ストリーム用の配列
	public static BufferedReader[] in;//バッファリングをによりテキスト読み込み用の配列
	public static PrintWriter[] out;//出力ストリーム用の配列
	public static ClientProcThread[] myClientProcThread;//スレッド用の配列
	public static int member;//接続しているメンバーの数
	public static String[] menber_name=new String[100];

	static void setName(String name,int number){
		menber_name[number]=name;
	}
	//全員にメッセージを送る
	public static void SendAll(String str, String myName){
		//送られた来たメッセージを接続している全員に配る
		for(int i=1;i<=member;i++){
			if(flag[i] == true){
				out[i].println(myName+":"+str);
				out[i].flush();//バッファをはき出す＝＞バッファにある全てのデータをすぐに送信する
			}
		}	
	}

	public static void SendAll(String str,int number){
		//送られた来たメッセージを接続している全員に配る（自分は除く）
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
			out[number].print("現在接続しているのは");
			for(int i=1;i<number;i++){
				if(flag[i]){
					out[number].print(menber_name[i]+"さん");
					if(i+1!=number)out[number].print(":");
					out[number].flush();
				}
			}
			out[number].println("です");
			out[number].flush();
		}
		else {
			out[number].println("現在誰も接続していません");
			out[number].flush();
		}

	}

	public static void sendOnly(String str,int number,int me){
		//送られた来たメッセージを指定された人に送る
			out[number].println(str);
			out[number].flush();
			out[me].println(str);
			out[me].flush();

	}
	public static void judName(String str,int i){
		out[i].println(str);
		out[i].flush();
	}


	//フラグの設定を行う
	public static void SetFlag(int n, boolean value){	
		flag[n] = value;
	}
	
	//mainプログラム
	public static void main(String[] args) {
		//必要な配列を確保する
		incoming = new Socket[maxConnection];
		flag = new boolean[maxConnection];
		isr = new InputStreamReader[maxConnection];
		in = new BufferedReader[maxConnection];
		out = new PrintWriter[maxConnection];
		myClientProcThread = new ClientProcThread[maxConnection];
		
		int n = 1;
		member = 0;//誰も接続していないのでメンバー数は０

		try {
			System.out.println("The server has launched!");
			ServerSocket server = new ServerSocket(25566);//10000番ポートを利用する
			while (true) {
				incoming[n] = server.accept();
				flag[n] = true;
				System.out.println("Accept client No." + n);
				//必要な入出力ストリームを作成する
				isr[n] = new InputStreamReader(incoming[n].getInputStream());
				in[n] = new BufferedReader(isr[n]);
				out[n] = new PrintWriter(incoming[n].getOutputStream(), true);
				
				myClientProcThread[n] = new ClientProcThread(n, incoming[n], isr[n], in[n], out[n]);//必要な情報を渡しスレッドを作成
				myClientProcThread[n] .start();//スレッドを開始する
				member = n;//メンバーの数を更新する
				n++;
			}
		} catch (Exception e) {
			System.err.println("ソケット作成失敗: " + e);
		}
	}
}
