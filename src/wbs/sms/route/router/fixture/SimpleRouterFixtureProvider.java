package wbs.sms.route.router.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;

@PrototypeComponent ("simpleRouterFixtureProvider")
public
class SimpleRouterFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuItemObjectHelper menuItemHelper;

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
					"sms"))

			.setCode (
				"simple_router")

			.setName (
				"Simple Router")

			.setDescription (
				"")

			.setLabel (
				"Simple routers")

			.setTargetPath (
				"/simpleRouters")

			.setTargetFrame (
				"main")

		);

	}


}
