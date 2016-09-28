package wbs.apn.chat.core.daemon;

import static wbs.utils.etc.EnumUtils.enumNotInSafe;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Collections;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import org.joda.time.LocalDate;

import wbs.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.contact.logic.ChatMessageLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic.TemplateMissing;
import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.help.logic.ChatHelpLogLogic;
import wbs.apn.chat.keyword.model.ChatKeywordJoinType;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.join.daemon.ChatJoiner;
import wbs.apn.chat.user.join.daemon.ChatJoiner.JoinType;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.keyword.model.ChatKeywordObjectHelper;
import wbs.apn.chat.keyword.model.ChatKeywordRec;
import wbs.apn.chat.scheme.model.ChatSchemeKeywordObjectHelper;
import wbs.apn.chat.scheme.model.ChatSchemeKeywordRec;
import wbs.apn.chat.scheme.model.ChatSchemeObjectHelper;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.entity.record.IdObject;
import wbs.platform.media.model.MediaRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.core.logic.DateFinder;
import wbs.sms.core.logic.KeywordFinder;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.CommandManager;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

/**
 * MainCommandHandler takes input from the main chat interface, looking for
 * keywords or box numbers and forwarding to the appropriate command.
 */
@Accessors (fluent = true)
@Log4j
@PrototypeComponent ("chatMainCommand")
public
class ChatMainCommand
	implements CommandHandler {

	// dependencies

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@SingletonDependency
	ChatHelpLogLogic chatHelpLogLogic;

	@SingletonDependency
	ChatKeywordObjectHelper chatKeywordHelper;

	@SingletonDependency
	ChatMessageLogic chatMessageLogic;

	@SingletonDependency
	ChatSchemeKeywordObjectHelper chatSchemeKeywordHelper;

	@SingletonDependency
	ChatSchemeObjectHelper chatSchemeHelper;

	@SingletonDependency
	ChatSendLogic chatSendLogic;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@WeakSingletonDependency
	CommandManager commandManager;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	KeywordFinder keywordFinder;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <ChatJoiner> chatJoinerProvider;

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional<Long> commandRef;

	@Getter @Setter
	String rest;

	// state

	ChatSchemeRec commandChatScheme;
	ChatRec chat;
	MessageRec smsMessage;
	ChatUserRec fromChatUser;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"chat_scheme.chat_scheme"
		};

	}

	// implementation

	InboxAttemptRec doCode (
			@NonNull String code,
			@NonNull String rest) {

		Optional<ChatUserRec> toUserOptional =
			chatUserHelper.findByCode (
				chat,
				code);

		ChatSchemeRec userChatScheme =
			fromChatUser.getChatScheme ();

		if (
			optionalIsNotPresent (
				toUserOptional)
		) {

			log.debug (
				stringFormat (
					"message %d: ignoring invalid user code %s",
					inbox.getId (),
					code));

			return smsInboxLogic.inboxProcessed (
				inbox,
				Optional.of (
					serviceHelper.findByCodeRequired (
						chat,
						"default")),
				Optional.of (
					chatUserLogic.getAffiliate (
						fromChatUser)),
				command);

		}

		ChatUserRec toUser =
			toUserOptional.get ();

		log.debug (
			stringFormat (
				"message %d: message to user %s",
				inbox.getId (),
				toUser.getId ()));

		chatMessageLogic.chatMessageSendFromUser (
			fromChatUser,
			toUser,
			rest,
			Optional.of (
				smsMessage.getThreadId ()),
			ChatMessageMethod.sms,
			Collections.<MediaRec>emptyList ());

		// send signup info if relevant

		if (fromChatUser.getFirstJoin () == null) {

			chatSendLogic.sendSystem (
				fromChatUser,
				Optional.of (
					smsMessage.getThreadId ()),
				"message_signup",
				userChatScheme.getRbFreeRouter (),
				userChatScheme.getRbNumber (),
				Collections.<String>emptySet (),
				Optional.<String>absent (),
				"system",
				TemplateMissing.error,
				Collections.<String,String>emptyMap ());

			chatSendLogic.sendSystemMagic (
				fromChatUser,
				Optional.of (
					smsMessage.getThreadId ()),
				"dob_request",
				commandHelper.findByCodeRequired (
					chat,
					"magic"),
				IdObject.objectId (
					commandHelper.findByCodeRequired (
						userChatScheme,
						"chat_dob")),
				TemplateMissing.error,
				Collections.emptyMap ());

		}

		return smsInboxLogic.inboxProcessed (
			inbox,
			Optional.of (
				serviceHelper.findByCodeRequired (
					chat,
					"default")),
			Optional.of (
				chatUserLogic.getAffiliate (
					fromChatUser)),
			command);

	}

	/**
	 * Tries to find a ChatSchemeKeyword to handle this message. Returns an
	 * appropriate CommandHandler if so, otherwise returns null.
	 */
	Optional<InboxAttemptRec> trySchemeKeyword (
			@NonNull String keyword,
			@NonNull String rest) {

		Optional<ChatSchemeKeywordRec> chatSchemeKeywordOptional =
			chatSchemeKeywordHelper.findByCode (
				commandChatScheme,
				keyword);

		if (
			optionalIsNotPresent (
				chatSchemeKeywordOptional)
		) {

			log.debug (
				stringFormat (
					"message %d: no chat scheme keyword \"%s\"",
					inbox.getId (),
					keyword));

			return Optional.<InboxAttemptRec>absent ();

		}

		ChatSchemeKeywordRec chatSchemeKeyword =
			chatSchemeKeywordOptional.get ();

		if (chatSchemeKeyword.getJoinType () != null) {

			log.debug (
				stringFormat (
					"message %d: chat scheme keyword \"%s\" is join type %s",
					inbox.getId (),
					keyword,
					chatSchemeKeyword.getJoinType ()));

			if (! chatSchemeKeyword.getNoCreditCheck ()) {

				Optional<InboxAttemptRec> inboxAttempt =
					performCreditCheck ();

				if (inboxAttempt.isPresent ())
					return inboxAttempt;

			}

			Long chatAffiliateId =
				chatSchemeKeyword.getJoinChatAffiliate () != null
					? chatSchemeKeyword.getJoinChatAffiliate ().getId ()
					: null;

			return Optional.of (
				chatJoinerProvider.get ()

				.chatId (
					chat.getId ())

				.joinType (
					ChatJoiner.convertJoinType (
						chatSchemeKeyword.getJoinType ()))

				.gender (
					chatSchemeKeyword.getJoinGender ())

				.orient (
					chatSchemeKeyword.getJoinOrient ())

				.chatAffiliateId (
					chatAffiliateId)

				.chatSchemeId (
					commandChatScheme.getId ())

				.confirmCharges (
					chatSchemeKeyword.getConfirmCharges ())

				.inbox (
					inbox)

				.rest (
					rest)

				.handleInbox (
					command)

			);

		}

		if (chatSchemeKeyword.getCommand () != null) {

			log.debug (
				stringFormat (
					"message %d: ",
					inbox.getId (),
					"chat scheme keyword \"%s\" ",
					keyword,
					"is command %s",
					chatSchemeKeyword.getCommand ().getId ()));

			if (! chatSchemeKeyword.getNoCreditCheck ()) {

				Optional<InboxAttemptRec> inboxAttempt =
					performCreditCheck ();

				if (inboxAttempt.isPresent ())
					return inboxAttempt;

			}

			return Optional.of (
				commandManager.handle (
					inbox,
					chatSchemeKeyword.getCommand (),
					optionalAbsent (),
					rest));

		}

		// this keyword does nothing?

		log.warn (
			stringFormat (
				"message %d: chat scheme keyword \"%s\" does nothing",
				inbox.getId (),
				keyword));

		return optionalAbsent ();

	}

	Optional <InboxAttemptRec> tryChatKeyword (
			@NonNull String keyword,
			@NonNull String rest) {

		Optional <ChatKeywordRec> chatKeywordOptional =
			chatKeywordHelper.findByCode (
				chat,
				keyword);

		if (
			optionalIsNotPresent (
				chatKeywordOptional)
		) {

			log.debug (
				stringFormat (
					"message %d: no chat keyword \"%s\"",
					inbox.getId (),
					keyword));

			return Optional.<InboxAttemptRec>absent ();

		}

		ChatKeywordRec chatKeyword =
			chatKeywordOptional.get ();

		if (chatKeyword.getJoinType () != null) {

			log.debug (
				stringFormat (
					"message %d: ",
					inbox.getId (),
					"chat keyword \"%s\" ",
					keyword,
					"is join type %s",
					chatKeyword.getJoinType ()));

			Long chatAffiliateId =
				chatKeyword.getJoinChatAffiliate () != null
					? chatKeyword.getJoinChatAffiliate ().getId ()
					: null;

			if (! chatKeyword.getNoCreditCheck ()) {

				Optional<InboxAttemptRec> inboxAttempt =
					performCreditCheck ();

				if (inboxAttempt.isPresent ())
					return inboxAttempt;

			}

			return Optional.of (
				chatJoinerProvider.get ()

				.chatId (
					chat.getId ())

				.joinType (
					ChatJoiner.convertJoinType (
						chatKeyword.getJoinType ()))

				.gender (
					chatKeyword.getJoinGender ())

				.orient (
					chatKeyword.getJoinOrient ())

				.chatAffiliateId (
					chatAffiliateId)

				.chatSchemeId (
					commandChatScheme.getId ())

				.inbox (
					inbox)

				.rest (
					rest)

				.handleInbox (
					command)

			);

		}

		if (chatKeyword.getCommand () != null) {

			log.debug (
				stringFormat (
					"message %d: chat keyword \"%s\" is command %d",
					inbox.getId (),
					keyword,
					chatKeyword.getCommand ().getId ()));

			return optionalOf (
				commandManager.handle (
					inbox,
					chatKeyword.getCommand (),
					optionalAbsent (),
					rest));

		}

		// this keyword does nothing

		log.warn (
			stringFormat (
				"message %d: ",
				inbox.getId (),
				"chat keyword \"%s\" ",
				keyword,
				"does nothing"));

		return optionalAbsent ();

	}

	Optional <InboxAttemptRec> tryKeyword (
			@NonNull String keyword,
			@NonNull String rest) {

		Optional <InboxAttemptRec> schemeKeywordInboxAttempt =
			trySchemeKeyword (
				keyword,
				rest);

		if (schemeKeywordInboxAttempt.isPresent ())
			return schemeKeywordInboxAttempt;

		Optional <InboxAttemptRec> chatKeywordInboxAttempt =
			tryChatKeyword (
				keyword,
				rest);

		if (chatKeywordInboxAttempt.isPresent ())
			return chatKeywordInboxAttempt;

		return Optional.<InboxAttemptRec>absent ();

	}

	Optional<InboxAttemptRec> tryDob () {

		if (fromChatUser.getFirstJoin () != null)
			return Optional.<InboxAttemptRec>absent ();

		if (

			isNotNull (
				fromChatUser.getNextJoinType ())

			&& enumNotInSafe (
				fromChatUser.getNextJoinType (),
				ChatKeywordJoinType.chatDob,
				ChatKeywordJoinType.dateDob)

		) {
			return Optional.<InboxAttemptRec>absent ();
		}

		Optional<LocalDate> dateOfBirth =
			DateFinder.find (
				rest,
				1915);

		if (
			optionalIsNotPresent (
				dateOfBirth)
		) {
			return Optional.absent ();
		}

		return Optional.of (
			chatJoinerProvider.get ()

			.chatId (
				chat.getId ())

			.joinType (
				JoinType.chatDob)

			.chatSchemeId (
				commandChatScheme.getId ())

			.inbox (
				inbox)

			.rest (
				rest)

			.handleInbox (
				command)

		);

	}

	@Override
	public
	InboxAttemptRec handle () {

		log.debug (
			stringFormat (
				"message %d: begin processing",
				inbox.getId ()));

		commandChatScheme =
			chatSchemeHelper.findRequired (
				command.getParentId ());

		chat =
			commandChatScheme.getChat ();

		smsMessage =
			inbox.getMessage ();

		fromChatUser =
			chatUserHelper.findOrCreate (
				chat,
				smsMessage);

		log.debug (
			stringFormat (
				"message %d: full text \"%s\"",
				inbox.getId (),
				smsMessage.getText ().getText ()));

		log.debug (
			stringFormat (
				"message %d: rest \"%s\"",
				inbox.getId (),
				rest));

		// set chat scheme and adult verify

		chatUserLogic.setScheme (
			fromChatUser,
			commandChatScheme);

		if (smsMessage.getRoute ().getInboundImpliesAdult ()) {

			chatUserLogic.adultVerify (
				fromChatUser);

		}

		// look for a date of birth

		Optional<InboxAttemptRec> dobInboxAttempt =
			tryDob ();

		if (dobInboxAttempt.isPresent ())
			return dobInboxAttempt.get ();

		// look for a keyword

		for (
			KeywordFinder.Match match
				: keywordFinder.find (
					rest)
		) {

			log.debug (
				stringFormat (
					"message %d: trying keyword \"%s\"",
					inbox.getId (),
					match.simpleKeyword ()));

			// check if the keyword is a 6-digit number

			if (match.simpleKeyword ().matches ("\\d{6}")) {

				return doCode (
					match.simpleKeyword (),
					match.rest ());

			}

			// check if it's a chat keyword

			Optional<InboxAttemptRec> keywordInboxAttempt =
				tryKeyword (
					match.simpleKeyword (),
					match.rest ());

			if (keywordInboxAttempt.isPresent ())
				return keywordInboxAttempt.get ();

		}

		// no keyword found

		if (fromChatUser.getLastJoin () == null) {

			if (chat.getErrorOnUnrecognised ()) {

				log.debug (
					stringFormat (
						"message %d: ",
						inbox.getId (),
						"no keyword found, new user, sending error"));

				chatHelpLogLogic.createChatHelpLogIn (
					fromChatUser,
					smsMessage,
					rest,
					null,
					false);

				chatSendLogic.sendSystemRbFree (
					fromChatUser,
					Optional.of (
						smsMessage.getThreadId ()),
					"keyword_error",
					TemplateMissing.error,
					Collections.<String,String>emptyMap ());

				return smsInboxLogic.inboxProcessed (
					inbox,
					Optional.of (
						serviceHelper.findByCodeRequired (
							chat,
							"default")),
					Optional.of (
						chatUserLogic.getAffiliate (
							fromChatUser)),
					command);

			} else {

				log.debug (
					stringFormat (
						"message %d: ",
						inbox.getId (),
						"no keyword found, new user, joining"));

				return chatJoinerProvider.get ()

					.chatId (
						chat.getId ())

					.joinType (
						JoinType.chatSimple)

					.chatSchemeId (
						commandChatScheme.getId ())

					.inbox (
						inbox)

					.rest (
						rest)

					.handleInbox (
						command);

			}

		} else {

			log.debug (
				stringFormat (
					"message %d: ",
					inbox.getId (),
					"no keyword found, existing user, sent to help"));

			chatHelpLogLogic.createChatHelpLogIn (
				fromChatUser,
				smsMessage,
				rest,
				null,
				true);

			return smsInboxLogic.inboxProcessed (
				inbox,
				Optional.of (
					serviceHelper.findByCodeRequired (
						chat,
						"default")),
				Optional.of (
					chatUserLogic.getAffiliate (
						fromChatUser)),
				command);

		}

	}

	Optional<InboxAttemptRec> performCreditCheck () {

		log.debug (
			stringFormat (
				"message %d: performing credit check",
				inbox.getId ()));

		if (fromChatUser.getNumber ().getNetwork ().getId () == 0) {

			log.debug (
				stringFormat (
					"message %d: network unknown, ignoring",
					inbox.getId ()));

			return Optional.of (
				smsInboxLogic.inboxNotProcessed (
					inbox,
					Optional.of (
						serviceHelper.findByCodeRequired (
							chat,
							"default")),
					Optional.of (
						chatUserLogic.getAffiliate (
							fromChatUser)),
					Optional.of (
						command),
					stringFormat (
						"network unknown")));

		}

		ChatCreditCheckResult creditCheckResult =
			chatCreditLogic.userSpendCreditCheck (
				fromChatUser,
				true,
				Optional.of (
					smsMessage.getThreadId ()));

		if (creditCheckResult.failed ()) {

			log.debug (
				stringFormat (
					"message %d: ",
					inbox.getId (),
					"credit check failed, sending to help"));

			chatHelpLogLogic.createChatHelpLogIn (
				fromChatUser,
				smsMessage,
				rest,
				null,
				true);

			return Optional.of (
				smsInboxLogic.inboxProcessed (
					inbox,
					Optional.of (
						serviceHelper.findByCodeRequired (
							chat,
							"default")),
					Optional.of (
						chatUserLogic.getAffiliate (
							fromChatUser)),
					command));

		}

		log.debug (
			stringFormat (
				"message %d: ",
				inbox.getId (),
				"not performing credit check"));

		return Optional.<InboxAttemptRec>absent ();

	}

}