package grails.gorm.tests

/**
 * Abstract base test for criteria queries. Subclasses should do the necessary setup to configure GORM
 */
class CriteriaBuilderSpec extends GormDatastoreSpec {

  def "Test disjunction query"() {
    given:
    def age = 40
    ["Bob", "Fred", "Barney", "Frank"].each { new TestEntity(name:it, age: age++, child:new ChildEntity(name:"$it Child")).save() }
    def criteria = TestEntity.createCriteria()

    when:
    def results = criteria.list {
      or {
        like('name', 'B%')
        eq('age', 41)
      }

    }

    then:
    3 == results.size()
  }

  void "Test conjunction query"() {
    given:
      def age = 40
      ["Bob", "Fred", "Barney", "Frank"].each { new TestEntity(name:it, age: age++, child:new ChildEntity(name:"$it Child")).save() }


    def criteria = TestEntity.createCriteria()

    when:
      def results = criteria.list {
        and {
          like('name', 'B%')
          eq('age', 40)
        }

      }

    then:
      1 == results.size()
  }

  void "Test list() query"() {
    given:
      def age = 40
      ["Bob", "Fred", "Barney", "Frank"].each { new TestEntity(name:it, age: age++, child:new ChildEntity(name:"$it Child")).save() }


      def criteria = TestEntity.createCriteria()

    when:
      def results = criteria.list {
         like('name', 'B%')
      }

    then:
      2 == results.size()

    when:
      results = criteria.list {
         like('name', 'B%')
         max 1
      }

    then:
      1 == results.size()
  }

  void "Test count()"() {
    given:
      def age = 40
      ["Bob", "Fred", "Barney", "Frank"].each { new TestEntity(name:it, age: age++, child:new ChildEntity(name:"$it Child")).save() }


      def criteria = TestEntity.createCriteria()

    when:
      def result = criteria.count {
         like('name', 'B%')
      }

    then:
      2 == result
  }

  void "Test obtain a single result"() {
    given:
      def age = 40
      ["Bob", "Fred", "Barney", "Frank"].each { new TestEntity(name:it, age: age++, child:new ChildEntity(name:"$it Child")).save() }


      def criteria = TestEntity.createCriteria()

    when:
      def result = criteria.get {
         eq('name', 'Bob')
      }

    then:
      result != null
      "Bob" == result.name

  }

  void "Test order by a property name"() {
    given:
      def age = 40
      ["Bob", "Fred", "Barney", "Frank"].each { new TestEntity(name:it, age: age++, child:new ChildEntity(name:"$it Child")).save() }


      def criteria = TestEntity.createCriteria()

    when:
      def results = criteria.list {
         like('name', 'B%')
         order "age"
      }

    then:
      "Bob" == results[0].name
      "Barney" == results[1].name

    when:
      results = criteria.list {
         like('name', 'B%')
         order "age", "desc"
      }

    then:
      "Barney" == results[0].name
      "Bob" == results[1].name

  }


  void "Test get minimum value with projection"() {
    given:
      def age = 40
      ["Bob", "Fred", "Barney", "Frank"].each { new TestEntity(name:it, age: age++, child:new ChildEntity(name:"$it Child")).save() }


      def criteria = TestEntity.createCriteria()

    when:
      def result = criteria.get {
        projections {
          min "age"
        }
      }

    then:
      40 == result

    when:
      result = criteria.get {
        projections {
          max "age"
        }
      }

    then:
      43 == result

    when:
      def results = criteria.list {
      projections {
        max "age"
        min "age"
      }
    }

    then:
      2 == results.size()
      43 == results[0]
      40 == results[1]
      [43, 40] == results
  }

  void "Test obtain property value using projection"() {
    given:
      def age = 40
      ["Bob", "Fred", "Barney", "Frank"].each { new TestEntity(name:it, age: age++, child:new ChildEntity(name:"$it Child")).save() }


      def criteria = TestEntity.createCriteria()

    when:
      def results = criteria.list {
        projections {
          property "age"
        }
      }

    then:
      [40, 41, 42, 43] == results
  }

  void "Test obtain association entity using property projection"() {
    given:
      def age = 40
      ["Bob", "Fred", "Barney", "Frank"].each { new TestEntity(name:it, age: age++, child:new ChildEntity(name:"$it Child")).save() }

      assert 4 == ChildEntity.count()


      def criteria = TestEntity.createCriteria()

    when:
      def results = criteria.list {
        projections {
          property "child"
        }
      }

    then:
      results.find { it.name = "Bob Child"}
      results.find { it.name = "Fred Child"}
      results.find { it.name = "Barney Child"}
      results.find { it.name = "Frank Child"}

  }
}
