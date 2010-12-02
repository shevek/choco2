/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package maif.parser;

import jxl.*;
import jxl.read.biff.BiffException;
import maif.entities.Person;
import maif.entities.Preference;

import java.util.*;
import java.io.File;
import java.io.IOException;

import choco.kernel.model.variables.integer.IntegerVariable;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Jul 20, 2009
 * Time: 5:56:09 PM
 */

public class FileReader {

    ArrayList<Date> days;
    HashMap<Preference,ArrayList<Person>> prefMap;
    HashMap<IntegerVariable,Preference> varMap;
    HashSet<Person> bouchon;

    static String[] months = {"Janvier","Fevrier","Mars","Avril","Mai","Juin","Juillet","Aout","Septembre","Octobre","Novembre","Decembre"};


    public FileReader()
    {
        this.days = new ArrayList<Date>();
        this.prefMap = new HashMap<Preference,ArrayList<Person>>();
        this.varMap = new HashMap<IntegerVariable,Preference>();
        this.bouchon = new HashSet<Person>();


    }

    public ArrayList<Person> makePeople(int month,int year, File planning, File preference) {

        days.clear();
        ArrayList<Person> people = new ArrayList<Person>();
        HashMap<String,Person> names = new HashMap<String,Person>();

        HashMap<Integer, Date> columnDay = new HashMap<Integer,Date>();

        try {
            Workbook wb = Workbook.getWorkbook(planning);
            Sheet sheet = wb.getSheet(0);
            for (Sheet s : wb.getSheets())
            {
                if (s.getName().equals(Integer.toString(year)))
                    sheet = s;
            }

            Cell[] col = sheet.getColumn(0);

            boolean found = false;
            int idx = 0;
            int nbdays = 0;
            Cell cons;
            while (!found)
            {
                cons = col[idx];
                if (removeSpaces(cons.getContents()).equalsIgnoreCase(months[month-1]))
                    found = true;
                else
                    idx++;
            }

            int i = 1;
            cons = sheet.getCell(i,idx+1);
            while (!(cons.getContents()).equals("") && i < sheet.getColumns() )
            {
                nbdays++;
                DateCell dcell = (DateCell)cons;
                Date d = dcell.getDate();
                this.days.add(d);
                columnDay.put(i,d);
                i++;
                if (i < sheet.getColumns())
                    cons = sheet.getCell(i,idx+1);

            }

            int j = 2;

            while(idx+j < sheet.getRows() && !col[idx+j].getContents().equals(""))
            {
                Person p = new Person(removeSpaces(col[idx+j].getContents()));
                names.put(p.getName(),p);
                for (i = 1 ; i <= nbdays ;i++)
                {
                    int val;
                    jxl.format.CellFormat format = sheet.getCell(i,idx+j).getCellFormat();
                    if (format != null)
                    {
                        val = format.getBackgroundColour().getValue();
                        if (val == 42)
                        p.addHollyday(columnDay.get(i));
                    }
                }
                people.add(p);
                p.setIndex(people.size()-1);

                j++;


            }

            // Loading Preferences
            wb = Workbook.getWorkbook(preference);
            sheet = wb.getSheet(0);

            for (j = 9 ; j < sheet.getRows();j++)
            {
                String n = removeSpaces(sheet.getCell(1,j).getContents());
                names.get(n).setPriority(true);
                this.bouchon.add(names.get(n));
            }

            Cell[] days = sheet.getColumn(0);
            ArrayList<Date> dates = new ArrayList<Date>();

            for (j = 2 ; j < days.length ; j++)
            {
                String d = removeSpaces(sheet.getCell(0,j).getContents().toLowerCase());
                fillDates(dates,d);
                for (i = 2 ; i < sheet.getColumns() ; i++)
                {
                    String[] name = sheet.getCell(i,j).getContents().split(",");
                    for (String norm : name)
                    {
                        String n = removeSpaces(norm);
                        try {
                            Person p = names.get(n);
                            for (Date date : dates)
                            {
                               // System.out.println(++bui);
                                Preference pref = p.addPreference(date,i-2);
                                ArrayList<Person> tmp;
                                tmp = prefMap.get(pref);
                                if (tmp == null)
                                {
                                    tmp = new ArrayList<Person>();
                                    prefMap.put(pref,tmp);
                                }
                                tmp.add(p);


                            }
                        }
                        catch (NullPointerException e)
                        {
                            System.err.println("Attention : "+n+" n'est pas de le planning");
                        }
                    }
                }

            }











        } catch (IOException e) {
            System.err.println("Invalid file name");
        } catch (BiffException e) {
            System.err.println("Invalid excel file");
        }

        return people;


    }

    private void fillDates(ArrayList<Date> dates, String d)
    {
        dates.clear();
        for (Date date : this.days)
        {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(date);
            if (d.equals("lundi") && cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)
            {
                dates.add(date);
            }
            if (d.equals("mardi") && cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY)
            {
                dates.add(date);
            }
            if (d.equals("mercredi") && cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY)
            {
                dates.add(date);
            }
            if (d.equals("jeudi") && cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY)
            {
                dates.add(date);
            }
            if (d.equals("vendredi") && cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
            {
                dates.add(date);
            }
            if (d.equals("samedi") && cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
            {
                dates.add(date);
            }
            if (d.equals("dimanche") && cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            {
                dates.add(date);
            }

        }

    }



    public HashSet<Person> getBouchon()
    {
        return bouchon;
    }

    public ArrayList<Date> getDays()
    {
        return this.days;
    }

    public HashMap<Preference,ArrayList<Person>> getPrefMap()
    {
        return this.prefMap;
    }


    public static String removeSpaces(String input)
    {
        int leftOffset = 0;
        int rightOffset = input.length();
        if (leftOffset < rightOffset)
        {
        while (input.charAt(leftOffset) == ' ')
            leftOffset++;
        while (input.charAt(rightOffset-1) == ' ')
            rightOffset--;

        return input.substring(leftOffset,rightOffset);
        }
        else
            return input;

    }

    /* private static Day parseDay(String tmp, int month, int year)
   {
       int day  = Integer.parseInt(tmp.split("-")[0]);
       return new Day(day,month,year);


   } */

}