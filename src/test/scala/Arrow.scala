package nettest

import org.specs2._

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }
import java.util.Collections
import java.util.Arrays.asList

import zio.{ DefaultRuntime }
// import zio.console.{ putStrLn }

import net.manub.embeddedkafka.{ EmbeddedKafka }
import kafkaconsumer.KafkaConsumer._
import zio.kafka.client.KafkaTestUtils.{ pollNtimes }
import KafkaPkg._
import KafkaTypes._
import zio.kafka.client.{ Consumer, Subscription }
import zio.kafka.client._

import org.apache.kafka.common.serialization.Serdes

import org.apache.arrow.memory.RootAllocator
import org.apache.arrow.vector.ipc.{ ArrowStreamReader, ArrowStreamWriter }
import org.apache.arrow.vector.{ IntVector, VectorSchemaRoot }
import org.apache.arrow.vector.types.pojo.{ ArrowType, Field, FieldType, Schema }

// import zio.serdes.{ Serdes => ZIOSerdes }
import zio.serdes.Serdes._
// import zio.{ Chunk }

class ArrowSpec extends Specification with DefaultRuntime {

  val allocator = new RootAllocator(Integer.MAX_VALUE)

  def is = s2"""

  TSP Arrow should      
    display parquet file contents     

    consume parquet from prod       
    consume arrow from prod         $prodArrowTest
    killall                         $killall

    """
  def prodParquetTest = {

    val slvCfg = SlaveConfig(
      server = "37.228.115.243:9092",
      client = "client5",
      group = "group5",
      topic = "parquet_small"
    )

    val exp = Array(1, 2, 3)

    val subscription = Subscription.Topics(Set(slvCfg.topic))
    val cons         = Consumer.make[String, BArr](settings(slvCfg))(Serdes.String, Serdes.ByteArray)

    unsafeRun(
      cons.use { r =>
        for {
          _     <- r.subscribe(subscription)
          batch <- pollNtimes(5, r)
          arr   = batch.map(_.value)
          _     <- r.unsubscribe
        } yield arr === exp

      }
    )
  }

  def prodArrowTest = {

    val slvCfg = SlaveConfig(
      server = "37.228.115.243:9092",
      client = "client5",
      group = "group5",
      topic = "batch_record_small"
      // topic = "table_small"
    )

    // val exp: BArr = Array(1, 2, 3)

    val subscription = Subscription.Topics(Set(slvCfg.topic))
    val cons         = Consumer.make[String, BArr](settings(slvCfg))(Serdes.String, Serdes.ByteArray)

    val schema = testSchema

    // val arr: Array[Int] = Array(1, 2, 3)
    // val chunk           = Chunk.fromArray(arr)
    // val bytes = ZIOSerdes[Chunk, Chunk].serialize[Int](chunk)

    unsafeRun(
      cons.use { r =>
        for {
          _      <- r.subscribe(subscription)
          batch  <- pollNtimes(5, r)
          _      <- r.unsubscribe
          arr    = batch.map(_.value)
          out    = scatter(arr)
          in     = new ByteArrayInputStream(out.toByteArray)
          reader = new ArrowStreamReader(in, allocator)
        } yield schema === reader.getVectorSchemaRoot.getSchema
      }
    )

    // true === true
  }

  def testSchema = {
    val schema = new Schema(
      asList(new Field("testField", FieldType.nullable(new ArrowType.Int(8, true)), Collections.emptyList()))
    )
    schema
  }

  def simpleSchema(vec: IntVector) =
    new Schema(Collections.singletonList(vec.getField), null)

  def simpleRoot(schema: Schema): VectorSchemaRoot =
    VectorSchemaRoot.create(schema, allocator)

  def serializeArrow(root: VectorSchemaRoot): ByteArrayOutputStream = {
    val out = new ByteArrayOutputStream

    val writer: ArrowStreamWriter = new ArrowStreamWriter(root, null, out)
    writer.close()
    out
  }

  def deserializeArrow(stream: ByteArrayOutputStream): ArrowStreamReader = {
    val in     = new ByteArrayInputStream(stream.toByteArray)
    val reader = new ArrowStreamReader(in, allocator)
    reader
  }

  def killall() = {
    EmbeddedKafka.stop
    true === true
  }
}
