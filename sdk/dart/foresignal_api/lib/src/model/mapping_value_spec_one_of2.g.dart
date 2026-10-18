// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mapping_value_spec_one_of2.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MappingValueSpecOneOf2CWProxy {
  MappingValueSpecOneOf2 const_(Object? const_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingValueSpecOneOf2(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingValueSpecOneOf2(...).copyWith(id: 12, name: "My name")
  /// ```
  MappingValueSpecOneOf2 call({Object? const_});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMappingValueSpecOneOf2.copyWith(...)` or call `instanceOfMappingValueSpecOneOf2.copyWith.fieldName(value)` for a single field.
class _$MappingValueSpecOneOf2CWProxyImpl
    implements _$MappingValueSpecOneOf2CWProxy {
  const _$MappingValueSpecOneOf2CWProxyImpl(this._value);

  final MappingValueSpecOneOf2 _value;

  @override
  MappingValueSpecOneOf2 const_(Object? const_) => call(const_: const_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingValueSpecOneOf2(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingValueSpecOneOf2(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MappingValueSpecOneOf2 call({Object? const_ = const $CopyWithPlaceholder()}) {
    return MappingValueSpecOneOf2(
      const_: const_ == const $CopyWithPlaceholder()
          ? _value.const_
          // ignore: cast_nullable_to_non_nullable
          : const_ as Object?,
    );
  }
}

extension $MappingValueSpecOneOf2CopyWith on MappingValueSpecOneOf2 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMappingValueSpecOneOf2.copyWith(...)` or `instanceOfMappingValueSpecOneOf2.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MappingValueSpecOneOf2CWProxy get copyWith =>
      _$MappingValueSpecOneOf2CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MappingValueSpecOneOf2 _$MappingValueSpecOneOf2FromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MappingValueSpecOneOf2', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['const']);
  final val = MappingValueSpecOneOf2(
    const_: $checkedConvert('const', (v) => v),
  );
  return val;
}, fieldKeyMap: const {'const_': 'const'});

Map<String, dynamic> _$MappingValueSpecOneOf2ToJson(
  MappingValueSpecOneOf2 instance,
) => <String, dynamic>{'const': instance.const_};
