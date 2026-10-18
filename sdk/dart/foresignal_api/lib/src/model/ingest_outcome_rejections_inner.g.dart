// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ingest_outcome_rejections_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IngestOutcomeRejectionsInnerCWProxy {
  IngestOutcomeRejectionsInner index(int? index);

  IngestOutcomeRejectionsInner externalId(String? externalId);

  IngestOutcomeRejectionsInner reason(String? reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestOutcomeRejectionsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestOutcomeRejectionsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  IngestOutcomeRejectionsInner call({
    int? index,
    String? externalId,
    String? reason,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIngestOutcomeRejectionsInner.copyWith(...)` or call `instanceOfIngestOutcomeRejectionsInner.copyWith.fieldName(value)` for a single field.
class _$IngestOutcomeRejectionsInnerCWProxyImpl
    implements _$IngestOutcomeRejectionsInnerCWProxy {
  const _$IngestOutcomeRejectionsInnerCWProxyImpl(this._value);

  final IngestOutcomeRejectionsInner _value;

  @override
  IngestOutcomeRejectionsInner index(int? index) => call(index: index);

  @override
  IngestOutcomeRejectionsInner externalId(String? externalId) =>
      call(externalId: externalId);

  @override
  IngestOutcomeRejectionsInner reason(String? reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestOutcomeRejectionsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestOutcomeRejectionsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IngestOutcomeRejectionsInner call({
    Object? index = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return IngestOutcomeRejectionsInner(
      index: index == const $CopyWithPlaceholder()
          ? _value.index
          // ignore: cast_nullable_to_non_nullable
          : index as int?,
      externalId: externalId == const $CopyWithPlaceholder()
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String?,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
    );
  }
}

extension $IngestOutcomeRejectionsInnerCopyWith
    on IngestOutcomeRejectionsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIngestOutcomeRejectionsInner.copyWith(...)` or `instanceOfIngestOutcomeRejectionsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IngestOutcomeRejectionsInnerCWProxy get copyWith =>
      _$IngestOutcomeRejectionsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IngestOutcomeRejectionsInner _$IngestOutcomeRejectionsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IngestOutcomeRejectionsInner', json, ($checkedConvert) {
  final val = IngestOutcomeRejectionsInner(
    index: $checkedConvert('index', (v) => (v as num?)?.toInt()),
    externalId: $checkedConvert('externalId', (v) => v as String?),
    reason: $checkedConvert('reason', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$IngestOutcomeRejectionsInnerToJson(
  IngestOutcomeRejectionsInner instance,
) => <String, dynamic>{
  'index': ?instance.index,
  'externalId': ?instance.externalId,
  'reason': ?instance.reason,
};
