import epic.slab._
import org.scalatest._
import scalaz.std.vector._

class ShapelessSlabTest extends FunSpec {
  import shapeless.test.illTyped
  import shapeless._

  describe("basic slab") {
    val slab = Slab("Foobar")
    val annotation = new Annotation {}
    it("should store an annotation") {
      val slab2 = slab.add(annotation)
      assert(slab2.select[Annotation](0) === annotation)
    }
    it("should add further annotations to the same vector, with disturbing the order") {
      case class Foo(foo: Int) extends Annotation
      val slab2 = slab.add(Foo(13))
      assert(slab2.select[Foo](0) === Foo(13))
      val slab3 = slab2.add(Foo(42))
      assert(slab3.select[Foo](0) === Foo(13))
      assert(slab3.select[Foo](1) === Foo(42))
    }
    it("should raise a type error if trying to retrieve that's not available") {
      case class Foo(foo: Int) extends Annotation
      val slab1 = slab.add(annotation)
      // could not find implicit value for parameter sel:
      // shapeless.ops.hlist.Selector[shapeless.::[Vector[Annotation],shapeless.HNil],Vector[Foo]]
      illTyped {
        """ slab1.select[Foo] """
      }
    }
    it("should also work with empty annotation lists") {
      case class Foo(foo: Int) extends Annotation
      val slab1 = slab.add(Vector[Int]().map(Foo(_)).toVector)
      assert(slab1.select[Foo] == Vector[Foo]())
    }
  }
}
