package wbs.sms.messageset.logic;

import static wbs.framework.utils.etc.Misc.doesNotContain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectHooks;
import wbs.platform.object.core.model.ObjectTypeDao;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.sms.messageset.model.MessageSetRec;
import wbs.sms.messageset.model.MessageSetTypeDao;
import wbs.sms.messageset.model.MessageSetTypeRec;

public
class MessageSetHooks
	implements ObjectHooks<MessageSetRec> {

	// dependencies

	@Inject
	Database database;

	@Inject
	MessageSetTypeDao messageSetTypeDao;

	@Inject
	ObjectTypeDao objectTypeDao;

	// state

	Map<Long,List<Long>> messageSetTypeIdsByParentTypeId =
		new HashMap<> ();

	// lifecycle

	@PostConstruct
	public
	void init () {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"privHooks.init ()",
				this);

		messageSetTypeIdsByParentTypeId =
			messageSetTypeDao.findAll ().stream ()

			.collect (
				Collectors.groupingBy (

				messageSetType ->
					messageSetType.getParentType ().getId (),

				Collectors.mapping (
					messageSetType ->
						messageSetType.getId (),
					Collectors.toList ())

			));

	}

	// implementation

	@Override
	public
	void createSingletons (
			@NonNull ObjectHelper<MessageSetRec> messageSetHelper,
			@NonNull ObjectHelper<?> parentHelper,
			@NonNull Record<?> parent) {

		if (
			doesNotContain (
				messageSetTypeIdsByParentTypeId.keySet (),
				parentHelper.objectTypeId ())
		) {
			return;
		}

		ObjectTypeRec parentType =
			objectTypeDao.findById (
				parentHelper.objectTypeId ());

		for (
			Long messageSetTypeId
				: messageSetTypeIdsByParentTypeId.get (
					parentHelper.objectTypeId ())
		) {

			MessageSetTypeRec messageSetType =
				messageSetTypeDao.findRequired (
					messageSetTypeId);

			messageSetHelper.insert (
				messageSetHelper.createInstance ()

				.setMessageSetType (
					messageSetType)

				.setCode (
					messageSetType.getCode ())

				.setParentType (
					parentType)

				.setParentId (
					parent.getId ())

			);

		}

	}

}