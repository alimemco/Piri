package com.alirnp.piri;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class Grab {

    @Test
    public void smsGrabbed(){

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        new GetSmsTask(context, (success, state) -> assertTrue(success)).execute();

    }
}
