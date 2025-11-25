package playground.play2

case class Tenant(id: String)

def processTenant(tenant: Tenant): Unit =
  println(s"Processing tenant with id: ${tenant.id}")

def withFooConversion[A](block: Conversion[String, Tenant] ?=> A): A =
  given Conversion[String, Tenant] with
    def apply(id: String): Tenant = {
      println(s"Loading tenant $id from database...")
      Tenant(id)
    }
  block

def withLogging[A](block: => A): A =
  println("start")
  block

@main def main(): Unit =
  // Example of decorator-style conversion
  val someParam = "some-tenant-id"

  withLogging:
    withFooConversion:
      processTenant(someParam)

