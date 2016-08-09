using System;
using System.Collections.Generic;
using System.Data;
using MySql.Data.MySqlClient;
using System.Data.SqlClient;
using System.Data.Common;
using System.Web.Configuration;
using System.Configuration;
using System.Globalization;

namespace Surfvind_2011.CrawlSpace
{
    public class CrawlSpaceDatabaseConnection
    {

        #region Declarations
        bool isMySqlDB = false;
        public String nameOfDatabase = "";
        public String imei;
        #endregion

        public CrawlSpaceDatabaseConnection(string i)
        {
            isMySqlDB = true;
            nameOfDatabase = "Krypgrund_data";
            imei = i;

        }
        public void SetImei(string i)
        {
            imei = i;
        }

        #region Database stuff
        string GetMySqlConnStr()
        {
            if (Convert.ToBoolean(ConfigurationManager.AppSettings["Debug"]))
            {
                return WebConfigurationManager.ConnectionStrings["mySqlConnStrDebug"].ConnectionString;
            }
            else
            {
                return WebConfigurationManager.ConnectionStrings["mySqlConnStr"].ConnectionString;
            }
        }

        string GetSQLConnStr()
        {
            return WebConfigurationManager.ConnectionStrings["mySqlConnStrDebug"].ConnectionString;
        }

        public string GetDBConnString()
        {
            return isMySqlDB ? GetMySqlConnStr() : GetSQLConnStr();
        }

        DbCommand GetDBCommand(string cmd, DbConnection conn)
        {
            return isMySqlDB ? (DbCommand)new MySqlCommand(cmd, (MySqlConnection)conn) : (DbCommand)new SqlCommand(cmd, (SqlConnection)conn);
        }

        DbConnection GetDbConnection(string connStr)
        {
            return isMySqlDB ? (DbConnection)new MySqlConnection(connStr) : (DbConnection)new SqlConnection(connStr);
        }

        DbParameter GetDBParam(string paramName, object paramValue)
        {
            return isMySqlDB ? (DbParameter)new MySqlParameter("?" + paramName, paramValue) : (DbParameter)new SqlParameter("@" + paramName, paramValue);
        }
        #endregion

        #region ObtainingData

        public CrawlSpaceMeasurements GetCurrentData()
        {
            CrawlSpaceMeasurements data = new CrawlSpaceMeasurements();

            using (DbConnection conn = GetDbConnection(GetDBConnString()))
            using (DbCommand cmd = GetDBCommand("select * from " + nameOfDatabase + " WHERE imei='" + imei + "' order by TimeStamp desc limit 0,1", conn))
            {
                conn.Open();
                DbDataReader reader = cmd.ExecuteReader(CommandBehavior.SingleRow);
                if (reader.Read())
                {
                    data.TimeStamp.Add(reader["TimeStamp"].ToString());
                    data.AbsolutFuktInne.Add(int.Parse(reader["AbsolutFuktInne"].ToString()));
                    data.AbsolutFuktUte.Add(int.Parse(reader["AbsolutFuktUte"].ToString()));
                    data.FuktInne.Add(float.Parse(reader["FuktInne"].ToString()));
                    data.FuktUte.Add(float.Parse(reader["FuktUte"].ToString()));
                    data.TempInne.Add(float.Parse(reader["TempInne"].ToString()));
                    data.TempUte.Add(float.Parse(reader["TempUte"].ToString()));
                    bool fanOn = bool.Parse(reader["FanOn"].ToString());
                    if (fanOn)
                    {
                        data.FanOn.Add(90);
                    }
                    else
                    {
                        data.FanOn.Add(10);
                    }


                }
            }
            return data;
        }


        private string GetMySqlCommand(TimeInterval interval, String imei)
        {
            String mySqlCommand = $" FROM `Krypgrund_data` WHERE Imei='{imei}' AND ";
            String firstPart = "";
            String timePart = "";
            String groupPart = "";
            DateTime now= DateTime.Now;
            switch (interval)
            {
                case TimeInterval.OneHour:
                    now =DateTime.Now.AddHours(-1);
                    firstPart = "SELECT *";
                    timePart = $"TimeStamp > '{now}'";
                    groupPart = ""; //Intentionally left blank.
                    break;
                case TimeInterval.FiveHours:
                    now = DateTime.Now.AddHours(-5);
                    firstPart = "SELECT *";
                    timePart = $"TimeStamp > '{now}'";
                    groupPart = ""; //Intentionally left blank.
                    break;
                case TimeInterval.OneDay:
                    now = DateTime.Now.AddHours(-24);
                    firstPart = "SELECT *";
                    timePart = $"TimeStamp > '{now}'";
                    groupPart = ""; //Intentionally left blank.
                    break;
                case TimeInterval.OneMonth:
                    now = DateTime.Now.AddDays(-30);
                    firstPart = "SELECT extract(YEAR_MONTH FROM TimeStamp) as YearMonth, extract(DAY_HOUR FROM TimeStamp) as DayHour, AVG(FuktInne) as FuktInne, AVG(FuktUte) as FuktUte, AVG(TempInne) as TempInne, AVG(TempUte) as TempUte, AVG(AbsolutFuktInne) as AbsolutFuktInne, AVG(AbsolutFuktUte) as AbsolutFuktUte, AVG(FanOn) as FanOn";
                    timePart = $"TimeStamp > '{now}'";
                    groupPart = "GROUP BY extract(YEAR_MONTH FROM TimeStamp), extract(DAY_HOUR FROM TimeStamp)";
                    break;
                case TimeInterval.OneYear:
                    now = DateTime.Now.AddDays(-365);
                    firstPart = "SELECT extract(YEAR_MONTH FROM TimeStamp) as YearMonth, extract(DAY_HOUR FROM TimeStamp) as DayHour, AVG(FuktInne) as FuktInne, AVG(FuktUte) as FuktUte, AVG(TempInne) as TempInne, AVG(TempUte) as TempUte, AVG(AbsolutFuktInne) as AbsolutFuktInne, AVG(AbsolutFuktUte) as AbsolutFuktUte, AVG(FanOn) as FanOn";
                    timePart = $"TimeStamp >'{now}'";
                    groupPart = "GROUP BY extract(YEAR_MONTH FROM TimeStamp), extract(DAY_HOUR FROM TimeStamp)";
                    break;

            }
            mySqlCommand = firstPart + mySqlCommand +timePart+ groupPart;
            String.Format(mySqlCommand, imei, now);
            return mySqlCommand;
        }
        public CrawlSpaceMeasurements GetMeasurements(TimeInterval interval)
        {
            String mySqlCommand = GetMySqlCommand(interval, imei);

            using (DbConnection conn = GetDbConnection(GetDBConnString()))
            {
                CrawlSpaceMeasurements list = new CrawlSpaceMeasurements();
                conn.Open();
                
                using (DbCommand cmd = new MySqlCommand(mySqlCommand, (MySqlConnection)conn))
                {
                    DbDataReader reader = cmd.ExecuteReader();

                    while (reader.Read())
                    {
                        try
                        {
                            if (interval == TimeInterval.OneMonth || interval == TimeInterval.OneYear)
                            {
                                String time = reader["YearMonth"].ToString();
                                String tmp =  reader["DayHour"].ToString();
                                if (tmp.Length == 3) tmp = "0" + tmp;
                                time += tmp;
                  
                                DateTime timeStamp;
                                if (DateTime.TryParseExact(time, "yyyyMMddHH", CultureInfo.InvariantCulture, DateTimeStyles.None, out timeStamp))
                                {
                                    list.TimeStamp.Add(timeStamp.ToString("yyyy-MM-dd HH:mm:ss"));
                                }
                            }
                            else
                            {
                                list.TimeStamp.Add(reader["TimeStamp"].ToString());

                            }
                            list.AbsolutFuktInne.Add((int)float.Parse(reader["AbsolutFuktInne"].ToString()));
                            list.AbsolutFuktUte.Add((int)float.Parse(reader["AbsolutFuktUte"].ToString()));
                            list.FuktInne.Add(float.Parse(reader["FuktInne"].ToString()));
                            list.FuktUte.Add(float.Parse(reader["FuktUte"].ToString()));
                            list.TempInne.Add(float.Parse(reader["TempInne"].ToString()));
                            list.TempUte.Add(float.Parse(reader["TempUte"].ToString()));
                            double fanOn = double.Parse(reader["FanOn"].ToString());
                            if (fanOn > 0.45)
                            {
                                list.FanOn.Add(90);
                            }
                            else
                            {
                                list.FanOn.Add(10);
                            }
                        }
                        catch (Exception ee) { }
                    }
                    reader.Close();



                }
                return list;
            }
        }


        public List<Location> GetLocations()
        {
            //string cmdText = string.Format("select distinct imei from Surfvind_data WHERE 1");
            DateTime start = DateTime.Now;
            start = start.AddDays(-7);
            string d = String.Format("{0:yyyy-MM-dd HH:mm}", start);          // "03/09/2008"

            string cmdText = string.Format("SELECT distinct Location,Surfvind_location.imei, Latitiud, Longitud FROM `Surfvind_location`,Krypgrund_data WHERE Surfvind_location.imei=Krypgrund_data.Imei and Krypgrund_data.TimeStamp > \"" + d + "\"");
            using (DbConnection conn = GetDbConnection(GetDBConnString()))
            {
                List<Location> list = new List<Location>();
                conn.Open();
                using (DbCommand cmd = GetDBCommand(cmdText, conn))
                {
                    DbDataReader reader = cmd.ExecuteReader();
                    while (reader.Read())
                    {
                        Location wr = new Location(reader["Location"].ToString(), reader["imei"].ToString(), reader["Longitud"].ToString(), reader["Latitiud"].ToString());
                        list.Add(wr);
                    }
                }
                return list;
            }
        }

        #endregion

        public string InsertMeasurements(CrawlSpaceMeasurements data)
        {
            String result = "Data inserted OK";
            int rowsAffected = 0;

            string baseText = "INSERT INTO " + nameOfDatabase + " SET imei = " + imei +",";
          
            using (DbConnection conn = GetDbConnection(GetDBConnString()))
            {
                try
                {
                    conn.Open();
                    for (int i = 0; i< data.TimeStamp.Count; i++){
                        string cmdText = baseText + " TimeStamp = '" + data.TimeStamp[i] +
                            "',AbsolutFuktInne ='" + data.AbsolutFuktInne[i] +
                            "',AbsolutFuktUte ='" + data.AbsolutFuktUte[i] +
                            "',FuktInne ='" + data.FuktInne[i] +
                            "',FuktUte ='" + data.FuktUte[i] +
                            "',TempInne ='" + data.TempInne[i] +
                            "',TempUte ='" + data.TempUte[i] +
                            "',FanOn ='" + data.FanOn[i] + "'";

                        using (DbCommand cmd = GetDBCommand(cmdText, conn))
                        {
                            rowsAffected += cmd.ExecuteNonQuery();
                        }
                    }
                }
                catch (DbException exDb)
                {
                    result = "\nDbException.GetType: " + exDb.GetType() +
                             "\nDbException.Source: " + exDb.Source +
                             "\nDbException.ErrorCode: " + exDb.ErrorCode +
                             "\nDbException.Message: " + exDb.Message;

                }
                // Handle all other exceptions. 
                catch (Exception ex)
                {
                    result = "Exception.Message: " + ex.Message;
                }
                result += "\n\n" + rowsAffected + " rows was successfully inserted";

            }
            return result;
        }
        }
}
