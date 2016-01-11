package cn.ac.iscas.oncecloudshare.service.utils.http;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 * 为了支持http Range把range封装成一个类，包括开始byte和结束byte
 * @author zhangxiaojie
 *
 */
public class ByteRange {
	private long startByte;
	private long endByte;
	public ByteRange(long startByte,long endByte) {
		this.startByte = startByte;
		this.endByte = endByte;
	}
	@Override
	public boolean equals(Object obj) {
		if(obj == this){
			return true;
		}
		if(obj instanceof ByteRange){
			ByteRange t = (ByteRange)obj;
			EqualsBuilder builder = new EqualsBuilder();
			builder.append(this.startByte, t.startByte);
			builder.append(this.endByte, t.endByte);
			return builder.isEquals();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		return builder.append(startByte).append(endByte).toHashCode();
	}

	public long getStartByte() {
		return startByte;
	}

	public void setStartByte(long startByte) {
		this.startByte = startByte;
	}

	public long getEndByte() {
		return endByte;
	}

	public void setEndByte(long endByte) {
		this.endByte = endByte;
	}
	@Override
	public String toString() {
		return "range: ["+startByte + " "+endByte + "]";
	}
	
	/**
	 * 传入的格式可能情况：
	 *  - The first 500 bytes (byte offsets 0-499, inclusive):  bytes=0-
     *   499
     * - The second 500 bytes (byte offsets 500-999, inclusive):
     *    bytes=500-999
     * - The final 500 bytes (byte offsets 9500-9999, inclusive):
     *   bytes=-500
     * - Or bytes=9500-
	 * @param range
	 * @return
	 */
	public static ByteRange getRangeInstance(String range,long iTotal){
		if(range.startsWith("bytes=")){
			range = range.substring(6) + " ";
			String[] point = range.split("-");
			long startIndex = 0 ,endIndex = iTotal-1;
			
			if(point.length == 2){
				if(!point[0].trim().isEmpty()){
					startIndex = (long)Long.parseLong(point[0].trim());
					if(!point[1].trim().isEmpty()){
						endIndex = (long)Long.parseLong(point[1].trim());
					}
				}else if(!point[1].trim().isEmpty()){
					startIndex = iTotal - (long)Long.parseLong(point[1].trim());
				}
				if(endIndex>iTotal){
					endIndex = iTotal-1;
				}
				if(startIndex<=endIndex && endIndex<iTotal && startIndex>=0){
					return	new ByteRange(startIndex,endIndex); 
				}
			}
		}
		throw new IllegalArgumentException();
	}
	
	/**
	 * 将重叠的range数组merge起来
	 * @param ranges
	 */
	public static void mergeRange(List<ByteRange> ranges){
		Collections.sort(ranges, new Comparator<ByteRange>(){
			@Override
			public int compare(ByteRange o1, ByteRange o2) {
				if(o1.getStartByte()<o2.getStartByte()){
					return -1;
				}else if(o1.getStartByte()>o2.getStartByte()){
					return 1;
				}else{
					if(o1.getEndByte()<o2.getEndByte()){
						return -1;
					}else if(o1.getEndByte()>o2.getEndByte()){
						return 1;
					}else{
						return 0;
					}
				}
			}
		} );
		
		for(int i=0;i<ranges.size();i++){
			ByteRange cur = ranges.get(i); 
			for(int j=i+1;j<ranges.size();j++){
				ByteRange next = ranges.get(j);
				if(cur.getEndByte()>next.getStartByte()){
					if(cur.getEndByte()>next.getEndByte()){
						ranges.remove(j);
					}else{
						next.setStartByte(cur.getEndByte());
					}
				}
			}
		}
		
		for(int i=0;i<ranges.size();i++){
			for(int j=i+1;j<ranges.size();){
				ByteRange first = ranges.get(j-1);
				ByteRange next = ranges.get(j);
				if(first.getEndByte() == next.getStartByte()){
					first.setEndByte(next.getEndByte());
					ranges.remove(j);
				}else{
					break;
				}
			}
		}
	}
	public static void main(String args[]){
		/*List<ByteRange> rangeList = new ArrayList<ByteRange>();
		rangeList.add(new ByteRange(1,22));
		//rangeList.add(new ByteRange(11,67));
		//rangeList.add(new ByteRange(11,68));
		System.out.println("=======before merge ============");
		for(ByteRange r : rangeList){
			System.out.println(r);
		}
		mergeRange(rangeList);
		System.out.println("=======after merge ============");
		for(ByteRange r : rangeList){
			System.out.println(r);
		}*/
	}
}
