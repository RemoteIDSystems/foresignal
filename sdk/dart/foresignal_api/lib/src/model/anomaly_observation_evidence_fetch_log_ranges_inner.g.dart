// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'anomaly_observation_evidence_fetch_log_ranges_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnomalyObservationEvidenceFetchLogRangesInnerCWProxy {
  AnomalyObservationEvidenceFetchLogRangesInner sourceId(String sourceId);

  AnomalyObservationEvidenceFetchLogRangesInner fromId(int fromId);

  AnomalyObservationEvidenceFetchLogRangesInner toId(int toId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnomalyObservationEvidenceFetchLogRangesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnomalyObservationEvidenceFetchLogRangesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  AnomalyObservationEvidenceFetchLogRangesInner call({
    String sourceId,
    int fromId,
    int toId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnomalyObservationEvidenceFetchLogRangesInner.copyWith(...)` or call `instanceOfAnomalyObservationEvidenceFetchLogRangesInner.copyWith.fieldName(value)` for a single field.
class _$AnomalyObservationEvidenceFetchLogRangesInnerCWProxyImpl
    implements _$AnomalyObservationEvidenceFetchLogRangesInnerCWProxy {
  const _$AnomalyObservationEvidenceFetchLogRangesInnerCWProxyImpl(this._value);

  final AnomalyObservationEvidenceFetchLogRangesInner _value;

  @override
  AnomalyObservationEvidenceFetchLogRangesInner sourceId(String sourceId) =>
      call(sourceId: sourceId);

  @override
  AnomalyObservationEvidenceFetchLogRangesInner fromId(int fromId) =>
      call(fromId: fromId);

  @override
  AnomalyObservationEvidenceFetchLogRangesInner toId(int toId) =>
      call(toId: toId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnomalyObservationEvidenceFetchLogRangesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnomalyObservationEvidenceFetchLogRangesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnomalyObservationEvidenceFetchLogRangesInner call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? fromId = const $CopyWithPlaceholder(),
    Object? toId = const $CopyWithPlaceholder(),
  }) {
    return AnomalyObservationEvidenceFetchLogRangesInner(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      fromId: fromId == const $CopyWithPlaceholder() || fromId == null
          ? _value.fromId
          // ignore: cast_nullable_to_non_nullable
          : fromId as int,
      toId: toId == const $CopyWithPlaceholder() || toId == null
          ? _value.toId
          // ignore: cast_nullable_to_non_nullable
          : toId as int,
    );
  }
}

extension $AnomalyObservationEvidenceFetchLogRangesInnerCopyWith
    on AnomalyObservationEvidenceFetchLogRangesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnomalyObservationEvidenceFetchLogRangesInner.copyWith(...)` or `instanceOfAnomalyObservationEvidenceFetchLogRangesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnomalyObservationEvidenceFetchLogRangesInnerCWProxy get copyWith =>
      _$AnomalyObservationEvidenceFetchLogRangesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnomalyObservationEvidenceFetchLogRangesInner
_$AnomalyObservationEvidenceFetchLogRangesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnomalyObservationEvidenceFetchLogRangesInner', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['sourceId', 'fromId', 'toId']);
  final val = AnomalyObservationEvidenceFetchLogRangesInner(
    sourceId: $checkedConvert('sourceId', (v) => v as String),
    fromId: $checkedConvert('fromId', (v) => (v as num).toInt()),
    toId: $checkedConvert('toId', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$AnomalyObservationEvidenceFetchLogRangesInnerToJson(
  AnomalyObservationEvidenceFetchLogRangesInner instance,
) => <String, dynamic>{
  'sourceId': instance.sourceId,
  'fromId': instance.fromId,
  'toId': instance.toId,
};
