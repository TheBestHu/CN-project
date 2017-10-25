import java.io.IOException;
import java.util.BitSet;
import java.util.Random;

public class ChooseState {
	
	
	void TypeChoosing(ActualMessage message) throws IOException{
		if(message.Message_type == 0) {
			ChokeProcess();
		}
		else if(message.Message_type == 1) {
			UnchokeProcess();
		}
		else if(message.Message_type ==2) {
			InterestProcess();
		}
		else if(message.Message_type ==3) {
			NotInterestProcess();
		}
		else if(message.Message_type ==4) {
			HaveProcess();
		}
		else if(message.Message_type ==5) {
			BitfieldProcess();
		}
		else if(message.Message_type ==6) {
			RequestProcess();
		}
		else if(message.Message_type ==7) {
			PieceProcess();
		}
			
	}

	private void ChokeProcess() throws IOException{
		
	}
	
	private void UnchokeProcess() throws IOException{
		/*
		 *random choose a piece not have send request（peer ID, piece index）
		 */
	}
	
	private void InterestProcess() throws IOException{
		/*
		 * update list that interested self
		 */
	}
	
	private void NotInterestProcess() throws IOException{
		/*
		 * 
		 */
	}
	
	private void HaveProcess() throws IOException{
		/*
		 * update corresponding bitfield
		 * check bitfield if any interesting piece send interest
		 * else send not interest
		 */
	}
	
	private void BitfieldProcess() throws IOException{
		/*
		 * establish a list reserve all peer connected peer ID and bitfield 
		 * first connect compare bitfield to decide send interest/not interest 
		 */
	}
	
	private void RequestProcess() throws IOException{
		/*
		 * check if choked
		 * send piece (Peer ID, piece , content)
		 */
	}
	
	private void PieceProcess() throws IOException{
		/*
		 * update own  bitfield
		 * send 'have' to all neighbors
		 */
	}
	
}
