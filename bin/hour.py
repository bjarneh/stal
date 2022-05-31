#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
 hour - create JSON and post it to 'stal'

 usage: hour [OPTIONS] <DIR|FILE>

 options:
     -h --help    :  print this menu and exit
     -v --version :  print version number and exit
     -t --time    :  set fixed time (or it will be calculated)
     -k --keep    :  keep current jobs, i.e. don't delete day first
     -u --url     :  REST URL (http://localhost:7676/d/rest/job)
     -d --date    :  fmt: yyyy-MM  [day is taken from filename]
"""

import os
import re
import sys
import getopt
import json
import traceback
import urllib.request, urllib.error, urllib.parse

__version__ = 1.0

# globals
REST_JOB_URL = 'http://localhost:7676/d/rest/job'


RM_FIRST = True


MONTHS = {
    'januar'    : '01',
    'februar'   : '02',
    'mars'      : '03',
    'april'     : '04',
    'mai'       : '05',
    'juni'      : '06',
    'juli'      : '07',
    'august'    : '08',
    'september' : '09',
    'oktober'   : '10',
    'november'  : '11',
    'desember'  : '12'
}

CUR_DATE = None

#___________________________________________________________________________

class Job(dict):

    def __init__(self):
        """ Init empty dict """
        dict.__init__(self)
        self['id']      = None
        self['dayId']   = None
        self['company'] = None
        self['start']   = None
        self['stop']    = None
        self['what']    = None

    def to_json(self):
        """ convert myself to json """
        return json.dumps( self )

    def to_bytes(self, encoding='utf-8'):
        """ convert myself to byte """
        urllib.parse.urlencode(self).encode(encoding)

    def __str__(self):
        """ convert myself to string """
        return self.to_json().__str__()

    def short_what(self, n):
        """ return at most n chars of the 'what' message """
        if self['what'] and len(self['what']) > n:
            return self['what'][0:n] + '..'
        else:
            return self['what']


#___________________________________________________________________________

def clear_days( days ):
    """ remove jobs belonging to dates in days """
    for dayId in days:
        req = urllib.request.Request(REST_JOB_URL+'?day='+dayId)
        req.get_method = lambda: 'DELETE'
        f = urllib.request.urlopen(req)
        response = f.read()
        f.close()
        sys.stdout.write('http=%d - %s  DELETE\n'%(f.code, dayId))



def post_job(job):
    """ POST a Job object as JSON to the REST API """

    global JOB_POSTER_URL

    data = job.to_json().encode('utf-8')
    req = urllib.request.Request(REST_JOB_URL)
    req.add_header('Content-Type', 'application/json; charset=utf-8')
    req.add_header('Content-Length', len(data))
    f = urllib.request.urlopen(req, data)
    response = f.read()
    f.close()

    return f.code, response


def post_jobs( jobs ):
    """ POST all the Jobs :-) """

    global RM_FIRST

    if RM_FIRST and jobs:
        days = set()
        for j in jobs:
            days.add( j['dayId'] )
        clear_days( days )

    for j in jobs:
        (c, r) = post_job( j )
        sys.stdout.write('http=%d - %s  %-11s %s\n'%
                         (c, j['dayId'], j['company'], j.short_what(42)))

#___________________________________________________________________________

def test_post():
    """ Test the REST API """

    j = Job()
    j['id'] = -1
    j['dayId'] = '2015-01-15'
    j['company'] = 'Texi'
    j['start'] = '2015-01-15 09:00:00.000'
    j['stop'] = '2015-01-15 17:00:00.000'
    j['what'] = 'I did not do too much today'

    post_job( j );

#___________________________________________________________________________


def parse_file( f, date_stub ):
    """ f = file-name, date_stub = yyyy-MM """

    base = os.path.basename(f)
    if not re.match('''^\d\d_.*\.txt$''', base):
        raise Exception("Not a valid file-name (dd_xxx.txt) : "+ base)
    else:
        day = date_stub + '-' + base[0:2]
        with open(f) as fd:
            lines = fd.readlines()
        if lines:
            jobs = []
            job  = None
            for line in lines:
                if re.match('''^\s+$''', line):
                    continue
                m = re.match('''^\s+(\d\d:\d\d) - (\d\d:\d\d) : ([^-]+)-(.*)$''', line)
                if m:
                    job = Job()
                    job['id']      = -1
                    job['dayId']   = day
##                     job['what']    = m.group(4).strip().capitalize()
                    job['what']    = m.group(4).strip()
                    job['company'] = m.group(3).strip()
                    job['start']   = day + ' '+ m.group(1) + ':00.000'
                    job['stop']    = day + ' '+ m.group(2) + ':00.000'
                    if job['what']:
                        job['what'] = job['what'][0].upper() + job['what'][1:]
                    jobs.append( job )
                else:
                    if job:
                        job['what'] += ' '+ line.strip()
            
            return jobs
    return None



def parse_files( args ):
    """ Extract Jobs from files and dirs in args """

    global CUR_DATE

    jobs = []
    fjobs = [] # these have to get a date calulated first..
    
    for f in args:
        if os.path.isfile( f ):
            jobs += parse_file( f, CUR_DATE )
        else:
            fjobs = path_walk(f, lambda x : x.endswith('.txt'))

    if jobs:
        post_jobs( jobs )

    if fjobs:
        jobs = []
        date_map = find_dates( fjobs )
        for k in date_map:
            for f in date_map[k]:
                jobs += parse_file( f, k )
        if jobs:
            post_jobs( jobs )



def find_dates( files ):
    """ file: 2020/april/20_tue.txt gets 2020-04 as 'date/month' """
    date_map = dict() # map[date] => [job1,job2,job3,...] 
    for f in files:
        stubs = f.split( os.path.sep )
        if len(stubs) < 3:
            continue
        if not re.match('''^\d\d_.*.txt$''', stubs[-1]):
            sys.stdout.write("[WARNING] ignoring file: %s\n"% (f) )
            continue
        if stubs[1] not in MONTHS:
            sys.stdout.write("[WARNING] ignoring file: %s\n"% (f) )
            continue
        day = stubs[0] + '-' + MONTHS[stubs[1]]
        if day not in date_map:
            date_map[day] = []
        date_map[day].append( f )

    return date_map



def path_walk(dir_name, criteria):
    """
    Traverse dir_name and collect files matching criteria
    """

    collected   = []

    for root, dirs, files in os.walk(dir_name):
        if files:
            for f in files:
                if criteria(f):
                    collected.append(os.path.join(root, f))

    return collected




#___________________________________________________________________________

def niceopt(argv, short_opts, long_opts):
    """ Allow long options which start with a single '-' sign"""
    for i, e in enumerate(argv):
        for opt in long_opts:
            if( e.startswith("-" + opt) or
              ( e.startswith("-" + opt[:-1]) and opt[-1] == "=") ):
                argv[i] = "-" + e
    return getopt.gnu_getopt(argv, short_opts, long_opts)



def main(argv=sys.argv):
    """ Entry point """

    global CUR_DATE, REST_JOB_URL, RM_FIRST

    try:

        (opts, args) = niceopt(argv[1:], "hvku:d:",
                               ['help', 'version','keep','url=','date='])

        for o, a in opts:
            if o in ('-h', '--help'):
                print( __doc__ );
                raise SystemExit(0)
            if o in ('-v', '--version'):
                print(( "%s - %s" % (argv[0], __version__)));
                raise SystemExit(0)
            if o in ('-u', '--url'):
                REST_JOB_URL = a
            if o in ('-d', '--date'):
                CUR_DATE = a
            if o in ('-k', '--keep'):
                RM_FIRST = False


        if not args:
            raise Exception("No file or directory given")
        else:
            for f in args:
                is_file = os.path.isfile(f)
                is_dir  = os.path.isdir(f)
                if not (is_dir or is_file):
                    raise Exception("Not file or dir: "+ f)
                if is_file and (not CUR_DATE):
                    raise Exception("Date missing for: "+ f)
                if is_dir:
                    okpath = os.path.basename( os.path.normpath(f) )
                    if not re.match("""^\d+$""", okpath):
                        raise Exception("Dir must be year: "+ okpath)


        # we should be good to go now..
        parse_files( args )

    except SystemExit as inst:
        pass
    except Exception as inst:
        sys.stderr.write("[ERROR] %s\n" % inst )
        traceback.print_exc()
        return 1
    return 0

#___________________________________________________________________________

if __name__ == '__main__':
    sys.exit( main() )

