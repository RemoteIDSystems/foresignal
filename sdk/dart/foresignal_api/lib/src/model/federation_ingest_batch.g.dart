// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'federation_ingest_batch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FederationIngestBatchCWProxy {
  FederationIngestBatch items(List<FederationItem> items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FederationIngestBatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FederationIngestBatch(...).copyWith(id: 12, name: "My name")
  /// ```
  FederationIngestBatch call({List<FederationItem> items});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFederationIngestBatch.copyWith(...)` or call `instanceOfFederationIngestBatch.copyWith.fieldName(value)` for a single field.
class _$FederationIngestBatchCWProxyImpl
    implements _$FederationIngestBatchCWProxy {
  const _$FederationIngestBatchCWProxyImpl(this._value);

  final FederationIngestBatch _value;

  @override
  FederationIngestBatch items(List<FederationItem> items) => call(items: items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FederationIngestBatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FederationIngestBatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FederationIngestBatch call({Object? items = const $CopyWithPlaceholder()}) {
    return FederationIngestBatch(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<FederationItem>,
    );
  }
}

extension $FederationIngestBatchCopyWith on FederationIngestBatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFederationIngestBatch.copyWith(...)` or `instanceOfFederationIngestBatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FederationIngestBatchCWProxy get copyWith =>
      _$FederationIngestBatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FederationIngestBatch _$FederationIngestBatchFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('FederationIngestBatch', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['items']);
  final val = FederationIngestBatch(
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map((e) => FederationItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$FederationIngestBatchToJson(
  FederationIngestBatch instance,
) => <String, dynamic>{'items': instance.items.map((e) => e.toJson()).toList()};
