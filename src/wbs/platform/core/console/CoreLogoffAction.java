package wbs.platform.core.console;

import javax.inject.Inject;

import lombok.Cleanup;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.logic.UserLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("coreLogoffAction")
public
class CoreLogoffAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	UserObjectHelper userHelper;

	@Inject
	UserConsoleLogic userConsoleLogic;

	@Inject
	UserLogic userLogic;

	// details

	@Override
	public
	Responder backupResponder () {

		return responder (
			"coreRedirectResponder");

	}

	// implementation

	@Override
	public
	Responder goReal () {

		if (! userConsoleLogic.loggedIn ()) {
			return null;
		}

		Long userId =
			userConsoleLogic.userIdRequired ();

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		UserRec user =
			userHelper.findOrNull (
				userId);

		userLogic.userLogoff (
			user);

		transaction.commit ();

		return null;

	}

}
