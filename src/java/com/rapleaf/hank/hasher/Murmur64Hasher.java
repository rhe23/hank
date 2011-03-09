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
package com.rapleaf.hank.hasher;

import java.nio.ByteBuffer;



/**
 * This is a very fast, non-cryptographic hash suitable for general hash-based
 * lookup. See http://murmurhash.googlepages.com/ for more details.
 */
public final class Murmur64Hasher implements Hasher {

  public static long murmurHash64(byte[] data, int off, int length, int seed) {
    long m = 0xc6a4a7935bd1e995L;
    int r = 47;

    long h = seed ^ (length * m);

    int i;
    for (i = off; i < length / 8; i += 8) {
      long k = data[i + 7];
      k = k << 8;
      k = k | (data[i + 6] & 0xff);
      k = k << 8;
      k = k | (data[i + 5] & 0xff);
      k = k << 8;
      k = k | (data[i + 4] & 0xff);
      k = k << 8;
      k = k | (data[i + 3] & 0xff);
      k = k << 8;
      k = k | (data[i + 2] & 0xff);
      k = k << 8;
      k = k | (data[i + 1] & 0xff);
      k = k << 8;
      k = k | (data[i + 0] & 0xff);

      k *= m;
      k ^= k >>> r;
      k *= m;

      h ^= k;
      h *= m;
    }

    switch (length & 7) {
    case 7: h ^= data[i+6] << 48;
    case 6: h ^= data[i+5] << 40;
    case 5: h ^= data[i+4] << 32;
    case 4: h ^= data[i+3] << 24;
    case 3: h ^= data[i+2] << 16;
    case 2: h ^= data[i+1] << 8;
    case 1: h ^= data[i];
    h *= m;
    }

    h ^= h >>> r;
    h *= m;
    h ^= h >>> r;

    return h;
  }

  @Override
  public void hash(ByteBuffer val, byte[] hashBytes) {
    int seed = 1;
    long hashValue = 0;
    for (int i = 0; i < hashBytes.length - 8; i+=8) {
      hashValue = murmurHash64(val.array(), val.arrayOffset() + val.position(), val.arrayOffset() + val.limit(), seed);
      seed = (int) hashValue;
      hashBytes[i]   = (byte)((hashValue >> 56) & 0xff);
      hashBytes[i+1] = (byte)((hashValue >> 48) & 0xff);
      hashBytes[i+2] = (byte)((hashValue >> 40) & 0xff);
      hashBytes[i+3] = (byte)((hashValue >> 32) & 0xff);
      hashBytes[i+4] = (byte)((hashValue >> 24) & 0xff);
      hashBytes[i+5] = (byte)((hashValue >> 16) & 0xff);
      hashBytes[i+6] = (byte)((hashValue >>  8) & 0xff);
      hashBytes[i+7] = (byte)((hashValue      ) & 0xff);
    }

    int shortHashBytes = hashBytes.length % 8;
    if (shortHashBytes > 0) {
      hashValue = murmurHash64(val.array(), val.arrayOffset() + val.position(), val.arrayOffset() + val.limit(), seed);
      int off = hashBytes.length - 1;
      switch (shortHashBytes) {
        case 7:
          hashBytes[off--] = (byte)((hashValue >>  8) & 0xff);
        case 6:
          hashBytes[off--] = (byte)((hashValue >> 16) & 0xff);
        case 5:
          hashBytes[off--] = (byte)((hashValue >> 24) & 0xff);
        case 4:
          hashBytes[off--] = (byte)((hashValue >> 32) & 0xff);
        case 3:
          hashBytes[off--] = (byte)((hashValue >> 40) & 0xff);
        case 2:
          hashBytes[off--] = (byte)((hashValue >> 48) & 0xff);
        case 1:
          hashBytes[off--] = (byte)((hashValue >> 56) & 0xff);
      }
    }
  }

  @Override
  public String toString() {
    return "Murmur64Hasher";
  }
}
