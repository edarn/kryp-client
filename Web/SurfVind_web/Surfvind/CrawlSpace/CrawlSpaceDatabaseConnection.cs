using System;
using System.Collections.Generic;
using System.Data;
using MySql.Data.MySqlClient;
using System.Data.SqlClient;
using System.Data.Common;
using System.Web.Configuration;
using System.Configuration;

namespace Surfvind_2011.CrawlSpace
{
    public class CrawlSpaceDatabaseConnection
    {

        #region Declarations
        bool isMySqlDB = false;
        public String nameOfDatabase = "";
        public String imei;
        #endregion

        public CrawlSpaceDatabaseConnection(bool isMySQL, String databaseName)
        {
            isMySqlDB = isMySQL;
            nameOfDatabase = databaseName;

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

        public CrawlSpaceMeasurements GetListBetweenDate(DateTime startDate, DateTime endDate)
        {
            DateTime tempDate;
            TimeSpan span = endDate.Subtract(startDate);
            int steps = 300;
            double interval = span.TotalMilliseconds / steps;
            startDate.AddMilliseconds(interval);

      

            string cmdText = string.Format("select AVG(TimeStamp),AVG(AbsolutFuktInne),AVG(AbsolutFuktUte),AVG(FuktInne),AVG(FuktUte),AVG(TempInne),AVG(TempUte),AVG(FanOn) from " + nameOfDatabase + " WHERE imei='" + imei + "' AND TimeStamp between {0} order by TimeStamp desc", (isMySqlDB ? "?start and ?end" : "@start and @end"));
            using (DbConnection conn = GetDbConnection(GetDBConnString()))
            {
                CrawlSpaceMeasurements list = new CrawlSpaceMeasurements();
                conn.Open();
                using (DbCommand cmd = GetDBCommand(cmdText, conn))
                {
                    tempDate = startDate;
                    for (int i = 0; i < steps; i++)
                    {
                        cmd.Parameters.Clear();
                        cmd.Parameters.Add(GetDBParam("start", startDate));
                        cmd.Parameters.Add(GetDBParam("end", startDate.AddMilliseconds(interval)));
                        DbDataReader reader = cmd.ExecuteReader();

                        while (reader.Read())
                        {
                            try { 
                            list.TimeStamp.Add(startDate.ToString("yyyy-MM-dd HH:mm:ss"));
                            list.AbsolutFuktInne.Add((int)float.Parse(reader["AVG(AbsolutFuktInne)"].ToString()));
                            list.AbsolutFuktUte.Add((int)float.Parse(reader["AVG(AbsolutFuktUte)"].ToString()));
                            list.FuktInne.Add(float.Parse(reader["AVG(FuktInne)"].ToString()));
                            list.FuktUte.Add(float.Parse(reader["AVG(FuktUte)"].ToString()));
                            list.TempInne.Add(float.Parse(reader["AVG(TempInne)"].ToString()));
                            list.TempUte.Add(float.Parse(reader["AVG(TempUte)"].ToString()));
                            double fanOn = double.Parse(reader["AVG(FanOn)"].ToString());
                            if (fanOn> 0.45)
                            {
                                list.FanOn.Add(90);
                            }
                            else
                            {
                                list.FanOn.Add(10);
                            }
                            }catch(Exception ee) { }
                        }
                        reader.Close();

                        startDate = startDate.AddMilliseconds(interval);
                    }

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
    }
}
