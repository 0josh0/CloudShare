package cn.ac.iscas.oncecloudshare.service.dto.contact;

import cn.ac.iscas.oncecloudshare.service.dto.account.UserDto;
import cn.ac.iscas.oncecloudshare.service.model.contact.Contact;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

public class ContactDto {
	public long id;
	public UserDto contact;
	
	public static final Function<Contact, ContactDto> DEFAULT_TRANSFORMER = new Function<Contact, ContactDto>() {
		@Override
		public ContactDto apply(Contact input) {
			Preconditions.checkNotNull(input);
			ContactDto output = new ContactDto();
			output.id = input.getId();
			if (input.getContact() != null){
				output.contact = UserDto.ANON_TRANSFORMER.apply(input.getContact());
			}
			return output;
		}
	};
}