package com.wknight.safe.signer;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileStringMgr {
    private static String RN = "\r\n";

    public String getProfileString(String file, String section, String variable, String defaultValue)
            throws IOException
    {
        String valueString = "";
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        boolean isInSection = false;
        try
        {
            String strLine;
            while ((strLine = bufferedReader.readLine()) != null)
            {
                strLine = strLine.trim();

                Pattern p = Pattern.compile("\\[\\s*" + section + "\\s*\\]");
                Pattern p2 = Pattern.compile("\\[.*\\]");
                Matcher m = p.matcher(strLine);
                Matcher m2 = p2.matcher(strLine);
                if (m2.find()) {
                    if (m.find()) {
                        isInSection = true;
                    } else {
                        isInSection = false;
                    }
                }
                if (isInSection)
                {
                    strLine = strLine.trim();
                    String[] strArray = strLine.split("=");
                    if (strArray.length == 2)
                    {
                        valueString = strArray[0].trim();
                        if (valueString.equalsIgnoreCase(variable))
                        {
                            valueString = strLine.substring(strLine.indexOf("=") + 1).trim();
                            String str1 = valueString;return str1;
                        }
                    }
                }
            }
        }
        catch (Exception localException) {}finally
        {
            bufferedReader.close();
        }
        return defaultValue;
    }

    public boolean setProfileString(String file, String section, String variable, String value)
            throws IOException
    {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        boolean isInSection = false;
        String fileContent = "";
        try
        {
            String allLine;
            while ((allLine = bufferedReader.readLine()) != null)
            {
                allLine = allLine.trim();

                Pattern p = Pattern.compile("\\[\\s*" + section + "\\s*\\]");
                Pattern p2 = Pattern.compile("\\[.*\\]");
                Matcher m = p.matcher(allLine);
                Matcher m2 = p2.matcher(allLine);
                if (m2.find()) {
                    if (m.find()) {
                        isInSection = true;
                    } else {
                        isInSection = false;
                    }
                }
                if (isInSection)
                {
                    String[] strArray = allLine.split("=");
                    if (allLine.indexOf("=") > 0)
                    {
                        String valueString = strArray[0].trim();
                        if (valueString.equalsIgnoreCase(variable))
                        {
                            String newLine = valueString + "=" + value;
                            fileContent = fileContent + newLine + RN;
                        }
                        else
                        {
                            fileContent = fileContent + allLine + RN;
                        }
                    }
                    else
                    {
                        fileContent = fileContent + allLine + RN;
                    }
                }
                else if (allLine.indexOf("=") > 0)
                {
                    fileContent = fileContent + allLine + RN;
                }
                else
                {
                    fileContent = fileContent + allLine + RN;
                }
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, false));
            bufferedWriter.write(fileContent);
            bufferedWriter.flush();
            bufferedWriter.close();
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            bufferedReader.close();
        }
        return false;
    }
}
