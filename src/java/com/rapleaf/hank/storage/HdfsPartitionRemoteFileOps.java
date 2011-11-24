/**
 *  Copyright 2011 Rapleaf
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.rapleaf.hank.storage;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

public class HdfsPartitionRemoteFileOps implements PartitionRemoteFileOps {

  public static class Factory implements PartitionRemoteFileOpsFactory {
    @Override
    public PartitionRemoteFileOps getFileOps(String remoteDomainRoot, int partitionNumber) throws IOException {
      return new HdfsPartitionRemoteFileOps(remoteDomainRoot, partitionNumber);
    }
  }

  private final String partitionRoot;
  private final FileSystem fs;

  public HdfsPartitionRemoteFileOps(String remoteDomainRoot,
                                    int partitionNumber) throws IOException {
    this.partitionRoot = remoteDomainRoot + "/" + partitionNumber;
    this.fs = FileSystem.get(new Configuration());
    if (!new Path(partitionRoot).isAbsolute()) {
      throw new IOException("Cannot initialize " + this.getClass().getSimpleName()
          + " with a non absolute remote partition root: "
          + partitionRoot);
    }
  }

  @Override
  public boolean exists(String relativePath) throws IOException {
    return fs.exists(new Path(getAbsolutePath(relativePath)));
  }

  @Override
  public void copyToLocalRoot(String relativePath, String localRoot) throws IOException {
    Path source = new Path(getAbsolutePath(relativePath));
    Path destination = new Path(localRoot + "/" + source.getName());
    fs.copyToLocalFile(source, destination);
  }

  @Override
  public boolean attemptDelete(String relativePath) throws IOException {
    if (exists(relativePath)) {
      fs.delete(new Path(getAbsolutePath(relativePath)), true);
    }
    return true;
  }

  private String getAbsolutePath(String relativePath) {
    return partitionRoot + "/" + relativePath;
  }

  @Override
  public String toString() {
    return "hdfs://" + partitionRoot;
  }
}