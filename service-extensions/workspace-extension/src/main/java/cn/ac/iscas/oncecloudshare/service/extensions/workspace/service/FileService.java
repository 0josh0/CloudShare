package cn.ac.iscas.oncecloudshare.service.extensions.workspace.service;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.application.dto.ReviewApplication;
import cn.ac.iscas.oncecloudshare.service.application.model.Application;
import cn.ac.iscas.oncecloudshare.service.application.service.ApplicationHandlerAdapter;
import cn.ac.iscas.oncecloudshare.service.application.service.ApplicationService;
import cn.ac.iscas.oncecloudshare.service.dao.common.SpaceFileDao;
import cn.ac.iscas.oncecloudshare.service.dao.filemeta.FileCommentDao;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.file.FileCommentCreateDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dao.WorkspaceUploadApplicationDao;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dao.WorkspaceUploadVersionApplicationDao;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.ReviewUpload;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.UploadFile;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.UploadFileVersion;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WorkspaceUploadApplication;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WorkspaceUploadVersionApplication;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.account.UserStatus;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFile;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileComment;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileStatus;
import cn.ac.iscas.oncecloudshare.service.service.account.UserService;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.service.common.SpaceService;
import cn.ac.iscas.oncecloudshare.service.service.common.TempFileStorageService;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Service("workspaceFileService")
@Transactional(readOnly = true)
public class FileService {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

	@Resource
	private SpaceService spaceService;
	@Resource
	private ApplicationService applicationService;
	@Resource
	private TempFileStorageService tfsService;
	@Resource(name = "globalConfigService")
	private ConfigService<?> configService;
	@Resource
	private WorkspaceUploadApplicationDao workspaceUploadApplicationDao;
	@Resource
	private WorkspaceUploadVersionApplicationDao workspaceUploadVersionApplicationDao;
	@Resource
	private UserService userService;
	@Resource
	private FileCommentDao commentDao;
	@Resource
	private SpaceFileDao spaceFileDao;

	@PostConstruct
	public void init() {
		// 上传文件申请
		applicationService.addApplicationDao(WorkspaceUploadApplication.class, workspaceUploadApplicationDao);
		applicationService.addApplicationHandler(new ApplicationHandlerAdapter() {
			@Override
			public boolean canHandle(Application application) {
				return application instanceof WorkspaceUploadApplication;
			}
			
			@Override
			public void preReview(Application application, ReviewApplication review, User master) {
				if (review.getAgreed()) {
					ReviewUpload uploadReview = (ReviewUpload) review;
					UploadFile applyContent = application.getContentObject(UploadFile.class);
					long parentId = uploadReview.getParentId() == null ? applyContent.getParentId() : uploadReview.getParentId();
					SpaceFile parent = spaceService.findFolder(parentId);
					if (parent == null || !FileStatus.HEALTHY.equals(parent.getStatus())) {
						throw new RestException(ErrorCode.FILE_NOT_FOUND);
					}
					String name = Strings.isNullOrEmpty(uploadReview.getName()) ? applyContent.getName() : uploadReview.getName();
					try {
						if (!Strings.isNullOrEmpty(applyContent.getFileMd5())) {
							spaceService.saveNewFile(application.getApplyBy(), parent, name, applyContent.getFileMd5(), null);
						} else if (!Strings.isNullOrEmpty(applyContent.getTfKey())) {
							spaceService.saveNewFile(application.getApplyBy(), parent, name, null, tfsService.getTempFile(applyContent.getTfKey()));
						}
					} catch (Exception e) {
						if (e instanceof RuntimeException) {
							throw (RuntimeException) e;
						}
						LOGGER.error(null, e);
						throw new RuntimeException(e);
					}
				}
			}
		});
		// 上传新版本申请
		applicationService.addApplicationDao(WorkspaceUploadVersionApplication.class, workspaceUploadVersionApplicationDao);
		applicationService.addApplicationHandler(new ApplicationHandlerAdapter() {
			@Override
			public boolean canHandle(Application application) {
				return application instanceof WorkspaceUploadVersionApplication;
			}
			
			@Override
			public void preReview(Application application, ReviewApplication review, User master) {
				if (review.getAgreed()) {
					UploadFileVersion applyContent = application.getContentObject(UploadFileVersion.class);
					SpaceFile file = spaceService.findFile(applyContent.getFileId());
					if (file == null || !FileStatus.HEALTHY.equals(file.getStatus())) {
						throw new RestException(ErrorCode.FILE_NOT_FOUND);
					}
					try {
						if (!Strings.isNullOrEmpty(applyContent.getFileMd5())) {
							spaceService.saveNewFileVersion(application.getApplyBy(), file, applyContent.getFileMd5(), null);
						} else if (!Strings.isNullOrEmpty(applyContent.getTfKey())) {
							spaceService.saveNewFileVersion(application.getApplyBy(), file, null, tfsService.getTempFile(applyContent.getTfKey()));
						}
					} catch (Exception e) {
						if (e instanceof RuntimeException) {
							throw (RuntimeException) e;
						}
						LOGGER.error(null, e);
						throw new RuntimeException(e);
					}
				}
			}
		});
	}

	/**
	 * 添加评论
	 * 
	 * @param creater
	 * @param file
	 * @param creation
	 */
	@Transactional(readOnly = false)
	public FileComment addComment(User creater, SpaceFile file, FileCommentCreateDto creation) {
		List<User> at = null;
		if (ArrayUtils.isNotEmpty(creation.getAt())) {
			List<SearchFilter> filters = Lists.newArrayList(new SearchFilter("id", Operator.IN, creation.getAt()));
			filters.add(new SearchFilter("status", Operator.EQ, UserStatus.ACTIVE));
			at = userService.findAll(filters);
			at.remove(creater);
		}
		FileComment comment = commentDao.save(new FileComment(creater, file, creation.getContent(), at));
		return comment;
	}

	/**
	 * 获取评论列表
	 * 
	 * @param filters
	 * @param pageable
	 * @return
	 */
	public Page<FileComment> commentList(List<SearchFilter> filters, Pageable pageable) {
		return commentDao.findAll(Specifications.fromFilters(filters, FileComment.class), pageable);
	}
	
	@Transactional(readOnly = false)
	public int incrDownloads(SpaceFile file, long increment) {
		Preconditions.checkNotNull(file);
		return spaceFileDao.incrDownloads(file.getId(), increment);
	}
}
