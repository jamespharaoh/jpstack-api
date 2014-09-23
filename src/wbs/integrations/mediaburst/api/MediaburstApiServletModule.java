package wbs.integrations.mediaburst.api;

import static wbs.framework.utils.etc.Misc.fromHex;
import static wbs.framework.utils.etc.Misc.isNotEmpty;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.AbstractWebFile;
import wbs.framework.web.PathHandler;
import wbs.framework.web.RegexpPathHandler;
import wbs.framework.web.RequestContext;
import wbs.framework.web.ServletModule;
import wbs.framework.web.WebFile;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.sms.core.logic.NoSuchMessageException;
import wbs.sms.gsm.ConcatenatedInformationElement;
import wbs.sms.gsm.UserDataHeader;
import wbs.sms.message.core.logic.InvalidMessageStateException;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.report.logic.ReportLogic;
import wbs.sms.message.report.model.MessageReportCodeObjectHelper;
import wbs.sms.message.report.model.MessageReportCodeRec;
import wbs.sms.message.report.model.MessageReportCodeType;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

@Log4j
@SingletonComponent ("meidaburtApiServletModule")
public
class MediaburstApiServletModule
	implements ServletModule {

	@Inject
	Database database;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	MessageReportCodeObjectHelper messageReportCodeHelper;

	@Inject
	NetworkObjectHelper networkHelper;

	@Inject
	ReportLogic reportLogic;

	@Inject
	RequestContext requestContext;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	TextObjectHelper textHelper;

	public static
	String hexToUnicode (
			String hexString) {

		StringBuilder output =
			new StringBuilder ();

		String subString = null;

		for (
			int postition = 0;
			postition < hexString.length ();
			postition = postition + 2
		) {

			subString =
				hexString.substring (
					postition,
					postition + 2);

			char character =
				(char)
				Integer.parseInt (
					subString,
					16);

			output.append (
				character);

		}

		return output.toString ();

	}

	String testContent (
			String messageParam) {

		try {

			byte[] bytes = messageParam.getBytes();
			boolean zeroBytes = false;
			for (int i = 0; i < bytes.length; i++) {
				if ((int) bytes[i] == 0) {
					zeroBytes = true;
					break;
				}
			}

			if (!zeroBytes) {
				return messageParam;
			}

			// convert
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < bytes.length; i++) {
				if ((int) bytes[i] != 0) {
					sb.append(Integer.toHexString(bytes[i]));
				}
			}
			messageParam = sb.toString();
			messageParam = hexToUnicode(messageParam);
		} catch (Exception e) {
			throw new RuntimeException (e);
		}

		return messageParam;

	}

	/** WebFile to handle incoming messages. */

	WebFile inFile =
		new AbstractWebFile () {

		@Override
		public
		void doPost ()
			throws
				ServletException,
				IOException {

			// logger.info("MB: "+requestContext.getRequest().getQueryString());

			@Cleanup
			Transaction transaction =
				database.beginReadWrite ();

			// debugging

			requestContext.debugParameters (
				log);

			// get request stuff

			int routeId =
				requestContext.requestInt ("routeId");

			// get params in local variables

			String numFromParam =
				requestContext.parameter ("phonenumber");

			String numToParam =
				requestContext.parameter ("tonumber");

			String networkParam =
				requestContext.parameter ("netid");

			String messageParam =
				requestContext.parameter ("message");

			String msgIdParam =
				requestContext.parameter ("msg_id");

			String udhParam =
				requestContext.parameter ("udh");

			Integer networkId = null;

			if (networkParam != null) {
				if (networkParam.equals("51"))
					networkId = 1;
				else if (networkParam.equals("81"))
					networkId = 2;
				else if (networkParam.equals("3"))
					networkId = 3;
				else if (networkParam.equals("1"))
					networkId = 4;
				else if (networkParam.equals("9"))
					networkId = 6;
				// else throw new RuntimeException ("Unknown network: " +
				// networkParam);
			}

			messageParam =
				testContent (messageParam);

			// load the stuff

			RouteRec route =
				routeHelper.find (routeId);

			NetworkRec network =
				networkId == null
					? null
					: networkHelper.find (networkId);

			// check for concatenation

			UserDataHeader userDataHeader;

			if (
				isNotEmpty (
					udhParam)
			) {

				userDataHeader =
					UserDataHeader.decode (
						ByteBuffer.wrap (
							fromHex (udhParam)));

			} else {

				userDataHeader =
					null;

			}

			ConcatenatedInformationElement concatenatedInformationElement;

			if (
				userDataHeader != null
			) {

				concatenatedInformationElement =
					userDataHeader.find (
						ConcatenatedInformationElement.class);

			} else {

				concatenatedInformationElement =
					null;

			}

			// insert the message

			TextRec messageText =
				textHelper.findOrCreate (messageParam);

			if (concatenatedInformationElement != null) {

				inboxLogic.insertInboxMultipart (
					route,
					concatenatedInformationElement.getRef (),
					concatenatedInformationElement.getSeqMax (),
					concatenatedInformationElement.getSeqNum (),
					numToParam,
					numFromParam,
					null,
					network,
					msgIdParam,
					messageText.getText ());

			} else {

				inboxLogic.inboxInsert (
					msgIdParam,
					messageText,
					numFromParam,
					numToParam,
					route,
					network,
					null,
					null,
					null,
					null);

			}

			// commit etc

			transaction.commit ();

			PrintWriter out =
				requestContext.writer ();

			out.println ("OK");

		}

	};

	static
	Map<String,MessageStatus> stringToMessageStatus =
		ImmutableMap.<String,MessageStatus>builder ()
			.put ("acceptd", MessageStatus.submitted)
			.put ("deleted", MessageStatus.undelivered)
			.put ("delivrd", MessageStatus.delivered)
			.put ("enroute", MessageStatus.submitted)
			.put ("expired", MessageStatus.undelivered)
			.put ("rejectd", MessageStatus.undelivered)
			.put ("undeliv", MessageStatus.undelivered)
			.build ();

	static Set<String> messageStatusStrings =
		ImmutableSet.<String>builder ()
			.addAll (stringToMessageStatus.keySet ())
			.add ("unknown")
			.build ();

	WebFile reportFile =
		new AbstractWebFile () {

		@Override
		public
		void doGet ()
			throws
				ServletException,
				IOException {

			@Cleanup
			Transaction transaction =
				database.beginReadWrite ();

			RouteRec route =
				routeHelper.find (
					requestContext.requestInt ("routeId"));

			String statusParam =
				requestContext.parameter ("status").toLowerCase ();

			if (statusParam == null) {

				throw new RuntimeException (
					"Required parameter 'status' not provided");

			}

			if (! messageStatusStrings.contains (statusParam)) {

				throw new RuntimeException (
					"Unknown message status: " + statusParam);

			}

			MessageStatus newMessageStatus =
				stringToMessageStatus.get (
					statusParam.toLowerCase ());

			try {

				Integer statusCode = null;

				try {

					statusCode =
						Integer.parseInt (
							requestContext.parameter ("deliver_code"));

				} catch (NumberFormatException exception) {

					statusCode = null;

				}

				Integer statusType = null;
				Integer reason = null;

				statusType =
					Arrays.asList (
						stringToMessageStatus.keySet ().toArray ()).indexOf (
							statusParam.toLowerCase ());

				MessageReportCodeRec messageReportCode =
					messageReportCodeHelper.findOrCreate (
						statusCode,
						statusType,
						reason,
						MessageReportCodeType.mediaburst,
						newMessageStatus == MessageStatus.delivered,
						false,
						statusParam);

				if (newMessageStatus != null) {

					reportLogic.deliveryReport (
						route,
						requestContext.parameter ("msg_id"),
						newMessageStatus,
						null,
						messageReportCode);

				}

				transaction.commit ();

			} catch (NoSuchMessageException exception) {

				log.fatal (
					stringFormat (
						"Ignoring report for unknown message %s/%s",
						requestContext.requestInt ("routeId"),
						requestContext.parameter ("msg_id")));

			} catch (InvalidMessageStateException exception) {

				log.fatal (
					stringFormat (
						"Ignoring report for message %s/%s: %s",
						requestContext.requestInt ("routeId"),
						requestContext.parameter ("msg_id"),
						exception.getMessage ()));

			}

		}

	};

	// ============================================================ entries

	final
	RegexpPathHandler.Entry routeEntry =
		new RegexpPathHandler.Entry (
			"/route/([0-9]+)/([^/]+)") {

		@Override
		protected
		WebFile handle (
				Matcher matcher) {

			requestContext.request (
				"routeId",
				Integer.parseInt (matcher.group (1)));

			return defaultFiles.get (
				matcher.group (2));

		}

	};

	// ============================================================ path handler

	final
	PathHandler pathHandler =
		new RegexpPathHandler (routeEntry);

	// ============================================================ files

	final
	Map<String,WebFile> defaultFiles =
		ImmutableMap.<String,WebFile>builder ()
			.put ("in", inFile)
			.put ("report", reportFile)
			.build ();

	// ========================================================= servlet module

	@Override
	public
	Map<String,PathHandler> paths () {

		return ImmutableMap.<String,PathHandler>builder ()
			.put ("/mediaburst", pathHandler)
			.build ();

	}

	@Override
	public
	Map<String,WebFile> files () {
		return null;
	}

}