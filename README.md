# 安卓设备上备份已安装应用的apk包技术实现方案
## 需求的目的
在只有安装应用, 没有该应用的apk,而我们又想活取应用apk,用来分享给别人,或是应用的备份, 说是应用的增量升级的, 怎么办?

本文将告诉你如何靠谱的导出一个已安装应用的apk.

## 了解相关目录
安卓设备上根据安卓方式的不同,安装的数据存放路径也不同
### 1. system/app
 此类应用是系统初始化时候安装完成的, 存放在 'system/app' 目录下, 用户无法删除及操作.
### 2.  data/app
通过market下载后安装(无安装界面),或是用户手动安装(adb,packageinstall等).这类apk安装过程系统会备份在data/app目录下
### 3. /mnt/asec/
安装到sd卡上的应用,作用同上
### 4. data/data
应用安装过程,会在该目录下存在应用的私有数据
### 5. data/dalvik-cache

应用安装过程中,会解析data/app下的安装包中的dex文件, 拷贝到data/dalvik-cache,以备应用运行时使用.

> 应用的删除过程即删除应用安装时所产生的这几个文件

## 备份apk思路分享
了解这个文件夹的作用后, 可以看到导出已安装应用只要拷贝相关目录下的应用即可.

通过测试,可以确定除了系统应用安装的备份数据没有权限, 第三方应用所产生的文件夹下的内容都是有读权限的. 所以我们不需要任何特殊权限即可实现安装应用的备份导出.

那问题来了, 在相关目录下如:data/app下如何才能知道一个应用的决定路径?

> 答案:

```
> context.getPackageManager().getApplicationInfo("packagename",
> 0).sourceDir;
```

所以需要了解下 PackageManager 相关api

## 实现代码
知道路径后,那么我们接可以将文件拷贝到指定地方了,代码如下:

```
    private void backupApp(String path, String outname) throws IOException {
        File in = new File(path);

        if (!mBaseFile.exists()) mBaseFile.mkdir();
        File out = new File(mBaseFile, outname + ".apk");
        if (!out.exists()) out.createNewFile();
        FileInputStream fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);

        int count;
        byte[] buffer = new byte[256 * 1024];
        while ((count = fis.read(buffer)) > 0) {
            fos.write(buffer, 0, count);
        }

        fis.close();
        fos.flush();
        fos.close();
    }

	//活取全面已安装应用的方法
        PackageManager packageManager = getPackageManager();
        List<PackageInfo> allPackages = packageManager.getInstalledPackages(0);
        for (int i = 0; i < allPackages.size(); i++) {
            PackageInfo packageInfo = allPackages.get(i);
            String path = packageInfo.applicationInfo.sourceDir;
            String name = packageInfo.applicationInfo.loadLabel(packageManager).toString();
        }

	//判断是否是第三方应用方法
    public boolean isUserApp(PackageInfo pInfo) {
        return (((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) && ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0));
    }
```
## 正确性保障

如何确保拷贝出来的apk的正确性呢?

直接的办法可以校验md5或sha1, 当然还可以尝试解决文件格式等

## Demo源代码
测试小程序已上传到[github](https://github.com/CankingApp/ApkBackup.git),  有兴趣的可以直接下载研究学习.

![这里写图片描述](http://img.blog.csdn.net/20160204192838199)



