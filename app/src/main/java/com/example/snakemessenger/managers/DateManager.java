package com.example.snakemessenger.managers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DateManager {
    public static List<Integer> thirtyOneDaysMonths = Arrays.asList(1, 3, 5, 7, 8, 10, 12);

    public static String getLastActiveText(String now, String last) {
        String[] partsNow = now.split(" ");
        String[] partsLast = last.split(" ");

        String dateNow = partsNow[0];

        String dateLast = partsLast[0];
        String timeLast = partsLast[1];

        if (dateNow.equals(dateLast)) {
            return "today at " + timeLast;
        }

        if (dateLast.equals(dayBefore(dateNow))) {
            return "yesterday at " + timeLast;
        }

        int daysBetweenDates = daysBetween(dateNow, dateLast);

        if (daysBetweenDates != -1 && daysBetweenDates < 7) {
            SimpleDateFormat df = new SimpleDateFormat("EEEE", Locale.US);

            try {
                return df.format(Objects.requireNonNull(new SimpleDateFormat("dd/MM/yyyy", Locale.US).parse(dateLast))) + " at " + timeLast;
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return null;
        }

        return dateLast + " at " + timeLast;
    }

    private static int daysBetween(String now, String last) {
        Calendar cal1 = new GregorianCalendar();
        Calendar cal2 = new GregorianCalendar();

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

        try {
            Date dateNow = df.parse(now);
            Date dateLast = df.parse(last);

            assert dateNow != null;
            cal1.setTime(dateNow);

            assert dateLast != null;
            cal2.setTime(dateLast);

            return (int)( (dateNow.getTime() - dateLast.getTime()) / (1000 * 60 * 60 * 24));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static boolean isLeapYear(int year) {
        if (year % 4 == 0) {
            if (year % 100 == 0) {
                return year % 400 == 0;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    private static String dayBefore(String date) {
        String[] dateParts = date.split("/");

        int day = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]);
        int year = Integer.parseInt(dateParts[2]);

        /* Not the first day of the month */
        if (day > 1) {
            return String.format("%02d", day - 1) + "/" + String.format("%02d", month) + "/" + year;
            /* First day of the month */
        } else {
            /* Not the first month of the year */
            if (month > 1) {
                /* Last month had 31 days */
                if (thirtyOneDaysMonths.contains(month - 1)) {
                    return "31/" + String.format("%02d", month - 1) + "/" + year;
                    /* Last month had 30 days */
                } else if (month - 1 != 2) {
                    return "30/" + String.format("%02d", month - 1) + "/" + year;
                    /* Last month was February */
                } else {
                    if (isLeapYear(year)) {
                        return "29/02/" + year;
                    } else {
                        return "28/02/" + year;
                    }
                }
                /* First month of the year */
            } else {
                return "31/12/" + (year - 1);
            }
        }
    }

    public static String getLastMessageDate(String now, String last) {
        String[] partsNow = now.split(" ");
        String[] partsLast = last.split(" ");

        String dateNow = partsNow[0];

        String dateLast = partsLast[0];
        String timeLast = partsLast[1];

        if (dateNow.equals(dateLast)) {
            return timeLast;
        }

        int daysBetweenDates = daysBetween(dateNow, dateLast);

        if (daysBetweenDates != -1 && daysBetweenDates < 7) {
            SimpleDateFormat df = new SimpleDateFormat("E", Locale.US);

            try {
                return df.format(Objects.requireNonNull(new SimpleDateFormat("dd/MM/yyyy", Locale.US).parse(dateLast)));
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }

        SimpleDateFormat df = new SimpleDateFormat("E d", Locale.US);

        try {
            return df.format(Objects.requireNonNull(new SimpleDateFormat("dd/MM/yyyy", Locale.US).parse(dateLast)));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
