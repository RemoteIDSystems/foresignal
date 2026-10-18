// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'federation_ingest_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FederationIngestResultCWProxy {
  FederationIngestResult accepted(int accepted);

  FederationIngestResult duplicates(int duplicates);

  FederationIngestResult dropped(FederationIngestResultDropped dropped);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FederationIngestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FederationIngestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  FederationIngestResult call({
    int accepted,
    int duplicates,
    FederationIngestResultDropped dropped,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFederationIngestResult.copyWith(...)` or call `instanceOfFederationIngestResult.copyWith.fieldName(value)` for a single field.
class _$FederationIngestResultCWProxyImpl
    implements _$FederationIngestResultCWProxy {
  const _$FederationIngestResultCWProxyImpl(this._value);

  final FederationIngestResult _value;

  @override
  FederationIngestResult accepted(int accepted) => call(accepted: accepted);

  @override
  FederationIngestResult duplicates(int duplicates) =>
      call(duplicates: duplicates);

  @override
  FederationIngestResult dropped(FederationIngestResultDropped dropped) =>
      call(dropped: dropped);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FederationIngestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FederationIngestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FederationIngestResult call({
    Object? accepted = const $CopyWithPlaceholder(),
    Object? duplicates = const $CopyWithPlaceholder(),
    Object? dropped = const $CopyWithPlaceholder(),
  }) {
    return FederationIngestResult(
      accepted: accepted == const $CopyWithPlaceholder() || accepted == null
          ? _value.accepted
          // ignore: cast_nullable_to_non_nullable
          : accepted as int,
      duplicates:
          duplicates == const $CopyWithPlaceholder() || duplicates == null
          ? _value.duplicates
          // ignore: cast_nullable_to_non_nullable
          : duplicates as int,
      dropped: dropped == const $CopyWithPlaceholder() || dropped == null
          ? _value.dropped
          // ignore: cast_nullable_to_non_nullable
          : dropped as FederationIngestResultDropped,
    );
  }
}

extension $FederationIngestResultCopyWith on FederationIngestResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFederationIngestResult.copyWith(...)` or `instanceOfFederationIngestResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FederationIngestResultCWProxy get copyWith =>
      _$FederationIngestResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FederationIngestResult _$FederationIngestResultFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('FederationIngestResult', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['accepted', 'duplicates', 'dropped']);
  final val = FederationIngestResult(
    accepted: $checkedConvert('accepted', (v) => (v as num).toInt()),
    duplicates: $checkedConvert('duplicates', (v) => (v as num).toInt()),
    dropped: $checkedConvert(
      'dropped',
      (v) => FederationIngestResultDropped.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$FederationIngestResultToJson(
  FederationIngestResult instance,
) => <String, dynamic>{
  'accepted': instance.accepted,
  'duplicates': instance.duplicates,
  'dropped': instance.dropped.toJson(),
};
