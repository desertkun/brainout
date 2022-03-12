package com.desertkun.brainout.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringFunctions
{
    public static interface ReplaceFunction
    {
        public String replace(MatchResult from);
    }

    public static String StringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

    public static String generate(int length)
    {
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }

        return sb.toString();
    }

    public static String replaceWithFunction(String string, String regexp,  ReplaceFunction callback)
    {
        String result = "";

        if (string == null)
            return "";

        final Matcher matcher = Pattern.compile(regexp).matcher(string);
        int lastMatch = 0;
        while(matcher.find())
        {
            final MatchResult matchResult = matcher.toMatchResult();
            final String replacement = callback.replace(matchResult);
            result += string.substring(lastMatch, matchResult.start()) +
                    replacement;
            lastMatch = matchResult.end();
        }
        if (lastMatch < string.length())
            result += string.substring(lastMatch);
        return result;
    }

    public static String readFile(String path)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, "UTF-8");
    }

    public static String format(float d)
    {
        if(d == (int) d)
            return String.format("%d",(int)d);
        else
            return String.format("%s",d);
    }
}
