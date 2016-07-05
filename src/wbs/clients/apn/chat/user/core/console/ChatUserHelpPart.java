package wbs.clients.apn.chat.user.core.console;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import wbs.clients.apn.chat.help.model.ChatHelpLogRec;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.TimeFormatter;
import wbs.framework.utils.etc.Html;
import wbs.sms.message.core.console.MessageConsoleLogic;

@PrototypeComponent ("chatUserHelpPart")
public
class ChatUserHelpPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	MessageConsoleLogic messageConsoleLogic;

	@Inject
	TimeFormatter timeFormatter;

	// state

	ChatUserRec chatUser;

	Set<ChatHelpLogRec> chatHelpLogs;

	// implementation

	@Override
	public
	void prepare () {

		chatUser =
			chatUserHelper.findOrNull (
				requestContext.stuffInt ("chatUserId"));

		chatHelpLogs =
			new TreeSet<ChatHelpLogRec> (
				chatUser.getChatHelpLogs ());

	}

	@Override
	public
	void renderHtmlBodyContent () {

		String link =
			requestContext.resolveLocalUrl (
				"/chatUser.helpForm");

		printFormat (
			"<p><button onclick=\"%h\">send message</button></p>\n",
			stringFormat (
				"top.frames['inbox'].location='%j';",
				link));

		if (chatHelpLogs.size () == 0) {

			printFormat (
				"<p>No history to display.</p>\n");

			return;

		}

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>&nbsp;</th>\n",
			"<th>Time</th>\n",
			"<th>Message</th>\n",
			"<th>Our number</th>\n",
			"<th>User</th>\n",
			"</tr>\n");

		LocalDate previousDate = null;

		DateTimeZone timezone =
			chatUserLogic.timezone (
				chatUser);

		for (
			ChatHelpLogRec chatHelpLog
				: chatHelpLogs
		) {

			LocalDate nextDate =
				chatHelpLog.getTimestamp ()

				.toDateTime (
					timezone)

				.toLocalDate ();

			if (
				notEqual (
					nextDate,
					previousDate)
			) {

				previousDate =
					nextDate;

				printFormat (
					"<tr class=\"sep\">\n");

				printFormat (
					"<tr style=\"font-weight: bold\">\n",

					"<td colspan=\"5\">%h</td>\n",
					timeFormatter.dateStringLong (
						chatUserLogic.timezone (
							chatUser),
						chatHelpLog.getTimestamp ()),

					"</tr>\n");

			}

			String rowClass =
				messageConsoleLogic.classForMessageDirection (
					chatHelpLog.getDirection ());

			printFormat (
				"<tr class=\"%h\">\n",
				rowClass);

			printFormat (
				"<td style=\"background: %h\">&nbsp;</td>\n",
				Html.genHtmlColor (
					ifNull (
						chatHelpLog.getOurNumber (),
						0)));

			printFormat (
				"<td>%h</td>\n",
				timeFormatter.timeString (
					chatUserLogic.timezone (
						chatUser),
					chatHelpLog.getTimestamp ()));

			printFormat (
				"<td>%h</td>\n",
				chatHelpLog.getText ());

			printFormat (
				"<td>%h</td>\n",
				chatHelpLog.getOurNumber ());

			printFormat (
				"<td>%h</td>\n",
				chatHelpLog.getUser () == null
					? ""
					: chatHelpLog.getUser ().getUsername ());

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
