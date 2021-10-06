package xiangshan.cache

import chipsalliance.rocketchip.config.Parameters
import chisel3._
import chisel3.util._

class RefillPipeReq(implicit p: Parameters) extends DCacheBundle {
  val source = UInt(sourceTypeWidth.W)
  val addr = UInt(PAddrBits.W)
  val way_en = UInt(DCacheWays.W)
  val wmask = UInt(DCacheBanks.W)
  val data = Vec(DCacheBanks, UInt(DCacheSRAMRowBits.W))
  val meta = new Meta
  val alias = UInt(2.W) // TODO: parameterize

  val id = UInt(reqIdWidth.W)

  def paddrWithVirtualAlias: UInt = {
    Cat(alias, addr(DCacheSameVPAddrLength - 1, 0))
  }
}

class RefillPipe(implicit p: Parameters) extends DCacheModule {
  val io = IO(new Bundle() {
    val req = Flipped(DecoupledIO(new RefillPipeReq))
    val data_write = DecoupledIO(new L1BankedDataWriteReq)
    val meta_write = DecoupledIO(new MetaWriteReq)
    val tag_write = DecoupledIO(new TagWriteReq)
    val store_resp = ValidIO(new DCacheLineResp)
    val replace_access = ValidIO(new ReplacementAccessBundle)
  })

  // Assume that write in refill pipe is always ready
  assert(RegNext(io.data_write.ready))
  assert(RegNext(io.meta_write.ready))
  assert(RegNext(io.tag_write.ready))

  io.req.ready := io.data_write.ready && io.meta_write.ready && io.tag_write.ready

  val idx = io.req.bits.paddrWithVirtualAlias
  val tag = addr_to_dcache_tag(io.req.bits.addr)

  io.data_write.valid := io.req.valid
  io.data_write.bits.addr := idx
  io.data_write.bits.way_en := io.req.bits.way_en
  io.data_write.bits.wmask := io.req.bits.wmask
  io.data_write.bits.data := io.req.bits.data

  io.meta_write.valid := io.req.valid
  io.meta_write.bits.idx := idx
  io.meta_write.bits.way_en := io.req.bits.way_en
  io.meta_write.bits.meta := io.req.bits.meta
  io.meta_write.bits.tag := tag

  io.tag_write.valid := io.req.valid
  io.tag_write.bits.idx := idx
  io.tag_write.bits.way_en := io.req.bits.way_en
  io.tag_write.bits.tag := tag

  io.store_resp.valid := io.req.fire() && io.req.bits.source === STORE_SOURCE.U
  io.store_resp.bits := DontCare
  io.store_resp.bits.miss := false.B
  io.store_resp.bits.replay := false.B
  io.store_resp.bits.id := io.req.bits.id

  io.replace_access.valid := RegNext(io.req.fire())
  io.replace_access.bits.set := RegNext(idx)
  io.replace_access.bits.way := RegNext(OHToUInt(io.req.bits.way_en))
}
