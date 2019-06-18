package evolution.gaming.json

import evolution.gaming.factory.ObjectFactory
import evolution.gaming.json.JsonModule
import org.scalatest.{Matchers, WordSpec}

class JsonModuleSpec extends WordSpec with Matchers {
  "decode" should {
    "parse ping with out of order $type & space" in {
      JsonModule.decode("""{"$type":"ping", "seq":1}""") should be(Right(ObjectFactory.ping(1)))
    }
    "parse ping with in order $type" in {
      JsonModule.decode("""{"seq":1,"$type":"ping"}""") should be(Right(ObjectFactory.ping(1)))
    }
    "parse nested tables types json without $type field" in {
      JsonModule.decode("""{"tables":[{"name":"name","participants":10,"id":1}],"$type":"table_list"}""") should
        be(Right(ObjectFactory.table_list(List(ObjectFactory.table(Some(1), "name", 10)))))
    }

    "parse nested table type json without $type field, while null field Id is skipped" in {
      JsonModule.decode(
        """{
                            "$type": "add_table",
                            "after_id": 1,
                            "table": {
                              "name": "table - Foo Fighters",
                              "participants": 4
                            }
                          }""") should
        be(Right(ObjectFactory.add_table(1, ObjectFactory.table(None, "table - Foo Fighters", 4))))
    }
  }
  "encode" should {
    "produce pong json" in {
      JsonModule.toJson(ObjectFactory.pong(1): ObjectFactory.Message) should be("""{"seq":1,"$type":"pong"}""")
    }
    "produce nested tables types json without $type field" in {
      JsonModule.toJson(ObjectFactory.table_list(List(ObjectFactory.table(Some(1), "name", 10))): ObjectFactory.Message) should
        be("""{"tables":[{"id":1,"name":"name","participants":10}],"$type":"table_list"}""")
    }

    "produce nested table type json without $type field, skip null field Id" in {
      JsonModule.toJson(ObjectFactory.add_table(1, ObjectFactory.table(None, "table - Foo Fighters", 4)): ObjectFactory.Message) should
        be("""{"after_id":1,"table":{"name":"table - Foo Fighters","participants":4},"$type":"add_table"}""")
    }
  }
}
