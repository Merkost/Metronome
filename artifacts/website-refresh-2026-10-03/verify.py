from pathlib import Path
from html.parser import HTMLParser
from urllib.parse import urlsplit
import json, re, subprocess

root=Path('/Users/merkost/StudioProjects/Metronome')
docs=root/'docs'
class Page(HTMLParser):
    def __init__(self):super().__init__();self.links=[];self.ids=[];self.h1=0;self.legal=[];self.inlegal=False
    def handle_starttag(self,tag,attrs):
        attrs=dict(attrs)
        self.links.extend(attrs[k] for k in ['href','src'] if k in attrs)
        if 'id' in attrs:self.ids.append(attrs['id'])
        if tag=='h1':self.h1+=1
        if attrs.get('class')=='document-content wrap':self.inlegal=True
    def handle_endtag(self,tag):
        if tag=='main':self.inlegal=False
    def handle_data(self,data):
        if self.inlegal:self.legal.append(data)
report={}
for name in ['index.html','support.html','privacy.html','404.html']:
    text=(docs/name).read_text();page=Page();page.feed(text)
    assert page.h1==1,name
    assert len(page.ids)==len(set(page.ids)),name
    missing=[]
    for link in page.links:
        parsed=urlsplit(link)
        if parsed.scheme or parsed.netloc:continue
        if parsed.path=='/app/':target=root/'shared/build/dist/wasmJs/productionExecutable/index.html'
        elif parsed.path in ['','/']:target=docs/'index.html'
        else:target=docs/parsed.path.lstrip('/')
        if not target.exists():missing.append(link)
    assert not missing,(name,missing)
    assert '<!--' not in text,name
    report[name]={'h1':page.h1,'links':len(page.links),'missing':missing}
source=subprocess.check_output(['git','show','698c588:docs/privacy.html'],text=True)
original=re.search(r'(<h2>Overview</h2>.*?)\s*<footer>',source,re.S).group(1)
updated=re.search(r'(<h2>Overview</h2>.*?)</main>',(docs/'privacy.html').read_text(),re.S).group(1)
def plain(text):return re.sub(r'\s+',' ',re.sub(r'<[^>]+>',' ',text)).strip()
assert plain(original)==plain(re.sub(r'</div>\s*$','',updated)), 'Legal text drift'
assert '6480380648' not in (docs/'index.html').read_text()
assert '6761737690' in (docs/'index.html').read_text()
for file in ['site.css','site.js','motion.js']:
    text=(docs/file).read_text()
    assert '/*' not in text,file
    if file.endswith('.js'):subprocess.run(['node','--check',str(docs/file)],check=True)
report['privacy_body']='unchanged'
report['js_syntax']='passed'
report['local_assets']='passed'
(root/'artifacts/website-refresh-2026-10-03/source-checks.json').write_text(json.dumps(report,indent=2))
print(json.dumps(report,indent=2))
