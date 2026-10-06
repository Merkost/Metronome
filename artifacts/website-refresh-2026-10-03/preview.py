from http.server import ThreadingHTTPServer, SimpleHTTPRequestHandler
from pathlib import Path

base=Path('/Users/merkost/StudioProjects/Metronome')
class Preview(SimpleHTTPRequestHandler):
    def translate_path(self,path):
        translated=super().translate_path(path)
        if self.path.startswith('/app/'):
            return str(base/'shared/build/dist/wasmJs/productionExecutable'/self.path.split('?',1)[0][5:])
        return translated
    def __init__(self,*args,**kwargs):
        super().__init__(*args,directory=str(base/'docs'),**kwargs)
ThreadingHTTPServer(('127.0.0.1',8768),Preview).serve_forever()
