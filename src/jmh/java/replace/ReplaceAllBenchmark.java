/*
 * Copyright 2003-2015 the original author or authors.
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

package replace;

import org.apache.commons.lang3.StringUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class ReplaceAllBenchmark {

    @Param({"10", "100", "1000"})
    private int size;

    String string;

    private final static Random RANDOM = new Random();

    private static String randomAlphanumericString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            int r = RANDOM.nextInt(10);
            switch (r) {
                case 0:
                    sb.append('\r');
                    break;
                case 1:
                    sb.append('\n');
                    break;
                case 2:
                    sb.append(' ');
                    break;
                default:
                    sb.append((char) ('a' + RANDOM.nextInt(26)));
            }
        }
        return sb.toString();
    }

    @Setup
    public void setup() throws Throwable {
        string = randomAlphanumericString(size / 3) + "\n\r " + randomAlphanumericString(size / 3) + "\n "
                + randomAlphanumericString(size / 3);
    }

    static class Replacer {
        private static final Pattern COMPILE = Pattern.compile("[\n\r]+ ");

        static String unfold2(String s) {
            s = s.replaceAll("\n\r ", "");
            s = s.replaceAll("\r\n ", "");
            s = s.replaceAll("\n ", "");
            s = s.replaceAll("\r ", "");
            return s;
        }

        static String unfold_regexp(String s) {
            s = s.replaceAll("\n\r |\r\n |\n |\r ", "");
            return s;
        }

        static String unfold_regexpcompiled(String s) {
            s = COMPILE.matcher(s).replaceAll("");
            return s;
        }

        static String unfold_cedric(String s) {
            if (s == null || s.length() < 2) {
                return s;
            }
            int len = s.length();
            char p1 = 'x';
            char p2 = 'x';
            char[] sb = new char[len];
            int wrtAt = 0;
            for (int i = 0; i < len; i++) {
                char c = s.charAt(i);
                if (' ' == c) {
                    if ('\n' == p1) {
                        if ('\r' == p2) {
                            wrtAt--;
                        }
                        wrtAt--;
                    } else if ('\r' == p1) {
                        if ('\n' == p2) {
                            wrtAt--;
                        }
                        wrtAt--;
                    } else {
                        sb[wrtAt++] = c;
                    }
                } else {
                    sb[wrtAt++] = c;
                }

                p2 = p1;
                p1 = c;
            }

            return new String(sb, 0, wrtAt);
        }

        static String unfold_cedric_improved(String s) {
            if (s == null || s.length() < 2) {
                return s;
            }

            int len = s.length();
            char p1 = 'x';
            char p2 = 'x';
            char[] chars = s.toCharArray();
            int wrtAt = 0;

            for (int i = 0; i < len; i++) {
                char c = chars[i];

                if (' ' == c) {
                    if ('\n' == p1) {
                        if ('\r' == p2) {
                            wrtAt--;
                        }
                        wrtAt--;
                    } else if ('\r' == p1) {
                        if ('\n' == p2) {
                            wrtAt--;
                        }
                        wrtAt--;
                    } else {
                        chars[wrtAt++] = c;
                    }
                } else {
                    chars[wrtAt++] = c;
                }

                p2 = p1;
                p1 = c;
            }

            return new String(chars, 0, wrtAt);
        }

        static String unfold_cedric_ultimate(String s) {
            if (s == null || s.length() < 2) {
                return s;
            }

            char p1 = 'x';
            char p2 = 'x';
            char[] chars = s.toCharArray();
            int wrtAt = 0;

            for (char c : chars) {
                chars[wrtAt++] = c;
                if (' ' == c) {
                    if ('\n' == p1) {
                        if ('\r' == p2) {
                            wrtAt--;
                        }
                        wrtAt--;
                        wrtAt--;
                    }
                    if ('\r' == p1) {
                        if ('\n' == p2) {
                            wrtAt--;
                        }
                        wrtAt--;
                        wrtAt--;
                    }
                }

                p2 = p1;
                p1 = c;

            }

            return new String(chars, 0, wrtAt);
        }

        static String unfold_cedric_ultimate2(String s) {
            if (s == null || s.length() < 2) {
                return s;
            }

            char p1 = 'x';
            char p2 = 'x';
            char[] chars = s.toCharArray();
            int wrtAt = 0;

            for (char c : chars) {
                chars[wrtAt++] = c;
                if (' ' == c) {
                    if ('\n' == p1) {
                        if ('\r' == p2) {
                            wrtAt -= 3;
                        } else {
                            wrtAt -= 2;
                        }
                    }
                    if ('\r' == p1) {
                        if ('\n' == p2) {
                            wrtAt-=3;
                        } else {
                            wrtAt-=2;
                        }
                    }
                }

                p2 = p1;
                p1 = c;

            }

            return new String(chars, 0, wrtAt);
        }


        private enum NLStatus {
            NONE, RC, NL, RC_NL, NL_RC;
        }

        /**
         * Remove all the '\n' and ’\r' followed by a ' ' from a LDIF String.
         *
         * @param s The String to unfold
         * @return The resulting String
         */
        protected static String unfold(String s) {
            int pos = 0;
            char[] unfold = new char[s.length()];
            NLStatus newLine = NLStatus.NONE;

            for (char c : s.toCharArray()) {
                switch (c) {
                    case '\n':
                        switch (newLine) {
                            case NONE:
                                newLine = NLStatus.NL;
                                break;

                            case RC:
                                newLine = NLStatus.RC_NL;
                                break;

                            case NL:
                                unfold[pos++] = '\n';
                                break;

                            case RC_NL:
                                unfold[pos++] = '\r';
                                unfold[pos++] = '\n';
                                newLine = NLStatus.NL;
                                break;

                            case NL_RC:
                                unfold[pos++] = '\n';
                                unfold[pos++] = '\r';
                                newLine = NLStatus.NL;
                                break;
                        }

                        break;

                    case '\r':
                        switch (newLine) {
                            case NONE:
                                newLine = NLStatus.RC;
                                break;

                            case NL:
                                newLine = NLStatus.NL_RC;
                                break;

                            case RC:
                                unfold[pos++] = '\r';
                                break;

                            case RC_NL:
                                unfold[pos++] = '\r';
                                unfold[pos++] = '\n';
                                newLine = NLStatus.RC;
                                break;

                            case NL_RC:
                                unfold[pos++] = '\n';
                                unfold[pos++] = '\r';
                                newLine = NLStatus.RC;
                                break;
                        }

                        break;

                    case ' ':
                        if (newLine == NLStatus.NONE) {
                            unfold[pos++] = c;
                        } else {
                            newLine = NLStatus.NONE;
                        }

                        break;

                    default:
                        switch (newLine) {
                            case NONE:
                                break;

                            case NL:
                                unfold[pos++] = '\n';
                                newLine = NLStatus.NONE;
                                break;

                            case RC:
                                unfold[pos++] = '\r';
                                newLine = NLStatus.NONE;
                                break;

                            case NL_RC:
                                unfold[pos++] = '\n';
                                unfold[pos++] = '\r';
                                newLine = NLStatus.NONE;
                                break;

                            case RC_NL:
                                unfold[pos++] = '\r';
                                unfold[pos++] = '\n';
                                newLine = NLStatus.NONE;
                                break;
                        }

                        unfold[pos++] = c;
                }
            }

            switch (newLine) {
                case NONE:
                    break;

                case NL:
                    unfold[pos++] = '\n';
                    break;

                case RC:
                    unfold[pos++] = '\r';
                    break;

                case NL_RC:
                    unfold[pos++] = '\n';
                    unfold[pos++] = '\r';
                    break;

                case RC_NL:
                    unfold[pos++] = '\r';
                    unfold[pos++] = '\n';
                    break;
            }

            return new String(unfold, 0, pos);
        }

        private static final String[] TODO = {"\n\r ", "\r\n ", "\r ", "\n "};

        private static final String[] TO = {"", "", "", ""};

        protected static String unfold_common(final String string) {
            return StringUtils.replaceEach(string, TODO, TO);
        }

        public static String unfold_olivier(String test) {

// Null -> null
            if (test == null) {
                return null;
            }

// 0 or 1 char
            if (test.length() < 2)
                return test;

// 2 chars
            if (test.length() == 2) {
                if (test.charAt(1) == ' ') {
                    char c0 = test.charAt(0);
                    if (c0 == '\r' || c0 == '\n') {
                        return "";
                    }
                }
                return test;
            }

// More than 2 chars
            char[] chars = test.toCharArray();
            char[] dest = new char[chars.length];
            int p = chars.length - 1;
            int d = p;
            while (p >= 2) {
                char c = chars[p];
// Not a space : keep as is
                if (c != ' ') {
                    dest[d] = c;
                    p--;
                    d--;
                }
// Space
                else {
                    char c1 = chars[p - 1];
// Previous char is special : investigate deeper
                    if (c1 == '\r' || c1 == '\n') {
                        p--;
                        char c2 = chars[p - 1];
                        if ((c2 == '\r' || c2 == '\n') && c2 != c1) {
                            p--;
                        }
                        p--;
                    }
// It was just a space : keep it
                    else {
                        dest[d] = c;
                        p--;
                        d--;
                    }
                }
            }
// Keep the remaining chars as it (special cases already covered)
            while (p >= 0) {
                dest[d--] = chars[p--];
            }

            return new String(dest, d + 1, chars.length - d - 1);
        }

        public static String unfold3(String test) {
// Null -> null
            if (test == null) {
                return null;
            }

            char[] chars = test.toCharArray();

// 0 or 1 char
            if (chars.length < 2) {
                return test;
            }

// 2 chars
            if (chars.length == 2) {
                if (chars[1] == ' ') {
                    if (chars[0] == '\r' || chars[0] == '\n') {
                        return "";
                    }
                }

                return test;
            }

// More than 2 chars
            int p = chars.length - 1;
            int d = p;

            while (p >= 0) {
                char c = chars[p];
// Not a space : keep as is
                if (c != ' ') {
                    chars[d] = c;
                    p--;
                    d--;
                }
// Space
                else {
                    char c1 = chars[p - 1];
// Previous char is special : investigate deeper
                    if (c1 == '\r') {
                        if ((chars[p - 2] == '\n')) {
                            p -= 3;
                        } else {
                            p -= 2;
                        }
                    } else if (c1 == '\n') {
                        if ((chars[p - 2] == '\r')) {
                            p -= 3;
                        } else {
                            p -= 2;
                        }
                    }
// It was just a space : keep it
                    else {
                        chars[d] = c;
                        p--;
                        d--;
                    }
                }
            }
// Keep the remaining chars as it (special cases already covered)
            while (p >= 0) {
                chars[d--] = chars[p--];
            }

            return new String(chars, d + 1, chars.length - d - 1);
        }

    }

    @Benchmark
    public String unfold_original() {
        return Replacer.unfold2(string);
    }

    @Benchmark
    public String unfold_all_regexp() {
        return Replacer.unfold_regexp(string);
    }

    @Benchmark
    public String unfold_compiled_regexp() {
        return Replacer.unfold_regexpcompiled(string);
    }

    @Benchmark
    public String unfold_very_complicated() {
        return Replacer.unfold(string);
    }

    @Benchmark
    public String unfold_unfold_common() {
        return Replacer.unfold_common(string);
    }

    @Benchmark
    public String unfold_unfold_olivier() {
        return Replacer.unfold_olivier(string);
    }

    @Benchmark
    public String unfold_unfold_optim_olivier() {
        return Replacer.unfold3(string);
    }

    @Benchmark
    public String unfold_cedric() {
        return Replacer.unfold_cedric(string);
    }

    @Benchmark
    public String unfold_cedric_improved() {
        return Replacer.unfold_cedric_improved(string);
    }

    @Benchmark
    public String unfold_cedric_ultimate() {
        return Replacer.unfold_cedric_ultimate(string);
    }

    @Benchmark
    public String unfold_cedric_ultimate2() {
        return Replacer.unfold_cedric_ultimate2(string);
    }


    public static void main(String[] args) {
        // Dummy main to check empirical correctness of an algorithm, using the regexp version as the reference
        for (int i = 0; i < 10000; i++) {
            String str = randomAlphanumericString(20) + "\n\r " + randomAlphanumericString(30) + "\n "
                    + randomAlphanumericString(30);
            String orig = str.replace('\n', 'N').replace('\r', 'R').replace(' ', '_');
            String ref = Replacer.unfold_regexp(str).replace('\n', 'N').replace('\r', 'R').replace(' ', '_');
            String cmp = Replacer.unfold_cedric_ultimate(str).replace('\n', 'N').replace('\r', 'R').replace(' ', '_');
            boolean equals = ref.equals(cmp);
            if (!equals) {
                System.err.println(orig);
                System.err.println(ref);
                System.err.println(cmp);
                System.err.println("---------------------------");
            }
        }
    }
} 
