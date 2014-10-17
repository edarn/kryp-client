using System;
using System.Web.Caching;
using System.Collections.Generic;
using System.Configuration;

using System.Net;
using System.IO;

using System.Data;
using System.Linq;
using System.Web;
using System.Web.Security;
using System.Web.UI.HtmlControls;
using System.Xml.Linq;

using System.Drawing;
using ZedGraph;
using ZedGraph.Web;

using System.Diagnostics;

namespace Surfvind_2011
{
    public partial class GenGraphs : System.Web.UI.Page
    {
        const int CountIntervals = 100;

        private float[] dirValues;
        private float[] speedValues;
        private String[] timeLabels;
        private float[] maxSpeedValues;
        private float[] minSpeedValues;

        private String location;
        private int interval;

        DateTime Start;

        protected void LogTime()
        {
            Logger.LogInfo("Duration: " + (DateTime.Now - Start).ToString());
            Start = DateTime.Now;
        }

        protected void Page_Load(object sender, EventArgs e)
        {
            Response.Cache.SetCacheability(System.Web.HttpCacheability.NoCache);
            string location = Request.QueryString["location"];
            string duration = Request.QueryString["duration"];
            if (Convert.ToBoolean(ConfigurationManager.AppSettings["Debug"]))
            {
              location = "354745031074596";  
              duration = "1"; 
            }

            update(location, duration);
            
        }

        /* There are two ways of using this site. Either no parameters, then all graphs for all locations will 
         * be generated, or with arguments (e.g. location=12345&duration=0) and only one graph will be generated
         */
        public void update(String location, String duration)
        {
            String dbToUse = "";
            dbToUse = "Surfvind_data";
            WindData wd = new WindData(true, dbToUse);
            bool isMySQL = Convert.ToBoolean(ConfigurationManager.AppSettings["isMySQL"]);

            List<Location> loc = wd.GetLocations();
            loc.Sort();

            if (!checkArguments(location, duration, loc))
            {  /* Update everything */
                foreach (Location l in loc)
                {
                    this.location = l.imei.ToString();
                    wd.SetImei(this.location);
                    for (int i = 0; i < 5; i++)
                    {
                        this.interval = i;
                        generateGraph(this.interval, this.location, wd);
                        createGraph();
                    }
                }
            }
            else
            {
                this.location = location;
                /* Safe, we know this value can be parsed, we've tried it before */
                this.interval = int.Parse(duration);
                wd.SetImei(this.location);
                generateGraph(this.interval, this.location, wd);
                createGraph();
            }
        }

        public void update2(String location)
        {
            String dbToUse = "";
            dbToUse = "Surfvind_data";
            WindData wd = new WindData(true, dbToUse);
            wd.SetImei(location);
            bool isMySQL = Convert.ToBoolean(ConfigurationManager.AppSettings["isMySQL"]);

            List<Location> loc = wd.GetLocations();
            loc.Sort();

                    for (int i = 0; i < 3/*5*/; i++)
                    {
                        this.interval = i;
                        generateGraph(this.interval, location, wd);
                    }
        }

        /* Check the arguments if we want to generate only one image for any given location and duration */
        private bool checkArguments(String location, String duration, List<Location> locations)
        {
            bool canParse;
            int dur;
            bool isValidLocation;

            if(location == null || duration == null) {
                return false;
            }

            canParse = int.TryParse(duration, out dur);
            if (canParse)
            {
                /* Check valid duration */
                if (dur < 0 || dur > 5)
                {
                    return false;
                }
                /* Interval is ok */
                isValidLocation = false;
                foreach (Location l in locations)
                {
                    if(l.imei.ToString().Equals(location)) {
                        isValidLocation = true;
                        break;
                    }
                }
                return isValidLocation;
            }
            return false;
        }

        public void generateGraph(int interval, String imei, WindData wd)
        {
            Start = DateTime.Now;
            try
            {
                DateTime endInterval = DateTime.Now;
                DateTime beginInterval = GetStartInterval(interval, endInterval);
                LogTime();

                WindRecord currentWind = wd.GetCurrentWind();
                if (currentWind.Time < DateTime.Now.AddHours(-1))
                {
                    currentWind.AverageSpeed = 0;
                    currentWind.MinSpeed = 0;
                    currentWind.MaxSpeed = 0;
                    currentWind.AverageDirection = 0;
                    currentWind.MinDirection = 0;
                    currentWind.MaxDirection = 0;
                }
                List<float> dirValues = new List<float>();
                List<string> timeLabels = new List<string>();
                List<float> speedValues = new List<float>();
                List<float> minValues = new List<float>();
                List<float> maxValues = new List<float>();
                LogTime();
                List<WindRecord> windData = wd.GetListBetweenDate2(beginInterval, endInterval);
                LogTime();

                foreach (WindRecord w in windData)
                {
                    minValues.Add(w.MinSpeed);
                    maxValues.Add(w.MaxSpeed);
                    speedValues.Add(w.AverageSpeed);
                    timeLabels.Add(w.Time.ToShortDateString() + "*" + w.Time.ToShortTimeString());
                    dirValues.Add(w.AverageDirection);
                }
                LogTime();

                this.dirValues = dirValues.ToArray();
                this.speedValues = speedValues.ToArray();
                this.timeLabels = timeLabels.ToArray();
                this.minSpeedValues = minValues.ToArray();
                this.maxSpeedValues = maxValues.ToArray();
                LogTime();

                /* TODO, move this functionality to an own method since we only need to do this once
                 * For no, lets settle with a check if this is the first time for any given location
                 */
                if (interval == 0)
                {
                    String path = HttpContext.Current.Server.MapPath("~/") + "Images/" + imei;
                    if (!System.IO.Directory.Exists(path))
                    {
                        System.IO.Directory.CreateDirectory(path);
                    }

                    Helper.GetWindSpeedPic(currentWind.AverageSpeed, currentWind.MinSpeed, currentWind.MaxSpeed, Server).Save(Server.MapPath("~/Images/" + imei + "_img_speed.png"));
                    LogTime();
                    Helper.GetCompassPic(currentWind.AverageDirection, currentWind.MinDirection, currentWind.MaxDirection, Server).Save(Server.MapPath("~/Images/" + imei + "_img_compass.png"));
                    LogTime();

                    /* Create temperature images */
                    int water_temp;
                    int air_temp;

                    water_temp = currentWind.AverageWaterTemp;
                    air_temp = currentWind.AverageAirTemp;
                    String test = Server.MapPath("~/Images/" + imei + "_img_water_temp.png");
                    Helper.getTempImage(water_temp).Save(Server.MapPath("~/Images/" + imei + "_img_water_temp.png"));
                    Helper.getTempImage(air_temp).Save(Server.MapPath("~/Images/" + imei + "_img_air_temp.png"));
                }
            }
            catch
            {
                int a = 0;
                a++;
                Debug.WriteLine("Problem1");
            }
        }

        float[] GetDirectionValuesToTime(int cntIntervals, DateTime begin, DateTime end, WindData wd)
        {
            List<float> result = new List<float>();
            TimeSpan t = end.Subtract(begin);
            double minutesStep = t.TotalMinutes / cntIntervals;
            // start with 1, because first value is 'current'
            for (int i = 0; i < cntIntervals; i++)
            {
                // begin of date interval
                DateTime first = begin.AddMinutes(minutesStep * i);
                // end of date interval
                DateTime last = begin.AddMinutes(minutesStep * (i + 1));
                if (last < first)
                    last = first.AddSeconds(5);
                result.Add(wd.GetTopDirectionValueBetweenDate(first, last));
            }
            return result.ToArray();
        }

		DateTime GetStartInterval(int ddlSelectedIndex, DateTime endInterval)
		{
			DateTime beginInterval = endInterval;
			switch (ddlSelectedIndex)
			{
				case 0: // 1 Hour
					beginInterval = beginInterval.AddHours(-1);
					break;
				case 1: // 5 Hours
					beginInterval = beginInterval.AddHours(-5);
					break;
				case 2: // 1 Day
					beginInterval = beginInterval.AddDays(-1);
					break;
				case 3: // 1 Week
					beginInterval = beginInterval.AddDays(-7);
					break;
				case 4: // 1 Month
					beginInterval = beginInterval.AddMonths(-1);
					beginInterval = beginInterval.AddDays(-1);
					break;
				case 5: // 1 Year
					beginInterval = beginInterval.AddYears(-1);
					beginInterval = beginInterval.AddDays(-1);
					break;
			}
			return beginInterval;
		}

        public void createGraph()
        {
            zgwCtl.Draw(false);
        }
        



        private double[] TranslateSpeedValues(float[] values)
        {
            List<double> arr = new List<double>();
            if (values != null && values.Length > 0)
            {
                for (int i = 0; i < values.Length; i++)
                {
                    arr.Add((double)values[i]);

                }
            }
            return arr.ToArray();
        }

        private XDate[] TranslateTimeValues(string[] values)
        {
            List<XDate> arr = new List<XDate>();
            for (int i = 0; i < values.Length; i++)
            {
                XDate a = new XDate(DateTime.ParseExact(values[i], "yyyy-MM-dd*HH:mm", null));
                arr.Add(a);
            }
            return arr.ToArray();
        }

        private double GetMaxValue(float[] values)
        {
            double rez = 0;

            foreach (double val in values)
            {
                if (val > rez)
                    rez = val;
            }
            return rez;
        }

        private double GetMinValue(double[] values)
        {
            double rez = 0;
            foreach (double val in values)
            {
                if (val < rez)
                    rez = val;
            }
            return rez;
        }

        protected void zgwCtl_OnRenderGraph(ZedGraphWeb zgw, Graphics g, MasterPane masterPane)
        {
            if (g != null)
            {
                g.Clear(Color.White);
            }
            double[] valuesY = TranslateSpeedValues(this.speedValues);
            XDate[] valuesX = TranslateTimeValues(this.timeLabels);
            GraphPane graphPane = null;
            if (masterPane != null)
            {
                graphPane = masterPane[0];
            }
            else
            {
                graphPane = new GraphPane();
            }
            PointPairList listMean = new PointPairList();
            PointPairList listMax = new PointPairList();
            PointPairList listMin = new PointPairList();
            PointPairList listSplineMax = new PointPairList();
            PointPairList listSplineMin = new PointPairList();
            PointPairList listSplineMean = new PointPairList();

            //Create PointPair lists to use later. (Either to plot them directly, or as below)
            for (int i = 0; i < valuesX.Length; i++)
            {
                listMean.Add(valuesX[i], valuesY[i]);
                listMax.Add(valuesX[i], maxSpeedValues[i]);
                listMin.Add(valuesX[i], minSpeedValues[i]);

            }
            listMax.Sort();
            listMin.Sort();
            listMean.Sort();

            //I´d whish the charts to be nice and "round", not edgy. Zed has a way of interpolating with Splines, making it easy
            //So lets create 150 new points to plot instead.
            int count = 150;
            for (int i = 0; i < count && valuesX.Length != 0; i++)
            {
                //interval size
                double x = listMax[0].X + (double)i * (listMax[valuesX.Length - 1].X - listMax[0].X) / count;

                // Zedgraph throws an exception here if we do not have enough data. 
                try
                {
                    listSplineMax.Add(x, listMax.SplineInterpolateX(x, 0.2));
                    listSplineMin.Add(x, listMin.SplineInterpolateX(x, 0.2));
                    listSplineMean.Add(x, listMean.SplineInterpolateX(x, 0.5));
                }
                catch (Exception)
                {
                    break;
                    // ignore
                }
            }

            // because next added Curve place in back to from, we display first max & min lines
            if (minSpeedValues != null || minSpeedValues.Length != 0)
            {
                LineItem minLineCurve = graphPane.AddCurve("Minimum Wind", listSplineMin, Color.Red, SymbolType.None);
                minLineCurve.Line.Width = 1.6f;
                //minLineCurve.Line.Fill = new Fill(Color.FromArgb(0, 230, 0), Color.FromArgb(0, 94, 0), 90F);
                minLineCurve.Line.Fill = new Fill(Color.White);
            }

            LineItem myCurve = graphPane.AddCurve("Average Wind", listSplineMean, Color.FromArgb(43, 75, 157), SymbolType.None);

            // Fill the area under the curve with a white-red gradient at 90 degrees
            //myCurve.Line.Fill = new Fill(Color.FromArgb(118, 164, 251), Color.FromArgb(118, 164, 251), 90F);
            myCurve.Line.Fill = new Fill(Color.FromArgb(152, 251, 152), Color.FromArgb(0, 255, 127), 90F);
            myCurve.Line.Width = 1.6f;
            myCurve.Line.IsAntiAlias = true;
            // Make the symbols opaque by filling them with white
            myCurve.Symbol.Fill = new Fill(Color.DarkGray);

            graphPane.XAxis.Scale.FontSpec.FontColor = Color.Black;

            graphPane.XAxis.Type = AxisType.Date;
            if (valuesX.Length > 0)
            {
                graphPane.XAxis.Scale.Min = (double)valuesX[valuesX.Length - 1];
                graphPane.XAxis.Scale.Max = (double)valuesX[0];
            }
            graphPane.XAxis.MajorGrid.IsVisible = true;

            graphPane.YAxis.Scale.Min = 0;
            graphPane.YAxis.Scale.Max = 15;
            if (GetMaxValue(maxSpeedValues) > 15)
            {
                graphPane.YAxis.Scale.Max = GetMaxValue(maxSpeedValues) + 1;
            }
            graphPane.YAxis.IsAxisSegmentVisible = false;

            graphPane.Y2Axis.Scale.Align = AlignP.Inside;
            graphPane.YAxis.Title.Text = "m/s";
            graphPane.YAxis.Title.FontSpec.Size = 22;

            graphPane.YAxis.MajorTic.IsBetweenLabels = false;
            graphPane.YAxis.MajorTic.Color = Color.DarkGray;
            graphPane.YAxis.MajorTic.IsInside = false;
            graphPane.YAxis.MajorTic.IsOpposite = false;
            graphPane.YAxis.MinorTic.Color = Color.DarkGray;
            graphPane.YAxis.MinorTic.IsInside = false;
            graphPane.YAxis.MinorTic.IsOpposite = false;
            graphPane.YAxis.MinorTic.IsOutside = false;
            graphPane.YAxis.MajorGrid.IsVisible = true;
            graphPane.YAxis.MinorGrid.IsVisible = false;
            graphPane.YAxis.Scale.Align = AlignP.Inside;
            graphPane.YAxis.Title.FontSpec.FontColor = Color.DarkGray;
            graphPane.YAxis.Color = Color.DarkGray;

            if (maxSpeedValues != null || maxSpeedValues.Length != 0)
            {
                LineItem maxLineCurve = graphPane.AddCurve("Maximum Wind", /*listMax*/listSplineMax, Color.Red, SymbolType.None);
                maxLineCurve.Line.Width = 1.6f;
                maxLineCurve.Line.Fill = new Fill(Color.FromArgb(152, 251, 152), Color.FromArgb(0, 255, 127), 90F);
            }

            // disable legend
            graphPane.Legend.IsVisible = false;
            graphPane.Legend.FontSpec.Size = 20;
            graphPane.Border.Color = Color.White;
            graphPane.BarSettings.Type = BarType.SortedOverlay;
            graphPane.Chart.Border.Color = Color.DarkGray;
            graphPane.Title.FontSpec.FontColor = Color.DarkGray;
            // to disable scaling
            //graphPane.IsPenWidthScaled = false;
            //graphPane.IsFontsScaled = false;			

            masterPane.AxisChange(g);

            // Draw wind direction arrows
            
            ImageObj[] dirImages = new ImageObj[dirValues.Length];
            int nbrOfPlots = Math.Min(20, dirImages.Length);

            float step, xMin, xMax;
            double next, yMin, yMax;

            step = 0;
            xMin = xMax = 0;
            if (valuesX.Length != 0)
            {
                xMin = valuesX[valuesX.Length - 1];
                xMax = valuesX[0];
            }

            yMin = yMax = 0;
            if (valuesY.Length != 0)
            {
                yMin = 0;
                yMax = graphPane.YAxis.Scale.Max;
            }

            step = (xMax - xMin) / nbrOfPlots;
            next = 0;

            Image image = Image.FromFile(Server.MapPath("~/design/wind_arrow.png"));
            float[] rot;

            rot = getDirections(dirValues, (float)dirValues.Length / (float)nbrOfPlots, nbrOfPlots);

            if (rot != null)
            {
                if (dirImages.Length < nbrOfPlots)
                {
                    for(int i = 0; i < nbrOfPlots - dirImages.Length; i++) {
                        next += step;
                    }
                    
                }
                for (int i = 0; i < dirImages.Length && i < rot.Length && i < nbrOfPlots; i++)
                {
                    dirImages[i] = new ImageObj(rotateImage(image, rot[i]), xMin + next, (yMax / 10) * 9, step, yMax / 7);
                    graphPane.GraphObjList.Add(dirImages[i]);
                    next += step;
                }
            }

            // Write the date
            if (valuesX.Length > 0)
            {
                DateTime date = valuesX[0];
                graphPane.Title.Text = getInfoText(date);
            }
            graphPane.Title.FontSpec.Size = 24;
            graphPane.Title.FontSpec.Fill.Color = Color.Black;

            // Get interval so we can name the picture
            String name = "graph_" + this.interval + ".png";
            
            
            try
            {
                String path = HttpContext.Current.Server.MapPath("/") + "Applet/" + location;
                if (!System.IO.Directory.Exists(path))
                {
                    System.IO.Directory.CreateDirectory(path);
                }

                masterPane.GetImage().Save(Server.MapPath("~/Applet/" + location + "/" + name), System.Drawing.Imaging.ImageFormat.Png);
            }
            catch (Exception)
            {
                Debug.WriteLine("Problem3");
                Debug.WriteLine("Exception raised. Location: " + location);
            }
        }

        private String getInfoText(DateTime date)
        {
            String infoText;

            infoText = date.ToShortDateString();
            if (interval == 0)
            {
                infoText += " last 1 hour";
            }
            else if (interval == 1)
            {
                infoText += " last 5 hours";
            }
            else if (interval == 2)
            {
                infoText += " last 24 hours";
            }
            else if (interval == 3)
            {
                infoText += " last 1 week";
            }
            else if (interval == 4)
            {
                infoText += " last 1 month";
            }

            return infoText;
        }

        public double[] GenerateArrayWithValue(int arraySize, double value) //where T:new()
        {
            List<double> result = new List<double>();
            for (int i = 0; i < arraySize; i++)
                result.Add(value - 0.1 * i);
            return result.ToArray();
        }

        private Image rotateImage(Image b, float angle)
        {
            try
            {
                if (b == null || angle < 0 || angle > 360)
                {
                    return null;
                }
                //create a new empty bitmap to hold rotated image
                Image returnBitmap = new Bitmap(b.Width, b.Height);
                //make a graphics object from the empty bitmap
                Graphics g = Graphics.FromImage(returnBitmap);
                //move rotation point to center of image
                g.TranslateTransform((float)b.Width / 2, (float)b.Height / 2);
                //rotate
                g.RotateTransform(angle);
                //move image back
                g.TranslateTransform(-(float)b.Width / 2, -(float)b.Height / 2);
                //draw passed in image onto graphics object
                g.DrawImage(b, new Point(0, 0));
                return returnBitmap;
            }
            catch 
            {
                Debug.WriteLine("Problem4");
                Debug.WriteLine("Exception when rotating image");
            }
            return null;
        }

        private RectangleF[] getArrowPositions(XDate[] timeValues, float step, int size)
        {
            RectangleF[] ret;

            ret = new RectangleF[size];

            long sum;
            XDate sumDate;
            for (int i = 0; i < size; i+=(int)step)
            {
                sum = 0;
                for(int j = i; j < i + (int)step; j++) 
                {
                    sum += timeValues[j].DateTime.Ticks;
                }
                sumDate = new XDate(XDate.MakeValidDate(((double)sum/step)));

                Debug.WriteLine(sumDate.ToString());

                ret[i] = new RectangleF();
            }

            return null;
        }

        /*
         * Calculates the average direction based on the step factor. 
         */
        private float[] getDirections(float[] dirValues, float step, int size)
        {
            /* Return null if no values */
            if (dirValues.Length == 0)
            {
                return null;
            }

            if (dirValues.Length < size)
            {
                return dirValues;
            }


            float[] ret = new float[size];
            /* first and last element of next calculation */
            int first, last;
            /* The overall direction for one step */
            float sum;

            first = 0;
            for (int i = 0; i < size; i++)
            {
                /* find out last element */
                last = (int)((i + 1) * step);
                sum = 0;
                for (int j = 1; j < last - first; j++)
                {
                    /* Get the movement based on the first element */
                    float a = dirValues[(dirValues.Length - 1) - (first + j)] - dirValues[(dirValues.Length - 1) - first];
                    float b = dirValues[(dirValues.Length - 1) - (first + j)] - (360 + dirValues[(dirValues.Length - 1) - first]);
                    if (Math.Abs(a) < Math.Abs(b))
                    {
                        sum += a;
                    }
                    else
                    {
                        sum += b;
                    }
                }
                /* Calculate the average movement and add it to the initial movement */
                ret[i] = dirValues[dirValues.Length - 1 - first] + (sum / (last - first));
                /* Get the movement in percent, so we can rotate the image with this value */
               // ret[i] = 100 * ret[i] / 360f;

                /* the new first is the old last */
                first = last;
            }

            return ret;

        }

    }
}