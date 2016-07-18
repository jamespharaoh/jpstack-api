package wbs.clients.apn.chat.affiliate.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.clients.apn.chat.affiliate.model.ChatAffiliateDao;
import wbs.clients.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;

@SingletonComponent ("chatAffiliateDao")
public
class ChatAffiliateDaoHibernate
	extends HibernateDao
	implements ChatAffiliateDao {

	@Override
	public
	List<ChatAffiliateRec> find (
			@NonNull ChatRec chat) {

		return findMany (
			"find (chat)",
			ChatAffiliateRec.class,

			createCriteria (
				ChatAffiliateRec.class,
				"_chatAffiliate")

			.add (
				Restrictions.eq (
					"_chatAffiliate.chat",
					chat))

		);

	}

}
