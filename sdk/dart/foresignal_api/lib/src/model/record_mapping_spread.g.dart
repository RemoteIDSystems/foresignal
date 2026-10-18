// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'record_mapping_spread.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RecordMappingSpreadCWProxy {
  RecordMappingSpread path(String path);

  RecordMappingSpread type(RecordMappingSpreadTypeEnum? type);

  RecordMappingSpread max(int? max);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordMappingSpread(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordMappingSpread(...).copyWith(id: 12, name: "My name")
  /// ```
  RecordMappingSpread call({
    String path,
    RecordMappingSpreadTypeEnum? type,
    int? max,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRecordMappingSpread.copyWith(...)` or call `instanceOfRecordMappingSpread.copyWith.fieldName(value)` for a single field.
class _$RecordMappingSpreadCWProxyImpl implements _$RecordMappingSpreadCWProxy {
  const _$RecordMappingSpreadCWProxyImpl(this._value);

  final RecordMappingSpread _value;

  @override
  RecordMappingSpread path(String path) => call(path: path);

  @override
  RecordMappingSpread type(RecordMappingSpreadTypeEnum? type) =>
      call(type: type);

  @override
  RecordMappingSpread max(int? max) => call(max: max);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordMappingSpread(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordMappingSpread(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RecordMappingSpread call({
    Object? path = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? max = const $CopyWithPlaceholder(),
  }) {
    return RecordMappingSpread(
      path: path == const $CopyWithPlaceholder() || path == null
          ? _value.path
          // ignore: cast_nullable_to_non_nullable
          : path as String,
      type: type == const $CopyWithPlaceholder()
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as RecordMappingSpreadTypeEnum?,
      max: max == const $CopyWithPlaceholder()
          ? _value.max
          // ignore: cast_nullable_to_non_nullable
          : max as int?,
    );
  }
}

extension $RecordMappingSpreadCopyWith on RecordMappingSpread {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRecordMappingSpread.copyWith(...)` or `instanceOfRecordMappingSpread.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RecordMappingSpreadCWProxy get copyWith =>
      _$RecordMappingSpreadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RecordMappingSpread _$RecordMappingSpreadFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RecordMappingSpread', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['path']);
      final val = RecordMappingSpread(
        path: $checkedConvert('path', (v) => v as String),
        type: $checkedConvert(
          'type',
          (v) => $enumDecodeNullable(_$RecordMappingSpreadTypeEnumEnumMap, v),
        ),
        max: $checkedConvert('max', (v) => (v as num?)?.toInt() ?? 32),
      );
      return val;
    });

Map<String, dynamic> _$RecordMappingSpreadToJson(
  RecordMappingSpread instance,
) => <String, dynamic>{
  'path': instance.path,
  'type': ?_$RecordMappingSpreadTypeEnumEnumMap[instance.type],
  'max': ?instance.max,
};

const _$RecordMappingSpreadTypeEnumEnumMap = {
  RecordMappingSpreadTypeEnum.number: 'number',
  RecordMappingSpreadTypeEnum.integer: 'integer',
  RecordMappingSpreadTypeEnum.string: 'string',
  RecordMappingSpreadTypeEnum.boolean: 'boolean',
};
