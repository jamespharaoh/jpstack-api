package wbs.clients.apn.chat.user.image.console;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.toEnum;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.google.common.base.Optional;

import lombok.Cleanup;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageType;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.media.logic.MediaLogic;

@PrototypeComponent ("chatUserImageListAction")
public
class ChatUserImageListAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	Database database;

	@Inject
	MediaLogic mediaLogic;

	@Inject
	ConsoleRequestContext requestContext;

	// details

	@Override
	public
	Responder backupResponder () {

		ChatUserImageType type =
			toEnum (
				ChatUserImageType.class,
				(String) requestContext.stuff (
					"chatUserImageType"));

		return responder (
			stringFormat (
				"chatUser%sListResponder",
				capitalise (type.toString ())));

	}

	@Override
	public
	Responder goReal () {

		String notice = null;

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		ChatUserRec chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		ChatUserImageType type =
			toEnum (
				ChatUserImageType.class,
				(String) requestContext.stuff ("chatUserImageType"));

		List<ChatUserImageRec> list =
			chatUserLogic.getChatUserImageListByType (
				chatUser,
				type);

		for (
			String key
				: requestContext.parameterMap ().keySet ()
		) {

			Matcher matcher =
				keyPattern.matcher (key);

			if (! matcher.matches ())
				continue;

			String command =
				matcher.group (1);

			int index =
				Integer.parseInt (matcher.group (2));

			if (index >= list.size ())
				throw new RuntimeException ();

			if (equal (
					command,
					"remove")) {

				if (
					equal (
						list.get (index),
						chatUserLogic.getMainChatUserImageByType (
							chatUser,
							type))
				) {

					chatUserLogic.setMainChatUserImageByType (
						chatUser,
						type,
						null);

				}

				list.get (index).setIndex (null);

				for (int otherIndex = index + 1;
						otherIndex < list.size ();
						otherIndex++) {

					list.get (otherIndex).setIndex (otherIndex - 1);

				}

				notice = "Image/video removed";

			}

			if (
				notEqual (
					type,
					ChatUserImageType.video)
				&& in (
					command,
					"rotate_cw",
					"rotate_ccw")
			) {

				ChatUserImageRec chatUserImage =
					list.get (index);

				BufferedImage smallImage =
					mediaLogic.getImage (
						chatUserImage.getMedia ());

				BufferedImage fullImage =
					mediaLogic.getImage (
						chatUserImage.getFullMedia ());

				if (equal (command, "rotate_ccw")) {

					smallImage =
						mediaLogic.rotateImage270 (smallImage);

					fullImage =
						mediaLogic.rotateImage270 (fullImage);

				}

				if (equal (command, "rotate_cw")) {

					smallImage =
						mediaLogic.rotateImage90 (smallImage);

					fullImage =
						mediaLogic.rotateImage90 (fullImage);

				}

				String filename =
					chatUser.getCode () + ".jpg";

				chatUserImage.setMedia (
					mediaLogic.createMediaFromImage (
						smallImage,
						"image/jpeg",
						filename));

				chatUserImage.setFullMedia (
					mediaLogic.createMediaFromImage (
						fullImage,
						"image/jpeg",
						filename));

				notice =
					"Image/video rotated";

			}

			if (
				in (
					command,
					"move_up",
					"move_down")
			) {

				int diff =
					equal (command, "move_up") ? -1 : 1;

				int otherIndex =
					(index + list.size () + diff) % list.size ();

				ChatUserImageRec thisImage =
					list.get (index);

				ChatUserImageRec otherImage =
					list.get (otherIndex);

				thisImage.setIndex (null);
				otherImage.setIndex (null);

				transaction.flush ();

				thisImage.setIndex (otherIndex);
				otherImage.setIndex (index);

				notice = "Image/video moved";

			}

			if (in (command, "select")) {

				ChatUserImageRec chatUserImage =
					list.get (index);

				if (
					equal (
						chatUserLogic.getMainChatUserImageByType (
							chatUser,
							type),
						chatUserImage)
				) {

					chatUserLogic.setMainChatUserImageByType (
						chatUser,
						type,
						Optional.<ChatUserImageRec>absent ());

					notice =
						"Image unselected";

				} else {

					chatUserLogic.setMainChatUserImageByType (
						chatUser,
						type,
						Optional.of (
							chatUserImage));

					notice =
						"Image selected";

				}

			}

		}

		transaction.commit ();

		if (notice != null)
			requestContext.addNotice (notice);

		return null;

	}

	static
	Pattern keyPattern =
		Pattern.compile (
			"(remove|rotate_ccw|rotate_cw|move_up|move_down|select)_([0-9]+)");

}
