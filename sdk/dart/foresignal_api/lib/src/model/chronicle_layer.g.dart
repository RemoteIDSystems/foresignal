// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_layer.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleLayerCWProxy {
  ChronicleLayer id(String id);

  ChronicleLayer chronicleId(String chronicleId);

  ChronicleLayer name(String name);

  ChronicleLayer selector(ChronicleLayerSelector selector);

  ChronicleLayer areaOverride(ChronicleArea? areaOverride);

  ChronicleLayer capture(CaptureMode capture);

  ChronicleLayer effectiveCapture(CaptureMode? effectiveCapture);

  ChronicleLayer enabled(bool enabled);

  ChronicleLayer addedAt(DateTime addedAt);

  ChronicleLayer addedAtSeq(int? addedAtSeq);

  ChronicleLayer removedAt(DateTime? removedAt);

  ChronicleLayer estimate(ChronicleLayerEstimate? estimate);

  ChronicleLayer stats(ChronicleLayerStats? stats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayer(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayer(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleLayer call({
    String id,
    String chronicleId,
    String name,
    ChronicleLayerSelector selector,
    ChronicleArea? areaOverride,
    CaptureMode capture,
    CaptureMode? effectiveCapture,
    bool enabled,
    DateTime addedAt,
    int? addedAtSeq,
    DateTime? removedAt,
    ChronicleLayerEstimate? estimate,
    ChronicleLayerStats? stats,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleLayer.copyWith(...)` or call `instanceOfChronicleLayer.copyWith.fieldName(value)` for a single field.
class _$ChronicleLayerCWProxyImpl implements _$ChronicleLayerCWProxy {
  const _$ChronicleLayerCWProxyImpl(this._value);

  final ChronicleLayer _value;

  @override
  ChronicleLayer id(String id) => call(id: id);

  @override
  ChronicleLayer chronicleId(String chronicleId) =>
      call(chronicleId: chronicleId);

  @override
  ChronicleLayer name(String name) => call(name: name);

  @override
  ChronicleLayer selector(ChronicleLayerSelector selector) =>
      call(selector: selector);

  @override
  ChronicleLayer areaOverride(ChronicleArea? areaOverride) =>
      call(areaOverride: areaOverride);

  @override
  ChronicleLayer capture(CaptureMode capture) => call(capture: capture);

  @override
  ChronicleLayer effectiveCapture(CaptureMode? effectiveCapture) =>
      call(effectiveCapture: effectiveCapture);

  @override
  ChronicleLayer enabled(bool enabled) => call(enabled: enabled);

  @override
  ChronicleLayer addedAt(DateTime addedAt) => call(addedAt: addedAt);

  @override
  ChronicleLayer addedAtSeq(int? addedAtSeq) => call(addedAtSeq: addedAtSeq);

  @override
  ChronicleLayer removedAt(DateTime? removedAt) => call(removedAt: removedAt);

  @override
  ChronicleLayer estimate(ChronicleLayerEstimate? estimate) =>
      call(estimate: estimate);

  @override
  ChronicleLayer stats(ChronicleLayerStats? stats) => call(stats: stats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayer(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayer(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleLayer call({
    Object? id = const $CopyWithPlaceholder(),
    Object? chronicleId = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? selector = const $CopyWithPlaceholder(),
    Object? areaOverride = const $CopyWithPlaceholder(),
    Object? capture = const $CopyWithPlaceholder(),
    Object? effectiveCapture = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? addedAt = const $CopyWithPlaceholder(),
    Object? addedAtSeq = const $CopyWithPlaceholder(),
    Object? removedAt = const $CopyWithPlaceholder(),
    Object? estimate = const $CopyWithPlaceholder(),
    Object? stats = const $CopyWithPlaceholder(),
  }) {
    return ChronicleLayer(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      chronicleId:
          chronicleId == const $CopyWithPlaceholder() || chronicleId == null
          ? _value.chronicleId
          // ignore: cast_nullable_to_non_nullable
          : chronicleId as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      selector: selector == const $CopyWithPlaceholder() || selector == null
          ? _value.selector
          // ignore: cast_nullable_to_non_nullable
          : selector as ChronicleLayerSelector,
      areaOverride: areaOverride == const $CopyWithPlaceholder()
          ? _value.areaOverride
          // ignore: cast_nullable_to_non_nullable
          : areaOverride as ChronicleArea?,
      capture: capture == const $CopyWithPlaceholder() || capture == null
          ? _value.capture
          // ignore: cast_nullable_to_non_nullable
          : capture as CaptureMode,
      effectiveCapture: effectiveCapture == const $CopyWithPlaceholder()
          ? _value.effectiveCapture
          // ignore: cast_nullable_to_non_nullable
          : effectiveCapture as CaptureMode?,
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      addedAt: addedAt == const $CopyWithPlaceholder() || addedAt == null
          ? _value.addedAt
          // ignore: cast_nullable_to_non_nullable
          : addedAt as DateTime,
      addedAtSeq: addedAtSeq == const $CopyWithPlaceholder()
          ? _value.addedAtSeq
          // ignore: cast_nullable_to_non_nullable
          : addedAtSeq as int?,
      removedAt: removedAt == const $CopyWithPlaceholder()
          ? _value.removedAt
          // ignore: cast_nullable_to_non_nullable
          : removedAt as DateTime?,
      estimate: estimate == const $CopyWithPlaceholder()
          ? _value.estimate
          // ignore: cast_nullable_to_non_nullable
          : estimate as ChronicleLayerEstimate?,
      stats: stats == const $CopyWithPlaceholder()
          ? _value.stats
          // ignore: cast_nullable_to_non_nullable
          : stats as ChronicleLayerStats?,
    );
  }
}

extension $ChronicleLayerCopyWith on ChronicleLayer {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleLayer.copyWith(...)` or `instanceOfChronicleLayer.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleLayerCWProxy get copyWith => _$ChronicleLayerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleLayer _$ChronicleLayerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleLayer', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'chronicleId',
      'name',
      'selector',
      'capture',
      'enabled',
      'addedAt',
    ],
  );
  final val = ChronicleLayer(
    id: $checkedConvert('id', (v) => v as String),
    chronicleId: $checkedConvert('chronicleId', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    selector: $checkedConvert(
      'selector',
      (v) => ChronicleLayerSelector.fromJson(v as Map<String, dynamic>),
    ),
    areaOverride: $checkedConvert(
      'areaOverride',
      (v) =>
          v == null ? null : ChronicleArea.fromJson(v as Map<String, dynamic>),
    ),
    capture: $checkedConvert(
      'capture',
      (v) => CaptureMode.fromJson(v as Map<String, dynamic>),
    ),
    effectiveCapture: $checkedConvert(
      'effectiveCapture',
      (v) => v == null ? null : CaptureMode.fromJson(v as Map<String, dynamic>),
    ),
    enabled: $checkedConvert('enabled', (v) => v as bool),
    addedAt: $checkedConvert('addedAt', (v) => DateTime.parse(v as String)),
    addedAtSeq: $checkedConvert('addedAtSeq', (v) => (v as num?)?.toInt()),
    removedAt: $checkedConvert(
      'removedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    estimate: $checkedConvert(
      'estimate',
      (v) => v == null
          ? null
          : ChronicleLayerEstimate.fromJson(v as Map<String, dynamic>),
    ),
    stats: $checkedConvert(
      'stats',
      (v) => v == null
          ? null
          : ChronicleLayerStats.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$ChronicleLayerToJson(ChronicleLayer instance) =>
    <String, dynamic>{
      'id': instance.id,
      'chronicleId': instance.chronicleId,
      'name': instance.name,
      'selector': instance.selector.toJson(),
      'areaOverride': ?instance.areaOverride?.toJson(),
      'capture': instance.capture.toJson(),
      'effectiveCapture': ?instance.effectiveCapture?.toJson(),
      'enabled': instance.enabled,
      'addedAt': instance.addedAt.toIso8601String(),
      'addedAtSeq': ?instance.addedAtSeq,
      'removedAt': ?instance.removedAt?.toIso8601String(),
      'estimate': ?instance.estimate?.toJson(),
      'stats': ?instance.stats?.toJson(),
    };
