package wbs.smsapps.broadcast.logic;

import java.util.List;

import javax.inject.Inject;

import lombok.NonNull;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.user.model.UserRec;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.lookup.logic.NumberLookupManager;
import wbs.smsapps.broadcast.model.BroadcastConfigRec;
import wbs.smsapps.broadcast.model.BroadcastNumberObjectHelper;
import wbs.smsapps.broadcast.model.BroadcastNumberRec;
import wbs.smsapps.broadcast.model.BroadcastNumberState;
import wbs.smsapps.broadcast.model.BroadcastRec;

@SingletonComponent ("broadcastLogic")
public
class BroadcastLogicImpl
	implements BroadcastLogic {

	// dependencies

	@Inject
	BroadcastNumberObjectHelper broadcastNumberHelper;

	@Inject
	NumberObjectHelper numberHelper;

	@Inject
	NumberLookupManager numberLookupManager;

	// implementation

	@Override
	public
	AddResult addNumbers (
			@NonNull BroadcastRec broadcast,
			@NonNull List<String> numbers,
			UserRec user) {

		AddResult result =
			new AddResult ();

		BroadcastConfigRec broadcastConfig =
			broadcast.getBroadcastConfig ();

		// add numbers

		for (String numberString : numbers) {

			NumberRec numberRecord =
				numberHelper.findOrCreate (
					numberString);

			BroadcastNumberRec broadcastNumber =
				broadcastNumberHelper.findOrCreate (
					broadcast,
					numberRecord);

			// check block list

			boolean reject =
				broadcastConfig.getBlockNumberLookup () != null
					? numberLookupManager.lookupNumber (
						broadcastConfig.getBlockNumberLookup (),
						numberRecord)
					: false;

			// add number

			switch (broadcastNumber.getState ()) {

			case removed:

				if (reject) {

					broadcastNumber
						.setState (BroadcastNumberState.rejected)
						.setAddedByUser (user);

					broadcast
						.setNumRemoved (broadcast.getNumRemoved () - 1)
						.setNumRejected (broadcast.getNumRejected () + 1);

					result.numRejected ++;

				} else {

					broadcastNumber
						.setState (BroadcastNumberState.accepted)
						.setAddedByUser (user);

					broadcast
						.setNumRemoved (broadcast.getNumRemoved () - 1)
						.setNumAccepted (broadcast.getNumAccepted () + 1);

					result.numAdded ++;

				}

				break;

			case accepted:

				result.numAlreadyAdded ++;

				break;

			case rejected:

				if (reject) {

					result.numAlreadyRejected ++;

				} else {

					broadcastNumber
						.setState (BroadcastNumberState.accepted)
						.setAddedByUser (user);

					broadcast
						.setNumRejected (broadcast.getNumRejected () - 1)
						.setNumAccepted (broadcast.getNumAccepted () + 1);

					result.numAdded ++;

				}

				break;

			case sent:

				throw new RuntimeException (
					"Should never happen");

			}

		}

		return result;

	}

}
