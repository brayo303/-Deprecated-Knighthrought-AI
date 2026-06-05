
//untuk menggunakan export class ini menjadi jar

//import - import yang digunakan

// Class Game Milik Ludii
import game.Game;
//Fast ArrayList yaitu arraylist special ludii yang punya remove and swap
import main.collections.FastArrayList;
//Class AI milik ludii yang mana kita implementasikan di class ini
import util.AI;
//Context yang memuat semua data state dari permainan
import util.Context;
//Move adalah class dari ludii yang merepresentasikan sebuah gerakan dalam knight througt adalah gerakan l di kuda
import util.Move;
//Container state adalah state dari board yang kita miliki dimana nanti dibutuhkan untuk perhitungan heuristik
import util.state.containerState.ContainerState;

//Arrays Sebenarnya hanya dibutuhkan untuk sorting nantinya
import java.util.Arrays;
//Merupakan cara random baru dari java yang nantinya di pakai di method selectMove
import java.util.concurrent.ThreadLocalRandom;

//Bryan Heryanto - 6181801031
//Michael Sotaronggal Manurung - 6181801027
//Deddy Chandra - 6181801007


//note : waktu pada ludii tidak berpengaruh pada algoritma ini karena diberhentikan dengan cutting off depth yang fix
//		apabila terdapat hal yang tidak lengkap bisa dibaca dokumentasi yang dicantumkan beserta kode ini
//		maaf apabila terdapat typo atau kesalahan penulisan pada komen-komen karena tidak ditulis secara formal
//		bukan AI yang generik untuk semua game AI ini khusus untuk knight throught saja
//		hanya class ini yang perlu diexport ke jar

//membuat customAI dengan mengextends AI
//cara bisa dilihat di https://github.com/Ludeme/LudiiExampleAI
public class AlphaBetaAI extends AI{
	
	// playerID adalah yang merupakan alpha(angka id, 1 (putih) atau 2 (hitam) )
	protected int playerID;
	// buat menyimpan move terbaik
	protected Move bestMove;
	//atribut untuk keperluan analysis
	private String analysisReport;
	
	@Override
	//Method yang dijalankan Ludi, ketika ia ingin memilih suatu moves
	public Move selectAction(Game game, Context context, double maxSeconds, int maxIterations, int maxDepth) {
			// untuk menghitung berapa waktu yang diperlukan untuk menemukan move yang bagus
			long prev = System.currentTimeMillis();
			// Node yang merupakan root
			Node root = new Node(null,null,context,this.playerID,this.playerID);
			// generate semua move yang possible pada root
			root.getAllPossibleMove();
			// mengisi bestMove dengan move yang null(Jika tidak ada moves yang bagus, maka ini yang dipilih)
			bestMove=null;
			
			// Menjalankan alpha beta pruning
			//menyimpan evaluation best value terbaik untuk analisis
			analysisReport="Custom Alpha Beta Best evaluation value: "+alphaBeta(root,0,true,-1000000000,1000000000)+" ";
			//menyimpan waktu lama kira-kira penentuan move
			analysisReport+="Time used:"+(System.currentTimeMillis()-prev)+"ms";
			
		// return bestMove yang didapat (alias menggerakan Kudanya)
		return bestMove;
	}
	
	//method ludii hanya untuk keperluan analisis bagian ini akan ditampilkan di tab analysis ludii
	@Override
	public String generateAnalysisReport()
	{
		return analysisReport;
	}
	
	
	@Override
	//Sebelum melakukan moves, method ini dijalankan
	public void initAI(final Game game, final int playerID)
	{
		//mengisi alpha dengan playerID sekarang
		this.playerID = playerID;
	}
	
	// Method implementasi algoritma Minmax Alpha Beta Pruning
	public double alphaBeta(Node node,int depth , boolean isAlpha , double alpha,double beta) {
		//apabila isCuttingOff / node adalah terminal ini adalah baseCasenya
		if(isCuttingOff(depth)||node.isTerminal()) {
			//dapatkan hasil evaluation function dari node tersebut
			//contoh cara debugging
			//printState(node.getState(),node.getValue());
			return node.getValue();
		}
		//cek apakah ini adalah alpha atau beta
		if(isAlpha) {
			//bagian alpha kami tidak membuat satu method lagi untuk menghandle alpha seperti di pseudocode pada umumnya untuk memangkas pemanggilan method
			//karena diketahui pemanggilan method memakan waktu lagi
			//Move Ordering / pemilihan move (exploration)
			FastArrayList<Node> adjNode = selectMove(node,(this.playerID%2)+1);
			//iterasi untuk exploration move
			for(int i = 0 ; i <adjNode.size(); i++) {
				//generate semua possible move, hal ini dilakukan disini agar memangkas complexity algoritma
				//dengan apabila node terpangkas di move ordering maka tidak usah lagi memanggil method ini
				adjNode.get(i).getAllPossibleMove();
				//exploitation pada tahap ini kita akan mengubah alpha jadi beta lalu depthnya ditambah dengan 1
				double value = alphaBeta(adjNode.get(i)
						,depth+1
						,false
						,alpha,beta
						);
				//cek apakah apakah alpha yang didapat dari exploitation(rekursif) lebih baik ketimbang sebelumnya
				if(alpha<value) {
					//cek apakah sedang berada di depth 0 
					//sebenarnya pada umumnya method ini berada di luar looping tapi dengan cara ini diharapkan kita mempercepat kompleksitas waktu
					if(depth==0) {
						//apabila sedang di depth 0 berati kita mendapatkan move yang lebih baik sehingga bestMove di ganti nilainya
						bestMove=adjNode.get(i).getAction();
					}
					//set alpha ke value tersebut
					alpha=value;
				}
				
				//prunning alpha betanya apabila beta sekarang sudah lebih besar dari alpha maka di prune saja
				//karena pada saat ini kita memilih yang maksimal
				if(beta<=alpha) {	
					break;
				}	
			}
			//kalau belum ada bestMovenya
			if(depth==0&&bestMove==null) {
				bestMove=adjNode.get(0).getAction();
			}
			// mengembalikan nilai alpha
			return alpha;
		}else {
			//bagian beta kami tidak membuat satu method lagi untuk menghandle beta seperti di pseudocode pada umumnya untuk memangkas pemanggilan method
			//karena diketahui pemanggilan method memakan waktu lagi
			//Move Ordering / pemilihan move (exploration)
			FastArrayList<Node> adjNode = selectMove(node,this.playerID);
			
			//exploration iterasi setiap move
			for(int i = 0 ; i <adjNode.size(); i++) {
				//generate semua possible move, hal ini dilakukan disini agar memangkas complexity algoritma
				//dengan apabila node terpangkas di move ordering maka tidak usah lagi memanggil method ini
				adjNode.get(i).getAllPossibleMove();
				//exploitation pada tahap ini kita akan mengubah alpha jadi beta lalu depthnya ditambah dengan 1
				double value = alphaBeta(adjNode.get(i)
						,depth+1
						,true
						,alpha
						,beta
						);
				
				
				//cek apakah apakah beta yang didapat dari exploitation(rekursif) lebih baik ketimbang sebelumnya
				if(beta>value) {
					// set beta ke value tersebut
					beta=value;
				}
				//prunning alpha betanya apabila beta sekarang sudah lebih besar dari alpha maka di prune saja
				//karena pada saat ini kita memilih yang maksimal
				if(beta<=alpha) {
					//lakukan break
					break;
				}	
			}
			// mengembalikan beta
			return beta;		
		}	
	}
	
	
	//method untuk mengecek cutting off (memberhentikan pencarian jika belum mencapai terminal state)
	public boolean isCuttingOff(int depth) {
		//jika depthnya 5, maka pencarian diberhentikan (mengembalikan true atau merupakan cutting off)
		if(depth==5) {
			return true;
		}else {
			// jika depthnya kurang dari 5, maka belum cutting off
			return false;
		}
	}
	
	/*
	 * Method untuk melakukan move ordering yaitu pemilihan node pada exproration untuk nantinya diexploit 
	 * (5 dengan heuristic evaluation value terbaik, 14 random, dan 1 evaluation value terburuk)
	 */
	public FastArrayList<Node> selectMove(Node node,int currentId){
		//inisialisasi FastArrayList sementara
		FastArrayList<Node> listnode = new FastArrayList<>();
		//inisialisasi objek BantuanSorting yang berukuran sesuai dengan besar move yang possible di node ini
		BantuanSorting bs[]= new BantuanSorting[node.getMoves().size()];
		// iterasi sebanyak move yang possible
		for(int i = 0 ; i <node.getMoves().size(); i++) {
			//dapatkan salah satu move sesuai iterasi
			Move move = node.getMoves().get(i);
			//buat state baru(harus di clone agar tidak menyimpan alamat dari context sebelumnya)
			Context nextState = new Context(node.getState());
			//state baru diberikan move dan disimpan di next state 
			nextState.game().apply(nextState, move);
			// Membuat Node baru sesuai parameter terbaru
			Node nextNode = new Node(node,move,nextState,currentId,this.playerID);
			//masukan node tersebut ke BantuanSorting agar nanti dapat di sort menggunakan library java.
			bs[i] = new BantuanSorting(nextNode);		
		}
		// sort menggunakan library java
		Arrays.sort(bs);
		//iterasi sepanjang variabel bs
		for(int i = 0 ; i < bs.length ; i++) {
			//isi listnode dengan semua move yang sudah di sort
			listnode.add(bs[i].node);
		}
		// cara sort mirip seperti ini bisa dilihat di https://github.com/Ludeme/LudiiAI/blob/master/AI/src/search/minimax/AlphaBetaSearch.java
		// inisialisasi FastArrayList baru
		FastArrayList<Node> hasilnode = new FastArrayList<>();
		//currentId(jika node yang dibuat idnya bukan sama dengan alpha, berarti yang melakukan move sekarang adalah alpha)
		if(currentId!=this.playerID) {
			//memilih 5 move terbaik dan masukan hasilnya
			for(int i = 0 ; i < 5&&listnode.size()>0 ; i++) {
				//tambahkan ke ArrayList hasil dari removeSwap list node
				//remove swap sendiri akan mereturn isi pada list node yang diremove oleh method FastArrayList ini
				//disini kita memakai index 0 dimana setelah di sort merupakan node dengan hasil dari evaluation function paling besar
				hasilnode.add(listnode.removeSwap(0));
			}
			//apabila masih ada value di listnode memilih yang terburuk
			for(int i = 0 ; i < 1&&listnode.size()>0 ; i++) {
				//tambahkan ke ArrayList hasil dari removeSwap list node
				//kali ini kita memakai index size dari listnode-1 dimana setelah di sort merupakan node dengan hasil dari evaluation function paling kecil
				hasilnode.add(listnode.removeSwap(listnode.size()-1));
			}
			//apabila masih ada value di listnode memilih 14 secara random
			for(int i = 0 ; i < 14&&listnode.size()>0 ; i++) {
				//tambahkan ke ArrayList hasil dari removeSwap list node
				//disini kita memilih removeswap secara random
				hasilnode.add(listnode.removeSwap(ThreadLocalRandom.current().nextInt(listnode.size())));
			}
		}
		//currentId(jika node yang dibuat idnya sama dengan alpha, berarti yang melakukan move sekarang adalah beta) 
		else {
			//memilih 5 move terbaik dan masukan hasilnya
			for(int i = 0 ; i < 5&&listnode.size()>0 ; i++) {
				//tambahkan ke ArrayList hasil dari removeSwap list node
				//disini kita memakai index size dari listnode-1 dimana setelah di sort merupakan node dengan hasil dari evaluation function paling kecil
				hasilnode.add(listnode.removeSwap(listnode.size()-1));
			}
			//memilih yang terburuk
			for(int i = 0 ; i < 1&&listnode.size()>0 ; i++) {
				//tambahkan ke ArrayList hasil dari removeSwap list node
				//remove swap sendiri akan mereturn isi pada list node yang diremove oleh method FastArrayList ini
				//kali ini kita memakai index 0 dimana setelah di sort merupakan node dengan hasil dari evaluation function paling besar
				hasilnode.add(listnode.removeSwap(0));
			}
			//memilih 14 secara random
			for(int i = 0 ; i < 14&&listnode.size()>0 ; i++) {
				//tambahkan ke ArrayList hasil dari removeSwap list node
				//disini kita memilih removeswap secara random
				hasilnode.add(listnode.removeSwap(ThreadLocalRandom.current().nextInt(listnode.size())));
			}
		}
		//kembalikan move yang dipilih
		return hasilnode;
	}
	
	//Debugger tidak perlu diuncoment fungsinya adalah memprint semua isi state dan valuenya harus digunakan berbarengan dengan
	//https://github.com/Ludeme/LudiiTutorials/blob/master/src/ludii_tutorials/RunningTrials.java
	//atau RunningTrials yang disediakan
	/*
	void printState(Context state,double value) {
		for ( ContainerState containerState : state.state().containerStates())
		{
			
			for(int i = 0 ; i < 64 ; i++) {
				System.out.print(containerState.whatCell(i));
				if(i%8==7) {
					System.out.println();
				}
				
				
			}
			System.out.println("value"+value);
			
			
		}
	}*/
	

}
// Class Model untuk bobot heuristic evaluation function yang dipakai
class Heuristics {
	//bobot heuristic yang dipakai yang terurut berdasarkan baris
	static double heuristics[] = new double[] {1,1,4,4,8,640,640};
	//bobot heuristic yang dipakai lawan
	static double heuristicsmusuh[] = new double[] {1,1,4,4,8,1280,1280};
}

class Node {
	//node parent
	private Node parent;
	//state dari node ini
	private Context state;
	//action yang diapply ke state parent untuk sampai ke node ini
	private Move action;
	//list dari node yang merupakan anak dari node ini
	private FastArrayList<Node> children;
	//list move yang possible
	private FastArrayList<Move> possibleMove;
	//value dari hasil perhitungan dari evaluation function
	private double value;
	//apakah ini terminal (apakah node sudah tidak dapat melakukan eksplorasi)
	private boolean isTerminal;
	//Heuristic function, mengacu pada class Heuristic di atas
	private double heuristic[];
	//Heuristic function mengacu pada class Heuristic di atas untuk yang musuh
	private double heuristicmusuh[];
	
	//No player(1 atau 2) yang dapat melakukan move
	private int currentPlayer;
	
	//player yang menjadi alpha
	private int maximizingPlayer;
	
	
	/*
	 * Referensi kelas node diambil dari node uct : https://github.com/Ludeme/LudiiExampleAI/blob/master/src/mcts/ExampleUCT.java, tetapi algoritma tidak
	 * diambil dari sini
	 */
	public Node(Node parent,Move action,Context state,int currentPlayer,int maximizingPlayer){
		//parent diisi pointer ke parentnya
		this.parent = parent;
		//action diinitiate isinya move terakhir untuk menuju state di node ini
		this.action = action;
		//state diinstantiate dimana sebenarnya state representation disini menggunakan context dari ludii
		this.state=state;
		//initiate child array;
		this.children = new FastArrayList<Node>();
		//initiate possible move array list
		this.possibleMove = new FastArrayList<Move>();
		//initiate heuristic (dari class Heuristic) dimana disini bobot dari kuda yang kita anggap menang didefinisikan
		this.heuristic = Heuristics.heuristics ;
		//inititate heuristic musuh (dari class Heuristic)  dimana disini bobot dari kuda yang ingin kita anggap kalah didefinisikan
		this.heuristicmusuh = Heuristics.heuristicsmusuh ;
		//initiate nomor player yang dapat giliran move di node ini
		this.currentPlayer=currentPlayer;
		//intiate player yang menjadi alpha
		this.maximizingPlayer=maximizingPlayer;
		
		//kalau parent bukan null atau kalau bukan root
		if(parent!=null) {
			//membuat node ini menjadi children dari node parentnya dengancara menambah arraylist dari parent dengan pointer ke child ini 
			this.parent.addChildren(this);
			//menghitung kembali hasil dari evaluation function menggunakan heuristic
			//menggunakan method ini karena lebih cepat daripada initHeuristic() kira kira O(10)
			changeHeuristic(action);
		}else {
			//Menghitung hasil dari evaluation function pada root kira kira O(64)
			initHeuristic();
		}
		
	}
	
	// Method yang digunakan untuk mengimplementasi evaluation function pada root
	public void initHeuristic() {
		//ContainerState merupakan container, dalam kasus ini containernya adalah board papan catur knight through
		for ( ContainerState containerState : state.state().containerStates())
		{
			//kalau alpha merupakan player 1 atau putih
			if(maximizingPlayer==1) {
				// iterasikan 64 kotak (board 8 x 8)
				for(int i = 0 ; i < 64 ; i++) {
					//whatCell() adalah fungsi yang digunakan untuk mendapatkan isi dari container(board). Return 0 apabila kosong dan Player numbernya apabila ditempati (1 adalah putih dan 2 adalah hitam)
					int isi=containerState.whatCell(i);
					//Kalau player hitam(no 2) sudah masuk ke barisan paling akhir(pada ludi player itu posisinya paling bawah)
					if(i/8==0&&isi==2) {
						
						this.value=-1000000000;
						// heuristic value yang didapat dari node ini adalah minimum value
						return;
					}
					//Kalau player putih(no 1) sudah masuk ke barisan paling awal(pada ludi player itu posisinya paling atas)
					else if(i/8==7&&isi==1) {
						
						this.value=1000000000;
						// heuristic value yang didapat dari node ini adalah maximum value
						return;
					}
					//value apabila posisi tidak ada kedua syarat di atas
					// jika isi 1 (player 1) dan jika isi 2 (player 2)
					if(isi==1) {
						//value ditambahkan oleh bobot heuristic untuk menentukan evaluation musuh yang dapatkan
						this.value += heuristic[i/8];					
					}else if(isi==2) {
						//value kurangi oleh bobot heuristic untuk menentukan evaluation musuh yang dapatkan
						this.value -= heuristicmusuh[7-(i/8)];						
					}					
				}
			//kalau alpha merupakan player 2 atau hitam	
			}else if(maximizingPlayer==2) {
				// iterasikan 64 kotak (board 8 x 8)
				for(int i = 0 ; i < 64 ; i++) {
					//whatCell() adalah fungsi yang digunakan untuk mendapatkan isi dari container(board). Return 0 apabila kosong dan Player numbernya apabila ditempati (1 adalah putih dan 2 adalah hitam)
					int isi=containerState.whatCell(i);
					//Kalau player hitam(no 2) sudah masuk ke barisan paling akhir(pada ludi player itu posisinya paling bawah)
					if(i/8==0&&isi==2) {
						this.value=1000000000;
						// heuristic value yang didapat dari node ini adalah maximum value
						return;
					}
					//Kalau player putih(no 1) sudah masuk ke barisan paling awal(pada ludi player itu posisinya paling atas)
					else if(i/8==7&&isi==1) {
						this.value=-1000000000;
						// heuristic value yang didapat dari node ini adalah minimum value
						return;
					}
					//value apabila posisi tidak ada kedua syarat di atas
					// jika isi 1 (player 1) dan jika isi 2 (player 2)
					
					if(isi==1) {
						//value dikurangi oleh bobot heuristic musuh yang dapatkan 
						this.value -= heuristicmusuh[i/8];
					}else if(isi==2) {
						//value ditambahkan oleh bobot heuristic yang dapatkan
						this.value += heuristic[7-(i/8)];
					}
				}
			}
			// * Perlu diperhatikan, untuk yang no 2 heuristicnya indexnya dibalik dengan yang 1, karena posisinya terbalik
			// dalam dokumen ini sering disbeut posisi index baris relatif yaitu 7-i untuk hitam dan i untuk si putih
		}
	}
	
	
	
	//untuk menghitung mengimplementasi evaluation function yang bukan merupakan root (Fungsi ini lebih cepat daripada initHeuristic() )
	public void changeHeuristic(Move action) {
		//kalau alpha merupakan player 1 atau putih
		if(maximizingPlayer==1) {
			//method bantuan untuk menghitung value dari kalau alphanya player 1
			heuristicPlayerOne();
		}else {
			//method bantuan untuk menghitung value dari kalau alphanya player 2
			heuristicPlayerTwo();
		}
	}
	
	//method untuk menghitung value dari kalau alphanya player 1
	public void heuristicPlayerOne() {
		//inisialisasi dengan mengambil value dari node parent
		this.value = parent.getValue();
		//ContainerState merupakan state yang menyimpan isi dari board(cell) dalam Library ludii
		//dalam kasus ini containernya adalah board papan catur knight through
		//ContainerState bisa ditemukan di https://github.com/Ludeme/LudiiExampleAI/blob/master/src/experiments/Tutorial.java
		//kita juga bisa menggunakan java decompiler untuk melihatnya
		//ContainerState disini bisa  kita gunakan sebagai state representation
		//Dibawah ini adalah iterator untuk mengakses container state
		for ( ContainerState containerState : this.parent.getState().state().containerStates())
		{
			//temp variable untuk menampung isi dari containerState setelah dilakukan move pada parent
			int isito=containerState.whatCell(action.to());
			//Kalau kuda hitam yang sudah sampai garis terminal (kuda hitam menang)
			if(action.to()/8==0) {
				//value diinisiasi dengan minimum value
				value=-1000000000;
				return;
			}
			//Kalau kuda putih yang sudah sampai garis terminal (kuda putih menang)
			if(action.to()/8==7) {
				value=1000000000;
				return;
			}
			//kalau move parent memakan sebuah kuda
			if(isito!=0) {
				//kalau yang dimakan kuda putih
				if(isito==1) {
					//maka value dikurangi suatu heuristic dari indeks baris tonya
					value-=this.heuristic[action.to()/8];
				//kalau yang dimakan kuda hitam
				}else if(isito==2) {
					//maka value ditambah suatu heuristic dari indeks baris to musuh
					value+=this.heuristicmusuh[7-(action.to()/8)];
				}
			}
			// kalau untuk sampai state ini yang melakukan move adalah player 1
			if(parent.getCurentNodePlayer()==1) {
				//maka value ditambah bobot heuristic dari posisi indeks baris terbaru
				value+=this.heuristic[action.to()/8];
				//dan value dikurangi bobot heuristic dari posisi indeks baris sebelumnya
				value-=this.heuristic[action.from()/8];
			}else {
				//maka value ditambah bobot heuristic dari posisi indeks baris musuh sebelumnya
				value+=this.heuristicmusuh[7-(action.from()/8)];
				//dan value dikurangi bobot heuristic dari posisi indeks baris musuh terbarunya
				value-=this.heuristicmusuh[7-(action.to()/8)];
			}	
		}
	}
	
	//method untuk menghitung value dari kalau alphanya player 2
	public void heuristicPlayerTwo() {
		//inisialisasi dengan mengambil value dari node parent
		this.value = parent.getValue();
		//ContainerState merupakan state yang menyimpan isi dari board(cell) dalam Library ludii
		//dalam kasus ini containernya adalah board papan catur knight through
		//ContainerState bisa ditemukan di https://github.com/Ludeme/LudiiExampleAI/blob/master/src/experiments/Tutorial.java
		//kita juga bisa menggunakan java decompiler untuk melihatnya
		//ContainerState disini bisa  kita gunakan sebagai state representation
		//Dibawah ini adalah iterator untuk mengakses container state
		for ( ContainerState containerState : this.parent.getState().state().containerStates())
		{
			//ini dari containerState setelah dilakukan move pada parent
			int isito=containerState.whatCell(action.to());
			//Kalau kuda hitam yang sudah sampai garis terminal (kuda hitam menang)
			if(action.to()/8==0) {
				//value diinisiasi dengan maximum value
				value=1000000000;
				return;
			}
			//Kalau kuda putih yang sudah sampai garis terminal (kuda putih menang)
			if(action.to()/8==7) {
				//value diinisiasi dengan minimum value
				value=-1000000000;
				return;
			}
			//kalau move parent memakan sebuah kuda
			if(isito!=0) {
				//kalau yang dimakan kuda putih
				if(isito==1) {
					//maka value ditambah suatu bobot heuristic dari index baris to musuh
					value+=this.heuristicmusuh[action.to()/8];
				//kalau yang dimakan kuda hitam
				}else if(isito==2) {
					//maka value dikurang suatu bobot heuristic dari index baris tonya
					value-=this.heuristic[7-(action.to()/8)];
				}
			}
			// kalau untuk sampai state ini yang melakukan move adalah player 1
			if(parent.getCurentNodePlayer()==1) {
				//maka value dikurangi bobot heuristic dari posisi indeks baris musuh terbaru
				value-=this.heuristicmusuh[action.to()/8];
				//maka value ditambah bobot heuristic dari posisi indeks baris musuh sebelumnya
				value+=this.heuristicmusuh[action.from()/8];
			}else {
				//maka value dikurangi bobot heuristic dari posisi indeks baris sebelumnya
				value-=this.heuristic[7-(action.from()/8)];
				//maka value ditambah bobot heuristic dari posisi indeks baris terbaru
				value+=this.heuristic[7-(action.to()/8)];
			}	
		}
	}
	// getter dari move yang dilakukan parent untuk sampai ke state ini
	public Move getAction() {
		return this.action;
	}
	// getter dari player yang dapat melakukan move di nodenya
	public int getCurentNodePlayer() {
		return currentPlayer;
	}
	//method untuk mengecek status apakah game sudah berakhir atau belum (Terminal state)
	public boolean isTerminal() {
		//Method yang disediakan Ludii untuk mengecek status dari context sekarang, apakah sudah berakhir
		if(this.state.trial().over())
			isTerminal=true;
		return isTerminal;
	}
	
	//method add children ke node ini
	public void addChildren(Node child) {
		//add children ke arraylist
		this.children.add(child);
	}
	
	//generate atau dapatkan semua move yang bisa dilakukan
	public void getAllPossibleMove() {
		// Membuat objek Game agar dapat meng generate move
		Game game = this.state.game();
		//inisialisasi sekaligus pengisian arraylist untuk move yang possible
		this.possibleMove = new FastArrayList<Move>(game.moves(state).moves());
	}
	//Getter dari possibleMove
	public FastArrayList<Move> getMoves() {
		return this.possibleMove;
	}
	// getter dari value(hasil dari evaluation function)
	public double getValue() {
		return value;
	}
	
	//getter dari context(state)
	public Context getState() {
		return state;
	}
	//Debugger tidak perlu diuncoment fungsinya adalah memprint semua isi state dan valuenya harus digunakan berbarengan dengan
	//https://github.com/Ludeme/LudiiTutorials/blob/master/src/ludii_tutorials/RunningTrials.java
	//atau RunningTrials yang disediakan
	/*
	public void print( double value) {
		for ( ContainerState containerState : state.state().containerStates())
		{
			for(int i = 0 ; i < 64 ; i ++) {
				System.out.print(containerState.whatCell(i));
				if(i%8==7) {
					System.out.println();
				}
			}
			System.out.println("value:"+value);
		}
	}
	*/	
}


/*
 * Class untuk membantu sorting move selection (cara mudah membungkus node agar bisa di sort)
 */
class BantuanSorting implements Comparable<BantuanSorting>{
	// Node yang dipakai
	public Node node;
	
	// constructor untuk Node yang dipakai
	BantuanSorting(Node node){
		// mengisi atribut node sesuai parameter mana node yang ingin dibungkus objek ini
		this.node=node;
	}
	
	@Override
	//Method untuk sortingnya (implementasi Comparable dalam java)
	public int compareTo(BantuanSorting o) {
		//kalau value objek node ini lebih besar dari objek node yang ada di parameter
		if(this.node.getValue()>o.node.getValue()) {
			//maka objek ini diletakan di sebelum (lebih depan) objek yang diparameter
			return -1;
		//kalau value objek node ini sama dari objek node yang ada di parameter
		}else if(this.node.getValue()==o.node.getValue()) {
			//maka objek ini tidak perlu diubah posisinya
			return 0;
		}else {
			//maka objek ini diletakan di setelah objek yang diparameter
			return 1;
		}
	}
	
}
