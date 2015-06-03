using System;
using System.Data;
using System.Web;
using MySql.Data;
using MySql.Data.MySqlClient;
using System.Collections.Generic;
using System.Data.SqlClient;
using System.Data.Common;
using System.Web.Configuration;
using System.Configuration;

namespace Surfvind_2011
{
    public class WindData
    {
        #region Declarations
        bool isMySqlDB = false;
        public String dbToUse = "";
        public String imei;
        #endregion

        public WindData(bool isMySQL, String idb)
        {
            isMySqlDB = isMySQL;
            dbToUse = idb;

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

        public List<WindRecord> GetFullList()
        {
            string cmdText = "select * from " + dbToUse + " imei='" + imei + "'order by time";
            using (DbConnection conn = GetDbConnection(GetDBConnString()))
            {
                conn.Open();
                List<WindRecord> list = new List<WindRecord>();
                using (DbCommand cmd = GetDBCommand(cmdText, conn))
                {
                    DbDataReader reader = cmd.ExecuteReader();
                    while (reader.Read())
                    {
                        WindRecord wr = new WindRecord();
                        wr.Time = Convert.ToDateTime(reader["time"]);
                        wr.AverageDirection = int.Parse(reader["averageDir"].ToString());
                        wr.MaxDirection = int.Parse(reader["maxDir"].ToString());
                        wr.MinDirection = int.Parse(reader["minDir"].ToString());
                        wr.AverageSpeed = float.Parse(reader["averageSpeed"].ToString());
                        wr.MaxSpeed = float.Parse(reader["maxSpeed"].ToString());
                        wr.MinSpeed = float.Parse(reader["minSpeed"].ToString());
                        list.Add(wr);
                    }
                }
                return list;
            }
        }

        public List<WindRecord> GetListBetweenDate(DateTime startDate, DateTime endDate)
        {
            string cmdText = string.Format("select * from " + dbToUse + " WHERE imei='" + imei + "' AND time between {0} order by time desc", (isMySqlDB ? "?start and ?end" : "@start and @end"));
            using (DbConnection conn = GetDbConnection(GetDBConnString()))
            {
                List<WindRecord> list = new List<WindRecord>();
                conn.Open();
                using (DbCommand cmd = GetDBCommand(cmdText, conn))
                {
                    cmd.Parameters.Add(GetDBParam("start", startDate));
                    cmd.Parameters.Add(GetDBParam("end", endDate));
                    DbDataReader reader = cmd.ExecuteReader();
                    while (reader.Read())
                    {
                        WindRecord wr = new WindRecord();
                        wr.Time = Convert.ToDateTime(reader["time"]);
                        wr.AverageDirection = int.Parse(reader["averageDir"].ToString());
                        wr.MaxDirection = int.Parse(reader["maxDir"].ToString());
                        wr.MinDirection = int.Parse(reader["minDir"].ToString());
                        wr.AverageSpeed = float.Parse(reader["averageSpeed"].ToString());
                        wr.MaxSpeed = float.Parse(reader["maxSpeed"].ToString());
                        wr.MinSpeed = float.Parse(reader["minSpeed"].ToString());
                        list.Add(wr);
                    }
                }
                return list;
            }
        }

        public List<WindRecord> GetListBetweenDate2(DateTime startDate, DateTime endDate)
        {
            TimeSpan interval = endDate.Subtract(startDate);
            TimeSpan t = new TimeSpan(interval.Ticks / 50);
            endDate = startDate.Add(t);
            List<WindRecord> list = new List<WindRecord>();

            try
            {
                using (DbConnection conn = GetDbConnection(GetDBConnString()))
                {
                    conn.Open();
                    for (int i = 0; i < 50; i++)
                    {
                        string cmdText = string.Format("select avg(averageDir),max(maxDir),min(minDir),avg(averageSpeed),max(maxSpeed),min(minSpeed) from " + dbToUse + " WHERE imei='" + imei + "' AND time >\"" + startDate.ToString() + "\" and time <\"" + endDate.ToString() + "\" order by time desc");
                        using (DbCommand cmd = GetDBCommand(cmdText, conn))
                        {
                            DbDataReader reader = cmd.ExecuteReader();
                            while (reader.Read())
                            {

                                WindRecord wr = new WindRecord();
                                //wr.Time = Convert.ToDateTime(reader["time"]);
                                if (reader["avg(averageDir)"] != null)
                                {
                                    string test = reader["avg(averageDir)"].ToString();
                                    if (test.Equals("null") || test.Equals(""))
                                    {
                                        continue;
                                    }
                                    wr.Time = endDate;//
                                    wr.AverageDirection = (int)float.Parse(reader["avg(averageDir)"].ToString());
                                    wr.MaxDirection = (int)float.Parse(reader["max(maxDir)"].ToString());
                                    wr.MinDirection = (int)float.Parse(reader["min(minDir)"].ToString());
                                    wr.AverageSpeed = float.Parse(reader["avg(averageSpeed)"].ToString());
                                    wr.MaxSpeed = float.Parse(reader["max(maxSpeed)"].ToString());
                                    wr.MinSpeed = float.Parse(reader["min(minSpeed)"].ToString());
                                    list.Add(wr);

                                }
                                else
                                {

                                }
                            }
                            reader.Close();
                        }
                        startDate = startDate.Add(t);
                        endDate = endDate.Add(t);
                    }
                    list.Reverse();

                }

            }
            catch (Exception e)
            {

                int a = 0;
                a++;
            }
            return list;
        }

        public List<Location> GetLocations()
        {
            //string cmdText = string.Format("select distinct imei from Surfvind_data WHERE 1");
            DateTime start = DateTime.Now;
            start = start.AddDays(-7);
            string d = String.Format("{0:yyyy-MM-dd HH:mm}", start);          // "03/09/2008"

            string cmdText = string.Format("SELECT distinct Location,Surfvind_location.imei, Latitiud, Longitud FROM `Surfvind_location`,Surfvind_data WHERE Surfvind_location.imei=Surfvind_data.imei and Surfvind_data.Time > \"" + d + "\"");
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

        #region # Aggregated functions
        public float GetMiddleSpeedValueBetweenDate(DateTime startDate, DateTime endDate)
        {
            string cmdText = string.Format("SELECT AVG(averageSpeed) as AvgSpeed from " + dbToUse + " where imei='" + imei + "'time between {0} ", (isMySqlDB ? "?start and ?end" : "@start and @end"));
            using (DbConnection conn = GetDbConnection(GetDBConnString()))
            {
                float rez = -1;
                conn.Open();
                using (DbCommand cmd = GetDBCommand(cmdText, conn))
                {
                    cmd.Parameters.Add(GetDBParam("start", startDate));
                    cmd.Parameters.Add(GetDBParam("end", endDate));
                    DbDataReader reader = cmd.ExecuteReader();
                    if (reader.Read())
                    {
                        string s = reader["AvgSpeed"].ToString();
                        if (!string.IsNullOrEmpty(s))
                        {
                            rez = float.Parse(s);
                        }
                        else
                        {
                            rez = 0;
                        }
                    }
                }
                return rez;
            }
        }

        public float GetTopDirectionValueBetweenDate(DateTime startDate, DateTime endDate)
        {
            /*SELECT averageDir AS TopDirection from "+dbToUse+" WHERE time = (select MAX(time) from "+dbToUse+")*/
            // use AVG to once value to return null if not have result instead empty result

            //string cmdText = string.Format("SELECT AVG(averageDir) AS TopDirection from "+dbToUse+" where (time = (select MAX(time) from "+dbToUse+")) and (time between {0}) ", (isMySqlDB ? "?start and ?end" : "@start and @end"));
            string cmdText = string.Format("SELECT AVG(averageDir) AS TopDirection from " + dbToUse + " where imei='" + imei + "' AND time between {0} ", (isMySqlDB ? "?start and ?end" : "@start and @end"));
            using (DbConnection conn = GetDbConnection(GetDBConnString()))
            {
                float rez = -1;
                conn.Open();
                using (DbCommand cmd = GetDBCommand(cmdText, conn))
                {
                    cmd.Parameters.Add(GetDBParam("start", startDate));
                    cmd.Parameters.Add(GetDBParam("end", endDate));
                    DbDataReader reader = cmd.ExecuteReader();
                    if (reader.Read())
                    {
                        string s = reader["TopDirection"].ToString();
                        if (!string.IsNullOrEmpty(s))
                        {
                            rez = float.Parse(s);
                        }
                        else
                        {
                            rez = 0;
                        }
                    }
                }
                return rez;
            }
        }
        #endregion

        public WindRecord GetCurrentWind()
        {
            WindRecord currWind = new WindRecord();

            using (DbConnection conn = GetDbConnection(GetDBConnString()))
            using (DbCommand cmd = GetDBCommand("select * from " + dbToUse + " WHERE imei='" + imei + "' order by time desc limit 0,1", conn))
            {
                conn.Open();
                DbDataReader reader = cmd.ExecuteReader(CommandBehavior.SingleRow);
                if (reader.Read())
                {
                    currWind.Time = Convert.ToDateTime(reader["time"]);
                    currWind.AverageDirection = int.Parse(reader["averageDir"].ToString());
                    currWind.MaxDirection = int.Parse(reader["maxDir"].ToString());
                    currWind.MinDirection = int.Parse(reader["minDir"].ToString());
                    currWind.AverageSpeed = float.Parse(reader["averageSpeed"].ToString());
                    currWind.MaxSpeed = float.Parse(reader["maxSpeed"].ToString());
                    currWind.MinSpeed = float.Parse(reader["minSpeed"].ToString());
                    // TODO, uncomment next two lines when the database has posts for temperature
                    currWind.AverageAirTemp = float.Parse(reader["airTemp"].ToString());
                    currWind.AverageWaterTemp = float.Parse(reader["waterTemp"].ToString());
                    currWind.Moisture = float.Parse(reader["moisture"].ToString());
                }
            }

            return currWind;
        }
    }

    public class Location : IComparable
    {
        public String Name = "";
        public String imei = "";
        public double Latitud;
        public double Longitude;
        public Location(String i, String j, String Long, String Lat)
        {
            Name = i;
            imei = j;
            Longitude = Convert.ToDouble(Long);
            Latitud = Convert.ToDouble(Lat);
        }

        public int CompareTo(object o)
        {
            Location target = (Location)o;
            return this.Name.CompareTo(target.Name);
        }
    }
    public class WindRecord
    {
        DateTime _time;
        int _averageDir;
        int _maxDir;
        int _minDir;
        float _averageSpeed;
        float _maxSpeed;
        float _minSpeed;
        float _averageWaterTemp;
        float _averageAirTemp;
        float _moisture;


        public DateTime Time
        {
            get { return _time; }
            set { _time = value; }
        }

        public int AverageDirection
        {
            get { return _averageDir; }
            set { _averageDir = value; }
        }

        public int MaxDirection
        {
            get { return _maxDir; }
            set { _maxDir = value; }
        }

        public int MinDirection
        {
            get { return _minDir; }
            set { _minDir = value; }
        }

        public float AverageSpeed
        {
            get { return _averageSpeed; }
            set { _averageSpeed = value; }
        }

        public float MaxSpeed
        {
            get { return _maxSpeed; }
            set { _maxSpeed = value; }
        }

        public float MinSpeed
        {
            get { return _minSpeed; }
            set { _minSpeed = value; }
        }

        // TODO
        public float AverageWaterTemp
        {
            get { return _averageWaterTemp; }
            set { _averageWaterTemp = value; }
        }

        // TODO
        public float AverageAirTemp
        {
            get { return _averageAirTemp; }
            set { _averageAirTemp = value; }
        }

        public float Moisture
        {
            get { return _moisture; }
            set { _moisture = value; }
        }
    }
}