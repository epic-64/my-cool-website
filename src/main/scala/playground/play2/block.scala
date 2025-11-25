package playground.play2

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

enum TenantStatus:
  case Unverified
  case Verified(date: String)

// Phantom types for compile-time status tracking
sealed trait VerificationState
object VerificationState:
  trait Unverified extends VerificationState
  trait Verified   extends VerificationState

case class Tenant[S <: VerificationState](id: String, name: String, status: TenantStatus)

object Tenant:
  def unverified(id: String, name: String): Tenant[VerificationState.Unverified] =
    Tenant[VerificationState.Unverified](id, name, TenantStatus.Unverified)

  def verified(id: String, name: String, date: String): Tenant[VerificationState.Verified] =
    Tenant[VerificationState.Verified](id, name, TenantStatus.Verified(date))

object TenantStore:
  private val unverifiedTenants = mutable.Map[String, Tenant[VerificationState.Unverified]]()
  private val verifiedTenants   = mutable.Map[String, Tenant[VerificationState.Verified]]()

  def addUnverified(tenant: Tenant[VerificationState.Unverified]): Unit =
    unverifiedTenants.put(tenant.id, tenant)

  def addVerified(tenant: Tenant[VerificationState.Verified]): Unit =
    verifiedTenants.put(tenant.id, tenant)

  def getUnverifiedById(id: String): Try[Tenant[VerificationState.Unverified]] =
    unverifiedTenants.get(id) match
      case Some(tenant) => Success(tenant)
      case None         => Failure(new NoSuchElementException(s"Unverified tenant with id '$id' not found"))

  def getVerifiedById(id: String): Try[Tenant[VerificationState.Verified]] =
    verifiedTenants.get(id) match
      case Some(tenant) => Success(tenant)
      case None         => Failure(new NoSuchElementException(s"Verified tenant with id '$id' not found"))

inline def withLogging[A](block: => A): A =
  println("start")
  block

def summarize(tenant: Tenant[VerificationState.Verified]): Try[String] =
  Success:
    s"Tenant ${tenant.name} is verified as of ${tenant.status.asInstanceOf[TenantStatus.Verified].date}."

// Compile-time proof: can only verify unverified tenants!
def verifyTenant(tenant: Tenant[VerificationState.Unverified]): Try[Tenant[VerificationState.Verified]] =
  println(s"Verifying tenant ${tenant.name}...")
  val verifiedTenant = Tenant.verified(tenant.id, tenant.name, "2025-01-01")
  TenantStore.addVerified(verifiedTenant)
  Success(verifiedTenant)

def fetchInput(): Try[String] =
  println("Enter tenant ID:")
  Try(scala.io.StdIn.readLine())

def err[A](context: String)(block: => Try[A]): Try[A] =
  block.recoverWith:
    case e => Failure(new Exception(s"$context: ${e.getMessage}", e))

@main def main(): Unit =
  TenantStore.addUnverified(Tenant.unverified("abc", "Test Tenant"))

  val result = for
    x <- err("Fetching input failed")(fetchInput())
    x <- err("Fetching unverified Tenant failed")(TenantStore.getUnverifiedById(x))
    x <- err("Tenant verification failed")(verifyTenant(x))
    x <- err("Summary failed")(summarize(x))
  yield x

  result.recover(e => s"Error: ${e.getMessage}").foreach(println)
