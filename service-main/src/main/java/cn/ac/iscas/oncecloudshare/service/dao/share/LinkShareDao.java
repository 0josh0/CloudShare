package cn.ac.iscas.oncecloudshare.service.dao.share;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.filemeta.File;
import cn.ac.iscas.oncecloudshare.service.model.share.LinkShare;

public interface LinkShareDao extends PagingAndSortingRepository<LinkShare, Long>, JpaSpecificationExecutor<LinkShare> {
	/**
	 * 根据外链中的key 查询出LinkShare 对象
	 * 
	 * @param key
	 * @return
	 */
	@Query("select es from LinkShare as es where es.key = ?1")
	public LinkShare findByKey(String key);

	/**
	 * 更新外链下载次数
	 * 
	 * @param externalId
	 */
	@Modifying
	@Query("update LinkShare es set es.downloads = es.downloads + 1 where es.key = ?1")
	public void updateDownloads(String key);

	/**
	 * 获取外链共享的列表
	 * 
	 * @param userId
	 * @param pagable
	 * @return
	 */
	@Query("select es from LinkShare as es join es.owner as u where u.id = ?1")
	public Page<LinkShare> findByUserId(Long userId, Pageable pagable);

	/**
	 * 获取被外链共享的文件列表
	 * 
	 * @param userId
	 * @param pageable
	 * @return
	 */
	@Query("select f from File as f where f.id in (select distinct es.file.id from LinkShare as es where es.owner.id = ?1)")
	public Page<File> findShareFilesByUserId(Long userId, Pageable pageable);

	/**
	 * 获取某个文件的外链列表
	 * 
	 * @param userId
	 * @param fileId
	 * @param pageable
	 * @return
	 */
	@Query("select es from LinkShare as es where es.owner.id = ?1 and es.file.id = ?2")
	public Page<LinkShare> findSharesByUserIdAndFileId(Long userId, Long fileId, Pageable pageable);

}
