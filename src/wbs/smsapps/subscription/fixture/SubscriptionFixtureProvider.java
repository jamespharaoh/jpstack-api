package wbs.smsapps.subscription.fixture;

import static wbs.framework.utils.etc.Misc.codify;

import java.util.Map;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuObjectHelper;
import wbs.platform.menu.model.MenuRec;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.keyword.model.KeywordObjectHelper;
import wbs.sms.keyword.model.KeywordRec;
import wbs.sms.keyword.model.KeywordSetObjectHelper;
import wbs.sms.keyword.model.KeywordSetRec;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.router.model.RouterObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionAffiliateObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionAffiliateRec;
import wbs.smsapps.subscription.model.SubscriptionKeywordObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionKeywordRec;
import wbs.smsapps.subscription.model.SubscriptionListObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionListRec;
import wbs.smsapps.subscription.model.SubscriptionObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionRec;

import com.google.common.collect.ImmutableMap;

@PrototypeComponent ("subscriptionFixtureProvider")
public
class SubscriptionFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	KeywordObjectHelper keywordHelper;

	@Inject
	KeywordSetObjectHelper keywordSetHelper;

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuObjectHelper menuHelper;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	RouterObjectHelper routerHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	@Inject
	SubscriptionAffiliateObjectHelper subscriptionAffiliateHelper;

	@Inject
	SubscriptionObjectHelper subscriptionHelper;

	@Inject
	SubscriptionKeywordObjectHelper subscriptionKeywordHelper;

	@Inject
	SubscriptionListObjectHelper subscriptionListHelper;

	@Inject
	TextObjectHelper textHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		menuHelper.insert (
			new MenuRec ()

			.setMenuGroup (
				menuGroupHelper.findByCode (
					GlobalId.root,
					"facility"))

			.setCode (
				"subscription")

			.setLabel (
				"Subscription")

			.setPath (
				"/subscriptions")

		);

		SubscriptionRec subscription =
			subscriptionHelper.insert (
				new SubscriptionRec ()

			.setSlice (
				sliceHelper.findByCode (
					GlobalId.root,
					"test"))

			.setCode (
				"test")

			.setName (
				"Test")

			.setDescription (
				"Test subscription")

			.setBilledRoute (
				routeHelper.findByCode (
					GlobalId.root,
					"test",
					"bill"))

			.setBilledNumber (
				"bill")

			.setBilledMessage (
				textHelper.findOrCreate (
					"Billed message"))

			.setFreeRouter (
				routerHelper.findByCode (
					routeHelper.findByCode (
						GlobalId.root,
						"test",
						"free"),
					"static"))

			.setFreeNumber (
				"free")

			.setCreditsPerBill (
				2)

			.setDebitsPerSend (
				1)

			.setSubscribeMessageText (
				textHelper.findOrCreate (
					"Subsription confirmed"))

			.setUnsubscribeMessageText (
				textHelper.findOrCreate (
					"Subscription cancelled"))

		);

		KeywordSetRec inboundKeywordSet =
			keywordSetHelper.findByCode (
				GlobalId.root,
				"test",
				"inbound");

		keywordHelper.insert (
			new KeywordRec ()

			.setKeywordSet (
				inboundKeywordSet)

			.setKeyword (
				"sub")

			.setDescription (
				"Subscription subscribe")

			.setCommand (
				commandHelper.findByCode (
					subscription,
					"subscribe"))

		);

		keywordHelper.insert (
			new KeywordRec ()

			.setKeywordSet (
				inboundKeywordSet)

			.setKeyword (
				"unsub")

			.setDescription (
				"Subscription unsubscribe")

			.setCommand (
				commandHelper.findByCode (
					subscription,
					"unsubscribe"))

		);

		for (
			Map.Entry<String,String> listSpecEntry
				: listSpecs.entrySet ()
		) {

			SubscriptionListRec list =
				subscriptionListHelper.insert (
					new SubscriptionListRec ()

				.setSubscription (
					subscription)

				.setCode (
					codify (
						listSpecEntry.getValue ()))

				.setName (
					listSpecEntry.getValue ())

				.setDescription (
					listSpecEntry.getValue ())

			);

			subscriptionKeywordHelper.insert (
				new SubscriptionKeywordRec ()

				.setSubscription (
					subscription)

				.setKeyword (
					listSpecEntry.getKey ())

				.setDescription (
					listSpecEntry.getValue ())

				.setSubscriptionList (
					list)

			);

		}

		for (
			Map.Entry<String,String> affiliateSpecEntry
				: affiliateSpecs.entrySet ()
		) {

			SubscriptionAffiliateRec affiliate =
				subscriptionAffiliateHelper.insert (
					new SubscriptionAffiliateRec ()

				.setSubscription (
					subscription)

				.setCode (
					codify (
						affiliateSpecEntry.getValue ()))

				.setName (
					affiliateSpecEntry.getValue ())

				.setDescription (
					affiliateSpecEntry.getValue ())

			);

			keywordHelper.insert (
				new KeywordRec ()

				.setKeywordSet (
					inboundKeywordSet)

				.setKeyword (
					affiliateSpecEntry.getKey ())

				.setDescription (
					affiliateSpecEntry.getValue ())

				.setCommand (
					commandHelper.findByCode (
						affiliate,
						"subscribe"))

			);

		}

	}

	Map<String,String> affiliateSpecs =
		ImmutableMap.<String,String>builder ()
			.put ("jt", "Justin Toper")
			.put ("liv", "Psychic living")
			.put ("gts", "Gone Too Soon")
			.put ("sm", "Sally Morgan")
			.build ();

	Map<String,String> listSpecs =
		ImmutableMap.<String,String>builder ()
			.put ("ari", "Aries")
			.put ("tau", "Taurus")
			.put ("gem", "Gemini")
			.put ("leo", "Leo")
			.put ("vir", "Virgo")
			.put ("lib", "Libra")
			.put ("sco", "Scorpio")
			.put ("sag", "Saggitarius")
			.put ("cap", "Capricorn")
			.put ("aqu", "Aquarius")
			.put ("pic", "Pisces")
			.put ("can", "Cancer")
			.build ();

}