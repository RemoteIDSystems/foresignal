// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mapping_value_spec_one_of.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MappingValueSpecOneOfCWProxy {
  MappingValueSpecOneOf path(String path);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingValueSpecOneOf(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingValueSpecOneOf(...).copyWith(id: 12, name: "My name")
  /// ```
  MappingValueSpecOneOf call({String path});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMappingValueSpecOneOf.copyWith(...)` or call `instanceOfMappingValueSpecOneOf.copyWith.fieldName(value)` for a single field.
class _$MappingValueSpecOneOfCWProxyImpl
    implements _$MappingValueSpecOneOfCWProxy {
  const _$MappingValueSpecOneOfCWProxyImpl(this._value);

  final MappingValueSpecOneOf _value;

  @override
  MappingValueSpecOneOf path(String path) => call(path: path);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingValueSpecOneOf(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingValueSpecOneOf(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MappingValueSpecOneOf call({Object? path = const $CopyWithPlaceholder()}) {
    return MappingValueSpecOneOf(
      path: path == const $CopyWithPlaceholder() || path == null
          ? _value.path
          // ignore: cast_nullable_to_non_nullable
          : path as String,
    );
  }
}

extension $MappingValueSpecOneOfCopyWith on MappingValueSpecOneOf {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMappingValueSpecOneOf.copyWith(...)` or `instanceOfMappingValueSpecOneOf.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MappingValueSpecOneOfCWProxy get copyWith =>
      _$MappingValueSpecOneOfCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MappingValueSpecOneOf _$MappingValueSpecOneOfFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MappingValueSpecOneOf', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['path']);
  final val = MappingValueSpecOneOf(
    path: $checkedConvert('path', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$MappingValueSpecOneOfToJson(
  MappingValueSpecOneOf instance,
) => <String, dynamic>{'path': instance.path};
