package cn.ac.iscas.oncecloudshare.service.extensions.company.space.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.exceptions.SearchException;
import cn.ac.iscas.oncecloudshare.service.exceptions.filemeta.DuplicatePathException;
import cn.ac.iscas.oncecloudshare.service.exceptions.filemeta.InsufficientQuotaException;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.dao.CompanySpaceApplicationDao;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.dao.CompanySpaceDao;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.model.ApplicationStatus;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.model.CompanySpace;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.model.CompanySpaceApplication;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.model.UploadApplication;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.model.UploadVersionApplication;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFile;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFileVersion;
import cn.ac.iscas.oncecloudshare.service.service.common.SpaceService;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;

@Service
@Transactional(readOnly = true)
public class CompanySpaceService {
	@Resource
	private CompanySpaceDao companySpaceDao;
	@Resource
	private CompanySpaceApplicationDao companySpaceApplicationDao;
	@Resource
	private SpaceService spaceService;

	public CompanySpace findOne() {
		Iterable<CompanySpace> iterable = companySpaceDao.findAll();
		if (iterable == null) {
			return null;
		}
		List<CompanySpace> list = Lists.newArrayList(iterable);
		return list.size() > 0 ? list.get(0) : null;
	}

	@Transactional(readOnly = false)
	public synchronized CompanySpace create() {
		CompanySpace space = findOne();
		if (space == null) {
			space = new CompanySpace();
			space.setQuota(Long.MAX_VALUE);
			space.setRestQuota(Long.MAX_VALUE);
			space = spaceService.save(space);
			spaceService.makeBuildinFolders(space);
		}
		return space;
	}

	/**
	 * 用户上传文件
	 */
	@Transactional(readOnly = false, rollbackFor = Throwable.class)
	public SpaceFileVersion uploadFileByUser(User creator, SpaceFile parent, String name, ByteSource fileContent) throws IOException,
			IllegalArgumentException, InsufficientQuotaException, DuplicatePathException {
		// 将文件保存进临时文件夹
		SpaceFileVersion file = spaceService.saveTempFile(creator, parent.getOwner(), name, fileContent);
		// 生成上传文件申请
		createUploadApplication(creator, parent, file.getFile());
		return file;
	}

	/**
	 * 用户上传文件
	 */
	@Transactional(readOnly = false, rollbackFor = Throwable.class)
	public SpaceFileVersion uploadFileByUser(User creator, SpaceFile parent, String name, String md5) throws IOException, IllegalArgumentException,
			InsufficientQuotaException, DuplicatePathException {
		// 将文件保存进临时文件夹
		SpaceFileVersion file = spaceService.saveTempFile(creator, parent.getOwner(), name, md5);
		createUploadApplication(creator, parent, file.getFile());
		return file;
	}

	private CompanySpaceApplication createUploadApplication(User creator, SpaceFile parent, SpaceFile file) {
		// 生成上传文件申请
		UploadApplication application = new UploadApplication();
		application.setApplicant(creator);
		application.setUploadedFile(file);
		application.setTargetFolder(parent);
		application.setStatus(ApplicationStatus.TOREVIEW);
		companySpaceApplicationDao.save(application);
		return application;
	}

	/**
	 * 管理员上传文件
	 * 
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws DuplicatePathException
	 * @throws InsufficientQuotaException
	 */
	@Transactional(readOnly = false, rollbackFor = Throwable.class)
	public SpaceFileVersion uploadFileByAdmin(User creator, SpaceFile parent, String name, String md5) throws InsufficientQuotaException,
			DuplicatePathException, IllegalArgumentException, IOException {
		return spaceService.saveNewFile(creator, parent, name, md5, null);
	}

	/**
	 * 管理员上传文件
	 * 
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws DuplicatePathException
	 * @throws InsufficientQuotaException
	 */
	@Transactional(readOnly = false, rollbackFor = Throwable.class)
	public SpaceFileVersion uploadFileByAdmin(User creator, SpaceFile parent, String name, ByteSource fileContent) throws InsufficientQuotaException,
			DuplicatePathException, IllegalArgumentException, IOException {
		return spaceService.saveNewFile(creator, parent, name, null, fileContent);
	}

	@Transactional(readOnly = false, rollbackFor = Throwable.class)
	public SpaceFileVersion mergeTempFileFragmentByUser(User creator, SpaceFile folder, String name, List<Long> fragmentIds)
			throws InsufficientQuotaException, DuplicatePathException, IllegalArgumentException, IOException {
		SpaceFileVersion file = spaceService.mergeTempFileFragment(creator, spaceService.findTempDir(folder.getOwner().getId()), name, fragmentIds);
		createUploadApplication(creator, folder, file.getFile());
		return file;
	}

	@Transactional(readOnly = false, rollbackFor = Throwable.class)
	public SpaceFileVersion mergeTempFileFragmentByAdmin(User creator, SpaceFile folder, String name, List<Long> fragmentIds)
			throws InsufficientQuotaException, DuplicatePathException, IllegalArgumentException, IOException {
		return spaceService.mergeTempFileFragment(creator, folder, name, fragmentIds);
	}

	@Transactional(readOnly = false, rollbackFor = Throwable.class)
	public SpaceFileVersion uploadNewFileVersionByUser(User uploader, SpaceFile file, ByteSource fileContent) throws InsufficientQuotaException,
			DuplicatePathException, IllegalArgumentException, IOException {
		// 将文件保存进临时文件夹
		SpaceFileVersion tmpFile = spaceService.saveTempFile(uploader, file.getOwner(), file.getName(), fileContent);
		createUploadVersionApplication(uploader, file, tmpFile.getFile());
		return tmpFile;
	}

	@Transactional(readOnly = false, rollbackFor = Throwable.class)
	public SpaceFileVersion uploadNewFileVersionByUser(User uploader, SpaceFile file, String md5) throws InsufficientQuotaException,
			DuplicatePathException, IllegalArgumentException, IOException {
		// 将文件保存进临时文件夹
		SpaceFileVersion tmpFile = spaceService.saveTempFile(uploader, file.getOwner(), file.getName(), md5);
		createUploadVersionApplication(uploader, file, tmpFile.getFile());
		return tmpFile;
	}

	private CompanySpaceApplication createUploadVersionApplication(User creator, SpaceFile targetFile, SpaceFile uploadedFile) {
		// 生成上传文件申请
		UploadVersionApplication application = new UploadVersionApplication();
		application.setApplicant(creator);
		application.setUploadedFile(uploadedFile);
		application.setTargetFile(targetFile);
		application.setStatus(ApplicationStatus.TOREVIEW);
		companySpaceApplicationDao.save(application);
		return application;
	}

	public Page<CompanySpaceApplication> findApplications(List<SearchFilter> filters, Pageable pageable) {
		try {
			Specification<CompanySpaceApplication> spec = Specifications.fromFilters(filters, CompanySpaceApplication.class);
			return companySpaceApplicationDao.findAll(spec, pageable);
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}

	public CompanySpaceApplication findApplication(long applicationId) {
		return companySpaceApplicationDao.findOne(applicationId);
	}

	@Transactional(readOnly = false)
	public void reivewApplication(User reviewBy, UploadApplication application, boolean agreed, SpaceFile parent, String name, String message) {
		application.setReviewBy(reviewBy);
		application.setStatus(agreed ? ApplicationStatus.AGREED : ApplicationStatus.DISAGREED);
		application.setReviewTime(new Date());
		application.setReviewMessage(message);
		if (agreed) {
			Map<String, Object> map = ImmutableMap.<String, Object> of("parentId", parent.getId(), "name", name);
			application.setReviewContent(Gsons.defaultGsonNoPrettify().toJson(map));
		}
		companySpaceApplicationDao.save(application);

		if (agreed) {
			spaceService.move(application.getUploadedFile(), parent, name);
		}
	}

	@Transactional(readOnly = false, rollbackFor = Throwable.class)
	public void reivewApplication(User reviewBy, UploadVersionApplication application, Boolean agreed, String message) throws InsufficientQuotaException, IOException {
		application.setReviewBy(reviewBy);
		application.setStatus(agreed ? ApplicationStatus.AGREED : ApplicationStatus.DISAGREED);
		application.setReviewTime(new Date());
		application.setReviewMessage(message);
		companySpaceApplicationDao.save(application);

		if (agreed) {
			spaceService.saveNewFileVersion(application.getApplicant(), application.getTargetFile(), application.getUploadedFile().getHeadVersion()
					.getMd5(), null);
		}
	}
}
