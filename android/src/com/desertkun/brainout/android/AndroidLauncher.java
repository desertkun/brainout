package com.desertkun.brainout.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.desertkun.brainout.*;
import com.desertkun.brainout.client.settings.ClientSettings;

public class AndroidLauncher extends AndroidApplication
{
	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

        ClientSettings clientSettings = new AndroidSettings();
        clientSettings.init();

		initialize(BrainOutAndroid.initAndroidInstance(new AndroidEnvironment(getContext()),
                clientSettings), config);
	}
}
