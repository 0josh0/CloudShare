package cn.ac.iscas.oncecloudshare.messaging.repository;

import org.springframework.stereotype.Repository;

@Repository
public class CommonDao {
	
//	private static String FIND_ALL_CONV_SEQ=
//			"SELECT LEAST(receiver,sender) AS userId, " +
//			"  GREATEST(receiver,sender) AS oppositeId, MAX(seq) AS maxSeq "+
//			"FROM ocs_msg.msg_im "+
//			"GROUP BY userId,oppositeId ";
//
//	@PersistenceContext
//	private EntityManager em;
//	
//	public List<Conversation> findAllConvSeq(){
////		Session session=em.unwrap(Session.class);
////		session.
//		List<Conversation> convList=Lists.newArrayList();
//		@SuppressWarnings ("unchecked")
//		List<Object[]> resultList=em.createNativeQuery(FIND_ALL_CONV_SEQ)
//				.getResultList();
//		LOOP:for(Object[] item:resultList){
//			for(int i=0;i<item.length;i++){
//				if(item[i]==null || 
//					!(item[i] instanceof BigInteger)){
//					continue LOOP;
//				}
//			}
//			convList.add(new Conversation(
//					((BigInteger)item[0]).longValue(),
//					((BigInteger)item[1]).longValue(),
//					((BigInteger)item[2]).longValue()));
//		}
//		return convList;
//	}
}
