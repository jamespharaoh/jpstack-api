package wbs.platform.core.console;

import static wbs.utils.collection.MapUtils.mapContainsKey;
import static wbs.utils.etc.Misc.isNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import com.google.gson.JsonObject;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.async.ConsoleAsyncConnectionHandle;
import wbs.console.async.ConsoleAsyncEndpoint;
import wbs.console.priv.UserPrivChecker;
import wbs.console.priv.UserPrivCheckerBuilder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.NormalLifecycleTeardown;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.deployment.logic.DeploymentLogic;
import wbs.platform.scaffold.console.RootConsoleHelper;
import wbs.platform.status.console.StatusLineManager;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.model.UserRec;

import wbs.utils.time.TimeFormatter;

@PrototypeComponent ("consoleAsyncSubscription")
@Accessors (fluent = true)
public
class ConsoleAsyncSubscription <SubscriberState>
	implements ConsoleAsyncEndpoint {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	DeploymentLogic deploymentLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RootConsoleHelper rootHelper;

	@SingletonDependency
	StatusLineManager statusLineManager;

	@SingletonDependency
	TimeFormatter timeFormatter;

	@SingletonDependency
	UserConsoleHelper userHelper;

	@SingletonDependency
	WbsConfig wbsConfig;

	// prototype dependencies

	@PrototypeDependency
	Provider <UserPrivCheckerBuilder> userPrivCheckerBuilderProvider;

	// properties

	@Getter @Setter
	Helper <SubscriberState> helper;

	// state

	Thread backgroundThread;

	Map <String, Subscriber <SubscriberState>> subscribersByConnectionId =
		new HashMap<> ();

	// details

	@Override
	public
	String endpointPath () {
		return helper.endpointPath ();
	}

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"setup");

		taskLogger.noticeFormat (
			"%s async endpoint starting",
			helper.endpointName ());

		backgroundThread =
			new Thread (
				this::backgroundThread);

		backgroundThread.start ();

	}

	@NormalLifecycleTeardown
	public
	void tearDown (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"tearDown");

		if (
			isNull (
				backgroundThread)
		) {
			return;
		}

		taskLogger.noticeFormat (
			"%s async endpoint shutting down",
			helper.endpointName ());

		backgroundThread.interrupt ();

		try {

			backgroundThread.wait ();

		} catch (InterruptedException interruptedException) {

			taskLogger.fatalFormat (
				"Interrupted while waiting for shutdown");

		}

	}

	// implementation

	@Override
	public
	void message (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleAsyncConnectionHandle connectionHandle,
			@NonNull Long userId,
			@NonNull JsonObject jsonObject) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"message");

		synchronized (this) {

			if (
				mapContainsKey (
					subscribersByConnectionId,
					connectionHandle.connectionId ())
			) {

				taskLogger.errorFormat (
					"Duplicate subscription for connection id: %s",
					connectionHandle.connectionId ());

				return;

			}

			subscribersByConnectionId.put (
				connectionHandle.connectionId (),
				new Subscriber <SubscriberState> ()

				.connectionHandle (
					connectionHandle)

				.userId (
					userId)

				.state (
					helper.newSubscription (
						taskLogger))

			);

		}

	}

	// private implementation

	private
	void backgroundThread () {

		for (;;) {

			try {

				sendUpdates ();

				Thread.sleep (
					1000);

			} catch (InterruptedException interruptedException) {
				return;
			}

		}

	}

	private
	void sendUpdates () {

		TaskLogger taskLogger =
			logContext.createTaskLogger (
				"sendUpdates");

		synchronized (this) {

			Set <String> closedConnectionIds =
				new HashSet<> ();

			try (

				Transaction transaction =
					database.beginReadOnly (
						taskLogger,
						"sendUpdates",
						this);

			) {

				helper.prepareUpdate (
					taskLogger);

				for (
					Map.Entry <
						String,
						Subscriber <SubscriberState>
					> subscriberEntry
						: subscribersByConnectionId.entrySet ()
				) {

					String connectionId =
						subscriberEntry.getKey ();

					Subscriber <SubscriberState> subscriber =
						subscriberEntry.getValue ();

					if (! subscriber.connectionHandle ().isConnected ()) {

						closedConnectionIds.add (
							connectionId);

						continue;

					}

					if (! subscriber.connectionHandle ().isFresh ()) {

						continue;

					}

					updateSubscriber (
						taskLogger,
						transaction,
						subscriber);

				}

			}

			closedConnectionIds.forEach (
				subscribersByConnectionId::remove);

		}

	}

	private
	void updateSubscriber (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Transaction transaction,
			@NonNull Subscriber <SubscriberState> subscriber) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"updateSubscriber");

		UserRec user =
			userHelper.findRequired (
				subscriber.userId ());

		UserPrivChecker privChecker =
			userPrivCheckerBuilderProvider.get ()

			.userId (
				subscriber.userId ())

			.build (
				taskLogger);

		try {

			helper.updateSubscriber (
				taskLogger,
				subscriber.state (),
				subscriber.connectionHandle (),
				transaction,
				user,
				privChecker);

		} catch (Exception exception) {

			taskLogger.errorFormatException (
				exception,
				"%s async endpoint error updating subscriber",
				helper.endpointName ());

		}

	}

	// helper interface

	public static
	interface Helper <SubscriberStateType> {

		String endpointPath ();
		String endpointName ();

		SubscriberStateType newSubscription (
				TaskLogger parentTaskLogger);

		void prepareUpdate (
				TaskLogger parentTaskLogger);

		void updateSubscriber (
				TaskLogger parentTaskLogger,
				SubscriberStateType subscriberState,
				ConsoleAsyncConnectionHandle connectionHandle,
				Transaction transaction,
				UserRec user,
				UserPrivChecker privChecker);

	}

	// subscriber class

	@Accessors (fluent = true)
	@Data
	public static
	class Subscriber <SubscriberState> {
		ConsoleAsyncConnectionHandle connectionHandle;
		Long userId;
		SubscriberState state;
	}

}