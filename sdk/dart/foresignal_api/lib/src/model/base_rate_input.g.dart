// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'base_rate_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BaseRateInputCWProxy {
  BaseRateInput name(String? name);

  BaseRateInput definition(BaseRateDefinition definition);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BaseRateInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BaseRateInput(...).copyWith(id: 12, name: "My name")
  /// ```
  BaseRateInput call({String? name, BaseRateDefinition definition});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBaseRateInput.copyWith(...)` or call `instanceOfBaseRateInput.copyWith.fieldName(value)` for a single field.
class _$BaseRateInputCWProxyImpl implements _$BaseRateInputCWProxy {
  const _$BaseRateInputCWProxyImpl(this._value);

  final BaseRateInput _value;

  @override
  BaseRateInput name(String? name) => call(name: name);

  @override
  BaseRateInput definition(BaseRateDefinition definition) =>
      call(definition: definition);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BaseRateInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BaseRateInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BaseRateInput call({
    Object? name = const $CopyWithPlaceholder(),
    Object? definition = const $CopyWithPlaceholder(),
  }) {
    return BaseRateInput(
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      definition:
          definition == const $CopyWithPlaceholder() || definition == null
          ? _value.definition
          // ignore: cast_nullable_to_non_nullable
          : definition as BaseRateDefinition,
    );
  }
}

extension $BaseRateInputCopyWith on BaseRateInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBaseRateInput.copyWith(...)` or `instanceOfBaseRateInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BaseRateInputCWProxy get copyWith => _$BaseRateInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BaseRateInput _$BaseRateInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BaseRateInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['definition']);
      final val = BaseRateInput(
        name: $checkedConvert('name', (v) => v as String?),
        definition: $checkedConvert(
          'definition',
          (v) => BaseRateDefinition.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$BaseRateInputToJson(BaseRateInput instance) =>
    <String, dynamic>{
      'name': ?instance.name,
      'definition': instance.definition.toJson(),
    };
