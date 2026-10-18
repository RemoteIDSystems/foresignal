// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ingest_key_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IngestKeyResponseCWProxy {
  IngestKeyResponse ingestKey(String ingestKey);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestKeyResponse(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestKeyResponse(...).copyWith(id: 12, name: "My name")
  /// ```
  IngestKeyResponse call({String ingestKey});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIngestKeyResponse.copyWith(...)` or call `instanceOfIngestKeyResponse.copyWith.fieldName(value)` for a single field.
class _$IngestKeyResponseCWProxyImpl implements _$IngestKeyResponseCWProxy {
  const _$IngestKeyResponseCWProxyImpl(this._value);

  final IngestKeyResponse _value;

  @override
  IngestKeyResponse ingestKey(String ingestKey) => call(ingestKey: ingestKey);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestKeyResponse(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestKeyResponse(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IngestKeyResponse call({Object? ingestKey = const $CopyWithPlaceholder()}) {
    return IngestKeyResponse(
      ingestKey: ingestKey == const $CopyWithPlaceholder() || ingestKey == null
          ? _value.ingestKey
          // ignore: cast_nullable_to_non_nullable
          : ingestKey as String,
    );
  }
}

extension $IngestKeyResponseCopyWith on IngestKeyResponse {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIngestKeyResponse.copyWith(...)` or `instanceOfIngestKeyResponse.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IngestKeyResponseCWProxy get copyWith =>
      _$IngestKeyResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IngestKeyResponse _$IngestKeyResponseFromJson(Map<String, dynamic> json) =>
    $checkedCreate('IngestKeyResponse', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['ingestKey']);
      final val = IngestKeyResponse(
        ingestKey: $checkedConvert('ingestKey', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$IngestKeyResponseToJson(IngestKeyResponse instance) =>
    <String, dynamic>{'ingestKey': instance.ingestKey};
