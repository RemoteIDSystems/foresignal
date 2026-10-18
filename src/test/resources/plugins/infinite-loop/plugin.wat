;; infinite-loop: a misbehaving ABI v1 plugin for WasmParserHostTest (prompt 141).
;;
;; foresignal_parse never returns. The host's 5 s wall-clock timeout must interrupt it.
(module
  (memory (export "memory") 1)
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
    (loop $forever
      (br $forever))
    (i32.const 0)))
