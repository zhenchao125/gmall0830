import org.json4s
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization

/**
  * Author atguigu
  * Date 2020/2/18 11:40
  */
object T1 {
    def main(args: Array[String]): Unit = {
        val abc = User(10, "abc")
        
        import org.json4s.DefaultFormats
        import org.json4s.jackson.Serialization
        // json4s  json for scala
        println(Serialization.write(abc)(DefaultFormats))
    }
}

case class User(age: Int, name: String)
