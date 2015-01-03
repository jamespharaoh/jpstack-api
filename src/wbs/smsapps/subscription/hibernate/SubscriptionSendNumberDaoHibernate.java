package wbs.smsapps.subscription.hibernate;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.smsapps.subscription.model.SubscriptionSendNumberDao;
import wbs.smsapps.subscription.model.SubscriptionSendNumberRec;
import wbs.smsapps.subscription.model.SubscriptionSendNumberState;
import wbs.smsapps.subscription.model.SubscriptionSendRec;

public
class SubscriptionSendNumberDaoHibernate
	extends HibernateDao
	implements SubscriptionSendNumberDao {

	@Override
	public
	List<SubscriptionSendNumberRec> findQueuedLimit (
			SubscriptionSendRec subscriptionSend,
			int maxResults) {

		return findMany (
			SubscriptionSendNumberRec.class,

			createCriteria (
				SubscriptionSendNumberRec.class,
				"_subscriptionSendNumber")

			.add (
				Restrictions.eq (
					"_subscriptionSendNumber.subscriptionSend",
					subscriptionSend))

			.add (
				Restrictions.eq (
					"_subscriptionSendNumber.state",
					SubscriptionSendNumberState.queued))

			.setMaxResults (
				maxResults)

			.list ());

	}

}