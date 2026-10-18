// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'record_mapping_geometry_one_of2.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RecordMappingGeometryOneOf2CWProxy {
  RecordMappingGeometryOneOf2 const_(List<num> const_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordMappingGeometryOneOf2(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordMappingGeometryOneOf2(...).copyWith(id: 12, name: "My name")
  /// ```
  RecordMappingGeometryOneOf2 call({List<num> const_});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRecordMappingGeometryOneOf2.copyWith(...)` or call `instanceOfRecordMappingGeometryOneOf2.copyWith.fieldName(value)` for a single field.
class _$RecordMappingGeometryOneOf2CWProxyImpl
    implements _$RecordMappingGeometryOneOf2CWProxy {
  const _$RecordMappingGeometryOneOf2CWProxyImpl(this._value);

  final RecordMappingGeometryOneOf2 _value;

  @override
  RecordMappingGeometryOneOf2 const_(List<num> const_) => call(const_: const_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordMappingGeometryOneOf2(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordMappingGeometryOneOf2(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RecordMappingGeometryOneOf2 call({
    Object? const_ = const $CopyWithPlaceholder(),
  }) {
    return RecordMappingGeometryOneOf2(
      const_: const_ == const $CopyWithPlaceholder() || const_ == null
          ? _value.const_
          // ignore: cast_nullable_to_non_nullable
          : const_ as List<num>,
    );
  }
}

extension $RecordMappingGeometryOneOf2CopyWith on RecordMappingGeometryOneOf2 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRecordMappingGeometryOneOf2.copyWith(...)` or `instanceOfRecordMappingGeometryOneOf2.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RecordMappingGeometryOneOf2CWProxy get copyWith =>
      _$RecordMappingGeometryOneOf2CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RecordMappingGeometryOneOf2 _$RecordMappingGeometryOneOf2FromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RecordMappingGeometryOneOf2', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['const']);
  final val = RecordMappingGeometryOneOf2(
    const_: $checkedConvert(
      'const',
      (v) => (v as List<dynamic>).map((e) => e as num).toList(),
    ),
  );
  return val;
}, fieldKeyMap: const {'const_': 'const'});

Map<String, dynamic> _$RecordMappingGeometryOneOf2ToJson(
  RecordMappingGeometryOneOf2 instance,
) => <String, dynamic>{'const': instance.const_};
