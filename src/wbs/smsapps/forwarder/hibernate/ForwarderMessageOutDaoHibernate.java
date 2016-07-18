package wbs.smsapps.forwarder.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.smsapps.forwarder.model.ForwarderMessageOutDao;
import wbs.smsapps.forwarder.model.ForwarderMessageOutRec;
import wbs.smsapps.forwarder.model.ForwarderRec;

public
class ForwarderMessageOutDaoHibernate
	extends HibernateDao
	implements ForwarderMessageOutDao {

	@Override
	public
	ForwarderMessageOutRec findByOtherId (
			@NonNull ForwarderRec forwarder,
			@NonNull String otherId) {

		return findOne (
			"findByOtherId (forwarder, otherId)",
			ForwarderMessageOutRec.class,

			createCriteria (
				ForwarderMessageOutRec.class,
				"_forwarderMessageOut")

			.add (
				Restrictions.eq (
					"_forwarderMessageOut.forwarder",
					forwarder))

			.add (
				Restrictions.eq (
					"_forwarderMessageOut.otherId",
					otherId))

		);

	}

	@Override
	public
	List<ForwarderMessageOutRec> findPendingLimit (
			@NonNull ForwarderRec forwarder,
			int maxResults) {

		return findMany (
			"findPendingLimit (forwarder, maxResults)",
			ForwarderMessageOutRec.class,

			createCriteria (
				ForwarderMessageOutRec.class,
				"_forwarderMessageOut")

			.add (
				Restrictions.eq (
					"_forwarderMessageOut.forwarder",
					forwarder))

			.add (
				Restrictions.isNotNull (
					"_forwarderMessageOut.reportIndexPending"))

			.addOrder (
				Order.asc (
					"_forwarderMessageOut.forwarder"))

			.addOrder (
				Order.asc (
					"_forwarderMessageOut.id"))

			.setMaxResults (
				maxResults)

		);

	}

}
