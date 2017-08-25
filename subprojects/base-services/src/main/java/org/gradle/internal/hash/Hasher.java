/*
 * Copyright 2017 the original author or authors.
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
 * limitations under the License.
 */

package org.gradle.internal.hash;

public interface Hasher {
    void putBytes(byte[] bytes);
    void putBytes(byte[] bytes, int off, int len);
    void putByte(byte value);
    void putShort(short value);
    void putInt(int value);
    void putLong(long value);
    void putFloat(float value);
    void putDouble(double value);
    void putBoolean(boolean value);
    void putChar(char value);
    void putString(CharSequence value);
    HashCode hash();
}
