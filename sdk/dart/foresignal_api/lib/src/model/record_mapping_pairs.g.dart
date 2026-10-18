// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'record_mapping_pairs.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RecordMappingPairsCWProxy {
  RecordMappingPairs path(String path);

  RecordMappingPairs key(String key);

  RecordMappingPairs value(String value);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordMappingPairs(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordMappingPairs(...).copyWith(id: 12, name: "My name")
  /// ```
  RecordMappingPairs call({String path, String key, String value});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRecordMappingPairs.copyWith(...)` or call `instanceOfRecordMappingPairs.copyWith.fieldName(value)` for a single field.
class _$RecordMappingPairsCWProxyImpl implements _$RecordMappingPairsCWProxy {
  const _$RecordMappingPairsCWProxyImpl(this._value);

  final RecordMappingPairs _value;

  @override
  RecordMappingPairs path(String path) => call(path: path);

  @override
  RecordMappingPairs key(String key) => call(key: key);

  @override
  RecordMappingPairs value(String value) => call(value: value);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordMappingPairs(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordMappingPairs(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RecordMappingPairs call({
    Object? path = const $CopyWithPlaceholder(),
    Object? key = const $CopyWithPlaceholder(),
    Object? value = const $CopyWithPlaceholder(),
  }) {
    return RecordMappingPairs(
      path: path == const $CopyWithPlaceholder() || path == null
          ? _value.path
          // ignore: cast_nullable_to_non_nullable
          : path as String,
      key: key == const $CopyWithPlaceholder() || key == null
          ? _value.key
          // ignore: cast_nullable_to_non_nullable
          : key as String,
      value: value == const $CopyWithPlaceholder() || value == null
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as String,
    );
  }
}

extension $RecordMappingPairsCopyWith on RecordMappingPairs {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRecordMappingPairs.copyWith(...)` or `instanceOfRecordMappingPairs.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RecordMappingPairsCWProxy get copyWith =>
      _$RecordMappingPairsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RecordMappingPairs _$RecordMappingPairsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RecordMappingPairs', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['path', 'key', 'value']);
      final val = RecordMappingPairs(
        path: $checkedConvert('path', (v) => v as String),
        key: $checkedConvert('key', (v) => v as String),
        value: $checkedConvert('value', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$RecordMappingPairsToJson(RecordMappingPairs instance) =>
    <String, dynamic>{
      'path': instance.path,
      'key': instance.key,
      'value': instance.value,
    };
