;; log-spammer: an ABI v1 plugin for WasmParserHostTest (prompt 141) that calls the single host import
;; env.foresignal_log 1000 times (level 1, "spam") and then returns a valid empty result. The host must rate-limit
;; the messages and still accept the result.
(module
  (import "env" "foresignal_log" (func $log (param i32 i32 i32)))
  (memory (export "memory") 1)
  (data (i32.const 256) "spam")
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
    (block $done
      (loop $spam
        (br_if $done (i32.ge_u (local.get $i) (i32.const 1000)))
        (call $log (i32.const 1) (i32.const 256) (i32.const 4))
        (local.set $i (i32.add (local.get $i) (i32.const 1)))
        (br $spam)))
    (local.set $i (i32.const 0))
    (i32.store (i32.const 64) (i32.const 30))
    (block $copied
      (loop $copy
        (br_if $copied (i32.ge_u (local.get $i) (i32.const 30)))
        (i32.store8 (i32.add (i32.const 68) (local.get $i)) (i32.load8_u (i32.add (i32.const 512) (local.get $i))))
        (local.set $i (i32.add (local.get $i) (i32.const 1)))
        (br $copy)))
    (i32.const 64)))
