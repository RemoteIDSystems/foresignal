// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'allow_external_snapshot200_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AllowExternalSnapshot200ResponseCWProxy {
  AllowExternalSnapshot200Response snapshotId(String snapshotId);

  AllowExternalSnapshot200Response removed(int removed);

  AllowExternalSnapshot200Response outcome(IngestOutcome outcome);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AllowExternalSnapshot200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AllowExternalSnapshot200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  AllowExternalSnapshot200Response call({
    String snapshotId,
    int removed,
    IngestOutcome outcome,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAllowExternalSnapshot200Response.copyWith(...)` or call `instanceOfAllowExternalSnapshot200Response.copyWith.fieldName(value)` for a single field.
class _$AllowExternalSnapshot200ResponseCWProxyImpl
    implements _$AllowExternalSnapshot200ResponseCWProxy {
  const _$AllowExternalSnapshot200ResponseCWProxyImpl(this._value);

  final AllowExternalSnapshot200Response _value;

  @override
  AllowExternalSnapshot200Response snapshotId(String snapshotId) =>
      call(snapshotId: snapshotId);

  @override
  AllowExternalSnapshot200Response removed(int removed) =>
      call(removed: removed);

  @override
  AllowExternalSnapshot200Response outcome(IngestOutcome outcome) =>
      call(outcome: outcome);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AllowExternalSnapshot200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AllowExternalSnapshot200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AllowExternalSnapshot200Response call({
    Object? snapshotId = const $CopyWithPlaceholder(),
    Object? removed = const $CopyWithPlaceholder(),
    Object? outcome = const $CopyWithPlaceholder(),
  }) {
    return AllowExternalSnapshot200Response(
      snapshotId:
          snapshotId == const $CopyWithPlaceholder() || snapshotId == null
          ? _value.snapshotId
          // ignore: cast_nullable_to_non_nullable
          : snapshotId as String,
      removed: removed == const $CopyWithPlaceholder() || removed == null
          ? _value.removed
          // ignore: cast_nullable_to_non_nullable
          : removed as int,
      outcome: outcome == const $CopyWithPlaceholder() || outcome == null
          ? _value.outcome
          // ignore: cast_nullable_to_non_nullable
          : outcome as IngestOutcome,
    );
  }
}

extension $AllowExternalSnapshot200ResponseCopyWith
    on AllowExternalSnapshot200Response {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAllowExternalSnapshot200Response.copyWith(...)` or `instanceOfAllowExternalSnapshot200Response.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AllowExternalSnapshot200ResponseCWProxy get copyWith =>
      _$AllowExternalSnapshot200ResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AllowExternalSnapshot200Response _$AllowExternalSnapshot200ResponseFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AllowExternalSnapshot200Response', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['snapshotId', 'removed', 'outcome']);
  final val = AllowExternalSnapshot200Response(
    snapshotId: $checkedConvert('snapshotId', (v) => v as String),
    removed: $checkedConvert('removed', (v) => (v as num).toInt()),
    outcome: $checkedConvert(
      'outcome',
      (v) => IngestOutcome.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$AllowExternalSnapshot200ResponseToJson(
  AllowExternalSnapshot200Response instance,
) => <String, dynamic>{
  'snapshotId': instance.snapshotId,
  'removed': instance.removed,
  'outcome': instance.outcome.toJson(),
};
