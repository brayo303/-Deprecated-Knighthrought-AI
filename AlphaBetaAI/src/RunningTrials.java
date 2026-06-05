
import java.util.ArrayList;
import java.util.List;

import game.Game;
import util.AI;
import util.Context;
import util.GameLoader;
import util.Trial;
import util.model.Model;


// class untuk running silahkan tidak digunakan / export menjadi jar apabila tidak diperlukan adanya debuggin menggunakan println
// diambil dan dimodifikasi dari https://github.com/Ludeme/LudiiTutorials/blob/master/src/ludii_tutorials/RunningTrials.java
// kami tidak banyak memberi komen komen disini karena sebenarnya membuat ini hanya untuk debugging saja.
public class RunningTrials
{
	
	
	private static final int NUM_TRIALS = 1;
	public static void main(final String[] args)
	{
		
		final Game game = GameLoader.loadGameFromName("Knightthrough.lud");
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		final List<AI> ais = new ArrayList<AI>();
		ais.add(null);
		for (int p = 1; p <= game.players().count(); ++p)
		{
			// nama ai yang ingin kita jalankan 
			ais.add(new AlphaBetaAI());
		}
		
		
		for (int i = 0; i < NUM_TRIALS; ++i)
		{
			
			game.start(context);
			System.out.println("Starting a new trial!");
			for (int p = 1; p <= game.players().count(); ++p)
			{
				ais.get(p).initAI(game, p);
			}
			final Model model = context.model();
			
			while (!trial.over())
			{
				model.startNewStep(context, ais, 1.0);
				System.out.println("===move mark====");
				
			}
			
			
			final double[] ranking = trial.ranking();
			
			for (int p = 1; p <= game.players().count(); ++p)
			{
				System.out.println("Agent " + context.state().playerToAgent(p) + " achieved rank: " + ranking[p]);
			}
			System.out.println();
		}
	}

}