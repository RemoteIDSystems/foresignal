;; geojson-passthrough, Foresignal plugin ABI v1 (docs/Plugin-Design.md), hand-written in WebAssembly text.
;;
;; This module is the behavior-equivalent of ../src/lib.rs. The repository's test artifact
;; (src/test/resources/plugins/geojson-passthrough/plugin.wasm) is assembled from this file, because the build
;; machine has no Rust toolchain; see ../README.md and scripts/plugins/README.md.
;;
;; Input: the raw upstream bytes, a GeoJSON FeatureCollection.
;; Output: a length-prefixed buffer, a little-endian u32 byte count followed by that many bytes of UTF-8 JSON:
;;   {"records":[{"externalId":<id>,"geometry":<geometry>,"payload":<properties>},...],"complete":true}
;; Each member value is copied verbatim from the feature (an absent member becomes null). Only the top-level
;; "features" member is read; every other member is skipped. Malformed input logs a warning through the host
;; import and returns 0 (no output).
;;
;; The scanner runs twice: a dry pass that validates the input and counts the output bytes, then a pass that
;; writes them into a buffer of exactly that size. Memory is a bump allocator; dealloc does nothing, because the
;; host discards the instance after every call.
(module
  (import "env" "foresignal_log" (func $log (param i32 i32 i32)))

  (memory (export "memory") 1)

  ;; Constant text, all below the heap start (1024).
  (data (i32.const 16) "features")
  (data (i32.const 32) "id")
  (data (i32.const 40) "geometry")
  (data (i32.const 48) "properties")
  (data (i32.const 64) "{\22records\22:[")
  (data (i32.const 80) "],\22complete\22:true}")
  (data (i32.const 112) "{\22externalId\22:")
  (data (i32.const 128) ",\22geometry\22:")
  (data (i32.const 144) ",\22payload\22:")
  (data (i32.const 160) "null")
  (data (i32.const 176) "malformed GeoJSON input")

  (global $heap (mut i32) (i32.const 1024))
  (global $inEnd (mut i32) (i32.const 0))
  (global $outBase (mut i32) (i32.const 0))
  (global $outLen (mut i32) (i32.const 0))
  (global $dry (mut i32) (i32.const 1))
  (global $first (mut i32) (i32.const 1))

  ;; Reserves size bytes, 8-byte aligned, growing memory when needed; 0 when memory cannot grow.
  (func $alloc (export "alloc") (param $size i32) (result i32)
    (local $ptr i32)
    (local $end i32)
    (local $have i32)
    (local.set $ptr (i32.and (i32.add (global.get $heap) (i32.const 7)) (i32.const -8)))
    (local.set $end (i32.add (local.get $ptr) (local.get $size)))
    (if (i32.lt_u (local.get $end) (local.get $ptr)) (then (return (i32.const 0))))
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

  ;; Nothing to free: the host drops the whole instance after the call.
  (func $dealloc (export "dealloc") (param $ptr i32) (param $len i32))

  ;; The input byte at p, or 0 past the end.
  (func $byte (param $p i32) (result i32)
    (if (result i32) (i32.ge_u (local.get $p) (global.get $inEnd))
      (then (i32.const 0))
      (else (i32.load8_u (local.get $p)))))

  ;; Skips JSON whitespace.
  (func $ws (param $p i32) (result i32)
    (local $c i32)
    (block $done
      (loop $next
        (local.set $c (call $byte (local.get $p)))
        (br_if $done
          (i32.eqz
            (i32.or
              (i32.or (i32.eq (local.get $c) (i32.const 32)) (i32.eq (local.get $c) (i32.const 9)))
              (i32.or (i32.eq (local.get $c) (i32.const 10)) (i32.eq (local.get $c) (i32.const 13))))))
        (local.set $p (i32.add (local.get $p) (i32.const 1)))
        (br $next)))
    (local.get $p))

  ;; p is at an opening quote; returns the position after the closing quote, or -1.
  (func $skipString (param $p i32) (result i32)
    (local $c i32)
    (local.set $p (i32.add (local.get $p) (i32.const 1)))
    (loop $next
      (if (i32.ge_u (local.get $p) (global.get $inEnd)) (then (return (i32.const -1))))
      (local.set $c (i32.load8_u (local.get $p)))
      (if (i32.eq (local.get $c) (i32.const 92))
        (then
          (local.set $p (i32.add (local.get $p) (i32.const 2)))
          (br $next)))
      (if (i32.eq (local.get $c) (i32.const 34)) (then (return (i32.add (local.get $p) (i32.const 1)))))
      (local.set $p (i32.add (local.get $p) (i32.const 1)))
      (br $next))
    (i32.const -1))

  ;; p is at the first character of a value; returns the position after it, or -1.
  (func $skipValue (param $p i32) (result i32)
    (local $c i32)
    (local $depth i32)
    (local $start i32)
    (local.set $c (call $byte (local.get $p)))
    (if (i32.eqz (local.get $c)) (then (return (i32.const -1))))
    (if (i32.eq (local.get $c) (i32.const 34)) (then (return (call $skipString (local.get $p)))))
    (if (i32.or (i32.eq (local.get $c) (i32.const 123)) (i32.eq (local.get $c) (i32.const 91)))
      (then
        (loop $next
          (if (i32.ge_u (local.get $p) (global.get $inEnd)) (then (return (i32.const -1))))
          (local.set $c (i32.load8_u (local.get $p)))
          (if (i32.eq (local.get $c) (i32.const 34))
            (then
              (local.set $p (call $skipString (local.get $p)))
              (if (i32.lt_s (local.get $p) (i32.const 0)) (then (return (i32.const -1))))
              (br $next)))
          (if (i32.or (i32.eq (local.get $c) (i32.const 123)) (i32.eq (local.get $c) (i32.const 91)))
            (then (local.set $depth (i32.add (local.get $depth) (i32.const 1)))))
          (if (i32.or (i32.eq (local.get $c) (i32.const 125)) (i32.eq (local.get $c) (i32.const 93)))
            (then
              (local.set $depth (i32.sub (local.get $depth) (i32.const 1)))
              (if (i32.eqz (local.get $depth)) (then (return (i32.add (local.get $p) (i32.const 1)))))))
          (local.set $p (i32.add (local.get $p) (i32.const 1)))
          (br $next))))
    ;; A number or a literal: up to the next delimiter.
    (local.set $start (local.get $p))
    (block $done
      (loop $next
        (local.set $c (call $byte (local.get $p)))
        (br_if $done (i32.eqz (local.get $c)))
        (br_if $done (i32.eq (local.get $c) (i32.const 44)))
        (br_if $done (i32.eq (local.get $c) (i32.const 125)))
        (br_if $done (i32.eq (local.get $c) (i32.const 93)))
        (br_if $done (i32.eq (local.get $c) (i32.const 32)))
        (br_if $done (i32.eq (local.get $c) (i32.const 9)))
        (br_if $done (i32.eq (local.get $c) (i32.const 10)))
        (br_if $done (i32.eq (local.get $c) (i32.const 13)))
        (local.set $p (i32.add (local.get $p) (i32.const 1)))
        (br $next)))
    (if (result i32) (i32.eq (local.get $p) (local.get $start))
      (then (i32.const -1))
      (else (local.get $p))))

  ;; 1 when the key token [ks, ke) (quotes included) spells the n bytes at lit.
  (func $keyIs (param $ks i32) (param $ke i32) (param $lit i32) (param $n i32) (result i32)
    (local $i i32)
    (if (i32.ne (i32.sub (i32.sub (local.get $ke) (local.get $ks)) (i32.const 2)) (local.get $n))
      (then (return (i32.const 0))))
    (block $done
      (loop $next
        (br_if $done (i32.ge_u (local.get $i) (local.get $n)))
        (if (i32.ne
              (i32.load8_u (i32.add (i32.add (local.get $ks) (i32.const 1)) (local.get $i)))
              (i32.load8_u (i32.add (local.get $lit) (local.get $i))))
          (then (return (i32.const 0))))
        (local.set $i (i32.add (local.get $i) (i32.const 1)))
        (br $next)))
    (i32.const 1))

  ;; Appends n bytes from src to the output (counts them only during the dry pass).
  (func $emit (param $src i32) (param $n i32)
    (local $i i32)
    (if (i32.eqz (global.get $dry))
      (then
        (block $done
          (loop $next
            (br_if $done (i32.ge_u (local.get $i) (local.get $n)))
            (i32.store8
              (i32.add (i32.add (global.get $outBase) (global.get $outLen)) (local.get $i))
              (i32.load8_u (i32.add (local.get $src) (local.get $i))))
            (local.set $i (i32.add (local.get $i) (i32.const 1)))
            (br $next)))))
    (global.set $outLen (i32.add (global.get $outLen) (local.get $n))))

  ;; Appends one byte.
  (func $emitByte (param $b i32)
    (if (i32.eqz (global.get $dry))
      (then (i32.store8 (i32.add (global.get $outBase) (global.get $outLen)) (local.get $b))))
    (global.set $outLen (i32.add (global.get $outLen) (i32.const 1))))

  ;; Appends the value token [s, e), or null when s is 0.
  (func $emitValue (param $s i32) (param $e i32)
    (if (i32.eqz (local.get $s))
      (then (call $emit (i32.const 160) (i32.const 4)))
      (else (call $emit (local.get $s) (i32.sub (local.get $e) (local.get $s))))))

  ;; One feature object at p; emits its record and returns the position after it, or -1.
  (func $feature (param $p i32) (result i32)
    (local $ks i32)
    (local $ke i32)
    (local $vs i32)
    (local $c i32)
    (local $idS i32)
    (local $idE i32)
    (local $gS i32)
    (local $gE i32)
    (local $prS i32)
    (local $prE i32)
    (if (i32.ne (call $byte (local.get $p)) (i32.const 123)) (then (return (i32.const -1))))
    (local.set $p (call $ws (i32.add (local.get $p) (i32.const 1))))
    (block $members
      (if (i32.eq (call $byte (local.get $p)) (i32.const 125))
        (then
          (local.set $p (i32.add (local.get $p) (i32.const 1)))
          (br $members)))
      (loop $member
        (if (i32.ne (call $byte (local.get $p)) (i32.const 34)) (then (return (i32.const -1))))
        (local.set $ks (local.get $p))
        (local.set $p (call $skipString (local.get $p)))
        (if (i32.lt_s (local.get $p) (i32.const 0)) (then (return (i32.const -1))))
        (local.set $ke (local.get $p))
        (local.set $p (call $ws (local.get $p)))
        (if (i32.ne (call $byte (local.get $p)) (i32.const 58)) (then (return (i32.const -1))))
        (local.set $p (call $ws (i32.add (local.get $p) (i32.const 1))))
        (local.set $vs (local.get $p))
        (local.set $p (call $skipValue (local.get $p)))
        (if (i32.lt_s (local.get $p) (i32.const 0)) (then (return (i32.const -1))))
        (if (call $keyIs (local.get $ks) (local.get $ke) (i32.const 32) (i32.const 2))
          (then (local.set $idS (local.get $vs)) (local.set $idE (local.get $p))))
        (if (call $keyIs (local.get $ks) (local.get $ke) (i32.const 40) (i32.const 8))
          (then (local.set $gS (local.get $vs)) (local.set $gE (local.get $p))))
        (if (call $keyIs (local.get $ks) (local.get $ke) (i32.const 48) (i32.const 10))
          (then (local.set $prS (local.get $vs)) (local.set $prE (local.get $p))))
        (local.set $p (call $ws (local.get $p)))
        (local.set $c (call $byte (local.get $p)))
        (if (i32.eq (local.get $c) (i32.const 44))
          (then
            (local.set $p (call $ws (i32.add (local.get $p) (i32.const 1))))
            (br $member)))
        (if (i32.eq (local.get $c) (i32.const 125))
          (then
            (local.set $p (i32.add (local.get $p) (i32.const 1)))
            (br $members)))
        (return (i32.const -1))))
    (if (i32.eqz (global.get $first)) (then (call $emitByte (i32.const 44))))
    (global.set $first (i32.const 0))
    (call $emit (i32.const 112) (i32.const 14))
    (call $emitValue (local.get $idS) (local.get $idE))
    (call $emit (i32.const 128) (i32.const 12))
    (call $emitValue (local.get $gS) (local.get $gE))
    (call $emit (i32.const 144) (i32.const 11))
    (call $emitValue (local.get $prS) (local.get $prE))
    (call $emitByte (i32.const 125))
    (local.get $p))

  ;; The features array at p; returns the position after it, or -1.
  (func $features (param $p i32) (result i32)
    (local $c i32)
    (if (i32.ne (call $byte (local.get $p)) (i32.const 91)) (then (return (i32.const -1))))
    (local.set $p (call $ws (i32.add (local.get $p) (i32.const 1))))
    (if (i32.eq (call $byte (local.get $p)) (i32.const 93)) (then (return (i32.add (local.get $p) (i32.const 1)))))
    (loop $item
      (local.set $p (call $feature (local.get $p)))
      (if (i32.lt_s (local.get $p) (i32.const 0)) (then (return (i32.const -1))))
      (local.set $p (call $ws (local.get $p)))
      (local.set $c (call $byte (local.get $p)))
      (if (i32.eq (local.get $c) (i32.const 44))
        (then
          (local.set $p (call $ws (i32.add (local.get $p) (i32.const 1))))
          (br $item)))
      (if (i32.eq (local.get $c) (i32.const 93)) (then (return (i32.add (local.get $p) (i32.const 1))))))
    (i32.const -1))

  ;; One pass over the document starting at p: 1 when it is well formed, else 0.
  (func $run (param $p i32) (result i32)
    (local $ks i32)
    (local $ke i32)
    (local $c i32)
    (global.set $first (i32.const 1))
    (call $emit (i32.const 64) (i32.const 12))
    (local.set $p (call $ws (local.get $p)))
    (if (i32.ne (call $byte (local.get $p)) (i32.const 123)) (then (return (i32.const 0))))
    (local.set $p (call $ws (i32.add (local.get $p) (i32.const 1))))
    (block $members
      (br_if $members (i32.eq (call $byte (local.get $p)) (i32.const 125)))
      (loop $member
        (if (i32.ne (call $byte (local.get $p)) (i32.const 34)) (then (return (i32.const 0))))
        (local.set $ks (local.get $p))
        (local.set $p (call $skipString (local.get $p)))
        (if (i32.lt_s (local.get $p) (i32.const 0)) (then (return (i32.const 0))))
        (local.set $ke (local.get $p))
        (local.set $p (call $ws (local.get $p)))
        (if (i32.ne (call $byte (local.get $p)) (i32.const 58)) (then (return (i32.const 0))))
        (local.set $p (call $ws (i32.add (local.get $p) (i32.const 1))))
        (if (call $keyIs (local.get $ks) (local.get $ke) (i32.const 16) (i32.const 8))
          (then (local.set $p (call $features (local.get $p))))
          (else (local.set $p (call $skipValue (local.get $p)))))
        (if (i32.lt_s (local.get $p) (i32.const 0)) (then (return (i32.const 0))))
        (local.set $p (call $ws (local.get $p)))
        (local.set $c (call $byte (local.get $p)))
        (if (i32.eq (local.get $c) (i32.const 44))
          (then
            (local.set $p (call $ws (i32.add (local.get $p) (i32.const 1))))
            (br $member)))
        (br_if $members (i32.eq (local.get $c) (i32.const 125)))
        (return (i32.const 0))))
    (call $emit (i32.const 80) (i32.const 18))
    (i32.const 1))

  ;; ABI v1 entry point: parses len input bytes at ptr; returns the length-prefixed output, or 0.
  (func (export "foresignal_parse") (param $ptr i32) (param $len i32) (result i32)
    (local $total i32)
    (local $buf i32)
    (global.set $inEnd (i32.add (local.get $ptr) (local.get $len)))
    (global.set $dry (i32.const 1))
    (global.set $outLen (i32.const 0))
    (if (i32.eqz (call $run (local.get $ptr)))
      (then
        (call $log (i32.const 2) (i32.const 176) (i32.const 23))
        (return (i32.const 0))))
    (local.set $total (global.get $outLen))
    (local.set $buf (call $alloc (i32.add (local.get $total) (i32.const 4))))
    (if (i32.eqz (local.get $buf)) (then (return (i32.const 0))))
    (i32.store (local.get $buf) (local.get $total))
    (global.set $outBase (i32.add (local.get $buf) (i32.const 4)))
    (global.set $outLen (i32.const 0))
    (global.set $dry (i32.const 0))
    (drop (call $run (local.get $ptr)))
    (local.get $buf)))
