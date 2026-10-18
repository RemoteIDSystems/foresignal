// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'product_loop_frames_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProductLoopFramesInnerCWProxy {
  ProductLoopFramesInner productId(String productId);

  ProductLoopFramesInner time(DateTime time);

  ProductLoopFramesInner timeKey(String? timeKey);

  ProductLoopFramesInner frameKey(String frameKey);

  ProductLoopFramesInner kind(ProductLoopFramesInnerKindEnum kind);

  ProductLoopFramesInner run(String? run);

  ProductLoopFramesInner step(int? step);

  ProductLoopFramesInner url(String url);

  ProductLoopFramesInner bytes(int? bytes);

  ProductLoopFramesInner expiresAt(DateTime? expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductLoopFramesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductLoopFramesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ProductLoopFramesInner call({
    String productId,
    DateTime time,
    String? timeKey,
    String frameKey,
    ProductLoopFramesInnerKindEnum kind,
    String? run,
    int? step,
    String url,
    int? bytes,
    DateTime? expiresAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProductLoopFramesInner.copyWith(...)` or call `instanceOfProductLoopFramesInner.copyWith.fieldName(value)` for a single field.
class _$ProductLoopFramesInnerCWProxyImpl
    implements _$ProductLoopFramesInnerCWProxy {
  const _$ProductLoopFramesInnerCWProxyImpl(this._value);

  final ProductLoopFramesInner _value;

  @override
  ProductLoopFramesInner productId(String productId) =>
      call(productId: productId);

  @override
  ProductLoopFramesInner time(DateTime time) => call(time: time);

  @override
  ProductLoopFramesInner timeKey(String? timeKey) => call(timeKey: timeKey);

  @override
  ProductLoopFramesInner frameKey(String frameKey) => call(frameKey: frameKey);

  @override
  ProductLoopFramesInner kind(ProductLoopFramesInnerKindEnum kind) =>
      call(kind: kind);

  @override
  ProductLoopFramesInner run(String? run) => call(run: run);

  @override
  ProductLoopFramesInner step(int? step) => call(step: step);

  @override
  ProductLoopFramesInner url(String url) => call(url: url);

  @override
  ProductLoopFramesInner bytes(int? bytes) => call(bytes: bytes);

  @override
  ProductLoopFramesInner expiresAt(DateTime? expiresAt) =>
      call(expiresAt: expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductLoopFramesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductLoopFramesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProductLoopFramesInner call({
    Object? productId = const $CopyWithPlaceholder(),
    Object? time = const $CopyWithPlaceholder(),
    Object? timeKey = const $CopyWithPlaceholder(),
    Object? frameKey = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? run = const $CopyWithPlaceholder(),
    Object? step = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
    Object? bytes = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
  }) {
    return ProductLoopFramesInner(
      productId: productId == const $CopyWithPlaceholder() || productId == null
          ? _value.productId
          // ignore: cast_nullable_to_non_nullable
          : productId as String,
      time: time == const $CopyWithPlaceholder() || time == null
          ? _value.time
          // ignore: cast_nullable_to_non_nullable
          : time as DateTime,
      timeKey: timeKey == const $CopyWithPlaceholder()
          ? _value.timeKey
          // ignore: cast_nullable_to_non_nullable
          : timeKey as String?,
      frameKey: frameKey == const $CopyWithPlaceholder() || frameKey == null
          ? _value.frameKey
          // ignore: cast_nullable_to_non_nullable
          : frameKey as String,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as ProductLoopFramesInnerKindEnum,
      run: run == const $CopyWithPlaceholder()
          ? _value.run
          // ignore: cast_nullable_to_non_nullable
          : run as String?,
      step: step == const $CopyWithPlaceholder()
          ? _value.step
          // ignore: cast_nullable_to_non_nullable
          : step as int?,
      url: url == const $CopyWithPlaceholder() || url == null
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String,
      bytes: bytes == const $CopyWithPlaceholder()
          ? _value.bytes
          // ignore: cast_nullable_to_non_nullable
          : bytes as int?,
      expiresAt: expiresAt == const $CopyWithPlaceholder()
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime?,
    );
  }
}

extension $ProductLoopFramesInnerCopyWith on ProductLoopFramesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProductLoopFramesInner.copyWith(...)` or `instanceOfProductLoopFramesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProductLoopFramesInnerCWProxy get copyWith =>
      _$ProductLoopFramesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProductLoopFramesInner _$ProductLoopFramesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ProductLoopFramesInner', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['productId', 'time', 'frameKey', 'kind', 'url'],
  );
  final val = ProductLoopFramesInner(
    productId: $checkedConvert('productId', (v) => v as String),
    time: $checkedConvert('time', (v) => DateTime.parse(v as String)),
    timeKey: $checkedConvert('timeKey', (v) => v as String?),
    frameKey: $checkedConvert('frameKey', (v) => v as String),
    kind: $checkedConvert(
      'kind',
      (v) => $enumDecode(_$ProductLoopFramesInnerKindEnumEnumMap, v),
    ),
    run: $checkedConvert('run', (v) => v as String?),
    step: $checkedConvert('step', (v) => (v as num?)?.toInt()),
    url: $checkedConvert('url', (v) => v as String),
    bytes: $checkedConvert('bytes', (v) => (v as num?)?.toInt()),
    expiresAt: $checkedConvert(
      'expiresAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$ProductLoopFramesInnerToJson(
  ProductLoopFramesInner instance,
) => <String, dynamic>{
  'productId': instance.productId,
  'time': instance.time.toIso8601String(),
  'timeKey': ?instance.timeKey,
  'frameKey': instance.frameKey,
  'kind': _$ProductLoopFramesInnerKindEnumEnumMap[instance.kind]!,
  'run': ?instance.run,
  'step': ?instance.step,
  'url': instance.url,
  'bytes': ?instance.bytes,
  'expiresAt': ?instance.expiresAt?.toIso8601String(),
};

const _$ProductLoopFramesInnerKindEnumEnumMap = {
  ProductLoopFramesInnerKindEnum.observed: 'observed',
  ProductLoopFramesInnerKindEnum.forecast: 'forecast',
};
