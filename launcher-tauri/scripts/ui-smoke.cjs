// Tests the real frontend with a controlled IPC fixture; not a native-webview or login test.
const {chromium}=require('playwright');const {spawn}=require('node:child_process');const fs=require('node:fs');const assert=require('node:assert/strict');
const version=fs.readFileSync(require("node:path").join(__dirname,"../../VERSION"),"utf8").trim();
(async()=>{const server=spawn(process.platform==='win32'?'npm.cmd':'npm',['run','dev'],{stdio:'ignore'});let browser;
try{for(let i=0;i<100;i++){try{await fetch('http://127.0.0.1:1420');break;}catch{await new Promise(r=>setTimeout(r,100));}}
browser=await chromium.launch({headless:true, ...(process.env.BLOSSOM_CHROMIUM ? {executablePath:process.env.BLOSSOM_CHROMIUM} : {})});const page=await browser.newPage({viewport:{width:1120,height:760}});const errors=[];page.on('pageerror',e=>errors.push(e.message));
await page.addInitScript(()=>{let callback;const config={javaAuto:true,java:'/home/jade/.blossom/runtimes/java/bin/java',game:'/home/jade/.blossom/instances/default',clientId:'',offlineName:'Jade',memory:4096,width:1280,height:720};window.__emitBlossom=payload=>callback({payload});window.__TAURI_INTERNALS__={transformCallback:fn=>{callback=fn;return 1;},invoke:async(cmd,args)=>{if(cmd==='wardrobe')return [];if(cmd==='settings')return config;if(cmd==='account')return false;if(cmd==='save_settings'){Object.assign(config,args.config);return;}return 1;}};});
await page.goto('http://127.0.0.1:1420');await page.waitForFunction(()=>document.getElementById('offline-name').value==='Jade'&&!document.querySelector('[data-action=offline]').disabled);assert.equal(await page.locator('#play-online').isDisabled(),true);assert.equal(await page.locator('[data-action="offline"]').isEnabled(),true);
fs.mkdirSync('../build/ui-check',{recursive:true});await page.waitForTimeout(250);await page.screenshot({animations:'disabled',path:'../build/ui-check/play.png',fullPage:true});
await page.locator('[data-tab="settings"]').click();assert.equal(await page.locator('#settings').isVisible(),true);await page.locator('#java-auto').uncheck();assert.equal(await page.locator('#java-path').isEnabled(),true);await page.waitForTimeout(250);await page.screenshot({animations:'disabled',path:'../build/ui-check/settings.png',fullPage:true});
await page.setViewportSize({width:800,height:620});assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false);await page.waitForTimeout(250);await page.screenshot({animations:'disabled',path:'../build/ui-check/settings-small.png',fullPage:true});
await page.locator('[data-tab="logs"]').click();await page.evaluate(()=>window.__emitBlossom({kind:'log',text:'<script>untrusted game output</script>'}));assert.match(await page.locator('#log').textContent(),/<script>/);assert.equal(await page.locator('#log script').count(),0);await page.waitForTimeout(250);await page.screenshot({animations:'disabled',path:'../build/ui-check/logs.png',fullPage:true});await page.locator('[data-tab=changelog]').click();assert.equal(await page.locator('#changelog').isVisible(),true);assert.ok((await page.locator('#release-notes').textContent()).startsWith('Blossom '+version));assert.equal(await page.locator('nav button').last().getAttribute('data-tab'),'changelog');await page.screenshot({animations:'disabled',path:'../build/ui-check/changelog.png',fullPage:true});for(const width of [1120,800,640]){
await page.setViewportSize({width,height:760});
for(const tab of ['play','settings','wardrobe','logs','changelog']){
await page.locator('[data-tab="'+tab+'"]').click();
assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false,tab+' overflows at '+width);
}
await page.locator('[data-tab="play"]').click();
await page.screenshot({animations:'disabled',path:'../build/ui-check/play-'+width+'.png',fullPage:true});
}
assert.deepEqual(errors,[]);console.log('Frontend tabs, controls, sizing and safe log rendering passed.');
}finally{if(browser)await browser.close();server.kill();}})().catch(e=>{console.error(e);process.exitCode=1;});
