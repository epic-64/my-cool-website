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

  def getUnverifiedById(id: String): Option[Tenant[VerificationState.Unverified]] =
    unverifiedTenants.get(id)

  def getVerifiedById(id: String): Option[Tenant[VerificationState.Verified]] =
    verifiedTenants.get(id)

inline def withLogging[A](block: => A): A =
  println("start")
  block

def processTenant[S <: VerificationState](tenant: Tenant[S]): Try[String] =
  println(s"Processing tenant ${tenant.name}...")
  println("Woooo scary side effects!")
  Success(s"Processed tenant ${tenant.name}...")

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
    x <- err("Fetching unverified Tenant failed")(Try(TenantStore.getUnverifiedById(x).get))
    x <- err("Tenant verification failed")(verifyTenant(x))
    x <- err("Processing verified Tenant failed")(processTenant(x))
  yield x

  result.recover(e => s"Error: ${e.getMessage}").foreach(println)
