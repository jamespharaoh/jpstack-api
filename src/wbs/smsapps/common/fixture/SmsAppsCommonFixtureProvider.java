package wbs.smsapps.common.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

@PrototypeComponent ("smsAppsCommonFixtureProvider")
public
class SmsAppsCommonFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		menuGroupHelper.insert (
			menuGroupHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCode (
					GlobalId.root,
					"test"))

			.setCode (
				"facility")

			.setName (
				"Facility")

			.setDescription (
				"")

			.setLabel (
				"Facilities")

			.setOrder (
				20)

		);

	}

}
