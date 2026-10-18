;; wasi-import: a misbehaving ABI v1 plugin for WasmParserHostTest (prompt 141).
;;
;; Otherwise a valid parser, it imports the WASI function wasi_snapshot_preview1.fd_write. The host grants no WASI
;; imports (no clock, filesystem or network), so the module must be refused at load.
(module
  (import "wasi_snapshot_preview1" "fd_write" (func $fdWrite (param i32 i32 i32 i32) (result i32)))
  (memory (export "memory") 1)
  (data (i32.const 512) "{\22records\22:[],\22complete\22:true}")
  (global $heap (mut i32) (i32.const 1024))

  (func $alloc (export "alloc") (param $size i32) (result i32)
    (local $ptr i32)
    (local $end i32)
    (local $have i32)
    (local.set $ptr (i32.and (i32.add (global.get $heap) (i32.const 7)) (i32.const -8)))
    (local.set $end (i32.add (local.get $ptr) (local.get $size)))
    (local.set $have (i32.shl (memory.size) (i32.const 16)))
    (if (i32.gt_u (local.get $end) (local.get $have))
      (then
        (if (i32.eq
              (memory.grow
                (i32.shr_u (i32.add (i32.sub (local.get $end) (local.get $have)) (i32.const 65535)) (i32.const 16)))
              (i32.const -1))
          (then (return (i32.const 0))))))
    (global.set $heap (local.get $end))
    (local.get $ptr))

  (func (export "dealloc") (param $ptr i32) (param $len i32))

  (func (export "foresignal_parse") (param $ptr i32) (param $len i32) (result i32)
    (local $i i32)
    ;; Writes "x" to stdout through WASI before answering: an iovec at 128 pointing at the byte at 136.
    (i32.store8 (i32.const 136) (i32.const 120))
    (i32.store (i32.const 128) (i32.const 136))
    (i32.store (i32.const 132) (i32.const 1))
    (drop (call $fdWrite (i32.const 1) (i32.const 128) (i32.const 1) (i32.const 140)))
    (i32.store (i32.const 64) (i32.const 30))
    (block $done
      (loop $copy
        (br_if $done (i32.ge_u (local.get $i) (i32.const 30)))
        (i32.store8 (i32.add (i32.const 68) (local.get $i)) (i32.load8_u (i32.add (i32.const 512) (local.get $i))))
        (local.set $i (i32.add (local.get $i) (i32.const 1)))
        (br $copy)))
    (i32.const 64)))
