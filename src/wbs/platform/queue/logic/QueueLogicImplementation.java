package wbs.platform.queue.logic;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.laterThan;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;
import org.joda.time.Instant;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.platform.queue.model.QueueItemClaimStatus;
import wbs.platform.queue.model.QueueItemObjectHelper;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueItemState;
import wbs.platform.queue.model.QueueObjectHelper;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.QueueSubjectObjectHelper;
import wbs.platform.queue.model.QueueSubjectRec;
import wbs.platform.queue.model.QueueTypeObjectHelper;
import wbs.platform.queue.model.QueueTypeRec;
import wbs.platform.scaffold.logic.SliceLogic;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.user.model.UserRec;

@Log4j
@SingletonComponent ("queueLogic")
public
class QueueLogicImplementation
	implements QueueLogic {

	// dependencies

	@Inject
	Database database;

	@Inject
	ObjectManager objectManager;

	@Inject
	ObjectTypeObjectHelper objectTypeHelper;

	@Inject
	QueueObjectHelper queueHelper;

	@Inject
	QueueItemObjectHelper queueItemHelper;

	@Inject
	QueueSubjectObjectHelper queueSubjectHelper;

	@Inject
	QueueTypeObjectHelper queueTypeHelper;

	@Inject
	SliceLogic sliceLogic;

	// implementation

	@Override
	public
	QueueRec findQueue (
			@NonNull Record<?> parentObject,
			@NonNull String code) {

		QueueRec queue =
			queueHelper.findByCodeOrNull (
				parentObject,
				code);

		if (queue == null) {

			throw new RuntimeException (
				stringFormat (
					"Can't find queue %s for %s",
					code,
					objectManager.objectPath (parentObject)));

		}

		return queue;
	}

	@Override
	public
	QueueRec findOrCreateQueue (
			@NonNull Record<?> parent,
			@NonNull String queueTypeCode,
			@NonNull String code) {

		ObjectHelper<?> parentHelper =
			objectManager.objectHelperForObject (
				parent);

		ObjectTypeRec parentType =
			objectTypeHelper.findOrNull (
				parentHelper.objectTypeId ());

		// lookup existing queue

		QueueRec queue =
			objectManager.findChildByCode (
				QueueRec.class,
				parent,
				code);

		if (queue == null) {

			QueueTypeRec queueType =
				queueTypeHelper.findByCodeOrNull (
					parentType,
					queueTypeCode);

			if (queueType == null) {

				throw new RuntimeException (
					stringFormat (
						"No such queue type %s for object type %s",
						queueTypeCode,
						parentHelper.objectName ()));

			}

			String objectPath =
				objectManager.objectPathMini (
					parent);

			log.info (
				stringFormat (
					"Creating new queue %s of type %s on %s",
					code,
					queueTypeCode,
					objectPath));

			// create new queue

			queue =
				queueHelper.insert (
					queueHelper.createInstance ()

				.setCode (
					code)

				.setQueueType (
					queueType)

				.setParentType (
					parentType)

				.setParentId (
					parent.getId ())

			);

		}

		return queue;

	}

	@Override
	public
	QueueItemRec createQueueItem (
			@NonNull QueueSubjectRec queueSubject,
			@NonNull Record<?> refObject,
			@NonNull String source,
			@NonNull String details) {

		Transaction transaction =
			database.currentTransaction ();

		QueueRec queue =
			queueSubject.getQueue ();

		QueueTypeRec queueType =
			queue.getQueueType ();

		// sanity check

		if (
			notEqual (
				objectManager.getObjectTypeId (refObject),
				queueType.getRefType ().getId ())
		) {

			throw new IllegalArgumentException ();

		}

		// create the queue item

		boolean waiting =
			queueSubject.getActiveItems () > 0;

		QueueItemRec queueItem =
			queueItemHelper.insert (
				queueItemHelper.createInstance ()

			.setQueueSubject (
				queueSubject)

			.setIndex (
				(int) (long)
				queueSubject.getTotalItems ())

			.setQueue (
				queue)

			.setSource (
				source)

			.setDetails (
				details)

			.setRefObjectId (
				(long)
				refObject.getId ())

			.setState (
				waiting
					? QueueItemState.waiting
					: QueueItemState.pending)

			.setCreatedTime (
				transaction.now ())

			.setPendingTime (
				waiting
					? null
					: transaction.now ())

			.setPriority (
				ifNull (
					queue.getDefaultPriority (),
					0l))

		);

		// update queue subject

		queueSubject

			.setTotalItems (
				queueSubject.getTotalItems () + 1)

			.setActiveItems (
				queueSubject.getActiveItems () + 1);

		// update slice

		Optional<SliceRec> optionalSlice =
			objectManager.getAncestor (
				SliceRec.class,
				queue);

		if (
			isNotPresent (
				optionalSlice)
		) {

			log.warn (
				stringFormat (
					"Unable to determine slice for queue %s",
					queue.getId ()));

		}

		sliceLogic.updateSliceInactivityTimestamp (
			optionalSlice.get (),
			Optional.of (
				transaction.now ()));

		// and return

		return queueItem;

	}

	@Override
	public
	QueueItemRec createQueueItem (
			@NonNull QueueRec queue,
			@NonNull Record<?> subjectObject,
			@NonNull Record<?> refObject,
			@NonNull String source,
			@NonNull String details) {

		QueueSubjectRec queueSubject =
			findOrCreateQueueSubject (
				queue,
				subjectObject);

		return createQueueItem (
			queueSubject,
			refObject,
			source,
			details);

	}

	@Override
	public
	QueueSubjectRec findOrCreateQueueSubject (
			@NonNull QueueRec queue,
			@NonNull Record<?> object) {

		QueueTypeRec queueType =
			queue.getQueueType ();

		// sanity check

		if (
			notEqual (
				objectManager.getObjectTypeId (
					object),
				queueType.getSubjectType ().getId ())
		) {

			throw new IllegalArgumentException (
				stringFormat (
					"Queue %s expected subject type %s, got %s",
					objectManager.objectPath (
						queue),
					queueType.getSubjectType ().getCode (),
					objectManager.getObjectTypeCode (
						object)));

		}

		// lookup existing

		QueueSubjectRec queueSubject =
			queueSubjectHelper.find (
				queue,
				object);

		if (queueSubject != null)
			return queueSubject;

		// create new

		queueSubject =
			queueSubjectHelper.insert (
				queueSubjectHelper.createInstance ()

			.setQueue (
				queue)

			.setObjectId (
				object.getId ())

		);

		return queueSubject;

	}

	@Override
	public
	void cancelQueueItem (
			@NonNull QueueItemRec queueItem) {

		Transaction transaction =
			database.currentTransaction ();

		QueueSubjectRec queueSubject =
			queueItem.getQueueSubject ();

		// sanity checks

		long currentItemIndex =
			+ queueSubject.getTotalItems ()
			- queueSubject.getActiveItems ();

		if (queueItem.getIndex () != currentItemIndex)
			throw new IllegalStateException ();

		if (! in (queueItem.getState (),
				QueueItemState.pending,
				QueueItemState.claimed)) {

			throw new RuntimeException (
				stringFormat (
					"Cannot cancel queue item in %s state",
					queueItem.getState ()));

		}

		// update queue item claim

		queueItem.getQueueItemClaim ()

			.setEndTime (
				transaction.now ())

			.setStatus (
				QueueItemClaimStatus.cancelled);

		// update the queue item

		queueItem

			.setState (
				QueueItemState.cancelled)

			.setCancelledTime (
				transaction.now ())

			.setQueueItemClaim (
				null);

		// update the queue subject

		queueSubject

			.setActiveItems (
				queueSubject.getActiveItems () - 1);

		// activate next queue item (if any)

		if (queueSubject.getActiveItems () > 0) {

			long nextItemIndex =
				+ queueSubject.getTotalItems ()
				- queueSubject.getActiveItems ();

			QueueItemRec nextQueueItem =
				queueItemHelper.findByIndex (
					queueSubject,
					nextItemIndex);

			if (nextQueueItem.getState () != QueueItemState.waiting)
				throw new IllegalStateException ();

			nextQueueItem

				.setState (
					QueueItemState.pending)

				.setPendingTime (
					transaction.now ());

		}

	}

	@Override
	public
	void processQueueItem (
			@NonNull QueueItemRec queueItem,
			@NonNull UserRec user) {

		Transaction transaction =
			database.currentTransaction ();

		QueueSubjectRec queueSubject =
			queueItem.getQueueSubject ();

		// sanity checks

		long currentItemIndex =
			+ queueSubject.getTotalItems ()
			- queueSubject.getActiveItems ();

		if (queueItem.getIndex () != currentItemIndex) {

			throw new IllegalStateException (
				stringFormat (
					"Cannot process queue item %s ",
					queueItem.getId (),
					"with index %s ",
					queueItem.getIndex (),
					"for queue subject %s ",
					queueSubject.getId (),
					"whose total is %s ",
					queueSubject.getTotalItems (),
					"and active is %s, ",
					queueSubject.getActiveItems (),
					"implying a current item index of %s",
					currentItemIndex));

		}

		if (queueItem.getState () != QueueItemState.claimed) {

			throw new RuntimeException (
				stringFormat (
					"Cannot process queue item %s in state %s",
					queueItem.getId (),
					queueItem.getState ()));

		}

		if (queueItem.getQueueItemClaim ().getUser () != user) {

			throw new RuntimeException (
				"Trying to process item belonging to another user");

		}

		// update queue item claim

		queueItem.getQueueItemClaim ()

			.setEndTime (
				transaction.now ())

			.setStatus (
				QueueItemClaimStatus.processed);

		// update the queue item

		queueItem

			.setState (
				QueueItemState.processed)

			.setProcessedTime (
				transaction.now ())

			.setProcessedUser (
				user)

			.setProcessedByPreferredUser (
				queueSubject.getPreferredUser () == null
					? null
					: queueSubject.getPreferredUser () == user)

			.setQueueItemClaim (
				null);

		// update the queue subject

		queueSubject

			.setActiveItems (
				queueSubject.getActiveItems () - 1)

			.setPreferredUser (
				user);

		// update slice

		sliceLogic.updateSliceInactivityTimestamp (
			user.getSlice (),
			Optional.<Instant>absent ());

		// activate next queue item (if any)

		if (queueSubject.getActiveItems () > 0) {

			long nextItemIndex =
				+ queueSubject.getTotalItems ()
				- queueSubject.getActiveItems ();

			QueueItemRec nextQueueItem =
				queueItemHelper.findByIndex (
					queueSubject,
					nextItemIndex);

			if (nextQueueItem.getState () != QueueItemState.waiting)
				throw new IllegalStateException ();

			nextQueueItem

				.setState (
					QueueItemState.pending)

				.setPendingTime (
					transaction.now ());

		}

	}

	@Override
	public
	List<QueueItemRec> getActiveQueueItems (
			@NonNull QueueSubjectRec queueSubject) {

		return queueSubject.getQueueItems ().subList (
			(int) (
				+ queueSubject.getTotalItems ()
				- queueSubject.getActiveItems ()),
			(int) (long) (
				queueSubject.getTotalItems ()));

	}

	@Override
	public
	boolean sliceHasQueueActivity (
			@NonNull SliceRec slice) {

		Transaction transaction =
			database.currentTransaction ();

		// active if no config or stats

		if (

			isNull (
				slice.getQueueOverflowInactivityTime ())

			|| isNull (
				slice.getCurrentQueueInactivityTime ())

		) {
			return true;
		}

		// check for recent activity

		return laterThan (
			slice.getCurrentQueueInactivityTime ().plus (
				Duration.standardSeconds (
					slice.getQueueOverflowInactivityTime ())),
			transaction.now ());

	}

}
