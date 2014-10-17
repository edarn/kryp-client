using System;
using System.Web;
using @DR = System.Drawing;

using System.Drawing;
using System.Drawing.Imaging;
using System.Drawing.Drawing2D;

namespace WindInfo
{
	public class Helper
	{
		private Helper() { }

       	#region #Speed Image
		private static DR.Bitmap GetSpeedBitmap(int alpha, int minAlpha, int maxAlpha, HttpServerUtility server)
		{
			//Speed meter		
			DR.Bitmap speed = new DR.Bitmap(server.MapPath("~/Images/ws_speed.png"));
			DR.Graphics speedGr = DR.Graphics.FromImage(speed);

			//Speed arrow
			DR.Bitmap arrow = new DR.Bitmap(server.MapPath("~/Images/ws_speed_arrow.png"));
			DR.Graphics arrowGr = DR.Graphics.FromImage(arrow);

			// center of arrow image
			DR.PointF rotationCenterArrow = new DR.PointF((float)arrow.Width / 2 + 1, (float)arrow.Height - 21);
			//arrowGr.DrawLine(new Pen(Color.Red), new Point(0, arrow.Height - 21), new Point(arrow.Width, arrow.Height - 21));
			//arrowGr.DrawLine(new Pen(Color.Red), new Point(arrow.Width / 2 + 1, 0), new Point(arrow.Width / 2 + 1, arrow.Height));

			//Green marker
			DR.Bitmap greenMarker = new DR.Bitmap(server.MapPath("~/Images/ws_arrow_white.png"));
			DR.Graphics greenMarkerGr = DR.Graphics.FromImage(arrow);

			//Red marker
			DR.Bitmap redMarker = new DR.Bitmap(server.MapPath("~/Images/ws_arrow_grey.png"));
			DR.Graphics redMarkerGr = DR.Graphics.FromImage(arrow);

			//Temporary bitmap
			DR.Bitmap tempBmp = new DR.Bitmap(speed.Width, speed.Height);
			DR.Graphics tempGr = DR.Graphics.FromImage(tempBmp);

			// center of speed image
			DR.PointF rotationCenterSpeed = new DR.PointF((float)speed.Width / 2 - 2 + 2, (float)speed.Height / 2 + 2 - 3);
			//PointF rotationCenterSpeed = new PointF((float)speed.Width / 2, (float)speed.Height / 2);
			//tempGr.DrawLine(new Pen(Color.Yellow), new Point(0, speed.Height / 2 + 2  - 3), new Point(speed.Width, speed.Height / 2 + 2  - 3));
			//tempGr.DrawLine(new Pen(Color.Yellow), new Point(speed.Width / 2 - 2 + 2, 0), new Point(speed.Width / 2 - 2 + 2, speed.Height));


			// draw light sector
			DR.Brush br = new DR.SolidBrush(DR.Color.FromArgb(30, 255, 200, 255));
			int dw = 12;
			tempGr.FillPie(br, new DR.Rectangle(dw, dw, speed.Width - 2 - dw * 2, speed.Height - 2 - dw * 2), minAlpha - 90, maxAlpha - minAlpha);

			//Draw green marker
			DR.Drawing2D.Matrix X = new DR.Drawing2D.Matrix();
			X.RotateAt(minAlpha, rotationCenterSpeed);
			tempGr.Transform = X;
			tempGr.DrawImage(greenMarker,
				new DR.PointF(rotationCenterSpeed.X - rotationCenterArrow.X, rotationCenterSpeed.Y - rotationCenterArrow.Y));
			//new DR.PointF(speed.Width / 2 - arrow.Width / 2 + 2, speed.Height / 2 - arrow.Height + 20));

			//Draw red marker
			X.Reset();
			X.RotateAt(maxAlpha, rotationCenterSpeed);
			tempGr.Transform = X;
			tempGr.DrawImage(redMarker,
				new DR.PointF(rotationCenterSpeed.X - rotationCenterArrow.X, rotationCenterSpeed.Y - rotationCenterArrow.Y));
			//new DR.PointF(speed.Width / 2 - arrow.Width / 2 + 2, speed.Height / 2 - arrow.Height + 20));

			//Draw arrow
			X.Reset();
			X.RotateAt(alpha, rotationCenterSpeed);
			tempGr.Transform = X;

			tempGr.DrawImage(arrow, new DR.PointF(rotationCenterSpeed.X - rotationCenterArrow.X, rotationCenterSpeed.Y - rotationCenterArrow.Y));
			X.Reset();

			//speedGr.DrawImage(tempBmp, new DR.Point(0, 0));
			tempBmp.MakeTransparent(tempBmp.GetPixel(0, 0));
			return tempBmp;
		}

		private static int ConvertToAngle(float windSpeed)
		{
			// -134 .. 134 == 0..40
			return (int)(-134 + (269) * windSpeed / 30);
		}

		public static DR.Bitmap GetWindSpeedPic(float windSpeed, float minSpeed, float maxSpeed, HttpServerUtility server)
		{
			return GetSpeedBitmap(ConvertToAngle(windSpeed), ConvertToAngle(minSpeed), ConvertToAngle(maxSpeed), server);
		}
		#endregion

		#region #Compass Image
        private static DR.Bitmap GetCompassBitmap(int avAngle, int minAngle, int maxAngle, HttpServerUtility server)
		{
			//Compass circle
			DR.Bitmap compass = new DR.Bitmap(server.MapPath("~/Images/ws_compass.png"));
			DR.Graphics compassGr = DR.Graphics.FromImage(compass);

			//Compass arrow
			DR.Bitmap arrow = new DR.Bitmap(server.MapPath("~/Images/ws_compass_arrow.png"));
			DR.Graphics arrowGr = DR.Graphics.FromImage(arrow);

			//Green marker
			DR.Bitmap greenMarker = new DR.Bitmap(server.MapPath("~/Images/ws_arrow_white.png"));
			DR.Graphics greenMarkerGr = DR.Graphics.FromImage(arrow);

			//Red marker
			DR.Bitmap redMarker = new DR.Bitmap(server.MapPath("~/Images/ws_arrow_grey.png"));
			DR.Graphics redMarkerGr = DR.Graphics.FromImage(arrow);

			//Temporary bitmap
			DR.Bitmap tempBmp = new DR.Bitmap(compass.Width, compass.Height);
			DR.Graphics tempGr = DR.Graphics.FromImage(tempBmp);

			
            // draw light sector
			DR.Brush br = new DR.SolidBrush(DR.Color.FromArgb(30, 255, 200, 255));
			int dw = 12;
            if ((maxAngle - minAngle) < 180)
            {
                tempGr.FillPie(br, new DR.Rectangle(dw, dw, compass.Width - 2 - dw * 2, compass.Height - 2 - dw * 2), minAngle - 90 + 3, maxAngle - minAngle);
            }
            else
            {
                tempGr.FillPie(br, new DR.Rectangle(dw, dw, compass.Width - 2 - dw * 2, compass.Height - 2 - dw * 2), maxAngle-90, 360-maxAngle);
                tempGr.FillPie(br, new DR.Rectangle(dw, dw, compass.Width - 2 - dw * 2, compass.Height - 2 - dw * 2), -90, minAngle);

           
            }
			//Draw green marker
			DR.Drawing2D.Matrix X = new DR.Drawing2D.Matrix();
			X.RotateAt(minAngle, new DR.PointF(compass.Width / 2, compass.Height / 2));
			tempGr.Transform = X;
			tempGr.DrawImage(greenMarker, new DR.PointF(compass.Width / 2 - greenMarker.Width / 2, compass.Height / 2 - arrow.Height / 2));

			//Draw red marker
			X.Reset();
			X.RotateAt(maxAngle, new DR.PointF(compass.Width / 2, compass.Height / 2));
			tempGr.Transform = X;
			tempGr.DrawImage(redMarker, new DR.PointF(compass.Width / 2 - redMarker.Width / 2, compass.Height / 2 - arrow.Height / 2));

			//Draw compass arrow
			X.Reset();
			X.RotateAt(avAngle, new DR.PointF(compass.Width / 2, compass.Height / 2));
			tempGr.Transform = X;
			tempGr.DrawImage(arrow, new DR.PointF(compass.Width / 2 - arrow.Width / 2, compass.Height / 2 - arrow.Height / 2));
			X.Reset();

			//compassGr.DrawImage(tempBmp, new DR.Point(0, 0));
			tempBmp.MakeTransparent(tempBmp.GetPixel(0, 0));
			return tempBmp;
		}

		public static DR.Bitmap GetCompassPic(int avDir, int minDir, int maxDir, HttpServerUtility server)
		{			
			float koeff = ((float)1400) / ((float)360);
			return GetCompassBitmap((int)(((float)avDir) / koeff), (int)(((float)minDir) / koeff), (int)(((float)maxDir) / koeff), server);
		}
		#endregion

        #region #Temperature Image
        /* Generate temperature image */
        public static Image getTempImage(int temp)
        {
            Image image;
            int width, height, rect_width, rect_height, rect_x, rect_y;
            Graphics g;
            Rectangle fillArea;

            width = 53;
            /* Each temp step is 2 pixels and we compensate with 30 for  the start 
             * So e.g. 15 degrees will make the image (15+30)*2 = 90 pixles high
             */
            height = 180;

            rect_width = 9;
            rect_height = (temp + 30) * 2;
            rect_x = 13;
            rect_y = 146 - rect_height;
            image = new Bitmap(width, height);

            g = Graphics.FromImage(image);

            /* Fill the entire image with red color */
            fillArea = new Rectangle(rect_x, rect_y, rect_width, rect_height);
            g.FillRectangle(GetBrush(fillArea, Color.DarkRed, Color.Red), fillArea);
            
            g.Dispose();

            return image;
        }
        #endregion

        #region Gradient Brush
        private static LinearGradientBrush GetBrush(Rectangle rect, Color c1, Color c2)
        {
            return new LinearGradientBrush(
              rect,
              c1,
              c2,
              LinearGradientMode.Vertical);
        }
        #endregion

		#region #Helped Methods
        /*
		public static float GetDirectionValueIndex(float val)
		{
			//"N"
			if (val > 187.5 * 0 && val <= 187.5 * 1)
				return 0f;
			//"NE"
			if (val > 187.5 * 1 && val <= 187.5 * 2)
				return 1f;
			//"E"
			if (val > 187.5 * 2 && val <= 187.5 * 3)
				return 2f;
			//"SE"
			if (val > 187.5 * 3 && val <= 187.5 * 4)
				return 3f;
			//"S"
			if (val > 187.5 * 4 && val <= 187.5 * 5)
				return 4f;
			//"SW"
			if (val > 187.5 * 5 && val <= 187.5 * 6)
				return 5f;
			// "W"
			if (val > 187.5 * 6 && val <= 187.5 * 7)
				return 6f;
			//"NW"
			if (val > 187.5 * 7 && val <= 1500)
				return 7f;
			// default value index is 0
			return 0f;
		}
        */
		#endregion
	}
}