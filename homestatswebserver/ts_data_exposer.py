__author__ = 'ivanbahdanau'
import os
import BaseHTTPServer
import json
import time
import sys

import MySQLdb


HOST_NAME = '10.8.0.10'  # !!!REMEMBER TO CHANGE THIS!!!
PORT_NUMBER = 8080  # Maybe set this to 9000.


is_real_data = len(sys.argv) > 1 and str(sys.argv[1]) == 'prod'
if is_real_data:
    print 'Starting the app in production mode'
    import BMP085 as BMP085
    # Create sensor instance with default I2C bus (On Raspberry Pi either 0 or
    # 1 based on the revision, on Beaglebone Black default to 1).
    bmp = BMP085.BMP085()

class MyHandler(BaseHTTPServer.BaseHTTPRequestHandler):
    def do_HEAD(s):
        s.send_response(200)
        s.send_header("Content-type", "text/html")
        s.end_headers()

    def do_GET(s):
        """Respond to a GET request."""
        s.send_response(200)
        s.send_header("Content-type", "text/html")
        s.end_headers()
        if s.path == '/':
            txt = open(os.path.dirname(os.path.realpath(__file__)) + "/index.html")
            s.wfile.write(txt.read())
        elif str(s.path).startswith("/getData"):
            db = MySQLdb.connect(passwd='root', db='ivannaroom', user='root')
            cursor = db.cursor()
            query = "SELECT UNIX_TIMESTAMP(id),temperature FROM sensor_stats where id > DATE_ADD(CURDATE(),INTERVAL -1 DAY) order by id asc "
            cursor.execute(query)
            fetchResult = cursor.fetchall()
            s.wfile.write(json.dumps(fetchResult,sort_keys=True))
        elif str(s.path).startswith("/getCurrentTemp"):
            if is_real_data:
                s.wfile.write(json.dumps(bmp.read_temperature()))
            else:
                s.wfile.write(json.dumps("fake temp"))

if __name__ == '__main__':
    server_class = BaseHTTPServer.HTTPServer
    httpd = server_class((HOST_NAME, PORT_NUMBER), MyHandler)
    print time.asctime(), "Server Starts - %s:%s" % (HOST_NAME, PORT_NUMBER)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    httpd.server_close()
    print time.asctime(), "Server Stops - %s:%s" % (HOST_NAME, PORT_NUMBER)