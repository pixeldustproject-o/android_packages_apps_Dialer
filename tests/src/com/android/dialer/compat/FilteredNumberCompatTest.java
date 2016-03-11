/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.dialer.compat;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.BlockedNumberContract.BlockedNumbers;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.android.contacts.common.compat.CompatUtils;
import com.android.dialer.database.FilteredNumberContract.FilteredNumber;
import com.android.dialer.database.FilteredNumberContract.FilteredNumberColumns;
import com.android.dialer.database.FilteredNumberContract.FilteredNumberSources;
import com.android.dialer.database.FilteredNumberContract.FilteredNumberTypes;

import java.util.Arrays;

@SmallTest
public class FilteredNumberCompatTest extends AndroidTestCase {

    private static final String E164_NUMBER = "+16502530000";
    private static final String NON_E164_NUMBER = "6502530000";
    private static final String COUNTRY_ISO = "US";

    private static final Uri EXPECTED_BASE_URI = CompatUtils.isNCompatible()
            ? BlockedNumbers.CONTENT_URI : FilteredNumber.CONTENT_URI;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        FilteredNumberCompat.setIsEnabledForTest(true);
    }

    public void testIsNewFilteringEnabled_TestValueFalse() {
        FilteredNumberCompat.setIsEnabledForTest(false);
        assertFalse(FilteredNumberCompat.useNewFiltering());
    }

    public void testIsNewFilteringEnabled_TestValueTrue() {
        FilteredNumberCompat.setIsEnabledForTest(true);
        assertEquals(CompatUtils.isNCompatible(), FilteredNumberCompat.useNewFiltering());
    }

    public void testGetContentUri_NullId() {
        assertEquals(EXPECTED_BASE_URI, FilteredNumberCompat.getContentUri(null));
    }

    public void testGetContentUri() {
        assertEquals(ContentUris.withAppendedId(EXPECTED_BASE_URI, 1),
                FilteredNumberCompat.getContentUri(1));
    }

    public void testFilter_NullProjection() {
        assertNull(FilteredNumberCompat.filter(null));
    }

    public void testFilter_NoNulls() {
        assertArrayEquals(new String[] {"a", "b", "c"},
                FilteredNumberCompat.filter(new String[] {"a", "b", "c"}));
    }

    public void testFilter_WithNulls() {
        assertArrayEquals(new String[] {"a", "b"},
                FilteredNumberCompat.filter(new String[] {"a", null, "b"}));
    }

    public void testNewBlockNumberContentValues_NullNumber() {
        try {
            FilteredNumberCompat.newBlockNumberContentValues(null, null, null);
            fail();
        } catch (NullPointerException e) {}
    }

    public void testNewBlockNumberContentValues_N() {
        if (!CompatUtils.isNCompatible()) {
            return;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(BlockedNumbers.COLUMN_ORIGINAL_NUMBER, NON_E164_NUMBER);
        assertEquals(contentValues, FilteredNumberCompat.newBlockNumberContentValues(
                NON_E164_NUMBER,
                null, null));
    }

    public void testNewBlockNumberContentValues_N_Disabled() {
        if (!CompatUtils.isNCompatible()) {
            return;
        }
        FilteredNumberCompat.setIsEnabledForTest(false);
        assertEquals(newExpectedContentValuesM(NON_E164_NUMBER, E164_NUMBER, COUNTRY_ISO),
                FilteredNumberCompat.newBlockNumberContentValues(NON_E164_NUMBER, E164_NUMBER, COUNTRY_ISO));
    }

    public void testNewBlockNumberContentValues_M_NullE164() {
        if (CompatUtils.isNCompatible()) {
            return;
        }
        assertEquals(newExpectedContentValuesM(NON_E164_NUMBER, E164_NUMBER, COUNTRY_ISO),
                FilteredNumberCompat.newBlockNumberContentValues(NON_E164_NUMBER, null, COUNTRY_ISO));
    }

    public void testNewBlockNumberContentValues_M_NullCountryIso() {
        if (CompatUtils.isNCompatible()) {
            return;
        }
        assertEquals(newExpectedContentValuesM(NON_E164_NUMBER, E164_NUMBER, null),
                FilteredNumberCompat.newBlockNumberContentValues(NON_E164_NUMBER, E164_NUMBER, null));
    }

    public void testNewBlockNumberContentValues_M_NullE164AndCountryIso() {
        if (CompatUtils.isNCompatible()) {
            return;
        }
        // Number can't be formatted properly without country code
        assertEquals(newExpectedContentValuesM(NON_E164_NUMBER, null, null),
                FilteredNumberCompat.newBlockNumberContentValues(NON_E164_NUMBER, null, null));
    }

    private ContentValues newExpectedContentValuesM(String number, String e164Number,
            String countryIso) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FilteredNumberColumns.NUMBER, number);
        contentValues.put(FilteredNumberColumns.NORMALIZED_NUMBER, e164Number);
        contentValues.put(FilteredNumberColumns.COUNTRY_ISO, countryIso);
        contentValues.put(FilteredNumberColumns.TYPE, FilteredNumberTypes.BLOCKED_NUMBER);
        contentValues.put(FilteredNumberColumns.SOURCE, FilteredNumberSources.USER);
        return contentValues;
    }

    private void assertArrayEquals(String[] expected, String[] actual) {
        assertEquals(Arrays.toString(expected), Arrays.toString(actual));
    }
}