package cn.ac.iscas.oncecloudshare.service.dto.contact;

import cn.ac.iscas.oncecloudshare.service.application.dto.ApplicationDto;
import cn.ac.iscas.oncecloudshare.service.dto.account.UserDto;
import cn.ac.iscas.oncecloudshare.service.model.contact.ContactApplication;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

public class ContactApplicationDto extends ApplicationDto {
	public UserDto contact;

	public static final Function<ContactApplication, ContactApplicationDto> DEFAULT_TRANSFORMER = new Function<ContactApplication, ContactApplicationDto>() {
		public ContactApplicationDto apply(ContactApplication input) {
			Preconditions.checkNotNull(input);
			ContactApplicationDto output = defaultInit(input, new ContactApplicationDto());
			if (input.getContact() != null) {
				output.contact = UserDto.GLANCE_TRANSFORMER.apply(input.getContact());
			}
			return output;
		}
	};
}