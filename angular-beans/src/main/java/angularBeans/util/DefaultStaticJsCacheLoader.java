package angularBeans.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

import javax.inject.Named;

import angularBeans.api.NGModules;
import angularBeans.boot.BeanRegistry;
import angularBeans.ngservices.NGService;

public class DefaultStaticJsCacheLoader extends StaticJsLoader {

	@Override
	public void LoadCoreScript() {
		//Load the AngularBeans Javascript Object.
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try (InputStream stream = classLoader.getResourceAsStream("staticjs/angularBeansObject.js");
			 BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
			String line=null;
			while((line = reader.readLine())!=null){
				StaticJsCache.appendToCore(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Generate the app object.
		Class<? extends Object> appClass = BeanRegistry.INSTANCE.getAppClass();
		String appName = getAppName(appClass);
		StaticJsCache.appendToCore(String.format("var app=angular.module('%s', [", appName));	
		if (appClass.isAnnotationPresent(NGModules.class)) {
			NGModules ngModAnno = appClass.getAnnotation(NGModules.class);
			String[] modules = ngModAnno.value();
			String modulesPart = "";
			for (String module : modules) {
				modulesPart += ("'" + module + "',");
			}
			modulesPart = modulesPart.substring(0, modulesPart.length() - 1);
			StaticJsCache.appendToCore(modulesPart);
		}
		
		StaticJsCache.appendToCore("])");
		StaticJsCache.appendToCore(".run(function($rootScope) {$rootScope.sessionUID = sessionId;");
		StaticJsCache.appendToCore("$rootScope.baseUrl=sript_origin;");
		StaticJsCache.appendToCore("});");
	}
	
	@Override
	public void LoadExtensions() {
		StringBuffer buffer = new StringBuffer();
		for (NGService extention : BeanRegistry.INSTANCE.getExtentions()) {
			Method m;
			try {
				m = extention.getClass().getMethod("render");
				buffer.append(m.invoke(extention) + ";");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		StaticJsCache.appendToExtensions(buffer.toString());
	}
	
	private String getAppName(Class<? extends Object> appClass){
		String appName = null;
		if (appClass.isAnnotationPresent(Named.class)) {
			appName = appClass.getAnnotation(Named.class).value();
		}

		if ((appName == null) || (appName.length() < 1)) {
			appName = CommonUtils.getBeanName(appClass);
		}
		return appName;
	}

}
