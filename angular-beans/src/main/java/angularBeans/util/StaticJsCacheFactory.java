package angularBeans.util;

public class StaticJsCacheFactory {

	private Class<? extends StaticJsLoader> staticJsLoader;
	
	public StaticJsCacheFactory(Class<? extends StaticJsLoader> staticJsLoader){
		this.staticJsLoader = staticJsLoader;
	}
	
	public void BuildStaticJsCache(){
		this.BuildStaticJsCache(false);
	}
	
	public void BuildStaticJsCache(boolean compressOutput){
		StaticJsLoader loader;
		try {
			loader = staticJsLoader.newInstance();
			loader.LoadCoreScript();
			loader.LoadExtensions();
			if(compressOutput)
				StaticJsCache.Compress();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
}
