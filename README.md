OFBiz NER Plugin
====
[中文](README_zh.md) 

NER is the abbreviation of Named Entity Recognition.

OFBiz NER plugin provides a set of services to analyze product contents which may contain up to 6 classes, such as brand (B), color (C), garment type/style (G), garment number (N), size (S) and other (O).

OFBiz NER plugin uses IKAnalyzer to token sentences and Stanford CoreNLP (NER) to recognize entities in sentences.

### Why IKAnalyzer
IKAnalyzer uses its own dictionary to token Chinese words and Apache Lucene Analyer to token English words. In our test result, it is the best in open source (Apache License V2.0).


### Why [Stanford CoreNLP](https://github.com/stanfordnlp/CoreNLP)
Our requirement is similar to the situation described in this [eBay paper](https://aclweb.org/anthology/D/D11/D11-1144.pdf). From the results of this paper we can see, CRF algorithm is the best.<br/>
![NER Algorithm Compare](./docs/ner_algorithm_compare.png)

Stanford CoreNLP uses CRF and open source (GLP v3+).

### How to make this plugin work in OFBiz
This plugin contains a folder in plugins and a folder in runtime, deploy them in OFBiz and start OFBiz.<br/>

Please note, this plugin depends on OFBiz Lucene and PriCat plugins, when training a new model, the log messages will be output to web page by htmlreport.

To avoid Chinese charaters encoding problem, please set file.encoding to UTF-8 in build.gradle, and expand jvm memory to a larger number:
```groovy
def jvmArguments = ['-Xms1024M', '-Xmx4096M', '-Dfile.encoding=UTF-8']
```

### Functions
1. Analyze a sentence:<br/>
![NER Analyze Sentence](./docs/ner_main.png)

2. Add new words to the dictionary:<br/>
![NER Add To Dictionary](./docs/ner_addtodictionary.png)

3. Add new words to ner:<br/>
![NER Add ML Words](./docs/ner_addmlwords.png)

### Thanks
Thanks Menghan Sun built the first version of this plugin in the summer of 2016. Wish her doctoral studies in CUHK happy and fun.

### Projects Related
[![Apache OFBiz](http://ofbiz.apache.org/images/ofbiz_logo.png)](http://ofbiz.apache.org/) &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; [Stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; [IKAnalyzer](https://oschina.net/p/ikanalyzer)