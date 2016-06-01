package wbs.integrations.paypal.fixture;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.joinWithoutSeparator;

import java.io.File;

import javax.inject.Inject;

import lombok.NonNull;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.integrations.paypal.model.PaypalAccountObjectHelper;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

@PrototypeComponent ("paypalFixtureProvider")
public
class PaypalFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuItemObjectHelper menuItemHelper;

	@Inject
	PaypalAccountObjectHelper paypalAccountHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCodeOrNull (
					GlobalId.root,
					"test",
					"integration"))

			.setCode (
				"paypal")

			.setName (
				"Paypal")

			.setDescription (
				"")

			.setLabel (
				"Paypal")

			.setTargetPath (
				"/paypalAccounts")

			.setTargetFrame (
				"main")

		);

		File testAccountsFile =
			new File ("conf/test-accounts.xml");

		if (testAccountsFile.exists ()) {

			DataFromXml dataFromXml =
				new DataFromXml ()

				.registerBuilderClasses (
					TestAccountsSpec.class,
					TestAccountSpec.class);

			TestAccountsSpec testAccounts =
				(TestAccountsSpec)
				dataFromXml.readFilename (
					"conf/test-accounts.xml");

			for (
				TestAccountSpec testAccount
					: testAccounts.accounts ()
			) {

				if (! equal (testAccount.type (), "paypal"))
					continue;

				if (! equal (testAccount.name (), "wbs-sandbox"))
					continue;

				createTestAccount (
					testAccount);

				break;

			}

		}

	}

	void createTestAccount (
			@NonNull TestAccountSpec testAccount) {

		paypalAccountHelper.insert (
			paypalAccountHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeOrNull (
					GlobalId.root,
					"test"))

			.setCode (
				"wbs_sandbox")

			.setName (
				"WBS Sandbox")

			.setDescription (
				"Test paypal account")

			.setUsername (
				testAccount.params ().get ("username"))

			.setPassword (
				testAccount.params ().get ("password"))

			.setSignature (
				testAccount.params ().get ("signature"))

			.setAppId (
				testAccount.params ().get ("app-id"))

			.setServiceEndpointPaypalApi (
				"https://api-3t.sandbox.paypal.com/2.0")

			.setServiceEndpointPaypalApiAa (
				"https://api-3t.sandbox.paypal.com/2.0")

			.setServiceEndpointPermissions (
				"https://svcs.sandbox.paypal.com/")

			.setServiceEndpointAdaptivePayments (
				"https://svcs.sandbox.paypal.com/")

			.setServiceEndpointAdaptiveAccounts (
				"https://svcs.sandbox.paypal.com/")

			.setServiceEndpointInvoice (
				"https://svcs.sandbox.paypal.com/")

			.setCheckoutUrl (
				joinWithoutSeparator (
					"https://www.paypal.com/cgi-bin/webscr",
					"?cmd=_express-checkout&token={token}"))

			.setMode (
				"live")

		);

	}

}


