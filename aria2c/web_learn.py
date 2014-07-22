#!/usr/bin/env python

import web
import json
import xmlrpclib

urls = (
	'/', 'index',
	'/add', 'add_file',
	'/stat', 'aria_stat',
	'/list', 'aria_list',
	'/session', 'aria_session',
	'/global_option', 'aria_global_option',
	'/version', 'aria_version',
	'/active', 'aria_active',
	'/waiting', 'aria_wating',
	'/stopped', 'aria_stopped',
	'/pause', 'aria_pause',
	'/unpause', 'aria_unpause',
	'/remove', 'aria_remove',
	'/shutdown', 'aria_shutdown'
)

render = web.template.render('./')

suren = json.dumps({'name' : 'suren', 'age' : 12})

class index:
	def GET(self):
		return render.index({'name' : 'suren', 'age' : 12})

class add_file:
	def GET(self):
		return render.add_file('a', 'b')
	def POST(self):
		aria = Aria()

		return 'post', aria.add(web.input().get('url'))

class aria_stat:
	def GET(self):
		aria = Aria()

		return aria.stat()

class aria_list:
	def GET(self):
		aria = Aria()

		return aria.list()

class aria_session:
	def GET(self):
		aria = Aria()

		return aria.session()

class aria_version:
	def GET(self):
		aria = Aria()

		return aria.version()
	
class aria_active:
	def GET(self):
		aria = Aria()

		return aria.active()

class aria_wating:
	def GET(self):
		aria = Aria()

		return aria.waiting()

class aria_stopped:
	def GET(self):
		aria = Aria()

		return aria.stopped()

class aria_pause:
	def GET(self):
		aria = Aria()
		gid = web.input().get('gid')

		if gid:
			return aria.pause(gid)
		else:
			return 'error'
class aria_unpause:
	def GET(self):
		aria = Aria()
		gid = web.input().get('gid')

		if gid:
			return aria.unpause(gid)
		else:
			return 'error'

class aria_remove:
	def GET(self):
		aria = Aria()
		gid = web.input().get('gid')

		if gid:
			return aria.remove(gid)
		else:
			return 'error'

class aria_global_option:
	def GET(self):
		aria = Aria()

		result = aria.global_option()

		result = json.dumps(result, indent = 4)

		return result

class aria_shutdown:
	def GET(self):
		aria = Aria()

		return aria.shutdown()

class Aria:
	def add(self, url):
		server = xmlrpclib.ServerProxy('http://127.0.0.1:6800/rpc')

		result = server.aria2.addUri([url])

		return result
	def stat(self):
		server = xmlrpclib.ServerProxy('http://127.0.0.1:6800/rpc')

		result = server.aria2.getGlobalStat()

		return result
	def list(self):
		server = xmlrpclib.ServerProxy('http://127.0.0.1:6800/rpc')

		result = server.aria2.getFiles('255a26')

		return result
	def session(self):
		server = xmlrpclib.ServerProxy('http://127.0.0.1:6800/rpc')

		result = server.aria2.getSessionInfo()

		return result
	def version(self):
		server = xmlrpclib.ServerProxy('http://127.0.0.1:6800/rpc')

		result = server.aria2.getVersion()
		result = json.dumps(result, indent = 4)

		return result
	def active(self):
		server = xmlrpclib.ServerProxy('http://127.0.0.1:6800/rpc')

		result = server.aria2.tellActive()

		result = json.dumps(result, indent = 4)

		return result
	def waiting(self):
		server = xmlrpclib.ServerProxy('http://127.0.0.1:6800/rpc')

		result = server.aria2.tellWaiting(1, 2)

		return result
	def stopped(self):
		server = xmlrpclib.ServerProxy('http://127.0.0.1:6800/rpc')

		numStopped = self.stat().get('numStopped')

		result = server.aria2.tellStopped(0, int(numStopped))
		result = json.dumps(result, indent = 4)

		return result
	def pause(self, gid):
		server = xmlrpclib.ServerProxy('http://127.0.0.1:6800/rpc')

		result = server.aria2.pause(gid)

		return result
	def unpause(self, gid):
		server = xmlrpclib.ServerProxy('http://127.0.0.1:6800/rpc')

		result = server.aria2.unpause(gid)

		return result;
	def remove(self, gid):
		server = xmlrpclib.ServerProxy('http://127.0.0.1:6800/rpc')

		result = server.aria2.removeDownloadResult(gid)

		return result;
	def global_option(self):
		server = xmlrpclib.ServerProxy('http://127.0.0.1:6800/rpc')

		result = server.aria2.getGlobalOption()

		return result
	def shutdown(self):
		server = xmlrpclib.ServerProxy('http://127.0.0.1:6800/rpc')

		result = server.aria2.shutdown()

		return result

if __name__ == "__main__":
	app = web.application(urls, globals())
	app.run()
