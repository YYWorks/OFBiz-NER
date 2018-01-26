OFBiz NER模块
====
[EN](README.md) 

NER是Named Entity Recognition的缩写，中文名称是命名实体识别。

OFBiz NER模块提供了一组服务，来分析商品内容数据，这些数据可能包含了6类信息，如品牌(B)、颜色(C)、类型(G)、型号(N)、尺寸(S)和其它(O)。

OFBiz NER模块使用IKAnalyzer进行分词、斯坦福CoreNLP进行实体识别。

### 为什么选用IKAnalyzer
IKAnalyzer可以使用字典来对中文进行分词，并用Apache Lucene对英文和数字进行分词。在我们的测试中，IKAnalyzer表现最好，并且开源(使用Apache License V2.0版权协议)。


### 为什么选用[Stanford CoreNLP](https://github.com/stanfordnlp/CoreNLP)
由于我们的需求很类似这篇[eBay论文](https://aclweb.org/anthology/D/D11/D11-1144.pdf)中提到的情况。从论文的结论中我们可以看出，CRF算法最优。<br/>
![NER算法比较](./docs/ner_algorithm_compare.png)

斯坦福CoreNLP使用CRF算法，并且开源(GLP v3+)。

### 如何让这个模块在OFBiz工作
本模块包含了一个plugins下的目录和一个runtime下的目录，把它们复制到OFBiz中，然后启动OFBiz即可。<br/>

注意：本模块需要与Lucene模块和PriCat模块一起使用，当训练一个新模型时，日志信息会通过htmlreport显示到网页上。

为了避免中文字符编码错误，在build.gradle，把file.encoding设置为UTF-8，并把JAVA虚拟机的内存设置得大一些：
```groovy
def jvmArguments = ['-Xms1024M', '-Xmx4096M', '-Dfile.encoding=UTF-8']
```

### 功能
1. 分析语句：<br/>
![NER Analyze Sentence](./docs/ner_main.png)

2. 向字典添加新词：<br/>
![NER Add To Dictionary](./docs/ner_addtodictionary.png)

3. 向NER添加新词：<br/>
![NER Add ML Words](./docs/ner_addmlwords.png)

### 致谢
感谢孙梦晗同学在2016年夏天做出了本模块的第一个版本，祝愿她在香港中文大学博士学习幸福快乐！

### 相关项目
[![Apache OFBiz](http://ofbiz.apache.org/images/ofbiz_logo.png)](http://ofbiz.apache.org/) &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; [Stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; [![Apache Lucene](http://lucene.apache.org/images/lucene_logo_green_300.png)](http://lucene.apache.org/) 
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; [IKAnalyzer](https://oschina.net/p/ikanalyzer)