// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'record_mapping_filter_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RecordMappingFilterInnerCWProxy {
  RecordMappingFilterInner path(String path);

  RecordMappingFilterInner op(RecordMappingFilterInnerOpEnum op);

  RecordMappingFilterInner value(Object? value);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordMappingFilterInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordMappingFilterInner(...).copyWith(id: 12, name: "My name")
  /// ```
  RecordMappingFilterInner call({
    String path,
    RecordMappingFilterInnerOpEnum op,
    Object? value,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRecordMappingFilterInner.copyWith(...)` or call `instanceOfRecordMappingFilterInner.copyWith.fieldName(value)` for a single field.
class _$RecordMappingFilterInnerCWProxyImpl
    implements _$RecordMappingFilterInnerCWProxy {
  const _$RecordMappingFilterInnerCWProxyImpl(this._value);

  final RecordMappingFilterInner _value;

  @override
  RecordMappingFilterInner path(String path) => call(path: path);

  @override
  RecordMappingFilterInner op(RecordMappingFilterInnerOpEnum op) =>
      call(op: op);

  @override
  RecordMappingFilterInner value(Object? value) => call(value: value);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordMappingFilterInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordMappingFilterInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RecordMappingFilterInner call({
    Object? path = const $CopyWithPlaceholder(),
    Object? op = const $CopyWithPlaceholder(),
    Object? value = const $CopyWithPlaceholder(),
  }) {
    return RecordMappingFilterInner(
      path: path == const $CopyWithPlaceholder() || path == null
          ? _value.path
          // ignore: cast_nullable_to_non_nullable
          : path as String,
      op: op == const $CopyWithPlaceholder() || op == null
          ? _value.op
          // ignore: cast_nullable_to_non_nullable
          : op as RecordMappingFilterInnerOpEnum,
      value: value == const $CopyWithPlaceholder()
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as Object?,
    );
  }
}

extension $RecordMappingFilterInnerCopyWith on RecordMappingFilterInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRecordMappingFilterInner.copyWith(...)` or `instanceOfRecordMappingFilterInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RecordMappingFilterInnerCWProxy get copyWith =>
      _$RecordMappingFilterInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RecordMappingFilterInner _$RecordMappingFilterInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RecordMappingFilterInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['path', 'op']);
  final val = RecordMappingFilterInner(
    path: $checkedConvert('path', (v) => v as String),
    op: $checkedConvert(
      'op',
      (v) => $enumDecode(_$RecordMappingFilterInnerOpEnumEnumMap, v),
    ),
    value: $checkedConvert('value', (v) => v),
  );
  return val;
});

Map<String, dynamic> _$RecordMappingFilterInnerToJson(
  RecordMappingFilterInner instance,
) => <String, dynamic>{
  'path': instance.path,
  'op': _$RecordMappingFilterInnerOpEnumEnumMap[instance.op]!,
  'value': ?instance.value,
};

const _$RecordMappingFilterInnerOpEnumEnumMap = {
  RecordMappingFilterInnerOpEnum.equalEqual: '==',
  RecordMappingFilterInnerOpEnum.exclamationEqual: '!=',
  RecordMappingFilterInnerOpEnum.exists: 'exists',
  RecordMappingFilterInnerOpEnum.in_: 'in',
};
