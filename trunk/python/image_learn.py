#-*-coding:utf-8-*-

import Image
import ImageFilter
import os
import glob
import urllib
import urllib2
import cookielib
import StringIO
import random
import string
import HTMLParser

def get_img():
	request = urllib2.Request('http://yue.haijia.org/tools/CreateCode.ashx?key=ImgCode&random=0.28656307230805833')
	add_header(request)
	response = urllib2.urlopen(request)

	return response.read()

def point_handler(x):
	if x > 150:
		return 255
	else:
		return 0

def img_proc(img):
	index = 10

	img = img.convert('RGB')
	img.save(str(index) + '.jpg')
	index += 1

	img = img.convert('L').point(point_handler)
	img.save(str(index) + '.jpg')
	index += 1

	return img

def get_similar(sample, target, index):
	s_w, s_h = sample.size
	t_w, t_h = target.size

	if index < 0:
		return -1
	if index + s_w > t_w:
		return -1

	sample = sample.convert('L').point(point_handler)
	target = target.convert('L').point(point_handler)

	count = 0
	similar = 0
	for i in range(s_w):
		for j in range(s_h):
			rgb = sample.getpixel((i, j))
			if rgb != 0:
				continue

			count += 1

			target_rgb = target.getpixel((i + index, j))
			if target_rgb == rgb:
				similar += 1
	
	return 1.0 * similar / count

def get_sample(dir):
	samples = {}

	for file in glob.glob(dir):
		samples[file] = Image.open(file)
	
	return samples

def add_header(request):
	request.add_header('User-Agent', 'Mozilla/5.0 (Windows NT 6.1; rv:30.0) Gecko/20100101 Firefox/30.0')
	request.add_header('Accept', 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8')
	request.add_header('Accept-Language', 'en-US,en;q=0.5')
	request.add_header('Accept-Encoding', 'gzip, deflate')
	request.add_header('Referer', 'http://yue.haijia.org')

def login(img_code, event_validation, view_state):
	data = {}
	data['BtnLogin'] = '登 录'
	data['__EVENTVALIDATION'] = event_validation
	data['__VIEWSTATE'] = view_state
	data['rcode'] = ''

	data['txtIMGCode'] = img_code
	data['txtPassword'] = '0820'
	data['txtUserName'] = '610122198908202811'

	encode_data = urllib.urlencode(data)
	request = urllib2.Request('http://yue.haijia.org', data = encode_data)
	add_header(request)
	response = None

	try:
		response = urllib2.urlopen(request)

		return response.read()
	except urllib2.HTTPError, e:
		print e, '===', e.code, '===', e.message, '==', e.info, '==', e.errno
		return ''

class SuRenHtmlParser(HTMLParser.HTMLParser):
	def __init__(self):
		HTMLParser.HTMLParser.__init__(self)

		self.view_state =''
		self.event_validation = ''
	def handle_starttag(self, tag, attrs):
		self.find_data(tag, attrs)
	def handle_startendtag(self, tag, attrs):
		self.find_data(tag, attrs)
		pass
	def handle_endtag(self, tag):
		pass
	def find_data(self, tag, attrs):
		if self.view_state == '':
			self.view_state = self.find_value(tag, attrs, '__VIEWSTATE')

		if self.event_validation == '':
			self.event_validation = self.find_value(tag, attrs, '__EVENTVALIDATION')
	def find_value(self, tag, attrs, id):
		if tag == 'input':
			index = 0
			i = 0
			found = False

			for (name, value) in attrs:
				if name == 'id' and value == id:
					found = True

				if name == 'value':
					index = i

				i += 1

			if found:
				return attrs[index][1]
			else:
				return ''
		else:
			return ''

def img_parse():
	cookie_jar = cookielib.LWPCookieJar()
	cookie_support = urllib2.HTTPCookieProcessor(cookie_jar)
	http_handler = urllib2.HTTPHandler(debuglevel = 1)
	opener = urllib2.build_opener(cookie_support, http_handler)
	urllib2.install_opener(opener)

	img_raw = get_img()
	img = Image.open(StringIO.StringIO(img_raw))
	img_proc(img)

	w, h = img.size

	index = 0
	samples = get_sample('num_lib/*.jpg')
	values = []

	for i in range(index, w):
		for file in samples:
			similar = get_similar(samples[file], img, i)

			if similar > 0.9:
				index = i
				name = os.path.basename(file)
				val = os.path.splitext(name)[0]
				print val, ' -- ', similar
				values.append(val)
				break

	img_code = ''.join(values)
	print img_code

	if len(values) != 4:
		print 'error match!'
	else:
		parser = SuRenHtmlParser()

		request = urllib2.Request('http://yue.haijia.org')
		add_header(request)
		res = urllib2.urlopen(request).read()
		file = open('result.html', 'w')
		file.write(res)
		file.close()

		parser.feed(res)

		res = login(img_code, parser.view_state, parser.event_validation)
		file = open('result.html', 'w')

		try:
			file.write(res)
		finally:
			file.close()

if __name__ == '__main__':
	img_parse()
