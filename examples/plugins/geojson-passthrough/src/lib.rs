//! geojson-passthrough: an example Foresignal parser plugin, ABI v1 (docs/Plugin-Design.md).
//!
//! Input: the raw upstream bytes, a GeoJSON FeatureCollection.
//! Output: a length-prefixed buffer (a little-endian u32 byte count, then that many bytes of UTF-8 JSON):
//!
//! ```json
//! {"records":[{"externalId":<id>,"geometry":<geometry>,"payload":<properties>}],"complete":true}
//! ```
//!
//! Each value is copied verbatim from its feature (an absent member becomes `null`), so the host's ingest
//! normalizer, not this plugin, decides what is valid: a feature without an id is rejected there as
//! `EXTERNAL_ID_INVALID`. The source's single kind is used because records carry no `kind`.
//!
//! Malformed input logs a warning through the one host import and returns 0 (no output).
//!
//! The module imports nothing but `env.foresignal_log`; it has no clock, filesystem or network, because the host
//! grants none. `../wat/plugin.wat` is a hand-written equivalent from which the repository's test artifact is
//! assembled.

use serde::Deserialize;
use serde_json::value::RawValue;
use std::alloc::{alloc as raw_alloc, dealloc as raw_dealloc, Layout};

#[link(wasm_import_module = "env")]
extern "C" {
    /// The host's rate-limited log: level 0 debug, 1 info, 2 warn, 3 error; UTF-8 bytes at `ptr`.
    fn foresignal_log(level: i32, ptr: *const u8, len: i32);
}

/// Buffers are 8-byte aligned; `dealloc` must be given the same size `alloc` was.
const ALIGN: usize = 8;

/// The part of a FeatureCollection this plugin reads; every other member is ignored.
#[derive(Deserialize)]
struct Collection<'a> {
    #[serde(borrow, default)]
    features: Vec<Feature<'a>>,
}

/// The three members of a Feature this plugin copies, borrowed verbatim from the input.
#[derive(Deserialize)]
struct Feature<'a> {
    #[serde(borrow, default)]
    id: Option<&'a RawValue>,
    #[serde(borrow, default)]
    geometry: Option<&'a RawValue>,
    #[serde(borrow, default)]
    properties: Option<&'a RawValue>,
}

/// Reserves `size` bytes for the host (the input buffer) or for the output; 0 when memory is exhausted.
#[no_mangle]
pub extern "C" fn alloc(size: i32) -> i32 {
    if size < 0 {
        return 0;
    }
    if size == 0 {
        // A non-null, aligned pointer that owns nothing; dealloc ignores size 0.
        return ALIGN as i32;
    }
    match Layout::from_size_align(size as usize, ALIGN) {
        Ok(layout) => unsafe { raw_alloc(layout) as i32 },
        Err(_) => 0,
    }
}

/// Frees a buffer from `alloc`.
#[no_mangle]
pub extern "C" fn dealloc(ptr: i32, size: i32) {
    if ptr == 0 || size <= 0 {
        return;
    }
    unsafe { raw_dealloc(ptr as *mut u8, Layout::from_size_align_unchecked(size as usize, ALIGN)) }
}

/// ABI v1 entry point: parses `len` input bytes at `ptr`; returns the length-prefixed output, or 0.
#[no_mangle]
pub extern "C" fn foresignal_parse(ptr: i32, len: i32) -> i32 {
    if len < 0 {
        return 0;
    }
    let input = unsafe { std::slice::from_raw_parts(ptr as *const u8, len as usize) };
    let Some(json) = render(input) else {
        log(2, "malformed GeoJSON input");
        return 0;
    };
    let n = json.len();
    let out = alloc((n + 4) as i32);
    if out == 0 {
        return 0;
    }
    unsafe {
        let dst = out as *mut u8;
        std::ptr::copy_nonoverlapping((n as u32).to_le_bytes().as_ptr(), dst, 4);
        std::ptr::copy_nonoverlapping(json.as_ptr(), dst.add(4), n);
    }
    out
}

/// The output document, or None when the input is not a JSON object with a features array of objects.
fn render(input: &[u8]) -> Option<Vec<u8>> {
    let collection: Collection = serde_json::from_slice(input).ok()?;
    let mut out = String::from("{\"records\":[");
    for (i, feature) in collection.features.iter().enumerate() {
        if i > 0 {
            out.push(',');
        }
        out.push_str("{\"externalId\":");
        out.push_str(raw(feature.id));
        out.push_str(",\"geometry\":");
        out.push_str(raw(feature.geometry));
        out.push_str(",\"payload\":");
        out.push_str(raw(feature.properties));
        out.push('}');
    }
    out.push_str("],\"complete\":true}");
    Some(out.into_bytes())
}

/// A borrowed value's text, or `null` when absent.
fn raw(value: Option<&RawValue>) -> &str {
    value.map(RawValue::get).unwrap_or("null")
}

/// Sends one message to the host log.
fn log(level: i32, message: &str) {
    unsafe { foresignal_log(level, message.as_ptr(), message.len() as i32) }
}
