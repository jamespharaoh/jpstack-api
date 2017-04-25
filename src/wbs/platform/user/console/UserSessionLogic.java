package wbs.platform.user.console;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.utils.string.StringUtils.stringToUtf8;

import java.io.Serializable;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.apache.commons.lang3.SerializationUtils;

import wbs.console.request.ConsoleRequestContext;
import wbs.console.session.UserSessionVerifyLogic;

import wbs.framework.logging.TaskLogger;

import wbs.platform.user.model.UserRec;
import wbs.platform.user.model.UserSessionRec;

import wbs.utils.string.StringUtils;

public
interface UserSessionLogic
	extends UserSessionVerifyLogic {

	UserSessionRec userLogon (
			TaskLogger parentTaskLogger,
			ConsoleRequestContext requestContext,
			UserRec user,
			Optional <String> userAgent,
			Optional <String> consoleDeploymentCode);

	Optional <UserSessionRec> userLogonTry (
			TaskLogger parentTaskLogger,
			ConsoleRequestContext requestContext,
			String sliceCode,
			String username,
			String password,
			Optional <String> userAgent,
			Optional <String> consoleDeploymentCode);

	void userLogoff (
			TaskLogger parentTaskLogger,
			UserRec user);

	boolean userSessionVerify (
			TaskLogger parentTaskLogger,
			ConsoleRequestContext requestContext);

	Optional <byte[]> userData (
			UserRec user,
			String code);

	default
	byte[] userDataRequired (
			@NonNull UserRec user,
			@NonNull String code) {

		return optionalGetRequired (
			userData (
				user,
				code));

	}

	void userDataStore (
			TaskLogger taskLogger,
			UserRec user,
			String code,
			byte[] value);

	void userDataRemove (
			TaskLogger parentTaskLogger,
			UserRec user,
			String code);

	Optional <Serializable> userDataObject (
			TaskLogger parentTaskLogger,
			UserRec user,
			String code);

	default
	Serializable userDataObjectRequired (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull UserRec user,
			@NonNull String code) {

		return optionalGetRequired (
			userDataObject (
				parentTaskLogger,
				user,
				code));

	}

	default
	void userDataObjectStore (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull UserRec user,
			@NonNull String code,
			@NonNull Serializable value) {

		userDataStore (
			parentTaskLogger,
			user,
			code,
			SerializationUtils.serialize (
				value));

	}

	default
	Optional <String> userDataString (
			@NonNull UserRec user,
			@NonNull String code) {

		return optionalMapRequired (
			userData (
				user,
				code),
			StringUtils::utf8ToString);

	}

	default
	String userDataStringRequired (
			@NonNull UserRec user,
			@NonNull String code) {

		return optionalGetRequired (
			userDataString (
				user,
				code));

	}

	default
	void userDataStringStore (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull UserRec user,
			@NonNull String code,
			@NonNull String value) {

		userDataStore (
			parentTaskLogger,
			user,
			code,
			stringToUtf8 (
				value));

	}

}
