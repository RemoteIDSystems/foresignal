// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mapping_value_spec_one_of1.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MappingValueSpecOneOf1CWProxy {
  MappingValueSpecOneOf1 template(String template);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingValueSpecOneOf1(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingValueSpecOneOf1(...).copyWith(id: 12, name: "My name")
  /// ```
  MappingValueSpecOneOf1 call({String template});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMappingValueSpecOneOf1.copyWith(...)` or call `instanceOfMappingValueSpecOneOf1.copyWith.fieldName(value)` for a single field.
class _$MappingValueSpecOneOf1CWProxyImpl
    implements _$MappingValueSpecOneOf1CWProxy {
  const _$MappingValueSpecOneOf1CWProxyImpl(this._value);

  final MappingValueSpecOneOf1 _value;

  @override
  MappingValueSpecOneOf1 template(String template) => call(template: template);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingValueSpecOneOf1(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingValueSpecOneOf1(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MappingValueSpecOneOf1 call({
    Object? template = const $CopyWithPlaceholder(),
  }) {
    return MappingValueSpecOneOf1(
      template: template == const $CopyWithPlaceholder() || template == null
          ? _value.template
          // ignore: cast_nullable_to_non_nullable
          : template as String,
    );
  }
}

extension $MappingValueSpecOneOf1CopyWith on MappingValueSpecOneOf1 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMappingValueSpecOneOf1.copyWith(...)` or `instanceOfMappingValueSpecOneOf1.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MappingValueSpecOneOf1CWProxy get copyWith =>
      _$MappingValueSpecOneOf1CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MappingValueSpecOneOf1 _$MappingValueSpecOneOf1FromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MappingValueSpecOneOf1', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['template']);
  final val = MappingValueSpecOneOf1(
    template: $checkedConvert('template', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$MappingValueSpecOneOf1ToJson(
  MappingValueSpecOneOf1 instance,
) => <String, dynamic>{'template': instance.template};
