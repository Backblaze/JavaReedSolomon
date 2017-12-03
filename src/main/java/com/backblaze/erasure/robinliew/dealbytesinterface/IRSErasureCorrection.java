package com.backblaze.erasure.robinliew.dealbytesinterface;
/**
 * Codec interface of RS erasure checking algorithm(RS纠删校验算法编解码器接口)
 * @author RobinLiew RobinLiew 2017.9.21
 *
 */
public interface IRSErasureCorrection {
	/**
	 * 编码
	 * @param srcBuffer Original data that needs to be erasure(需要进行纠删编码的原始数据)
	 * @param sliceLength The length of the file in the file block (the length of the file is consistent)(文件块中文件片长度（文件片的长度保持一致）)
	 * @param sliceCount The number of files in a file block(文件块中文件片的数量)
	 * @param fecSliceCount The number of pieces of erasure check in a file block(文件块内纠删校验的片的数量)
	 * @return The return value is the check data(返回值是校验数据)
	 */
	public byte[] rs_Encoder(byte[] srcBuffer,int sliceLength,int sliceCount,int fecSliceCount);
	/**
	 * 解码
	 * @param srcEraseBuff Received file blocks (including raw data and erasure check data)(接收到的文件块（包括原始数据和纠删校验数据）)
	 * @param sliceLen The length of the file in a file block(文件块中文件片的长度)，
	 * @param sliceCount The number of files in a file block(文件块中文件片的数量)
	 * @param rsSliceCount The number of RS erasure check pieces in a file block(文件块内rs纠删校验片的数量)
	 * @param eraserFlag Erase the image, the array length is sliceCount+rsSliceCount, the true element represents the file pieces without being erased, 
	 * 			false indicates that the file was wipe out(擦除样图，数组长度为sliceCount+rsSliceCount,其中元素true表示文件片未被擦除，false表示文件片被擦除)
	 * @return If the return value is 0 on behalf of success, 
	 * 			that piece of data by right or wipe out the number of pieces in the allowable range of file transmission in the process, 
	 * 			at the same time to write the original data deleted after srcEraseBuff correction; 
	 * 			if non zero represents no success, which shows that the number of wipe out more than RS erasure ability
	 *			(返回值如果是0代表成功，说明在传输过程中文件块数据正确或经擦出的片数在容许范围内，同时把纠删后的原始数据写入srcEraseBuff；
	 * 			如果非零代表不成功，说明经擦出的片数超过了RS的纠删能力)
	 */
	public int rs_Decoder(byte[] srcEraseBuff,int sliceLen,int sliceCount,int rsSliceCount,boolean[] eraserFlag);
}
