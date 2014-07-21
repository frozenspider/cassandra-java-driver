/*
 *      Copyright (C) 2012 DataStax Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.datastax.driver.core;

import java.nio.ByteBuffer;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import static com.datastax.driver.core.DataType.text;

public class TypeCodecTest {

    public static final DataType CUSTOM_FOO = DataType.custom("com.example.FooBar");

    @Test(groups = "unit")
    public void testCustomList() throws Exception {
        TypeCodec<?> listType = TypeCodec.listOf(CUSTOM_FOO, ProtocolVersion.V2);
        Assert.assertNotNull(listType);
    }

    @Test(groups = "unit")
    public void testCustomSet() throws Exception {
        TypeCodec<?> setType = TypeCodec.setOf(CUSTOM_FOO, ProtocolVersion.V2);
        Assert.assertNotNull(setType);
    }

    @Test(groups = "unit")
    public void testCustomKeyMap() throws Exception {
        TypeCodec<Map<ByteBuffer, String>> mapType = TypeCodec.mapOf(CUSTOM_FOO, text(), ProtocolVersion.V2);
        Assert.assertNotNull(mapType);
    }

    @Test(groups = "unit")
    public void testCustomValueMap() throws Exception {
        TypeCodec<Map<String, ByteBuffer>> mapType = TypeCodec.mapOf(text(), CUSTOM_FOO, ProtocolVersion.V2);
        Assert.assertNotNull(mapType);
    }
}
