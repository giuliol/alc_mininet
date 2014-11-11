package dsp.unige.ALC.utils;

import net.fec.openrq.ArrayDataEncoder;
import net.fec.openrq.EncodingPacket;
import net.fec.openrq.OpenRQ;
import net.fec.openrq.encoder.DataEncoder;
import net.fec.openrq.encoder.SourceBlockEncoder;
import net.fec.openrq.parameters.FECParameters;

public class ORQcore {



	/**
     * Returns an encoder for data inside an array of bytes.
     * 
     * @param data
     *            An array of bytes
     * @param fecParams
     *            FEC parameters associated to the encoded data
     * @return an instance of <code>ArrayDataEncoder</code>
     */
    public static ArrayDataEncoder getEncoder(byte[] data, FECParameters fecParams) {

//    	System.out.println("ORQcore.getEncoder() total symbols " +fecParams.totalSymbols());
        return OpenRQ.newEncoder(data, fecParams);
    }

    /**
     * Returns an encoder for data inside an array of bytes.
     * 
     * @param data
     *            An array of bytes
     * @param off
     *            The starting index of the data
     * @param fecParams
     *            FEC parameters associated to the encoded data
     * @return an instance of <code>ArrayDataEncoder</code>
     */
    public static ArrayDataEncoder getEncoder(byte[] data, int off, FECParameters fecParams) {

        return OpenRQ.newEncoder(data, off, fecParams);
    }
    
//    
//    /**
//     * Encodes all source blocks from a data encoder, sequentially.
//     * 
//     * @param dataEnc
//     *            A data encoder
//     */
//    public static void encodeData(DataEncoder dataEnc, int nr) {
//
//        for (SourceBlockEncoder sbEnc : dataEnc.sourceBlockIterable()) {
//            encodeSourceBlock(sbEnc,nr);
//        }
//    }

    /**
     * Encodes a specific source block from a data encoder.
     * 
     * @param dataEnc
     *            A data encoder
     * @param sbn
     *            A "source block number": the identifier of the source block to be encoded
     */
    
    public static byte[][] encodeBlock(DataEncoder dataEnc, int sbn,int nr) {

        SourceBlockEncoder sbEnc = dataEnc.sourceBlock(sbn);
        return encodeSourceBlock(sbEnc,nr);
    }

    private static byte[][] encodeSourceBlock(SourceBlockEncoder sbEnc, int nr) {

    	int i=0;
        byte[][]pkts = new byte [sbEnc.numberOfSourceSymbols()+nr][];

        // send all source symbols
        for (EncodingPacket pac : sbEnc.sourcePacketsIterable()) {
        	pkts[i++]=pac.asArray();
//            sendPacket(pac,i++);
        }

//        // number of repair symbols
//        int nr = numberOfRepairSymbols();

        // send nr repair symbols
        for (EncodingPacket pac : sbEnc.repairPacketsIterable(nr)) {
        	pkts[i++]=pac.asArray();
//            sendPacket(pac,i++);
        }  
        return pkts;
    
    }
    
//    private static int numberOfRepairSymbols() {
//        // return a number of repair symbols to encode
//        // (e.g. the number may depend on a channel loss rate)
//    	return REP_SYM;
//    }
    
    
    
//    private static void sendPacket(EncodingPacket pac, int n) {
//    	
//        // send the packet to the receiver
//    	pkts[n]=pac.asArray();
//    }
    
//    static byte [][] pkts =  new byte [REP_SYM*2][];
    
//    public static byte[][] getPacketsArray() {
//		// TODO Auto-generated method stub
//    	return pkts;
//
//	}
    
    
    
    
}