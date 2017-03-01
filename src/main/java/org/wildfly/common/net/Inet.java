/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.common.net;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.wildfly.common.Assert;
import org.wildfly.common._private.CommonMessages;

/**
 * Utilities relating to Internet protocol (a.k.a. "INET" or "IP") address manipulation.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class Inet {
    private Inet() {}

    /**
     * Get the optimal string representation of an IP address.  For IPv6 addresses, this representation will be
     * more compact that the default.
     *
     * @param inetAddress the address (must not be {@code null})
     * @return the string representation (not {@code null})
     */
    public static String toOptimalString(InetAddress inetAddress) {
        Assert.checkNotNullParam("inetAddress", inetAddress);
        return inetAddress instanceof Inet6Address ? toOptimalStringV6(inetAddress.getAddress()) : inetAddress.getHostAddress();
    }

    /**
     * Get the optimal string representation of the bytes of an IP address.
     *
     * @param addressBytes the address bytes (must not be {@code null})
     * @return the string representation (not {@code null})
     */
    public static String toOptimalString(byte[] addressBytes) {
        Assert.checkNotNullParam("addressBytes", addressBytes);
        if (addressBytes.length == 4) {
            return (addressBytes[0] & 0xff) + "." + (addressBytes[1] & 0xff) + "." + (addressBytes[2] & 0xff) + "." + (addressBytes[3] & 0xff);
        } else if (addressBytes.length == 16) {
            return toOptimalStringV6(addressBytes);
        } else {
            throw CommonMessages.msg.invalidAddressBytes(addressBytes.length);
        }
    }

    /**
     * Get the IPv6 equivalent of the given address.  If the address is IPv4 then it is returned as a compatibility
     * address.
     *
     * @param inetAddress the address to convert (must not be {@code null})
     * @return the converted address (not {@code null})
     */
    public static Inet6Address toInet6Address(InetAddress inetAddress) {
        if (inetAddress instanceof Inet6Address) {
            return (Inet6Address) inetAddress;
        } else {
            assert inetAddress instanceof Inet4Address;
            final byte[] addr = new byte[16];
            addr[10] = addr[11] = (byte) 0xff;
            System.arraycopy(inetAddress.getAddress(), 0, addr, 12, 4);
            // get unresolved host name
            try {
                return Inet6Address.getByAddress(getHostNameIfResolved(inetAddress), addr, 0);
            } catch (UnknownHostException e) {
                // not possible
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * Get the host name of the given address, if it is resolved.  Otherwise, return {@code null}.
     *
     * @param inetAddress the address to check (must not be {@code null})
     * @return the host name, or {@code null} if the address has no host name and is unresolved
     */
    public static String getHostNameIfResolved(InetAddress inetAddress) {
        final String hostString = new InetSocketAddress(inetAddress, 0).getHostString();
        final String toString = inetAddress.toString();
        final int slash = toString.lastIndexOf('/');
        if (slash == 0) {
            // it might be unresolved or it might explicitly be ""
            return hostString.isEmpty() ? "" : null;
        }
        return hostString;
    }

    /**
     * Get an IPv4 address from four integer segments.  Each segment must be between 0 and 255.
     *
     * @param s1 the first segment
     * @param s2 the second segment
     * @param s3 the third segment
     * @param s4 the fourth segment
     * @return the address (not {@code null})
     */
    public static Inet4Address getInet4Address(int s1, int s2, int s3, int s4) {
        byte[] bytes = new byte[4];
        Assert.checkMinimumParameter("s1", 0, s1);
        Assert.checkMaximumParameter("s1", 255, s1);
        Assert.checkMinimumParameter("s2", 0, s2);
        Assert.checkMaximumParameter("s2", 255, s2);
        Assert.checkMinimumParameter("s3", 0, s3);
        Assert.checkMaximumParameter("s3", 255, s3);
        Assert.checkMinimumParameter("s4", 0, s4);
        Assert.checkMaximumParameter("s4", 255, s4);
        bytes[0] = (byte) s1;
        bytes[1] = (byte) s2;
        bytes[2] = (byte) s3;
        bytes[3] = (byte) s4;
        try {
            return (Inet4Address) InetAddress.getByAddress(s1 + "." + s2 + "." + s3 + "." + s4, bytes);
        } catch (UnknownHostException e) {
            // not possible
            throw new IllegalStateException(e);
        }
    }

    /**
     * Get an IPv6 address from eight integer segments.  Each segment must be between 0 and 65535 ({@code 0xffff}).
     *
     * @param s1 the first segment
     * @param s2 the second segment
     * @param s3 the third segment
     * @param s4 the fourth segment
     * @param s5 the fifth segment
     * @param s6 the sixth segment
     * @param s7 the seventh segment
     * @param s8 the eighth segment
     * @return the address (not {@code null})
     */
    public static Inet6Address getInet6Address(int s1, int s2, int s3, int s4, int s5, int s6, int s7, int s8) {
        byte[] bytes = new byte[16];
        Assert.checkMinimumParameter("s1", 0, s1);
        Assert.checkMaximumParameter("s1", 0xffff, s1);
        Assert.checkMinimumParameter("s2", 0, s2);
        Assert.checkMaximumParameter("s2", 0xffff, s2);
        Assert.checkMinimumParameter("s3", 0, s3);
        Assert.checkMaximumParameter("s3", 0xffff, s3);
        Assert.checkMinimumParameter("s4", 0, s4);
        Assert.checkMaximumParameter("s4", 0xffff, s4);
        Assert.checkMinimumParameter("s5", 0, s5);
        Assert.checkMaximumParameter("s5", 0xffff, s5);
        Assert.checkMinimumParameter("s6", 0, s6);
        Assert.checkMaximumParameter("s6", 0xffff, s6);
        Assert.checkMinimumParameter("s7", 0, s7);
        Assert.checkMaximumParameter("s7", 0xffff, s7);
        Assert.checkMinimumParameter("s8", 0, s8);
        Assert.checkMaximumParameter("s8", 0xffff, s8);
        bytes[0] = (byte) (s1 >> 8);
        bytes[1] = (byte) s1;
        bytes[2] = (byte) (s2 >> 8);
        bytes[3] = (byte) s2;
        bytes[4] = (byte) (s3 >> 8);
        bytes[5] = (byte) s3;
        bytes[6] = (byte) (s4 >> 8);
        bytes[7] = (byte) s4;
        bytes[8] = (byte) (s5 >> 8);
        bytes[9] = (byte) s5;
        bytes[10] = (byte) (s6 >> 8);
        bytes[11] = (byte) s6;
        bytes[12] = (byte) (s7 >> 8);
        bytes[13] = (byte) s7;
        bytes[14] = (byte) (s8 >> 8);
        bytes[15] = (byte) s8;
        try {
            return Inet6Address.getByAddress(toOptimalStringV6(bytes), bytes, 0);
        } catch (UnknownHostException e) {
            // not possible
            throw new IllegalStateException(e);
        }
    }

    /**
     * Checks whether given String is a valid IPv6 address.
     *
     * @param address address textual representation
     * @return is valid IPv6 address?
     */
    public static boolean isIPv6Address(String address) {
        return toAddressBytesV6(address) != null;
    }

    /**
     * Converts IPv6 address from textual representation to bytes.
     *
     * If given string doesn't represent valid IPv6 address, returns {@code null}.
     *
     * @param address address textual representation
     * @return byte array representing the address
     */
    public static byte[] toAddressBytesV6(String address) {
        if (address == null || address.isEmpty()) {
            return null;
        }

        // remove brackets if present
        if (address.startsWith("[") && address.endsWith("]")) {
            address = address.substring(1, address.length() - 1);
        }

        String[] segments = address.split(":", 10);

        // there can be minimum of 2 and maximum of 8 colons, which makes 3 respectively 9 segments
        if (segments.length > 9 || segments.length < 3) {
            return null;
        }
        // if the first segment is empty, the second one must be too - "::<address end>"
        if (segments[0].length() == 0 && segments[1].length() != 0) {
            return null;
        }
        // if the last segment is empty, the segment before it must be too - "<address beginning>::"
        if (segments[segments.length - 1].length() == 0 && segments[segments.length - 2].length() != 0) {
            return null;
        }

        // validate segments
        for (int i = 0; i < segments.length; i++) {
            for (int charIdx = 0; charIdx < segments[i].length(); charIdx++) {
                char c = segments[i].charAt(charIdx);
                if (c == '.' && i != segments.length - 1) {
                    return null; // "." is allowed in the last segment only
                } else if (c != '.' && c != ':' && Character.digit(c, 16) == -1) {
                    return null; // not ".", ":" or a digit
                }
            }
        }

        // look for an empty segment - "::"
        int emptyIndex = -1;
        for (int i = 0; i < segments.length - 1; i++) {
            if (segments[i].length() == 0) {
                if (emptyIndex > 0) {
                    return null; // more than one occurrence of "::", invalid address
                } else if (emptyIndex != 0) { // don't rewrite skipIndex=0, when address starts with "::"
                    emptyIndex = i;
                }
            }
        }

        boolean containsIPv4 = segments[segments.length - 1].contains(".");
        int totalSegments = containsIPv4 ? 7 : 8; // if the last segment contains IPv4 notation ("::ffff:192.0.0.1"), the address only has 7 segments
        if (emptyIndex == -1 && segments.length != totalSegments) {
            return null; // no substitution but incorrect number of segments
        }

        int skipIndex;
        int skippedSegments;
        boolean isDefaultRoute = segments.length == 3
                && segments[0].isEmpty() && segments[1].isEmpty() && segments[2].isEmpty(); // is address just "::"?
        if (isDefaultRoute) {
            skipIndex = 0;
            skippedSegments = 8;
        } else if (segments[0].isEmpty() || segments[segments.length - 1].isEmpty()) {
            // "::" is at the beginning or end of the address
            skipIndex = emptyIndex;
            skippedSegments = totalSegments - segments.length + 2;
        } else if (emptyIndex > -1) {
            // "::" somewhere in the middle
            skipIndex = emptyIndex;
            skippedSegments = totalSegments - segments.length + 1;
        } else {
            // no substitution
            skipIndex = 0;
            skippedSegments = 0;
        }

        ByteBuffer bytes = ByteBuffer.allocate(16);

        try {
            // convert segments before "::"
            for (int i = 0; i < skipIndex; i++) {
                bytes.putShort(parseHexadecimal(segments[i]));
            }
            // fill "0" characters into expanded segments
            for (int i = skipIndex; i < skipIndex + skippedSegments; i++) {
                bytes.putShort((short) 0);
            }
            // convert segments after "::"
            for (int i = skipIndex + skippedSegments; i < totalSegments; i++) {
                int segmentIdx = segments.length - (totalSegments - i);
                if (containsIPv4 && i == totalSegments - 1) {
                    // we are at the last segment and it contains IPv4 address
                    String[] ipV4Segments = segments[segmentIdx].split("\\.");
                    if (ipV4Segments.length != 4) {
                        return null; // incorrect number of segments in IPv4
                    }
                    for (int idxV4 = 0; idxV4 < 4; idxV4++) {
                        bytes.put(parseDecimal(ipV4Segments[idxV4]));
                    }
                } else {
                    bytes.putShort(parseHexadecimal(segments[segmentIdx]));
                }
            }

            return bytes.array();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static byte parseDecimal(String number) {
        int i = Integer.parseInt(number);
        if (i > 255) {
            throw new NumberFormatException();
        }
        return (byte) i;
    }

    private static short parseHexadecimal(String hexNumber) {
        int i = Integer.parseInt(hexNumber, 16);
        if (i > 0xffff) {
            throw new NumberFormatException();
        }
        return (short) i;
    }

    private static String toOptimalStringV6(final byte[] bytes) {
        final int[] segments = new int[8];
        for (int i = 0; i < 8; i ++) {
            segments[i] = (bytes[i << 1] & 0xff) << 8 | bytes[(i << 1) + 1] & 0xff;
        }
        // now loop through the segments and add them as optimally as possible
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < 8; i ++) {
            if (segments[i] == 0) {
                if (i == 7) {
                    b.append('0');
                } else {
                    // possible to collapse it
                    i++;
                    if (segments[i] == 0) {
                        // yup
                        b.append(':').append(':');
                        for (i++; i < 8; i++) {
                            if (segments[i] == 0xffff && b.length() == 2) {
                                b.append("ffff");
                                if (i == 5) {
                                    // it's an IPv4 compat address.
                                    b.append(':').append(bytes[12] & 0xff).append('.').append(bytes[13] & 0xff).append('.').append(bytes[14] & 0xff).append('.').append(bytes[15] & 0xff);
                                    i = 8;
                                } else if (i == 4 && segments[5] == 0) {
                                    // it's a SIIT address.
                                    b.append(":0:").append(bytes[12] & 0xff).append('.').append(bytes[13] & 0xff).append('.').append(bytes[14] & 0xff).append('.').append(bytes[15] & 0xff);
                                    i = 8;
                                } else {
                                    // finally break and do the rest normally
                                    for (i++; i < 8; i++) {
                                        b.append(':').append(Integer.toHexString(segments[i]));
                                    }
                                }
                            } else if (segments[i] != 0) {
                                // finally break and do the rest normally
                                b.append(Integer.toHexString(segments[i]));
                                for (i++; i < 8; i++) {
                                    b.append(':').append(Integer.toHexString(segments[i]));
                                }
                            }
                        }
                    } else {
                        // no, just a single 0 in isolation doesn't get collapsed
                        if (i > 1) b.append(':');
                        b.append('0').append(':').append(Integer.toHexString(segments[i]));
                    }
                }
            } else {
                if (i > 0) b.append(':');
                b.append(Integer.toHexString(segments[i]));
            }
        }
        return b.toString();
    }
}
