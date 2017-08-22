/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.api.internal.changedetection.state;

import org.gradle.internal.file.FileType;
import org.gradle.internal.hash.HashCode;
import org.gradle.internal.hash.Hashing;

class DirContentSnapshot implements FileContentSnapshot {
    private static final DirContentSnapshot INSTANCE = new DirContentSnapshot();
    private static final HashCode SIGNATURE = Hashing.md5().hashString(DirContentSnapshot.class.getName());

    private DirContentSnapshot() {
    }

    static DirContentSnapshot getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isContentAndMetadataUpToDate(FileContentSnapshot snapshot) {
        return isContentUpToDate(snapshot);
    }

    public boolean isContentUpToDate(FileContentSnapshot snapshot) {
        return snapshot instanceof DirContentSnapshot;
    }

    @Override
    public FileType getType() {
        return FileType.Directory;
    }

    @Override
    public HashCode getContentMd5() {
        return SIGNATURE;
    }

    @Override
    public String toString() {
        return "DIR";
    }
}
