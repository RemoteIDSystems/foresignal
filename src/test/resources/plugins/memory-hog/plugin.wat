;; memory-hog: a misbehaving ABI v1 plugin for WasmParserHostTest (prompt 141).
;;
;; foresignal_parse grows memory 16 pages at a time until it holds 2048 pages (128 MB), twice the host's 64 MB cap,
;; and only then returns a valid empty result. Under the cap memory.grow fails at 1024 pages and the module traps
;; (unreachable), so the call must fail; if the cap were not enforced the call would succeed.
(module
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
    (loop $grow
      (if (i32.eq (memory.grow (i32.const 16)) (i32.const -1)) (then unreachable))
      (br_if $grow (i32.lt_u (memory.size) (i32.const 2048))))
    ;; Length-prefixed output at 64: the 30 bytes of the data segment at 512.
    (i32.store (i32.const 64) (i32.const 30))
    (block $done
      (loop $copy
        (br_if $done (i32.ge_u (local.get $i) (i32.const 30)))
        (i32.store8 (i32.add (i32.const 68) (local.get $i)) (i32.load8_u (i32.add (i32.const 512) (local.get $i))))
        (local.set $i (i32.add (local.get $i) (i32.const 1)))
        (br $copy)))
    (i32.const 64)))
