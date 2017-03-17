# dummy-es-auth-plugin
A dummy ElasticSearch authentication plugin. This plugin works on ElasticSearch 5.1.2 only

# Install

```bash
$ cd _REPO_ROOT_
$ mvn clean package
$ cd _ES_ROOT_
$ bin/elasticsearch-plugin install file:///_ABSOLUTE_REPO_ROOT_/target/releases/dummy-es-auth-plugin.zip
-> Downloading file:///_ABSOLUTE_REPO_ROOT_/target/releases/dummy-es-auth-plugin.zip
[=================================================] 100%
-> Installed dummy-es-auth-plugin
```

If you want to reinstall plugin you need to run `bin/elasticsearch-plugin remove dummy-es-auth-plugin` first

# Usage

Authentication is passed as HTTP headers, currently it checks if headers have `Dummy-Auth = somePassword`. 

# Basic structure

In `plugin-descriptor.properties` I specify classname identify plugin entry point.
In `DummyAuthPlugin`I use `DummyAuthRestHandler` as the `RestHandler` we are going to use in this plugin.

Most works are in `DummyAuthRestHandler`.

There are mainly two kinds of handlers, `RestHandler` and `ActionHandler`.
In `Plugin`, if you choose `RestHandler`, you can add multiple endpoints and corresponding `RestFilter`.
Or choose `ActionHandler` or `ActionFilter`, which handle particualr ElasticSearch `Request` event, but it doesn't have any information related to Http headers.
In original ElasticSearch source code it did the conversion right here https://github.com/elastic/elasticsearch/blob/v5.1.2/core/src/main/java/org/elasticsearch/rest/action/search/RestSearchAction.java#L78-L84 .
`RestRequest` (handled by `RestHandler`) is converted to `SearchRequest` (handled by `ActionHandler`). 
At this point I don't find any way to retrieve `RestRequest` from `ActionHandler`, nor pass http headers to `ActionHandler`, therefore I choose to add one more `RestHandler`.

To add one more `RestHandler`, I `extends BaseRestHandler` and register only `RestFilter` without any `RestHandler`.
The reason this will work is in ElasticSearch source code https://github.com/elastic/elasticsearch/blob/v5.1.2/core/src/main/java/org/elasticsearch/rest/RestController.java#L188-L206 ,
no matter which endpoint you hit, as long as your chain of `RestFilter` is not empty, it will go through chain of all `RestFilter` first, then dispatch to the corresponding handler based on your URI.

The way to send error message is also found in ElasticSearch source code https://github.com/elastic/elasticsearch/blob/v5.1.2/core/src/main/java/org/elasticsearch/rest/RestController.java#L250  
  
# Reference

- http://david.pilato.fr/blog/2016/10/19/adding-a-new-rest-endpoint-to-elasticsearch-updated-for-ga/
- https://github.com/floragunncom/search-guard/tree/es-5.1.2