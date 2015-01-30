package wbs.sms.keyword.daemon;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.core.logic.KeywordFinder;
import wbs.sms.keyword.logic.KeywordLogic;
import wbs.sms.keyword.model.KeywordRec;
import wbs.sms.keyword.model.KeywordSetFallbackObjectHelper;
import wbs.sms.keyword.model.KeywordSetFallbackRec;
import wbs.sms.keyword.model.KeywordSetObjectHelper;
import wbs.sms.keyword.model.KeywordSetRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.CommandManager;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

import com.google.common.base.Optional;

@Accessors (fluent = true)
@Log4j
@PrototypeComponent ("keywordCommand")
public
class KeywordCommand
	implements CommandHandler {

	// dependencies

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	Database database;

	@Inject
	KeywordFinder keywordFinder;

	@Inject
	KeywordLogic keywordLogic;

	@Inject
	KeywordSetFallbackObjectHelper keywordSetFallbackHelper;

	@Inject
	KeywordSetObjectHelper keywordSetHelper;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	CommandManager commandManager;

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional<Integer> commandRef;

	@Getter @Setter
	String rest;

	// state

	KeywordSetRec keywordSet;
	MessageRec message;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"keyword_set.default"
		};

	}

	// implementation

	@Override
	public
	InboxAttemptRec handle () {

		if (log.isDebugEnabled ()) {

			log.debug (
				stringFormat (
					"About to handle message %s ",
					inbox.getId (),
					"with command %s",
					command.getId ()));

		}

		keywordSet =
			keywordSetHelper.find (
				command.getParentObjectId ());

		message =
			inbox.getMessage ();

		// try and find a keyword

		Optional<Pair<KeywordRec,String>> matchResult =
			performMatch ();

		if (matchResult.isPresent ()) {

			KeywordRec keywordRecord =
				matchResult.get ().getLeft ();

			CommandRec nextCommand =
				keywordRecord.getCommand ();

			if (nextCommand == null) {

				log.info (
					stringFormat (
						"%s has no command, not processing %s",
						keywordRecord,
						message));

				return inboxLogic.inboxNotProcessed (
					inbox,
					Optional.<ServiceRec>absent (),
					Optional.<AffiliateRec>absent (),
					Optional.of (command),
					stringFormat (
						"No command for %s",
						keywordRecord));

			}

			String messageRest =
				keywordRecord.getLeaveIntact ()
					? rest
					: matchResult.get ().getRight ();

			if (log.isDebugEnabled ()) {

				log.debug (
					stringFormat (
						"Found keyword %s ",
						keywordRecord.getId (),
						"for message %s",
						inbox.getId ()));

			}

			// set fallback if sticky

			if (keywordRecord.getSticky ()) {

				keywordLogic.createOrUpdateKeywordSetFallback (
					keywordSet,
					message.getNumber (),
					nextCommand);

			}

			// hand off

			return commandManager.handle (
				inbox,
				nextCommand,
				Optional.<Integer>absent (),
				messageRest);

		}

		// ok that didn't work, try a fallback thingy

		KeywordSetFallbackRec keywordSetFallback =
			keywordSetFallbackHelper.find (
				keywordSet,
				message.getNumber ());

		if (keywordSetFallback != null) {

			if (log.isDebugEnabled ()) {

				log.debug (
					stringFormat (
						"Using keyword set fallback %s ",
						keywordSetFallback.getId (),
						"for message %s",
						inbox.getId ()));

			}

			CommandRec nextCommand =
				keywordSetFallback.getCommand ();

			return commandManager.handle (
				inbox,
				nextCommand,
				Optional.<Integer>absent (),
				rest);

		}

		// then try the keyword set's fallback

		if (keywordSet.getFallbackCommand () != null) {

			if (log.isDebugEnabled ()) {

				log.debug (
					stringFormat (
						"Using fallback command for message %s",
						inbox.getId ()));

			}

			CommandRec nextCommand =
				keywordSet.getFallbackCommand ();

			return commandManager.handle (
				inbox,
				nextCommand,
				Optional.<Integer>absent (),
				rest);

		}

		// mark as not processed

		if (log.isDebugEnabled ()) {

			log.debug (
				stringFormat (
					"Marking message %s ",
					inbox.getId (),
					"as not processed"));

		}

		return inboxLogic.inboxNotProcessed (
			inbox,
			Optional.<ServiceRec>absent (),
			Optional.<AffiliateRec>absent (),
			Optional.of (command),
			stringFormat (
				"No keyword matched in %s",
				keywordSet));

	}

	Optional<Pair<KeywordRec,String>> performMatch () {

		switch (keywordSet.getType ()) {

		case keyword:
			return matchKeyword ();

		case numTo:
			return matchNumTo ();

		default:
			throw new RuntimeException ();

		}

	}

	Optional<Pair<KeywordRec,String>> matchKeyword () {

		for (
			KeywordFinder.Match match
				: keywordFinder.find (
					rest)
		) {

			String keyword =
				match.simpleKeyword ();

			// lookup keyword

			KeywordRec keywordRecord =
				keywordSet.getKeywords ().get (keyword);

			if (keywordRecord != null) {

				return Optional.of (
					Pair.of (
						keywordRecord,
						match.rest ()));

			}

		}

		return Optional.absent ();

	}

	Optional<Pair<KeywordRec,String>> matchNumTo () {

		KeywordRec keywordRecord =
			keywordSet.getKeywords ().get (
				message.getNumTo ());

		if (keywordRecord == null)
			return Optional.absent ();

		return Optional.of (
			Pair.of (
				keywordRecord,
				rest));

	}

}
